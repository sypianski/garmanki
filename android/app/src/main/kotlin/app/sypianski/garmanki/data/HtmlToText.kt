package app.sypianski.garmanki.data

import java.text.Normalizer

/**
 * Flattens AnkiDroid-rendered card HTML into watch-displayable plain text.
 * Rules and their order come from SCHEMA.md §6 — keep both in sync.
 * Pure Kotlin (no android.*) so it stays unit-testable on the JVM.
 */
object HtmlToText {

    const val MAX_LEN = 300

    /** UTF-8 cap per field after shaping; presentation forms cost 3 B per letter (SCHEMA.md §2). */
    const val MAX_BYTES = 600

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

    fun clean(html: String, maxLen: Int = MAX_LEN, maxBytes: Int = MAX_BYTES): String {
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
        s = Normalizer.normalize(s, Normalizer.Form.NFC)
        if (s.length > maxLen) {
            s = s.substring(0, maxLen - 1) + "…"
        }
        return capBytes(ArabicShaper.shapeIfHarakat(s), maxBytes)
    }

    /** Cuts [s] at a code point boundary so it fits [maxBytes] in UTF-8, ending with `…`. */
    private fun capBytes(s: String, maxBytes: Int): String {
        if (utf8Len(s) <= maxBytes) return s
        val budget = maxBytes - 3 // `…` is 3 bytes
        var bytes = 0
        var end = 0
        while (end < s.length) {
            val cp = s.codePointAt(end)
            val n = utf8Len(cp)
            if (bytes + n > budget) break
            bytes += n
            end += Character.charCount(cp)
        }
        return s.substring(0, end).trimEnd() + "…"
    }

    private fun utf8Len(s: String): Int {
        var n = 0
        var i = 0
        while (i < s.length) {
            val cp = s.codePointAt(i)
            n += utf8Len(cp)
            i += Character.charCount(cp)
        }
        return n
    }

    private fun utf8Len(cp: Int) = when {
        cp < 0x80 -> 1
        cp < 0x800 -> 2
        cp < 0x10000 -> 3
        else -> 4
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
