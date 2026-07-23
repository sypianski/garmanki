package app.sypianski.garmanki

import app.sypianski.garmanki.anki.AnkiBridge
import app.sypianski.garmanki.anki.AnkiDeck
import app.sypianski.garmanki.anki.CardQA
import app.sypianski.garmanki.anki.DueCard
import app.sypianski.garmanki.ciq.CiqLink
import app.sypianski.garmanki.ciq.PushStatus
import app.sypianski.garmanki.ciq.WatchMessage
import app.sypianski.garmanki.data.Settings
import app.sypianski.garmanki.data.SyncPrefs
import app.sypianski.garmanki.sync.DELETE_TAG
import app.sypianski.garmanki.sync.FLAG_TAG
import app.sypianski.garmanki.sync.SyncEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeAnki : AnkiBridge {
    val answered = mutableListOf<Triple<Long, Int, Int>>()
    val suspended = mutableListOf<Pair<Long, Int>>()
    val buried = mutableListOf<Pair<Long, Int>>()
    val tags = mutableListOf<Pair<Long, String>>()
    var syncRequests = 0
    var failAnswers = false

    override fun available() = true
    override fun hasPermission() = true
    override fun listDecks() = listOf(AnkiDeck(1L, "Deck", 1, 1, 1, false))
    override fun dueCards(deckId: Long, limit: Int) =
        listOf(DueCard(100L, 0, 4, listOf("<1 min", "10 min", "3 d", "4 d")))
    override fun cardQA(noteId: Long, ord: Int) = CardQA("<b>front</b>", "back")
    override fun answerCard(noteId: Long, ord: Int, ease: Int, timeTakenMs: Long): Boolean {
        if (failAnswers) return false
        answered.add(Triple(noteId, ord, ease))
        return true
    }
    override fun suspendCard(noteId: Long, ord: Int): Boolean {
        suspended.add(noteId to ord); return true
    }
    override fun buryCard(noteId: Long, ord: Int): Boolean {
        buried.add(noteId to ord); return true
    }
    override fun addTag(noteId: Long, tag: String): Boolean {
        tags.add(noteId to tag); return true
    }
    override fun requestSync() { syncRequests++ }
}

private class FakeCiq : CiqLink {
    override val messages: SharedFlow<WatchMessage> = MutableSharedFlow()
    val pushes = mutableListOf<Pair<List<Map<String, Any?>>, Int>>()
    val sent = mutableListOf<Map<String, Any?>>()

    override suspend fun push(chunks: List<Map<String, Any?>>, rev: Int): PushStatus {
        pushes.add(chunks to rev)
        return PushStatus.Done(rev)
    }

    override fun send(message: Map<String, Any?>) {
        sent.add(message)
    }
}

private class FakePrefs(
    var settings: Settings = Settings(
        selectedDecks = setOf("1"),
        cardLimit = 100,
        lastRev = 0,
        lastAppliedBatch = 0,
        replayLog = emptyMap(),
        lastAnkiSyncMs = 0L,
    ),
) : SyncPrefs {
    val replays = mutableListOf<Pair<Int, Long>>()

    override suspend fun snapshot() = settings
    override suspend fun setLastRev(rev: Int) { settings = settings.copy(lastRev = rev) }
    override suspend fun setLastAppliedBatch(batch: Int) {
        settings = settings.copy(lastAppliedBatch = batch)
    }
    override suspend fun recordReplays(count: Int, epochDay: Long) {
        if (count > 0) replays.add(count to epochDay)
    }
    override suspend fun setLastAnkiSyncMs(ms: Long) {
        settings = settings.copy(lastAnkiSyncMs = ms)
    }
}

class SyncEngineTest {

    private fun answers(batch: Int) = WatchMessage.Answers(
        batch = batch,
        ans = listOf(listOf("100:0", "100", 0, 3, 4000, 1_700_000_000)),
        act = listOf(listOf("100:0", "100", 0, "flag")),
    )

    @Test
    fun `applies a batch, acks, records replays, pushes fresh state`() = runTest {
        val anki = FakeAnki()
        val ciq = FakeCiq()
        val prefs = FakePrefs()
        val engine = SyncEngine(anki, ciq, prefs, this, clock = { 1_000_000L })

        engine.applyAnswers(answers(batch = 5))

        assertEquals(listOf(Triple(100L, 0, 3)), anki.answered)
        assertEquals(listOf(100L to FLAG_TAG), anki.tags)
        assertEquals(5, prefs.settings.lastAppliedBatch)
        assertEquals(listOf(1 to 0L), prefs.replays.map { it.first to it.second / 100_000 })
        val ack = ciq.sent.single()
        assertEquals(5, ack["batch"])
        assertEquals(true, ack["ok"])
        assertEquals(2, ack["applied"])
        assertEquals(0, ack["stale"])
        assertEquals(1, ciq.pushes.size) // fresh state after replay
        assertEquals(1, anki.syncRequests)
    }

    @Test
    fun `duplicate batch is re-acked but never re-applied`() = runTest {
        val anki = FakeAnki()
        val ciq = FakeCiq()
        val prefs = FakePrefs()
        val engine = SyncEngine(anki, ciq, prefs, this, clock = { 1_000_000L })

        engine.applyAnswers(answers(batch = 5))
        val answeredAfterFirst = anki.answered.size
        engine.applyAnswers(answers(batch = 5))

        assertEquals(answeredAfterFirst, anki.answered.size)
        assertEquals(2, ciq.sent.size)
        val reAck = ciq.sent.last()
        assertEquals(5, reAck["batch"])
        assertEquals(0, reAck["applied"])
        assertEquals(1, ciq.pushes.size) // no second push either
    }

    @Test
    fun `failed rows count as stale`() = runTest {
        val anki = FakeAnki().apply { failAnswers = true }
        val ciq = FakeCiq()
        val prefs = FakePrefs()
        val engine = SyncEngine(anki, ciq, prefs, this, clock = { 1_000_000L })

        engine.applyAnswers(
            WatchMessage.Answers(3, ans = listOf(listOf("100:0", "100", 0, 3, 100, 0)), act = emptyList())
        )

        val ack = ciq.sent.single()
        assertEquals(0, ack["applied"])
        assertEquals(1, ack["stale"])
        assertTrue(prefs.replays.isEmpty())
    }

    @Test
    fun `del action suspends and tags`() = runTest {
        val anki = FakeAnki()
        val ciq = FakeCiq()
        val engine = SyncEngine(anki, ciq, FakePrefs(), this, clock = { 1_000_000L })

        engine.applyAnswers(
            WatchMessage.Answers(2, ans = emptyList(), act = listOf(listOf("100:0", "100", 0, "del")))
        )

        assertEquals(listOf(100L to 0), anki.suspended)
        assertEquals(listOf(100L to DELETE_TAG), anki.tags)
    }

    @Test
    fun `pushState builds increasing rev and persists on ack`() = runTest {
        val ciq = FakeCiq()
        val prefs = FakePrefs()
        val engine = SyncEngine(FakeAnki(), ciq, prefs, this, clock = { 1_000_000L })

        assertTrue(engine.pushState())
        assertEquals(1, prefs.settings.lastRev)
        assertTrue(engine.pushState())
        assertEquals(2, prefs.settings.lastRev)
        assertEquals(listOf(1, 2), ciq.pushes.map { it.second })
        // front got HTML-cleaned
        val firstChunk = ciq.pushes[0].first[0]
        val row = (firstChunk["cards"] as List<*>)[0] as List<*>
        assertEquals("front", row[4])
    }
}
