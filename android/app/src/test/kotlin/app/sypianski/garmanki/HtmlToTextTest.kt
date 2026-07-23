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
}
