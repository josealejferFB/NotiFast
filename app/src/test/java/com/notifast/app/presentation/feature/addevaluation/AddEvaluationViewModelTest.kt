package com.notifast.app.presentation.feature.addevaluation

import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.UserPrefsRepository
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
class AddEvaluationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val evaluationRepository: EvaluationRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val userPrefsRepository: UserPrefsRepository = mockk(relaxed = true)
    private val scheduleEvaluationAlertUseCase: ScheduleEvaluationAlertUseCase = mockk(relaxed = true)

    private val sampleSubjects = listOf(
        SubjectData(name = "Matemáticas", colorHex = "#3B9797")
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(sampleSubjects)
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-20")
        coEvery { evaluationRepository.addEvaluation(any(), any(), any()) } returns Result.success("eval-1")
    }

    private fun createViewModel(): AddEvaluationViewModel {
        return AddEvaluationViewModel(
            noteRepository,
            evaluationRepository,
            authRepository,
            userPrefsRepository,
            scheduleEvaluationAlertUseCase
        )
    }

    @Test
    fun `Save stamps active grade scale id`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddEvaluationEvent.SelectSubject("Matemáticas"))
        viewModel.onEvent(AddEvaluationEvent.UpdateTitle("Parcial 2"))
        viewModel.onEvent(AddEvaluationEvent.Save(Date(), null))
        advanceUntilIdle()

        coVerify {
            evaluationRepository.addEvaluation(
                "user_123",
                match {
                    it.subjectName == "Matemáticas" &&
                        it.title == "Parcial 2" &&
                        it.gradeScaleId == "0-20"
                },
                null
            )
        }
    }

    @Test
    fun `ResetState resets form fields and isSaved but preserves loaded subjects`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddEvaluationEvent.SelectSubject("Matemáticas"))
        viewModel.onEvent(AddEvaluationEvent.UpdateTitle("Parcial 1"))
        viewModel.onEvent(AddEvaluationEvent.UpdateDescription("Temas 1-3"))
        viewModel.onEvent(AddEvaluationEvent.UpdateQuickTips("Estudiar"))
        viewModel.onEvent(AddEvaluationEvent.UpdateWeight("25"))

        assertEquals("Parcial 1", viewModel.uiState.value.title)
        assertEquals(1, viewModel.uiState.value.subjects.size)

        viewModel.onEvent(AddEvaluationEvent.ResetState)

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("", state.description)
        assertEquals("", state.quickTips)
        assertEquals("", state.weight)
        assertEquals("", state.selectedSubject)
        assertFalse(state.isSaved)
        assertEquals(1, state.subjects.size)
    }

    @Test
    fun `RefreshSubjectsAndSelect reloads subjects and updates selectedSubject preserving other fields`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddEvaluationEvent.UpdateTitle("Parcial 1"))
        viewModel.onEvent(AddEvaluationEvent.UpdateDescription("Temas 1-3"))
        viewModel.onEvent(AddEvaluationEvent.UpdateWeight("25"))

        val updatedSubjects = listOf(
            SubjectData(name = "Matemáticas", colorHex = "#3B9797"),
            SubjectData(name = "Química", colorHex = "#FF5722")
        )
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(updatedSubjects)

        viewModel.onEvent(AddEvaluationEvent.RefreshSubjectsAndSelect("Química"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Química", state.selectedSubject)
        assertEquals(2, state.subjects.size)
        assertEquals("Parcial 1", state.title)
        assertEquals("Temas 1-3", state.description)
        assertEquals("25", state.weight)
    }

    @Test
    fun `double click on Save executes repository addEvaluation exactly once`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddEvaluationEvent.SelectSubject("Matemáticas"))
        viewModel.onEvent(AddEvaluationEvent.UpdateTitle("Examen Final"))

        viewModel.onEvent(AddEvaluationEvent.Save(Date(), null))
        viewModel.onEvent(AddEvaluationEvent.Save(Date(), null))
        advanceUntilIdle()

        coVerify(exactly = 1) {
            evaluationRepository.addEvaluation(eq("user_123"), any(), any())
        }
        assertTrue(viewModel.uiState.value.isSaved)
    }
}
