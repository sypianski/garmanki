package app.sypianski.garmanki

import app.sypianski.garmanki.anki.AnkiDeck
import app.sypianski.garmanki.anki.allDeckIds
import app.sypianski.garmanki.anki.buildDeckTree
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeckTreeTest {

    private fun deck(id: Long, name: String) = AnkiDeck(id, name, learn = 0, review = 0, new = 0, dynamic = false)

    @Test
    fun `flat deck without separator stays a top-level leaf`() {
        val tree = buildDeckTree(listOf(deck(1, "Default")))
        assertEquals(1, tree.size)
        assertEquals("Default", tree[0].segment)
        assertTrue(tree[0].children.isEmpty())
        assertEquals(1L, tree[0].deck?.id)
    }

    @Test
    fun `subdecks group under a shared parent header`() {
        val decks = listOf(deck(1, "HAKADO::vim"), deck(2, "HAKADO::git"))
        val tree = buildDeckTree(decks)

        assertEquals(1, tree.size)
        val parent = tree[0]
        assertEquals("HAKADO", parent.segment)
        assertEquals("HAKADO", parent.fullName)
        assertEquals(2, parent.children.size)
        assertEquals(listOf("vim", "git"), parent.children.map { it.segment })
        assertEquals(listOf("HAKADO::vim", "HAKADO::git"), parent.children.map { it.fullName })
    }

    @Test
    fun `implied parent with no cards of its own has no deck to select`() {
        val decks = listOf(deck(1, "HAKADO::vim"))
        val tree = buildDeckTree(decks)

        val parent = tree[0]
        assertNull(parent.deck)
        assertEquals(1L, parent.children.single().deck?.id)
    }

    @Test
    fun `parent that is itself a real deck keeps its own deck alongside children`() {
        val decks = listOf(deck(1, "HAKADO"), deck(2, "HAKADO::vim"))
        val tree = buildDeckTree(decks)

        val parent = tree[0]
        assertEquals(1L, parent.deck?.id)
        assertEquals(1, parent.children.size)
        assertEquals(2L, parent.children[0].deck?.id)
    }

    @Test
    fun `nesting beyond one level builds a multi-level tree`() {
        val decks = listOf(deck(1, "HAKADO::vim::advanced"))
        val tree = buildDeckTree(decks)

        val hakado = tree[0]
        assertNull(hakado.deck)
        val vim = hakado.children.single()
        assertEquals("vim", vim.segment)
        assertNull(vim.deck)
        val advanced = vim.children.single()
        assertEquals("advanced", advanced.segment)
        assertEquals(1L, advanced.deck?.id)
    }

    @Test
    fun `allDeckIds on implied parent collects only descendant ids`() {
        val decks = listOf(deck(1, "HAKADO::vim"), deck(2, "HAKADO::git"))
        val parent = buildDeckTree(decks)[0]
        assertEquals(setOf("1", "2"), parent.allDeckIds().toSet())
    }

    @Test
    fun `allDeckIds on a real parent deck includes its own id and its descendants`() {
        val decks = listOf(deck(1, "HAKADO"), deck(2, "HAKADO::vim"), deck(3, "HAKADO::git"))
        val parent = buildDeckTree(decks)[0]
        assertEquals(setOf("1", "2", "3"), parent.allDeckIds().toSet())
    }

    @Test
    fun `allDeckIds on a nested group collects the whole subtree`() {
        val decks = listOf(deck(1, "HAKADO::vim::basics"), deck(2, "HAKADO::vim::advanced"), deck(3, "HAKADO::git"))
        val hakado = buildDeckTree(decks)[0]
        assertEquals(setOf("1", "2", "3"), hakado.allDeckIds().toSet())
    }
}
