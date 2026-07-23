package app.sypianski.garmanki.anki

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

data class AnkiDeck(
    val id: Long,
    val name: String,
    val learn: Int,
    val review: Int,
    val new: Int,
    val dynamic: Boolean,
)

data class DueCard(
    val noteId: Long,
    val ord: Int,
    val buttonCount: Int,
    val nextTimes: List<String>,
)

data class CardQA(val questionHtml: String, val answerHtml: String)

/** Seam for SyncEngine tests — AnkiDroidClient is the real implementation. */
interface AnkiBridge {
    fun available(): Boolean
    fun hasPermission(): Boolean
    fun listDecks(): List<AnkiDeck>
    fun dueCards(deckId: Long, limit: Int): List<DueCard>
    fun cardQA(noteId: Long, ord: Int): CardQA?
    fun answerCard(noteId: Long, ord: Int, ease: Int, timeTakenMs: Long): Boolean
    fun suspendCard(noteId: Long, ord: Int): Boolean
    fun buryCard(noteId: Long, ord: Int): Boolean
    fun addTag(noteId: Long, tag: String): Boolean
    fun requestSync()
}

/** All access to AnkiDroid goes through its ContentProvider (SCHEMA.md §7). */
class AnkiDroidClient(private val context: Context) : AnkiBridge {

    private val resolver get() = context.contentResolver
    private val json = Json { ignoreUnknownKeys = true }

    override fun available(): Boolean =
        context.packageManager.resolveContentProvider(FlashCards.AUTHORITY, 0) != null

    override fun hasPermission(): Boolean =
        context.checkSelfPermission(FlashCards.READ_WRITE_PERMISSION) ==
            PackageManager.PERMISSION_GRANTED

    override fun listDecks(): List<AnkiDeck> {
        val out = mutableListOf<AnkiDeck>()
        resolver.query(FlashCards.DECKS_URI, null, null, null, null)?.use { c ->
            val iId = c.getColumnIndex(FlashCards.Deck.DECK_ID)
            val iName = c.getColumnIndex(FlashCards.Deck.DECK_NAME)
            val iCounts = c.getColumnIndex(FlashCards.Deck.DECK_COUNTS)
            val iDyn = c.getColumnIndex(FlashCards.Deck.DECK_DYN)
            while (c.moveToNext()) {
                val counts = parseIntArray(if (iCounts >= 0) c.getString(iCounts) else null)
                out.add(
                    AnkiDeck(
                        id = c.getLong(iId),
                        name = c.getString(iName) ?: continue,
                        // DECK_COUNTS documented order: [learn, review, new]
                        learn = counts.getOrElse(0) { 0 },
                        review = counts.getOrElse(1) { 0 },
                        new = counts.getOrElse(2) { 0 },
                        dynamic = iDyn >= 0 && c.getInt(iDyn) == 1,
                    )
                )
            }
        }
        return out
    }

    override fun dueCards(deckId: Long, limit: Int): List<DueCard> {
        val out = mutableListOf<DueCard>()
        // Selection string format is parsed by AnkiDroid itself — documented
        // example: selector "limit=?, deckID=?", args ["5", "123456789"].
        resolver.query(
            FlashCards.SCHEDULE_URI,
            null,
            "limit=?, deckID=?",
            arrayOf(limit.toString(), deckId.toString()),
            null,
        )?.use { c ->
            val iNote = c.getColumnIndex(FlashCards.ReviewInfo.NOTE_ID)
            val iOrd = c.getColumnIndex(FlashCards.ReviewInfo.CARD_ORD)
            val iBtn = c.getColumnIndex(FlashCards.ReviewInfo.BUTTON_COUNT)
            val iNext = c.getColumnIndex(FlashCards.ReviewInfo.NEXT_REVIEW_TIMES)
            while (c.moveToNext()) {
                out.add(
                    DueCard(
                        noteId = c.getLong(iNote),
                        ord = c.getInt(iOrd),
                        buttonCount = if (iBtn >= 0) c.getInt(iBtn) else 4,
                        nextTimes = parseStringArray(if (iNext >= 0) c.getString(iNext) else null),
                    )
                )
            }
        }
        return out
    }

    override fun cardQA(noteId: Long, ord: Int): CardQA? {
        val uri = FlashCards.NOTES_URI.buildUpon()
            .appendPath(noteId.toString())
            .appendPath("cards")
            .appendPath(ord.toString())
            .build()
        resolver.query(
            uri,
            arrayOf(FlashCards.Card.QUESTION_SIMPLE, FlashCards.Card.ANSWER_PURE),
            null, null, null,
        )?.use { c ->
            if (c.moveToFirst()) {
                return CardQA(
                    questionHtml = c.getString(0) ?: "",
                    answerHtml = c.getString(1) ?: "",
                )
            }
        }
        return null
    }

    override fun answerCard(noteId: Long, ord: Int, ease: Int, timeTakenMs: Long): Boolean {
        val values = ContentValues().apply {
            put(FlashCards.ReviewInfo.NOTE_ID, noteId)
            put(FlashCards.ReviewInfo.CARD_ORD, ord)
            put(FlashCards.ReviewInfo.EASE, ease)
            put(FlashCards.ReviewInfo.TIME_TAKEN, timeTakenMs)
        }
        return resolver.update(FlashCards.SCHEDULE_URI, values, null, null) > 0
    }

    override fun suspendCard(noteId: Long, ord: Int): Boolean =
        setScheduleFlag(noteId, ord, FlashCards.ReviewInfo.SUSPEND)

    override fun buryCard(noteId: Long, ord: Int): Boolean =
        setScheduleFlag(noteId, ord, FlashCards.ReviewInfo.BURY)

    private fun setScheduleFlag(noteId: Long, ord: Int, column: String): Boolean {
        val values = ContentValues().apply {
            put(FlashCards.ReviewInfo.NOTE_ID, noteId)
            put(FlashCards.ReviewInfo.CARD_ORD, ord)
            put(column, 1)
        }
        return resolver.update(FlashCards.SCHEDULE_URI, values, null, null) > 0
    }

    override fun addTag(noteId: Long, tag: String): Boolean {
        val noteUri = Uri.withAppendedPath(FlashCards.NOTES_URI, noteId.toString())
        var tags = ""
        resolver.query(noteUri, arrayOf(FlashCards.Note.TAGS), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                tags = c.getString(0) ?: ""
            } else {
                return false
            }
        } ?: return false
        val list = tags.split(" ").filter { it.isNotBlank() }
        if (list.contains(tag)) {
            return true // already tagged — idempotent
        }
        val values = ContentValues().apply {
            put(FlashCards.Note.TAGS, (list + tag).joinToString(" "))
        }
        return resolver.update(noteUri, values, null, null) > 0
    }

    override fun requestSync() {
        // AnkiDroid rate-limits this to once per 5 minutes on its side too.
        context.sendBroadcast(Intent(FlashCards.SYNC_ACTION))
        context.sendBroadcast(Intent(FlashCards.SYNC_ACTION).setPackage(FlashCards.ANKIDROID_PACKAGE))
    }

    private fun parseIntArray(raw: String?): List<Int> = try {
        if (raw == null) emptyList()
        else json.parseToJsonElement(raw).jsonArray.mapNotNull { it.jsonPrimitive.intOrNull }
    } catch (t: Throwable) {
        emptyList()
    }

    private fun parseStringArray(raw: String?): List<String> = try {
        if (raw == null) emptyList()
        else json.parseToJsonElement(raw).jsonArray.map { it.jsonPrimitive.content }
    } catch (t: Throwable) {
        emptyList()
    }
}
