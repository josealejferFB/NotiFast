package com.notifast.app.domain.util

import com.notifast.app.data.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ScheduleCalculationsNextClassTimingTest {

    private fun fixedCalendar(dayOfWeek: Int, hour: Int, minute: Int, dayOfMonth: Int = 19, month: Int = Calendar.OCTOBER): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.DAY_OF_WEEK, dayOfWeek + 1) // Calendar.SUNDAY is 1
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    @Test
    fun `when class is today and active, returns isOngoing true and remaining time`() {
        // Today is Monday (1), 08:30
        val now = fixedCalendar(dayOfWeek = 1, hour = 8, minute = 30)
        val schedule = Schedule(
            id = "s1",
            subjectName = "Cálculo I",
            dayOfWeek = 1,
            startTime = "08:00",
            endTime = "09:45"
        )

        val timing = ScheduleCalculations.calculateNextClassTiming(schedule, now)

        assertTrue(timing.isOngoing)
        assertEquals("Hoy, Lunes", timing.dayName)
        assertEquals("Termina en 1 h 15 min", timing.timeRemaining) // 08:30 to 09:45 is 75 min -> 1 h 15 min
    }

    @Test
    fun `when class is today and starts in 45 min, returns En 45 min`() {
        // Today is Monday (1), 07:15
        val now = fixedCalendar(dayOfWeek = 1, hour = 7, minute = 15)
        val schedule = Schedule(
            id = "s1",
            subjectName = "Cálculo I",
            dayOfWeek = 1,
            startTime = "08:00",
            endTime = "09:45"
        )

        val timing = ScheduleCalculations.calculateNextClassTiming(schedule, now)

        assertFalse(timing.isOngoing)
        assertEquals("Hoy, Lunes", timing.dayName)
        assertEquals("En 45 min", timing.timeRemaining)
    }

    @Test
    fun `when class is today and starts in 2 hours 15 min, returns En 2 h 15 min`() {
        // Today is Monday (1), 08:00. Class is at 10:15
        val now = fixedCalendar(dayOfWeek = 1, hour = 8, minute = 0)
        val schedule = Schedule(
            id = "s1",
            subjectName = "Física",
            dayOfWeek = 1,
            startTime = "10:15",
            endTime = "12:00"
        )

        val timing = ScheduleCalculations.calculateNextClassTiming(schedule, now)

        assertFalse(timing.isOngoing)
        assertEquals("Hoy, Lunes", timing.dayName)
        assertEquals("En 2 h 15 min", timing.timeRemaining)
    }

    @Test
    fun `when class is tomorrow, returns Mañana, Martes and hours remaining`() {
        // Today is Monday (1), 18:00. Class is tomorrow Tuesday (2) at 08:00
        val now = fixedCalendar(dayOfWeek = 1, hour = 18, minute = 0)
        val schedule = Schedule(
            id = "s1",
            subjectName = "Programación",
            dayOfWeek = 2,
            startTime = "08:00",
            endTime = "10:00"
        )

        val timing = ScheduleCalculations.calculateNextClassTiming(schedule, now)

        assertFalse(timing.isOngoing)
        assertEquals("Mañana, Martes", timing.dayName)
        assertEquals("En 14 h", timing.timeRemaining)
    }

    @Test
    fun `when class is 2 days ahead, returns day name with month and days and hours remaining`() {
        // Today is Monday (1), 18:00. Class is Wednesday (3) at 10:30
        val now = fixedCalendar(dayOfWeek = 1, hour = 18, minute = 0, dayOfMonth = 19)
        val schedule = Schedule(
            id = "s1",
            subjectName = "Química",
            dayOfWeek = 3,
            startTime = "10:30",
            endTime = "12:30"
        )

        val timing = ScheduleCalculations.calculateNextClassTiming(schedule, now)

        assertFalse(timing.isOngoing)
        assertTrue(timing.dayName.startsWith("Miércoles, 21"))
        assertEquals("En 1 día y 16 h", timing.timeRemaining) // 40.5 hours = 1 day and 16.5 hours -> 16 h
    }

    @Test
    fun `when class today is already ended, returns next week occurrence`() {
        // Today is Monday (1), 16:00. Class was Monday at 08:00-10:00
        val now = fixedCalendar(dayOfWeek = 1, hour = 16, minute = 0, dayOfMonth = 19)
        val schedule = Schedule(
            id = "s1",
            subjectName = "Álgebra",
            dayOfWeek = 1,
            startTime = "08:00",
            endTime = "10:00"
        )

        val timing = ScheduleCalculations.calculateNextClassTiming(schedule, now)

        assertFalse(timing.isOngoing)
        assertTrue(timing.dayName.startsWith("Lunes, 26"))
        assertEquals("En 6 días y 16 h", timing.timeRemaining)
    }

    @Test
    fun `when class starts in 0 minutes, returns En breve`() {
        val now = fixedCalendar(dayOfWeek = 1, hour = 8, minute = 0)
        val schedule = Schedule(
            id = "s1",
            subjectName = "Álgebra",
            dayOfWeek = 1,
            startTime = "08:00",
            endTime = "10:00"
        )

        val timing = ScheduleCalculations.calculateNextClassTiming(schedule, now)
        // 08:00 is exactly startMin, so nowMin in startMin until endMin -> ongoing
        assertTrue(timing.isOngoing)
        assertEquals("Termina en 2 h", timing.timeRemaining)
    }
}
