package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.repository.EvaluationRepository
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

class DeleteSubjectCascadeUseCaseTest {

    private lateinit var noteRepository: NoteRepository
    private lateinit var evaluationRepository: EvaluationRepository
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var deleteSubjectCascadeUseCase: DeleteSubjectCascadeUseCase

    @Before
    fun setup() {
        noteRepository = mockk(relaxed = true)
        evaluationRepository = mockk(relaxed = true)
        scheduleRepository = mockk(relaxed = true)
        deleteSubjectCascadeUseCase = DeleteSubjectCascadeUseCase(
            noteRepository = noteRepository,
            evaluationRepository = evaluationRepository,
            scheduleRepository = scheduleRepository
        )
    }

    @Test
    fun `invoke deletes evaluations, schedules and subject successfully`() = runTest {
        val userId = "user123"
        val subjectName = "Matemáticas"

        val eval1 = Evaluation(id = "eval1", userId = userId, subjectName = "Matemáticas", title = "Parcial 1")
        val eval2 = Evaluation(id = "eval2", userId = userId, subjectName = "Física", title = "Quiz")
        val sched1 = Schedule(id = "sched1", userId = userId, subjectName = "Matemáticas")

        coEvery { evaluationRepository.getEvaluations(userId) } returns flowOf(listOf(eval1, eval2))
        coEvery { scheduleRepository.getSchedules(userId) } returns flowOf(listOf(sched1))
        coEvery { evaluationRepository.deleteEvaluation(userId, eval1) } returns Result.success(Unit)
        coEvery { scheduleRepository.deleteSchedule(userId, sched1) } returns Result.success(Unit)
        coEvery { noteRepository.deleteSubject(userId, subjectName) } returns Result.success(Unit)

        val result = deleteSubjectCascadeUseCase(userId, subjectName)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { evaluationRepository.deleteEvaluation(userId, eval1) }
        coVerify(exactly = 0) { evaluationRepository.deleteEvaluation(userId, eval2) }
        coVerify(exactly = 1) { scheduleRepository.deleteSchedule(userId, sched1) }
        coVerify(exactly = 1) { noteRepository.deleteSubject(userId, subjectName) }
    }

    @Test
    fun `invoke returns failure when noteRepository fails`() = runTest {
        val userId = "user123"
        val subjectName = "Matemáticas"

        coEvery { evaluationRepository.getEvaluations(userId) } returns flowOf(emptyList())
        coEvery { scheduleRepository.getSchedules(userId) } returns flowOf(emptyList())
        coEvery { noteRepository.deleteSubject(userId, subjectName) } returns Result.failure(RuntimeException("Firestore error"))

        val result = deleteSubjectCascadeUseCase(userId, subjectName)

        assertTrue(result.isFailure)
        assertEquals("Firestore error", result.exceptionOrNull()?.message)
    }
}
