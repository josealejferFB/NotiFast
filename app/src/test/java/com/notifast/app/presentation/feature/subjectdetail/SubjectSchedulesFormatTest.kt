package com.notifast.app.presentation.feature.subjectdetail

import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.util.ScheduleCalculations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubjectSchedulesFormatTest {

    @Test
    fun `sorting schedules puts Monday before Friday and Sunday last`() {
        val sunday = Schedule(id = "sun", dayOfWeek = 0, startTime = "08:00", endTime = "10:00")
        val mondayLate = Schedule(id = "mon2", dayOfWeek = 1, startTime = "14:00", endTime = "16:00")
        val mondayEarly = Schedule(id = "mon1", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        val friday = Schedule(id = "fri", dayOfWeek = 5, startTime = "10:00", endTime = "12:00")

        val unsorted = listOf(sunday, friday, mondayLate, mondayEarly)
        val sorted = unsorted.sortedWith(
            compareBy(
                { if (it.dayOfWeek == 0) 7 else it.dayOfWeek },
                { ScheduleCalculations.timeToMinutes(it.startTime) }
            )
        )

        assertEquals("mon1", sorted[0].id)
        assertEquals("mon2", sorted[1].id)
        assertEquals("fri", sorted[2].id)
        assertEquals("sun", sorted[3].id)
    }

    @Test
    fun `total weekly minutes calculation aggregates all sessions properly`() {
        val s1 = Schedule(id = "1", startTime = "08:00", endTime = "10:00") // 120 mins
        val s2 = Schedule(id = "2", startTime = "14:00", endTime = "15:30") // 90 mins
        val list = listOf(s1, s2)

        val totalMinutes = list.sumOf { s ->
            val start = ScheduleCalculations.timeToMinutes(s.startTime)
            val end = ScheduleCalculations.timeToMinutes(s.endTime)
            (end - start).coerceAtLeast(0)
        }

        assertEquals(210, totalMinutes)
        val hours = totalMinutes / 60f
        assertEquals("3.5", String.format(java.util.Locale.US, "%.1f", hours))
    }

    @Test
    fun `duration badge formats correctly for hours and remaining minutes`() {
        fun formatDuration(start: String, end: String): String {
            val s = ScheduleCalculations.timeToMinutes(start)
            val e = ScheduleCalculations.timeToMinutes(end)
            val diff = (e - s).coerceAtLeast(0)
            val h = diff / 60
            val rem = diff % 60
            return when {
                h > 0 && rem > 0 -> "${h}h ${rem}m"
                h > 0 -> "${h}h"
                else -> "${diff}m"
            }
        }

        assertEquals("2h", formatDuration("08:00", "10:00"))
        assertEquals("1h 30m", formatDuration("10:00", "11:30"))
        assertEquals("45m", formatDuration("08:00", "08:45"))
    }
}
