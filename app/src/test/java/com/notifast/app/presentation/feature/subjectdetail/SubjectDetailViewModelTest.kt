package com.notifast.app.presentation.feature.subjectdetail

import androidx.lifecycle.SavedStateHandle
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.CalculateRequiredGradeUseCase
import com.notifast.app.domain.usecase.CalculateWeightedAverageUseCase
import com.notifast.app.domain.usecase.SimulatorStatus
import com.notifast.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class SubjectDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val evaluationRepository: EvaluationRepository = mockk(relaxed = true)
    private val scheduleRepository: ScheduleRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val userPrefsRepository: UserPrefsRepository = mockk(relaxed = true)
    private val calculateWeightedAverageUseCase = CalculateWeightedAverageUseCase()
    private val calculateRequiredGradeUseCase = CalculateRequiredGradeUseCase()
    private val exportSubjectPackageUseCase: com.notifast.app.domain.usecase.ExportSubjectPackageUseCase = mockk(relaxed = true)

    private val sampleSubjects = listOf(
        SubjectData(name = "Matemáticas", colorHex = "#3B9797", icon = "📐")
    )
    private val sampleEvaluations = listOf(
        Evaluation(id = "1", subjectName = "Matemáticas", title = "Parcial 1", isPresented = true, grade = 8.0, weight = 40.0),
        Evaluation(id = "2", subjectName = "Matemáticas", title = "Final", isPresented = false, weight = 40.0, date = Date(System.currentTimeMillis() + 86400000L))
    )
    private val sampleNotes = listOf(
        Note(id = "n1", subject = "Matemáticas", title = "Fórmulas de Integrales", content = "Integración por partes", tags = listOf("fórmulas", "resumen")),
        Note(id = "n2", subject = "Matemáticas", title = "Teorema de Stokes", content = "Cálculo vectorial", tags = listOf("teoría"))
    )
    private val sampleSchedules = listOf(
        Schedule(id = "s1", subjectName = "Matemáticas", dayOfWeek = 1, startTime = "08:00", endTime = "10:00", classroom = "A-201", teacher = "Dr. Gauss")
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(sampleSubjects)
        coEvery { noteRepository.getNotesBySubject("user_123", "Matemáticas") } returns flowOf(sampleNotes)
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(sampleEvaluations)
        coEvery { scheduleRepository.getSchedules("user_123") } returns flowOf(sampleSchedules)
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-10")
    }

    private fun createViewModel(): SubjectDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("subjectName" to "Matemáticas"))
        return SubjectDetailViewModel(
            savedStateHandle,
            noteRepository,
            evaluationRepository,
            scheduleRepository,
            authRepository,
            userPrefsRepository,
            calculateWeightedAverageUseCase,
            calculateRequiredGradeUseCase,
            exportSubjectPackageUseCase
        )
    }

    @Test
    fun `when loaded, calculates initial metrics weights and schedules correctly`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Matemáticas", state.subjectName)
        assertEquals("📐", state.subjectIcon)
        assertEquals("#3B9797", state.subjectColorHex)
        assertEquals(GradeScale.SCALE_0_10, state.gradeScale)
        assertEquals(1, state.schedules.size)
        assertEquals("A-201", state.classroom)
        assertEquals("Dr. Gauss", state.teacher)
        
        // Weight balance: 40% + 40% = 80% assigned, 20% available, 40% evaluated, 40% pending
        assertEquals(80.0, state.totalAssignedWeight, 0.01)
        assertEquals(20.0, state.availableWeight, 0.01)
        assertEquals(40.0, state.evaluatedWeight, 0.01)
        assertEquals(40.0, state.pendingWeight, 0.01)

        // Notes and tags
        assertEquals(2, state.notes.size)
        assertEquals(2, state.filteredNotes.size)
        assertTrue(state.availableTags.contains("fórmulas"))
        assertTrue(state.availableTags.contains("teoría"))

        // Simulation
        assertNotNull(state.simulationResult)
        assertEquals(3.2, state.simulationResult?.currentAccumulated ?: 0.0, 0.01)
        assertEquals(SimulatorStatus.AT_RISK, state.simulationResult?.status)

        // Milestone
        assertTrue(state.nextMilestone is SubjectMilestone.Exam)
    }

    @Test
    fun `when select quadrant event, updates selectedQuadrant`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SubjectDetailEvent.SelectQuadrant(SubjectQuadrant.NOTES))
        advanceUntilIdle()
        assertEquals(SubjectQuadrant.NOTES, viewModel.uiState.value.selectedQuadrant)

        viewModel.onEvent(SubjectDetailEvent.SelectQuadrant(SubjectQuadrant.SCHEDULES))
        advanceUntilIdle()
        assertEquals(SubjectQuadrant.SCHEDULES, viewModel.uiState.value.selectedQuadrant)
    }

    @Test
    fun `when search query or tag updated, filters notes correctly`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Filter by query
        viewModel.onEvent(SubjectDetailEvent.UpdateSearchQuery("Stokes"))
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredNotes.size)
        assertEquals("Teorema de Stokes", viewModel.uiState.value.filteredNotes.first().title)

        // Clear query and filter by tag
        viewModel.onEvent(SubjectDetailEvent.UpdateSearchQuery(""))
        viewModel.onEvent(SubjectDetailEvent.SelectTag("fórmulas"))
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredNotes.size)
        assertEquals("Fórmulas de Integrales", viewModel.uiState.value.filteredNotes.first().title)

        // Clear tag
        viewModel.onEvent(SubjectDetailEvent.SelectTag(null))
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.filteredNotes.size)
    }

    @Test
    fun `when target grade updated, recalculates simulation`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SubjectDetailEvent.UpdateTargetGrade(9.5))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(9.5, state.targetGrade, 0.01)
        assertEquals(SimulatorStatus.IMPOSSIBLE, state.simulationResult?.status)
    }

    @Test
    fun `when save grade event, calls repository update`() = runTest {
        coEvery { evaluationRepository.updateEvaluation(any(), any(), any()) } returns Result.success(Unit)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SubjectDetailEvent.SaveGrade("2", 9.0, true))
        advanceUntilIdle()

        coVerify { evaluationRepository.updateEvaluation("user_123", match { it.id == "2" && it.grade == 9.0 && it.isPresented }, null) }
    }

    @Test
    fun `SaveGrade stamps active grade scale id`() = runTest {
        coEvery { evaluationRepository.updateEvaluation(any(), any(), any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-20")
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SubjectDetailEvent.SaveGrade("2", 9.0, true))
        advanceUntilIdle()

        coVerify {
            evaluationRepository.updateEvaluation(
                "user_123",
                match { it.id == "2" && it.grade == 9.0 && it.gradeScaleId == "0-20" },
                null
            )
        }
    }

    @Test
    fun `when delete schedule event, calls repository delete`() = runTest {
        coEvery { scheduleRepository.deleteSchedule(any(), any()) } returns Result.success(Unit)
        val viewModel = createViewModel()
        advanceUntilIdle()

        val scheduleToDelete = sampleSchedules.first()
        viewModel.onEvent(SubjectDetailEvent.DeleteSchedule(scheduleToDelete))
        advanceUntilIdle()

        coVerify { scheduleRepository.deleteSchedule("user_123", scheduleToDelete) }
    }
}
