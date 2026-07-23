package app.sypianski.garmanki.sync

import app.sypianski.garmanki.anki.AnkiBridge
import app.sypianski.garmanki.ciq.CiqLink
import app.sypianski.garmanki.ciq.PushStatus
import app.sypianski.garmanki.ciq.StatePayload
import app.sypianski.garmanki.ciq.WatchCard
import app.sypianski.garmanki.ciq.WatchDeck
import app.sypianski.garmanki.ciq.WatchMessage
import app.sypianski.garmanki.data.HtmlToText
import app.sypianski.garmanki.data.Stats
import app.sypianski.garmanki.data.SyncPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

const val FLAG_TAG = "garmanki-flag"
const val DELETE_TAG = "garmanki-delete"

/**
 * Orchestrates AnkiDroid ↔ watch flows (SCHEMA.md §7): building/pushing
 * state, replaying answer batches, keeping the replay log for stats.
 */
class SyncEngine(
    private val anki: AnkiBridge,
    private val ciq: CiqLink,
    private val prefs: SyncPrefs,
    private val scope: CoroutineScope,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    data class UiState(
        val busy: Boolean = false,
        val lastPush: PushStatus? = null,
        val skipped: Int = 0,
        val lastApplied: Pair<Int, Int>? = null, // applied to stale
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private val lock = Mutex()

    fun start() {
        scope.launch {
            ciq.messages.collect { m ->
                when (m) {
                    is WatchMessage.Hello -> onHello(m)
                    is WatchMessage.Answers -> applyAnswers(m)
                }
            }
        }
    }

    suspend fun onHello(h: WatchMessage.Hello) {
        // A watch with queued answers flushes them right after hello and
        // applyAnswers ends with a fresh push — don't race it with our own.
        if (h.pend > 0) return
        val s = prefs.snapshot()
        if (h.rev != s.lastRev) {
            pushState()
        }
    }

    suspend fun pushState(): Boolean = lock.withLock {
        _ui.value = _ui.value.copy(busy = true)
        try {
            val s = prefs.snapshot()
            val chosen = anki.listDecks()
                .filter { s.selectedDecks.contains(it.id.toString()) }
                .take(8)
            val decks = mutableListOf<WatchDeck>()
            val cards = mutableListOf<WatchCard>()
            var skipped = 0
            chosen.forEachIndexed { idx, d ->
                decks.add(WatchDeck(idx, d.id.toString(), d.name, d.new, d.learn, d.review))
                for (due in anki.dueCards(d.id, s.cardLimit)) {
                    val qa = anki.cardQA(due.noteId, due.ord)
                    if (qa == null) {
                        skipped++
                        continue
                    }
                    val front = HtmlToText.clean(qa.questionHtml)
                    if (front.isEmpty()) {
                        skipped++
                        continue
                    }
                    cards.add(
                        WatchCard(
                            cid = "${due.noteId}:${due.ord}",
                            nid = due.noteId.toString(),
                            ord = due.ord,
                            deckIdx = idx,
                            front = front,
                            back = HtmlToText.clean(qa.answerHtml),
                            nextTimes = due.nextTimes,
                        )
                    )
                }
            }
            val today = epochDay()
            val rev = s.lastRev + 1
            val chunks = StatePayload.buildChunks(
                decks, cards,
                doneToday = Stats.doneToday(s.replayLog, today),
                streak = Stats.streak(s.replayLog, today),
                rev = rev,
                // Full watch-UI config on every push — sticky watch-side (§8).
                cfg = mapOf(
                    "am" to s.actionMap,
                    "ca" to s.cardActions.toList(),
                    "gr" to s.guideReset,
                ),
            )
            val result = ciq.push(chunks, rev)
            if (result is PushStatus.Done) {
                prefs.setLastRev(rev)
            }
            _ui.value = UiState(busy = false, lastPush = result, skipped = skipped,
                lastApplied = _ui.value.lastApplied)
            result is PushStatus.Done
        } finally {
            if (_ui.value.busy) {
                _ui.value = _ui.value.copy(busy = false)
            }
        }
    }

    suspend fun applyAnswers(a: WatchMessage.Answers) {
        val s = prefs.snapshot()
        if (a.batch == s.lastAppliedBatch) {
            // Duplicate delivery (lost ack) — re-ack, never re-apply (SCHEMA.md §3).
            sendAck(a.batch, applied = 0, stale = 0)
            return
        }
        var answersApplied = 0
        var applied = 0
        var stale = 0
        for (row in a.ans) {
            val nid = asLong(row.getOrNull(1))
            val ord = asInt(row.getOrNull(2))
            val ease = asInt(row.getOrNull(3))?.coerceIn(1, 4)
            val timeMs = (asLong(row.getOrNull(4)) ?: 0L).coerceIn(0L, 60_000L)
            val ok = if (nid != null && ord != null && ease != null) {
                runCatching { anki.answerCard(nid, ord, ease, timeMs) }.getOrDefault(false)
            } else {
                false
            }
            if (ok) {
                applied++
                answersApplied++
            } else {
                stale++
            }
        }
        for (row in a.act) {
            val nid = asLong(row.getOrNull(1))
            val ord = asInt(row.getOrNull(2))
            val action = row.getOrNull(3) as? String
            val ok = if (nid != null && ord != null && action != null) {
                runCatching { applyAction(nid, ord, action) }.getOrDefault(false)
            } else {
                false
            }
            if (ok) applied++ else stale++
        }
        prefs.recordReplays(answersApplied, epochDay())
        prefs.setLastAppliedBatch(a.batch)
        _ui.value = _ui.value.copy(lastApplied = applied to stale)
        sendAck(a.batch, applied, stale)

        pushState() // hand the watch a fresh queue right away

        val now = clock()
        if (now - s.lastAnkiSyncMs > SYNC_MIN_INTERVAL_MS) {
            runCatching { anki.requestSync() }
            prefs.setLastAnkiSyncMs(now)
        }
    }

    private fun applyAction(nid: Long, ord: Int, action: String): Boolean = when (action) {
        "susp" -> anki.suspendCard(nid, ord)
        "bury" -> anki.buryCard(nid, ord)
        "flag" -> anki.addTag(nid, FLAG_TAG)
        // Soft delete (DECYZJE.md D4): out of rotation now, hard-deleted by
        // the user in Anki via the tag filter.
        "del" -> anki.suspendCard(nid, ord) && anki.addTag(nid, DELETE_TAG)
        else -> false
    }

    private fun sendAck(batch: Int, applied: Int, stale: Int) {
        ciq.send(
            mapOf(
                "p" to 1, "t" to "aa", "batch" to batch,
                "ok" to true, "applied" to applied, "stale" to stale,
            )
        )
    }

    private fun epochDay(): Long = clock() / 86_400_000L

    private companion object {
        const val SYNC_MIN_INTERVAL_MS = 5 * 60_000L

        fun asInt(v: Any?): Int? = (v as? Number)?.toInt()

        fun asLong(v: Any?): Long? = when (v) {
            is Number -> v.toLong()
            is String -> v.toLongOrNull()
            else -> null
        }
    }
}
