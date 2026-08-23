package com.notifast.app.presentation.feature.grades

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.CalculateWeightedAverageUseCase
import com.notifast.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GradesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val evaluationRepository: EvaluationRepository = mockk(relaxed = true)
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val userPrefsRepository: UserPrefsRepository = mockk(relaxed = true)
    private val calculateWeightedAverageUseCase = CalculateWeightedAverageUseCase()

    private val sampleSubjects = listOf(
        SubjectData(name = "Matemáticas", colorHex = "#3B9797")
    )
    private val sampleEvaluations = listOf(
        Evaluation(id = "1", subjectName = "Matemáticas", title = "Parcial 1", isPresented = true, grade = 8.0, weight = 100.0)
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { authRepository.getUserProfile(any()) } returns Result.success(com.notifast.app.util.TestDataFactory.createUser())
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(sampleSubjects)
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(sampleEvaluations)
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-20")
    }

    @Test
    fun `when initialized, loads evaluations and calculates globalAverage with grade scale`() = runTest {
        val viewModel = GradesViewModel(
            evaluationRepository,
            noteRepository,
            authRepository,
            userPrefsRepository,
            calculateWeightedAverageUseCase
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(8.0, state.globalAverage, 0.01)
        assertEquals(GradeScale.SCALE_0_20, state.gradeScale)
        assertEquals(1, state.subjectGrades.size)
    }
}
