package app.sypianski.garmanki.data

/**
 * Flattens AnkiDroid-rendered card HTML into watch-displayable plain text.
 * Rules and their order come from SCHEMA.md §6 — keep both in sync.
 * Pure Kotlin (no android.*) so it stays unit-testable on the JVM.
 */
object HtmlToText {

    const val MAX_LEN = 300

    private val soundTag = Regex("""\[sound:[^\]]*]""")
    private val ttsTag = Regex("""\[anki:tts[^]]*].*?\[/anki:tts]""", RegexOption.DOT_MATCHES_ALL)
    private val styleBlock = Regex("""<style[^>]*>.*?</style>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    private val scriptBlock = Regex("""<script[^>]*>.*?</script>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    private val imgTag = Regex("""<img[^>]*>""", RegexOption.IGNORE_CASE)
    private val newlineTag = Regex("""<br\s*/?>|</?div[^>]*>|</?p[^>]*>""", RegexOption.IGNORE_CASE)
    private val anyTag = Regex("""<[^>]+>""")
    private val decimalEntity = Regex("""&#(\d+);""")
    private val hexEntity = Regex("""&#x([0-9a-fA-F]+);""")
    private val blankLines = Regex("""\n\s*\n+""")
    private val spaceRuns = Regex("""[ \t]+""")

    fun clean(html: String, maxLen: Int = MAX_LEN): String {
        var s = html
        s = soundTag.replace(s, "")
        s = ttsTag.replace(s, "")
        s = styleBlock.replace(s, "")
        s = scriptBlock.replace(s, "")
        s = imgTag.replace(s, "")
        s = newlineTag.replace(s, "\n")
        s = anyTag.replace(s, "")
        s = decodeEntities(s)
        s = spaceRuns.replace(s, " ")
        s = blankLines.replace(s, "\n")
        s = s.lines().joinToString("\n") { it.trim() }.trim()
        if (s.length > maxLen) {
            s = s.substring(0, maxLen - 1) + "…"
        }
        return s
    }

    private fun decodeEntities(input: String): String {
        var s = input
        s = decimalEntity.replace(s) { m ->
            m.groupValues[1].toIntOrNull()?.let { cp -> runCatching { String(Character.toChars(cp)) }.getOrNull() } ?: m.value
        }
        s = hexEntity.replace(s) { m ->
            m.groupValues[1].toIntOrNull(16)?.let { cp -> runCatching { String(Character.toChars(cp)) }.getOrNull() } ?: m.value
        }
        s = s.replace("&nbsp;", " ")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
        // &amp; last so `&amp;lt;` decodes to the literal `&lt;`, not `<`
        s = s.replace("&amp;", "&")
        return s
    }
}
