package com.notifast.app.domain.usecase.schedule

import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.schedule.UnergImportConflictStrategy
import com.notifast.app.domain.model.schedule.UnergImportPreviewSubject
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImportUnergScheduleUseCaseTest {

    private val noteRepository = mockk<NoteRepository>()
    private val scheduleRepository = mockk<ScheduleRepository>()
    private lateinit var useCase: ImportUnergScheduleUseCase

    @Before
    fun setUp() {
        useCase = ImportUnergScheduleUseCase(noteRepository, scheduleRepository)
    }

    @Test
    fun `import new subject creates subject with section and inserts schedules`() = runTest {
        coEvery { scheduleRepository.getSchedules("u1") } returns flowOf(emptyList())
        coEvery {
            noteRepository.addSubject(
                userId = "u1",
                subjectName = "Sistemas Distribuidos",
                icon = "Computer",
                colorHex = "#4338CA",
                cycle = "",
                section = "1"
            )
        } returns Result.success("new-subj-id")

        val schedule = Schedule(
            id = "temp-1",
            subjectName = "Sistemas Distribuidos",
            dayOfWeek = 1,
            startTime = "10:16",
            endTime = "12:30",
            classroom = "Aula 1",
            modality = "Presencial"
        )
        coEvery { scheduleRepository.addSchedule("u1", any()) } returns Result.success("sched-1")

        val previewSubject = UnergImportPreviewSubject(
            subjectName = "Sistemas Distribuidos",
            section = "1",
            hoursPerWeek = 4,
            classroomSummary = "Aula 1",
            schedules = listOf(schedule),
            isExistingSubject = false,
            colorHex = "#4338CA",
            icon = "Computer",
            selected = true
        )

        val result = useCase("u1", listOf(previewSubject), UnergImportConflictStrategy.MERGE_SMART)
        assertTrue(result.isSuccess)

        val stats = result.getOrThrow()
        assertEquals(1, stats.createdSubjectsCount)
        assertEquals(0, stats.reusedSubjectsCount)
        assertEquals(1, stats.createdSchedulesCount)

        coVerify {
            scheduleRepository.addSchedule("u1", match {
                it.subjectId == "new-subj-id" && it.subjectName == "Sistemas Distribuidos" && it.classroom == "Aula 1"
            })
        }
    }

    @Test
    fun `import existing subject with REPLACE_ALL_SCHEDULES removes previous schedules and updates section`() = runTest {
        val existingSubject = SubjectData(
            id = "subj-old",
            name = "Sistemas Distribuidos",
            icon = "Computer",
            colorHex = "#4338CA",
            section = ""
        )
        coEvery { noteRepository.getSubjects("u1") } returns flowOf(listOf(existingSubject))
        coEvery { noteRepository.updateSubject("u1", any()) } returns Result.success(Unit)

        val oldSchedule = Schedule(
            id = "old-sched-99",
            userId = "u1",
            subjectId = "subj-old",
            subjectName = "Sistemas Distribuidos",
            dayOfWeek = 2,
            startTime = "07:00",
            endTime = "09:00"
        )
        coEvery { scheduleRepository.getSchedules("u1") } returns flowOf(listOf(oldSchedule))
        coEvery { scheduleRepository.deleteSchedule("u1", oldSchedule) } returns Result.success(Unit)

        val newSchedule = Schedule(
            id = "temp-2",
            subjectName = "Sistemas Distribuidos",
            dayOfWeek = 1,
            startTime = "10:16",
            endTime = "12:30",
            classroom = "Aula 1"
        )
        coEvery { scheduleRepository.addSchedule("u1", any()) } returns Result.success("new-sched-id")

        val previewSubject = UnergImportPreviewSubject(
            subjectName = "Sistemas Distribuidos",
            section = "1",
            hoursPerWeek = 4,
            classroomSummary = "Aula 1",
            schedules = listOf(newSchedule),
            isExistingSubject = true,
            existingSubjectId = "subj-old",
            colorHex = "#4338CA",
            icon = "Computer",
            selected = true
        )

        val result = useCase("u1", listOf(previewSubject), UnergImportConflictStrategy.REPLACE_ALL_SCHEDULES)
        assertTrue(result.isSuccess)

        val stats = result.getOrThrow()
        assertEquals(0, stats.createdSubjectsCount)
        assertEquals(1, stats.reusedSubjectsCount)
        assertEquals(1, stats.createdSchedulesCount)
        assertEquals(1, stats.replacedSchedulesCount)

        coVerify { scheduleRepository.deleteSchedule("u1", oldSchedule) }
        coVerify {
            noteRepository.updateSubject("u1", match {
                it.id == "subj-old" && it.section == "1"
            })
        }
    }
}
