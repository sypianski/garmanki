package app.sypianski.garmanki.data

/** Watch-review stats derived from the local replay log (SCHEMA.md §7). */
object Stats {

    fun doneToday(log: Map<Long, Int>, today: Long): Int = log[today] ?: 0

    /**
     * Consecutive days with ≥1 applied answer, counting back from today —
     * or from yesterday when today has none yet (an unbroken streak isn't
     * lost at midnight).
     */
    fun streak(log: Map<Long, Int>, today: Long): Int {
        var day = if ((log[today] ?: 0) > 0) today else today - 1
        var n = 0
        while ((log[day] ?: 0) > 0) {
            n++
            day--
        }
        return n
    }
}
