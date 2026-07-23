package app.sypianski.garmanki.anki

import android.net.Uri

/**
 * Constants re-declared from AnkiDroid's FlashCardsContract to avoid a
 * library dependency. Literals verified against
 * https://github.com/ankidroid/Anki-Android/blob/main/api/src/main/java/com/ichi2/anki/FlashCardsContract.kt
 * (2026-07-22).
 */
object FlashCards {
    const val AUTHORITY = "com.ichi2.anki.flashcards"
    const val READ_WRITE_PERMISSION = "com.ichi2.anki.permission.READ_WRITE_DATABASE"
    const val ANKIDROID_PACKAGE = "com.ichi2.anki"
    const val SYNC_ACTION = "com.ichi2.anki.DO_SYNC"

    val AUTHORITY_URI: Uri = Uri.parse("content://$AUTHORITY")
    val DECKS_URI: Uri = Uri.withAppendedPath(AUTHORITY_URI, "decks")
    val SCHEDULE_URI: Uri = Uri.withAppendedPath(AUTHORITY_URI, "schedule")
    val NOTES_URI: Uri = Uri.withAppendedPath(AUTHORITY_URI, "notes")

    object Deck {
        const val DECK_NAME = "deck_name"
        const val DECK_ID = "deck_id"

        /** JSON array, documented order: `[learn, review, new]`. */
        const val DECK_COUNTS = "deck_count"
        const val DECK_DYN = "deck_dyn"
    }

    object ReviewInfo {
        const val NOTE_ID = "note_id"
        const val CARD_ORD = "ord"
        const val BUTTON_COUNT = "button_count"

        /** JSON array of per-button interval labels, e.g. `["<1 min","10 min","3 d","4 d"]`. */
        const val NEXT_REVIEW_TIMES = "next_review_times"

        /** Answer ease for update(): 1..4 = Again/Hard/Good/Easy (com.ichi2.anki.api.Ease). */
        const val EASE = "answer_ease"
        const val TIME_TAKEN = "time_taken"
        const val BURY = "buried"
        const val SUSPEND = "suspended"
    }

    object Card {
        const val CARD_ORD = "ord"

        /** Rendered question HTML without CSS/JS. */
        const val QUESTION_SIMPLE = "question_simple"

        /** Rendered answer HTML without the question part and without CSS/JS. */
        const val ANSWER_PURE = "answer_pure"
    }

    object Note {
        const val ID = "_id"

        /** Space-separated tag list. */
        const val TAGS = "tags"
    }
}
