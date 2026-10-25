package app.sypianski.garmanki

import app.sypianski.garmanki.data.ArabicShaper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.Normalizer

class ArabicShaperTest {

    @Test
    fun `every presentation form decomposes back to its base letter`() {
        val cls = Class.forName("app.sypianski.garmanki.data.ArabicShaper")
        val field = cls.getDeclaredField("forms").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        val forms = field.get(ArabicShaper) as Map<Char, String>
        assertEquals(36, forms.size)
        for ((base, fs) in forms) {
            for (f in fs) {
                assertTrue("U+%04X is not Forms-B".format(f.code), f in 'ﹰ'..'ﻼ')
                assertEquals(
                    "U+%04X should decompose to U+%04X".format(f.code, base.code),
                    base.toString(),
                    Normalizer.normalize(f.toString(), Normalizer.Form.NFKC),
                )
            }
        }
    }

    @Test
    fun `isolated initial medial final`() {
        // بسم: beh initial, seen medial, meem final (harakat force shaping)
        val out = ArabicShaper.shape("بِسم")
        assertEquals("ﺑِﺴﻢ", out)
    }

    @Test
    fun `right-joining letters break the chain`() {
        // دَرَس: dal isolated (does not join forward), reh final? dal never joins forward -> reh initial-less
        val out = ArabicShaper.shape("دَرَس")
        // dal isolated, reh isolated (previous dal is right-joining), seen isolated (reh is right-joining)
        assertEquals("ﺩَﺭَﺱ", out)
    }

    @Test
    fun `non arabic characters pass through and reset joining`() {
        val out = ArabicShaper.shape("abc بَ 123")
        assertEquals("abc ﺏَ 123", out)
    }

    @Test
    fun `shapeIfHarakat leaves text without harakat alone`() {
        val plain = "كتاب"
        assertEquals(plain, ArabicShaper.shapeIfHarakat(plain))
    }
}
