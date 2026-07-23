package app.sypianski.garmanki

import app.sypianski.garmanki.data.Stats
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsTest {

    private val today = 20_000L

    @Test
    fun `empty log means zero`() {
        assertEquals(0, Stats.doneToday(emptyMap(), today))
        assertEquals(0, Stats.streak(emptyMap(), today))
    }

    @Test
    fun `done today counts only today`() {
        val log = mapOf(today to 12, today - 1 to 30)
        assertEquals(12, Stats.doneToday(log, today))
    }

    @Test
    fun `streak counts consecutive days back from today`() {
        val log = mapOf(today to 1, today - 1 to 5, today - 2 to 2, today - 4 to 9)
        assertEquals(3, Stats.streak(log, today))
    }

    @Test
    fun `streak survives midnight before first review of the day`() {
        val log = mapOf(today - 1 to 5, today - 2 to 2)
        assertEquals(2, Stats.streak(log, today))
    }

    @Test
    fun `gap yesterday breaks streak even with older entries`() {
        val log = mapOf(today - 2 to 5, today - 3 to 2)
        assertEquals(0, Stats.streak(log, today))
    }
}
