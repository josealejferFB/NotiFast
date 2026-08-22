package com.notifast.app.presentation.feature.subjectdetail

import androidx.lifecycle.SavedStateHandle
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.usecase.CalculateWeightedAverageUseCase
import com.notifast.app.util.MainDispatcherRule
import com.notifast.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubjectDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val evaluationRepository: EvaluationRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val calculateWeightedAverageUseCase = CalculateWeightedAverageUseCase()

    private val testUserId = "user-test"
    private val subjectName = "Química"

    private val savedStateHandle = SavedStateHandle(mapOf("subjectName" to subjectName))

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns testUserId
    }

    @Test
    fun `loadData calculates progress and average grade correctly`() = runTest {
        val eval1 = TestDataFactory.createEvaluation(id = "e1", subjectName = subjectName, grade = 9.0, weight = 50.0, isPresented = true)
        val eval2 = TestDataFactory.createEvaluation(id = "e2", subjectName = subjectName, grade = null, weight = 50.0, isPresented = false)
        val note1 = TestDataFactory.createNote(id = "n1", subject = subjectName, title = "Reacciones")
        val subject = TestDataFactory.createSubject(name = subjectName, icon = "🧪", colorHex = "#3B9797")

        every { noteRepository.getNotesBySubject(testUserId, subjectName) } returns flowOf(listOf(note1))
        every { evaluationRepository.getEvaluations(testUserId) } returns flowOf(listOf(eval1, eval2))
        every { noteRepository.getSubjects(testUserId) } returns flowOf(listOf(subject))

        val viewModel = SubjectDetailViewModel(
            savedStateHandle = savedStateHandle,
            noteRepository = noteRepository,
            evaluationRepository = evaluationRepository,
            authRepository = authRepository,
            calculateWeightedAverageUseCase = calculateWeightedAverageUseCase
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(subjectName, state.subjectName)
        assertEquals("🧪", state.subjectIcon)
        assertEquals(1, state.completedEvalCount)
        assertEquals(1, state.pendingEvalCount)
        assertEquals(0.5f, state.progressPercent, 0.01f)
        assertEquals(9.0, state.averageGrade ?: 0.0, 0.01)
        assertEquals(1, state.notes.size)
    }

    @Test
    fun `saveGrade updates evaluation and saves via repository`() = runTest {
        val eval1 = TestDataFactory.createEvaluation(id = "e1", subjectName = subjectName, grade = null, isPresented = false)
        every { noteRepository.getNotesBySubject(testUserId, subjectName) } returns flowOf(emptyList())
        every { evaluationRepository.getEvaluations(testUserId) } returns flowOf(listOf(eval1))
        every { noteRepository.getSubjects(testUserId) } returns flowOf(emptyList())
        coEvery { evaluationRepository.updateEvaluation(testUserId, any(), null) } returns Result.success(Unit)

        val viewModel = SubjectDetailViewModel(
            savedStateHandle = savedStateHandle,
            noteRepository = noteRepository,
            evaluationRepository = evaluationRepository,
            authRepository = authRepository,
            calculateWeightedAverageUseCase = calculateWeightedAverageUseCase
        )

        viewModel.onEvent(SubjectDetailEvent.SaveGrade("e1", 10.0, true))

        coVerify {
            evaluationRepository.updateEvaluation(
                testUserId,
                match { it.id == "e1" && it.grade == 10.0 && it.isPresented },
                null
            )
        }
    }

    @Test
    fun `deleteEvaluation calls repository and updates error on failure`() = runTest {
        val eval1 = TestDataFactory.createEvaluation(id = "e1", subjectName = subjectName)
        every { noteRepository.getNotesBySubject(testUserId, subjectName) } returns flowOf(emptyList())
        every { evaluationRepository.getEvaluations(testUserId) } returns flowOf(listOf(eval1))
        every { noteRepository.getSubjects(testUserId) } returns flowOf(emptyList())
        coEvery { evaluationRepository.deleteEvaluation(testUserId, any()) } returns Result.failure(Exception("Error al borrar"))

        val viewModel = SubjectDetailViewModel(
            savedStateHandle = savedStateHandle,
            noteRepository = noteRepository,
            evaluationRepository = evaluationRepository,
            authRepository = authRepository,
            calculateWeightedAverageUseCase = calculateWeightedAverageUseCase
        )

        viewModel.onEvent(SubjectDetailEvent.DeleteEvaluation("e1"))

        coVerify { evaluationRepository.deleteEvaluation(testUserId, eval1) }
        assertEquals("Error al borrar", viewModel.uiState.value.error)

        viewModel.onEvent(SubjectDetailEvent.ClearError)
        assertNull(viewModel.uiState.value.error)
    }
}
