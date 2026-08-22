package com.notifast.app.presentation.feature.subjectdetail

import androidx.lifecycle.SavedStateHandle
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.CalculateRequiredGradeUseCase
import com.notifast.app.domain.usecase.CalculateWeightedAverageUseCase
import com.notifast.app.domain.usecase.SimulatorStatus
import com.notifast.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    private val userPrefsRepository: UserPrefsRepository = mockk(relaxed = true)
    private val calculateWeightedAverageUseCase = CalculateWeightedAverageUseCase()
    private val calculateRequiredGradeUseCase = CalculateRequiredGradeUseCase()

    private val sampleSubjects = listOf(
        SubjectData(name = "Matemáticas", colorHex = "#3B9797")
    )
    private val sampleEvaluations = listOf(
        Evaluation(id = "1", subjectName = "Matemáticas", title = "Parcial 1", isPresented = true, grade = 8.0, weight = 40.0),
        Evaluation(id = "2", subjectName = "Matemáticas", title = "Final", isPresented = false, weight = 60.0)
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(sampleSubjects)
        coEvery { noteRepository.getNotesBySubject("user_123", "Matemáticas") } returns flowOf(emptyList())
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(sampleEvaluations)
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-10")
    }

    @Test
    fun `when loaded, calculates initial simulation result`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("subjectName" to "Matemáticas"))
        val viewModel = SubjectDetailViewModel(
            savedStateHandle,
            noteRepository,
            evaluationRepository,
            authRepository,
            userPrefsRepository,
            calculateWeightedAverageUseCase,
            calculateRequiredGradeUseCase
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Matemáticas", state.subjectName)
        assertEquals(GradeScale.SCALE_0_10, state.gradeScale)
        assertNotNull(state.simulationResult)
        // 8.0 * 0.4 = 3.2 accumulated. Target: 6.0. Needed: 2.8 / 0.6 = 4.67
        assertEquals(3.2, state.simulationResult?.currentAccumulated ?: 0.0, 0.01)
        assertEquals(SimulatorStatus.ACHIEVABLE, state.simulationResult?.status)
    }

    @Test
    fun `when target grade updated, recalculates simulation`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("subjectName" to "Matemáticas"))
        val viewModel = SubjectDetailViewModel(
            savedStateHandle,
            noteRepository,
            evaluationRepository,
            authRepository,
            userPrefsRepository,
            calculateWeightedAverageUseCase,
            calculateRequiredGradeUseCase
        )
        advanceUntilIdle()

        viewModel.onEvent(SubjectDetailEvent.UpdateTargetGrade(9.5))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(9.5, state.targetGrade, 0.01)
        // Needed: 9.5 - 3.2 = 6.3 / 0.6 = 10.5 (> 10.0 max) -> IMPOSSIBLE
        assertEquals(SimulatorStatus.IMPOSSIBLE, state.simulationResult?.status)
    }
}
