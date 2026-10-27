package com.notifast.app.domain.util

import com.notifast.app.data.model.Evaluation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date

class ScheduleCalculationsTest {

    private fun fixedCalendar(year: Int, month: Int, dayOfMonth: Int, hour: Int = 12, minute: Int = 0): Calendar {
        return Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    @Test
    fun `getStartOfWeek when firstDayOfWeek is SUNDAY returns previous Sunday at midnight`() {
        // Wednesday, Oct 28, 2026 -> Start is Sunday, Oct 25, 2026
        val wednesday = fixedCalendar(2026, Calendar.OCTOBER, 28)
        val start = ScheduleCalculations.getStartOfWeek(wednesday, "SUNDAY")

        assertEquals(2026, start.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, start.get(Calendar.MONTH))
        assertEquals(25, start.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, start.get(Calendar.MINUTE))
        assertEquals(0, start.get(Calendar.SECOND))
        assertEquals(0, start.get(Calendar.MILLISECOND))
    }

    @Test
    fun `getStartOfWeek when firstDayOfWeek is SUNDAY and date is Sunday returns same Sunday`() {
        // Sunday, Oct 25, 2026
        val sunday = fixedCalendar(2026, Calendar.OCTOBER, 25, 15, 30)
        val start = ScheduleCalculations.getStartOfWeek(sunday, "SUNDAY")

        assertEquals(25, start.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `getStartOfWeek when firstDayOfWeek is MONDAY returns previous Monday at midnight`() {
        // Wednesday, Oct 28, 2026 -> Start is Monday, Oct 26, 2026
        val wednesday = fixedCalendar(2026, Calendar.OCTOBER, 28)
        val start = ScheduleCalculations.getStartOfWeek(wednesday, "MONDAY")

        assertEquals(2026, start.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, start.get(Calendar.MONTH))
        assertEquals(26, start.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, start.get(Calendar.MINUTE))
    }

    @Test
    fun `getStartOfWeek when firstDayOfWeek is MONDAY and date is Sunday returns Monday 6 days prior`() {
        // Sunday, Nov 1, 2026 -> belongs to week that started Monday, Oct 26, 2026
        val sunday = fixedCalendar(2026, Calendar.NOVEMBER, 1, 22, 0)
        val start = ScheduleCalculations.getStartOfWeek(sunday, "MONDAY")

        assertEquals(Calendar.OCTOBER, start.get(Calendar.MONTH))
        assertEquals(26, start.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, start.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `calculateEvaluationCountsForWeek returns empty map when evaluations is empty`() {
        val refDate = fixedCalendar(2026, Calendar.OCTOBER, 28)
        val result = ScheduleCalculations.calculateEvaluationCountsForWeek(emptyList(), refDate, "MONDAY")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `calculateEvaluationCountsForWeek ignores evaluations with null date`() {
        val refDate = fixedCalendar(2026, Calendar.OCTOBER, 28)
        val evals = listOf(Evaluation(id = "1", title = "No Date", date = null))
        val result = ScheduleCalculations.calculateEvaluationCountsForWeek(evals, refDate, "MONDAY")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `calculateEvaluationCountsForWeek counts only evaluations in current visible week`() {
        // Week of Monday Oct 26, 2026 to Sunday Nov 1, 2026
        val refDate = fixedCalendar(2026, Calendar.OCTOBER, 28) // Wednesday

        val tuesdayThisWeek = fixedCalendar(2026, Calendar.OCTOBER, 27).time // Tuesday (day 2)
        val thursdayThisWeek1 = fixedCalendar(2026, Calendar.OCTOBER, 29, 9, 0).time // Thursday (day 4)
        val thursdayThisWeek2 = fixedCalendar(2026, Calendar.OCTOBER, 29, 14, 0).time // Thursday (day 4)
        val tuesdayIn3Weeks = fixedCalendar(2026, Calendar.NOVEMBER, 17).time // Tuesday in 3 weeks
        val pastTuesday = fixedCalendar(2026, Calendar.OCTOBER, 20).time // Previous week Tuesday

        val evals = listOf(
            Evaluation(id = "e1", title = "Parcial Martes", date = tuesdayThisWeek),
            Evaluation(id = "e2", title = "Quiz Jueves 1", date = thursdayThisWeek1),
            Evaluation(id = "e3", title = "Quiz Jueves 2", date = thursdayThisWeek2),
            Evaluation(id = "e4", title = "Examen Futuro", date = tuesdayIn3Weeks),
            Evaluation(id = "e5", title = "Examen Pasado", date = pastTuesday)
        )

        val counts = ScheduleCalculations.calculateEvaluationCountsForWeek(evals, refDate, "MONDAY")

        // Day 2 (Tuesday) should have 1 evaluation (NOT 2 or 3)
        assertEquals(1, counts[2])
        // Day 4 (Thursday) should have 2 evaluations
        assertEquals(2, counts[4])
        // Total counted days must be 2 (days 2 and 4)
        assertEquals(2, counts.size)
    }

    @Test
    fun `calculateEvaluationCountsForWeek includes Sunday when firstDayOfWeek is MONDAY`() {
        val refDate = fixedCalendar(2026, Calendar.OCTOBER, 28) // Wednesday Oct 28
        val sundayThisWeek = fixedCalendar(2026, Calendar.NOVEMBER, 1, 18, 0).time // Sunday Nov 1

        val evals = listOf(
            Evaluation(id = "e_sun", title = "Entrega Domingo", date = sundayThisWeek)
        )

        val counts = ScheduleCalculations.calculateEvaluationCountsForWeek(evals, refDate, "MONDAY")
        // Day 0 (Sunday)
        assertEquals(1, counts[0])
    }

    @Test
    fun `calculateEvaluationCountsForWeek excludes Sunday when firstDayOfWeek is SUNDAY and date was previous week`() {
        // Week of Sunday Nov 1, 2026 to Saturday Nov 7, 2026
        val refDate = fixedCalendar(2026, Calendar.NOVEMBER, 2) // Monday Nov 2
        val lastSunday = fixedCalendar(2026, Calendar.OCTOBER, 25).time

        val evals = listOf(
            Evaluation(id = "e_past_sun", title = "Domingo Pasado", date = lastSunday)
        )

        val counts = ScheduleCalculations.calculateEvaluationCountsForWeek(evals, refDate, "SUNDAY")
        assertEquals(0, counts[0] ?: 0)
    }
}
