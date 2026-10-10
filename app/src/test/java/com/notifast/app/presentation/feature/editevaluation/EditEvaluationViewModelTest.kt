package com.notifast.app.presentation.feature.editevaluation

import androidx.lifecycle.SavedStateHandle
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.CancelAlertUseCase
import com.notifast.app.domain.usecase.ScheduleEvaluationAlertUseCase
import com.notifast.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class EditEvaluationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val evaluationRepository: EvaluationRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val userPrefsRepository: UserPrefsRepository = mockk(relaxed = true)
    private val scheduleEvaluationAlertUseCase: ScheduleEvaluationAlertUseCase = mockk(relaxed = true)
    private val cancelAlertUseCase: CancelAlertUseCase = mockk(relaxed = true)

    private val sampleSubjects = listOf(
        SubjectData(name = "Matemáticas", colorHex = "#3B9797")
    )
    private val sampleEvaluation = Evaluation(
        id = "eval_1",
        userId = "user_123",
        subjectName = "Matemáticas",
        title = "Parcial 1",
        description = "Temas 1 al 4",
        grade = 8.5,
        weight = 30.0,
        gradeScaleId = "0-10"
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(sampleSubjects)
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-10")
        coEvery { evaluationRepository.getEvaluation("user_123", "eval_1") } returns flowOf(sampleEvaluation)
    }

    @Test
    fun `when initialized with evalId, loads evaluation fields immediately`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("evalId" to "eval_1"))
        val viewModel = EditEvaluationViewModel(
            savedStateHandle,
            noteRepository,
            evaluationRepository,
            authRepository,
            userPrefsRepository,
            scheduleEvaluationAlertUseCase,
            cancelAlertUseCase
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("eval_1", state.evalId)
        assertEquals("Parcial 1", state.title)
        assertEquals("Matemáticas", state.subjectName)
        assertEquals("8.5", state.grade)
        assertEquals("30.0", state.weight)
    }

    @Test
    fun `when initialized with empty savedStateHandle, does not crash and Init event loads data`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = EditEvaluationViewModel(
            savedStateHandle,
            noteRepository,
            evaluationRepository,
            authRepository,
            userPrefsRepository,
            scheduleEvaluationAlertUseCase,
            cancelAlertUseCase
        )
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.evalId)
        assertEquals("", viewModel.uiState.value.title)

        viewModel.onEvent(EditEvaluationEvent.Init("eval_1"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("eval_1", state.evalId)
        assertEquals("Parcial 1", state.title)
        assertEquals("Matemáticas", state.subjectName)
        assertEquals("8.5", state.grade)
    }

    @Test
    fun `when saving updated evaluation, calls updateEvaluation`() = runTest {
        coEvery { evaluationRepository.updateEvaluation(any(), any(), any()) } returns Result.success(Unit)
        val savedStateHandle = SavedStateHandle(mapOf("evalId" to "eval_1"))
        val viewModel = EditEvaluationViewModel(
            savedStateHandle,
            noteRepository,
            evaluationRepository,
            authRepository,
            userPrefsRepository,
            scheduleEvaluationAlertUseCase,
            cancelAlertUseCase
        )
        advanceUntilIdle()

        viewModel.onEvent(EditEvaluationEvent.UpdateTitle("Parcial 1 Modificado"))
        viewModel.onEvent(EditEvaluationEvent.Save(Date(), null))
        advanceUntilIdle()

        coVerify { evaluationRepository.updateEvaluation("user_123", match { it.title == "Parcial 1 Modificado" }, null) }
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `when Init is called after save, resets isSaved and reloads fresh data`() = runTest {
        coEvery { evaluationRepository.updateEvaluation(any(), any(), any()) } returns Result.success(Unit)
        val savedStateHandle = SavedStateHandle(mapOf("evalId" to "eval_1"))
        val viewModel = EditEvaluationViewModel(
            savedStateHandle,
            noteRepository,
            evaluationRepository,
            authRepository,
            userPrefsRepository,
            scheduleEvaluationAlertUseCase,
            cancelAlertUseCase
        )
        advanceUntilIdle()

        viewModel.onEvent(EditEvaluationEvent.Save(Date(), null))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSaved)

        viewModel.onEvent(EditEvaluationEvent.UpdateTitle("Título Sucio"))
        assertEquals("Título Sucio", viewModel.uiState.value.title)

        viewModel.onEvent(EditEvaluationEvent.Init("eval_1"))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSaved)
        assertEquals("Parcial 1", viewModel.uiState.value.title)
    }
}
