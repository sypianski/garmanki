package app.sypianski.garmanki

import app.sypianski.garmanki.data.HtmlToText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlToTextTest {

    @Test
    fun `strips tags and sound, keeps text`() {
        val html = """<div class="front">Hund[sound:hund.mp3]</div>"""
        assertEquals("Hund", HtmlToText.clean(html))
    }

    @Test
    fun `br and block ends become newlines, blank runs collapse`() {
        val html = "pies<br><br/>kot<div>ryba</div><p>ptak</p>"
        assertEquals("pies\nkot\nryba\nptak", HtmlToText.clean(html))
    }

    @Test
    fun `style and script vanish with content`() {
        val html = "<style>.card{color:red}</style>A<script>alert(1)</script>B"
        assertEquals("AB", HtmlToText.clean(html))
    }

    @Test
    fun `img dropped, tts dropped`() {
        val html = """dom<img src="x.jpg">[anki:tts lang=pl_PL]dom[/anki:tts]"""
        assertEquals("dom", HtmlToText.clean(html))
    }

    @Test
    fun `entities decode, amp last`() {
        assertEquals("a<b & \"c\" — &lt;", HtmlToText.clean("a&lt;b &amp; &quot;c&quot; &#8212; &amp;lt;"))
    }

    @Test
    fun `cloze placeholder survives`() {
        assertEquals("Stolica Polski to [...]", HtmlToText.clean("Stolica Polski to [...]"))
    }

    @Test
    fun `polish diacritics survive`() {
        assertEquals("żółć gęślą jaźń", HtmlToText.clean("<b>żółć</b> gęślą&nbsp;jaźń"))
    }

    @Test
    fun `truncates to 300 with ellipsis`() {
        val out = HtmlToText.clean("x".repeat(500))
        assertEquals(300, out.length)
        assertTrue(out.endsWith("…"))
    }

    @Test
    fun `empty after cleanup yields empty string`() {
        assertEquals("", HtmlToText.clean("""<img src="a.png">[sound:a.mp3]"""))
    }

    @Test
    fun `combining marks are composed to NFC`() {
        // a + U+0304 (macron), h + U+0323 (dot below), s + U+030C (caron)
        assertEquals("ā ḥ š", HtmlToText.clean("a\u0304 h\u0323 s\u030C"))
        // already-composed text is unchanged
        assertEquals("ā ḥ š", HtmlToText.clean("ā ḥ š"))
    }

    @Test
    fun `plain arabic without harakat is untouched`() {
        val plain = "\u0643\u062A\u0627\u0628 \u0645\u0631\u062D\u0628\u0627"
        assertEquals(plain, HtmlToText.clean(plain))
    }

    @Test
    fun `arabic with harakat is shaped to presentation forms`() {
        // كِتَابٌ -> the probe string that rendered correctly on the Forerunner 965
        val logical = "\u0643\u0650\u062A\u064E\u0627\u0628\u064C"
        val expected = "\uFEDB\u0650\uFE98\u064E\uFE8E\uFE8F\u064C"
        assertEquals(expected, HtmlToText.clean(logical))
    }

    @Test
    fun `byte cap keeps shaped text within 600 bytes and cuts on a code point`() {
        val word = "\u0643\u0650\u062A\u064E\u0627\u0628\u064C "
        val out = HtmlToText.clean(word.repeat(40))
        assertTrue(out.endsWith("…"))
        assertTrue(out.toByteArray(Charsets.UTF_8).size <= HtmlToText.MAX_BYTES)
    }

    @Test
    fun `ascii and polish text up to 300 chars is not cut by the byte cap`() {
        val polish = "ż".repeat(300)
        assertEquals(polish, HtmlToText.clean(polish)) // 300 chars * 2 B = 600 B, exactly at the cap
    }
}
