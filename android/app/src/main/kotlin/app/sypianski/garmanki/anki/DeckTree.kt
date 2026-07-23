package app.sypianski.garmanki.anki

/**
 * Node in the tree built from Anki's "::" deck-name hierarchy (e.g. "HAKADO::vim" is
 * a subdeck of "HAKADO"). [deck] is null when this segment is only an implied parent
 * (no AnkiDroid deck record of its own, just a prefix shared by its children).
 */
data class DeckNode(
    val segment: String,
    val fullName: String,
    val deck: AnkiDeck?,
    val children: List<DeckNode>,
)

/** Groups flat AnkiDroid deck names by their "::" hierarchy; order follows first appearance. */
fun buildDeckTree(decks: List<AnkiDeck>): List<DeckNode> {
    class MutableNode(val segment: String, val fullName: String) {
        var deck: AnkiDeck? = null
        val children = LinkedHashMap<String, MutableNode>()
    }

    val root = MutableNode("", "")
    for (deck in decks) {
        val segments = deck.name.split("::")
        var node = root
        var path = ""
        segments.forEachIndexed { i, seg ->
            path = if (path.isEmpty()) seg else "$path::$seg"
            node = node.children.getOrPut(seg) { MutableNode(seg, path) }
            if (i == segments.lastIndex) node.deck = deck
        }
    }

    fun toDeckNode(n: MutableNode): DeckNode =
        DeckNode(n.segment, n.fullName, n.deck, n.children.values.map(::toDeckNode))

    return root.children.values.map(::toDeckNode)
}

/** Ids of this node's own deck (if any) plus every descendant's deck, depth-first. */
fun DeckNode.allDeckIds(): List<String> = buildList {
    deck?.let { add(it.id.toString()) }
    children.forEach { addAll(it.allDeckIds()) }
}
