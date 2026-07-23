package app.sypianski.garmanki.ciq

data class WatchDeck(
    val deckIdx: Int,
    val deckId: String,
    val name: String,
    val nNew: Int,
    val nLrn: Int,
    val nRev: Int,
)

data class WatchCard(
    /**
     * The schedule query returns no card id, so the cid is the composite
     * "<noteId>:<ord>" — the watch treats it as an opaque String (SCHEMA.md §2).
     */
    val cid: String,
    val nid: String,
    val ord: Int,
    val deckIdx: Int,
    val front: String,
    val back: String,
    val nextTimes: List<String>,
)

/** Builds the chunked state push of SCHEMA.md §4. */
object StatePayload {

    /** Keep each BLE message well under the ~16 KB practical payload limit. */
    const val MAX_CHUNK_CHARS = 10_000
    const val MAX_CARDS_PER_CHUNK = 60

    fun buildChunks(
        decks: List<WatchDeck>,
        cards: List<WatchCard>,
        doneToday: Int,
        streak: Int,
        rev: Int,
        cfg: Map<String, Any?>? = null,
    ): List<Map<String, Any?>> {
        require(decks.size <= 8) { "max 8 decks (SCHEMA.md §4)" }

        val decksPayload = decks.map { d ->
            listOf(d.deckIdx, d.deckId, d.name, d.nNew, d.nLrn, d.nRev)
        }
        val statsPayload = listOf(doneToday, streak)

        // Partition card rows: chunk 1's budget is reduced by the deck list.
        val groups = mutableListOf<List<List<Any?>>>()
        var current = mutableListOf<List<Any?>>()
        var chars = decksEstimate(decks)
        for (c in cards) {
            val row = listOf(c.cid, c.nid, c.ord, c.deckIdx, c.front, c.back, padTo4(c.nextTimes))
            val est = 40 + c.cid.length + c.front.length + c.back.length +
                c.nextTimes.sumOf { it.length }
            if (current.isNotEmpty() &&
                (current.size >= MAX_CARDS_PER_CHUNK || chars + est > MAX_CHUNK_CHARS)
            ) {
                groups.add(current)
                current = mutableListOf()
                chars = 0
            }
            current.add(row)
            chars += est
        }
        if (current.isNotEmpty() || groups.isEmpty()) {
            groups.add(current) // an empty state still ships decks+stats in seq 1
        }

        val of = groups.size
        return groups.mapIndexed { i, group ->
            val chunk = mutableMapOf<String, Any?>(
                "p" to 1,
                "t" to "s",
                "rev" to rev,
                "seq" to i + 1,
                "of" to of,
                "cards" to group,
            )
            if (i == 0) {
                chunk["decks"] = decksPayload
                chunk["stats"] = statsPayload
                if (cfg != null) chunk["cfg"] = cfg // SCHEMA.md §8
            }
            chunk
        }
    }

    private fun padTo4(nt: List<String>): List<String> =
        List(4) { i -> nt.getOrElse(i) { "" } }

    private fun decksEstimate(decks: List<WatchDeck>): Int =
        60 + decks.sumOf { 30 + it.name.length + it.deckId.length }
}
