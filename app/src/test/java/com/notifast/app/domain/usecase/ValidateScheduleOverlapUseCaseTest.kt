package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateScheduleOverlapUseCaseTest {

    private lateinit var useCase: ValidateScheduleOverlapUseCase

    @Before
    fun setUp() {
        useCase = ValidateScheduleOverlapUseCase()
    }

    @Test
    fun `when no existing schedules, returns NoOverlap`() {
        val newSchedule = Schedule(id = "1", subjectName = "Matemáticas", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        val result = useCase(emptyList(), newSchedule)
        assertEquals(ScheduleOverlapResult.NoOverlap, result)
    }

    @Test
    fun `when times overlap on same day, returns Conflict`() {
        val existing = listOf(
            Schedule(id = "1", subjectName = "Física", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        )
        val newSchedule = Schedule(id = "2", subjectName = "Química", dayOfWeek = 1, startTime = "09:00", endTime = "11:00")
        val result = useCase(existing, newSchedule)
        assertTrue(result is ScheduleOverlapResult.Conflict)
        assertEquals(1, (result as ScheduleOverlapResult.Conflict).conflictingSchedules.size)
        assertEquals("1", result.conflictingSchedules.first().id)
    }

    @Test
    fun `when times are adjacent on same day, returns NoOverlap`() {
        val existing = listOf(
            Schedule(id = "1", subjectName = "Física", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        )
        val newSchedule = Schedule(id = "2", subjectName = "Química", dayOfWeek = 1, startTime = "10:00", endTime = "12:00")
        val result = useCase(existing, newSchedule)
        assertEquals(ScheduleOverlapResult.NoOverlap, result)
    }

    @Test
    fun `when times overlap but on different days, returns NoOverlap`() {
        val existing = listOf(
            Schedule(id = "1", subjectName = "Física", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        )
        val newSchedule = Schedule(id = "2", subjectName = "Química", dayOfWeek = 2, startTime = "08:00", endTime = "10:00")
        val result = useCase(existing, newSchedule)
        assertEquals(ScheduleOverlapResult.NoOverlap, result)
    }

    @Test
    fun `when editing existing schedule, excludeScheduleId prevents self-conflict`() {
        val existing = listOf(
            Schedule(id = "1", subjectName = "Física", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        )
        val editedSchedule = Schedule(id = "1", subjectName = "Física Avanzada", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        val result = useCase(existing, editedSchedule, excludeScheduleId = "1")
        assertEquals(ScheduleOverlapResult.NoOverlap, result)
    }

    @Test
    fun `when time format is invalid, returns NoOverlap gracefully`() {
        val existing = listOf(
            Schedule(id = "1", subjectName = "Física", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        )
        val invalidSchedule = Schedule(id = "2", subjectName = "Química", dayOfWeek = 1, startTime = "invalid", endTime = "10:00")
        val result = useCase(existing, invalidSchedule)
        assertEquals(ScheduleOverlapResult.NoOverlap, result)
    }

    @Test
    fun `when end time is before or equal to start time, returns NoOverlap`() {
        val existing = listOf(
            Schedule(id = "1", subjectName = "Física", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        )
        val invalidSchedule = Schedule(id = "2", subjectName = "Química", dayOfWeek = 1, startTime = "10:00", endTime = "08:00")
        val result = useCase(existing, invalidSchedule)
        assertEquals(ScheduleOverlapResult.NoOverlap, result)
    }
}
