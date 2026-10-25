package app.sypianski.garmanki.data

/**
 * Arabic letter shaping for the watch (SCHEMA.md §6, PC-003).
 *
 * Garmin's text engine joins plain Arabic by itself, but harakat in logical
 * order break the joining (gaps between letters). Letters already converted to
 * Presentation Forms-B draw correctly with harakat, so when a text contains
 * harakat every basic Arabic letter is replaced by its contextual form.
 * Harakat stay right after their base letter. Pure Kotlin, JVM-testable.
 *
 * Not handled: lam-alef ligatures (lam and alef are drawn as separate forms)
 * and letters outside U+0621..U+064A (Persian/Urdu پ چ گ …), which keep their
 * base code point but still take part in the neighbours' joining.
 */
object ArabicShaper {

    private enum class Join { DUAL, RIGHT, NONE, TRANSPARENT }

    // base letter -> forms in order: isolated, final, initial, medial
    // (initial/medial absent for right-joining letters)
    private val forms: Map<Char, String> = mapOf(
        'ء' to "ﺀ",
        'آ' to "ﺁﺂ",
        'أ' to "ﺃﺄ",
        'ؤ' to "ﺅﺆ",
        'إ' to "ﺇﺈ",
        'ئ' to "ﺉﺊﺋﺌ",
        'ا' to "ﺍﺎ",
        'ب' to "ﺏﺐﺑﺒ",
        'ة' to "ﺓﺔ",
        'ت' to "ﺕﺖﺗﺘ",
        'ث' to "ﺙﺚﺛﺜ",
        'ج' to "ﺝﺞﺟﺠ",
        'ح' to "ﺡﺢﺣﺤ",
        'خ' to "ﺥﺦﺧﺨ",
        'د' to "ﺩﺪ",
        'ذ' to "ﺫﺬ",
        'ر' to "ﺭﺮ",
        'ز' to "ﺯﺰ",
        'س' to "ﺱﺲﺳﺴ",
        'ش' to "ﺵﺶﺷﺸ",
        'ص' to "ﺹﺺﺻﺼ",
        'ض' to "ﺽﺾﺿﻀ",
        'ط' to "ﻁﻂﻃﻄ",
        'ظ' to "ﻅﻆﻇﻈ",
        'ع' to "ﻉﻊﻋﻌ",
        'غ' to "ﻍﻎﻏﻐ",
        'ف' to "ﻑﻒﻓﻔ",
        'ق' to "ﻕﻖﻗﻘ",
        'ك' to "ﻙﻚﻛﻜ",
        'ل' to "ﻝﻞﻟﻠ",
        'م' to "ﻡﻢﻣﻤ",
        'ن' to "ﻥﻦﻧﻨ",
        'ه' to "ﻩﻪﻫﻬ",
        'و' to "ﻭﻮ",
        'ى' to "ﻯﻰ",
        'ي' to "ﻱﻲﻳﻴ",
    )

    private const val TATWEEL = 'ـ'

    // right-joining letters outside the basic block (joining behaviour only)
    private fun isExtendedRightJoining(c: Char) =
        c in 'ٱ'..'ٳ' || c in 'ٵ'..'ٷ' || c in 'ڈ'..'ڙ' ||
            c == 'ۀ' || c in 'ۃ'..'ۋ' || c == 'ۍ' || c == 'ۏ' ||
            c == 'ے' || c == 'ۓ'

    fun isHarakat(c: Char) = c in 'ً'..'ْ' || c == 'ٰ'

    private fun isTransparent(c: Char) =
        c in 'ً'..'ٟ' || c == 'ٰ' || c in 'ۖ'..'ۜ' ||
            c in '۟'..'ۤ' || c == 'ۧ' || c == 'ۨ' || c in '۪'..'ۭ'

    private fun joinOf(c: Char): Join = when {
        isTransparent(c) -> Join.TRANSPARENT
        c == TATWEEL -> Join.DUAL
        forms.containsKey(c) -> if ((forms[c]!!.length) >= 4) Join.DUAL else if (c == 'ء') Join.NONE else Join.RIGHT
        isExtendedRightJoining(c) -> Join.RIGHT
        c in 'ٴ'..'ە' || c == 'ؠ' -> Join.DUAL
        else -> Join.NONE
    }

    fun hasHarakat(text: String) = text.any { isHarakat(it) }

    /** Shapes [text] only when it contains harakat; otherwise returns it untouched. */
    fun shapeIfHarakat(text: String): String = if (hasHarakat(text)) shape(text) else text

    fun shape(text: String): String {
        val out = StringBuilder(text.length)
        for (i in text.indices) {
            val c = text[i]
            val f = forms[c]
            if (f == null) {
                out.append(c)
                continue
            }
            val prevJoinsForward = previousBase(text, i)?.let { joinOf(it) == Join.DUAL } ?: false
            val me = joinOf(c)
            val nextJoinsBack = nextBase(text, i)?.let { joinOf(it).let { j -> j == Join.DUAL || j == Join.RIGHT } } ?: false
            val connectsPrev = prevJoinsForward
            val connectsNext = me == Join.DUAL && nextJoinsBack
            val idx = when {
                me == Join.NONE -> 0
                me == Join.RIGHT -> if (connectsPrev) 1 else 0
                connectsPrev && connectsNext -> 3
                connectsPrev -> 1
                connectsNext -> 2
                else -> 0
            }
            out.append(f[idx])
        }
        return out.toString()
    }

    private fun previousBase(text: String, i: Int): Char? {
        var j = i - 1
        while (j >= 0 && joinOf(text[j]) == Join.TRANSPARENT) j--
        return if (j >= 0) text[j] else null
    }

    private fun nextBase(text: String, i: Int): Char? {
        var j = i + 1
        while (j < text.length && joinOf(text[j]) == Join.TRANSPARENT) j++
        return if (j < text.length) text[j] else null
    }
}
