package app.sypianski.garmanki

import app.sypianski.garmanki.ciq.StatePayload
import app.sypianski.garmanki.ciq.WatchCard
import app.sypianski.garmanki.ciq.WatchDeck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StatePayloadTest {

    private fun deck(i: Int) = WatchDeck(i, "160000000000$i", "Deck $i", 1, 2, 3)

    private fun card(i: Int, deckIdx: Int = 0, textLen: Int = 20) = WatchCard(
        cid = "1600000000000:$i",
        nid = "1600000000000",
        ord = i,
        deckIdx = deckIdx,
        front = "f".repeat(textLen),
        back = "b".repeat(textLen),
        nextTimes = listOf("<1 min", "10 min", "3 d", "4 d"),
    )

    @Test
    fun `single small chunk carries decks stats and cards`() {
        val chunks = StatePayload.buildChunks(listOf(deck(0)), List(3) { card(it) }, 5, 7, rev = 42)
        assertEquals(1, chunks.size)
        val c = chunks[0]
        assertEquals(1, c["p"])
        assertEquals("s", c["t"])
        assertEquals(42, c["rev"])
        assertEquals(1, c["seq"])
        assertEquals(1, c["of"])
        assertEquals(3, (c["cards"] as List<*>).size)
        assertEquals(listOf(5, 7), c["stats"])
        assertEquals(1, (c["decks"] as List<*>).size)
    }

    @Test
    fun `empty state still ships one chunk with decks`() {
        val chunks = StatePayload.buildChunks(listOf(deck(0)), emptyList(), 0, 0, rev = 1)
        assertEquals(1, chunks.size)
        assertEquals(0, (chunks[0]["cards"] as List<*>).size)
        assertEquals(1, (chunks[0]["decks"] as List<*>).size)
    }

    @Test
    fun `many cards split into sequenced chunks, decks only on first`() {
        val chunks = StatePayload.buildChunks(listOf(deck(0)), List(200) { card(it) }, 0, 0, rev = 7)
        assertTrue(chunks.size > 1)
        val of = chunks.size
        chunks.forEachIndexed { i, c ->
            assertEquals(i + 1, c["seq"])
            assertEquals(of, c["of"])
            if (i == 0) assertTrue(c.containsKey("decks")) else assertNull(c["decks"])
        }
        assertEquals(200, chunks.sumOf { (it["cards"] as List<*>).size })
    }

    @Test
    fun `chunk char budget respected for long cards`() {
        val chunks = StatePayload.buildChunks(
            listOf(deck(0)), List(50) { card(it, textLen = 300) }, 0, 0, rev = 1,
        )
        for (c in chunks) {
            val cards = c["cards"] as List<*>
            val chars = cards.sumOf { row ->
                (row as List<*>).filterIsInstance<String>().sumOf { it.length }
            }
            assertTrue("chunk too big: $chars", chars <= StatePayload.MAX_CHUNK_CHARS + 700)
            assertTrue(cards.size <= StatePayload.MAX_CARDS_PER_CHUNK)
        }
    }

    @Test
    fun `cfg rides on the first chunk only`() {
        val cfg = mapOf(
            "am" to mapOf("down" to 3, "start" to 1),
            "ca" to listOf("susp", "flag"),
            "gr" to 2,
        )
        val chunks = StatePayload.buildChunks(
            listOf(deck(0)), List(200) { card(it) }, 0, 0, rev = 3, cfg = cfg,
        )
        assertTrue(chunks.size > 1)
        assertEquals(cfg, chunks[0]["cfg"])
        chunks.drop(1).forEach { assertNull(it["cfg"]) }
    }

    @Test
    fun `omitted cfg is absent from the payload`() {
        val chunks = StatePayload.buildChunks(listOf(deck(0)), emptyList(), 0, 0, rev = 1)
        assertTrue(!chunks[0].containsKey("cfg"))
    }

    @Test
    fun `next times always padded to four`() {
        val c = WatchCard("1:0", "1", 0, 0, "f", "b", listOf("10 min"))
        val chunks = StatePayload.buildChunks(listOf(deck(0)), listOf(c), 0, 0, rev = 1)
        val row = (chunks[0]["cards"] as List<*>)[0] as List<*>
        assertEquals(listOf("10 min", "", "", ""), row[6])
    }
}
