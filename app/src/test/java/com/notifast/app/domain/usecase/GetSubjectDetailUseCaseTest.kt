package com.notifast.app.domain.usecase

import app.cash.turbine.test
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetSubjectDetailUseCaseTest {

    private lateinit var noteRepository: NoteRepository
    private lateinit var evaluationRepository: EvaluationRepository
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var calculateWeightedAverageUseCase: CalculateWeightedAverageUseCase
    private lateinit var getSubjectDetailUseCase: GetSubjectDetailUseCase

    @Before
    fun setup() {
        noteRepository = mockk(relaxed = true)
        evaluationRepository = mockk(relaxed = true)
        scheduleRepository = mockk(relaxed = true)
        calculateWeightedAverageUseCase = CalculateWeightedAverageUseCase()
        getSubjectDetailUseCase = GetSubjectDetailUseCase(
            noteRepository = noteRepository,
            evaluationRepository = evaluationRepository,
            scheduleRepository = scheduleRepository,
            calculateWeightedAverageUseCase = calculateWeightedAverageUseCase
        )
    }

    @Test
    fun `invoke combines subject, notes, evaluations and schedules correctly`() = runTest {
        val userId = "user123"
        val subjectName = "Matemáticas"

        val subject = SubjectData(name = "Matemáticas", icon = "📐", colorHex = "#FF5722")
        val note = Note(id = "n1", title = "Cálculo", subject = "Matemáticas")
        val eval = Evaluation(id = "e1", title = "Parcial", subjectName = "Matemáticas", grade = 8.5, weight = 1.0, isPresented = true)
        val schedule = Schedule(id = "s1", subjectName = "Matemáticas")

        every { noteRepository.getSubjects(userId) } returns flowOf(listOf(subject))
        every { noteRepository.getNotes(userId) } returns flowOf(listOf(note, Note(id = "n2", title = "Historia", subject = "Historia")))
        every { evaluationRepository.getEvaluations(userId) } returns flowOf(listOf(eval))
        every { scheduleRepository.getSchedules(userId) } returns flowOf(listOf(schedule))

        getSubjectDetailUseCase(userId, subjectName).test {
            val item = awaitItem()
            assertNotNull(item)
            assertEquals("Matemáticas", item?.subject?.name)
            assertEquals(1, item?.notes?.size)
            assertEquals("Cálculo", item?.notes?.first()?.title)
            assertEquals(1, item?.evaluations?.size)
            assertEquals(8.5, item?.averageGrade ?: 0.0, 0.01)
            assertEquals(1, item?.schedules?.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke returns null when subject is not found`() = runTest {
        val userId = "user123"
        val subjectName = "Inexistente"

        every { noteRepository.getSubjects(userId) } returns flowOf(emptyList())
        every { noteRepository.getNotes(userId) } returns flowOf(emptyList())
        every { evaluationRepository.getEvaluations(userId) } returns flowOf(emptyList())
        every { scheduleRepository.getSchedules(userId) } returns flowOf(emptyList())

        getSubjectDetailUseCase(userId, subjectName).test {
            val item = awaitItem()
            assertNull(item)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
