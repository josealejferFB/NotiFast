package com.notifast.app.presentation.feature.notes

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.User
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.usecase.CalculateWeightedAverageUseCase
import com.notifast.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val evaluationRepository: EvaluationRepository = mockk(relaxed = true)
    private val scheduleRepository: ScheduleRepository = mockk(relaxed = true)
    private val calculateWeightedAverageUseCase = CalculateWeightedAverageUseCase()
    private val importSubjectPackageUseCase: com.notifast.app.domain.usecase.ImportSubjectPackageUseCase = mockk(relaxed = true)

    private val sampleSubjects = listOf(
        SubjectData(name = "Matemáticas", colorHex = "#3B9797"),
        SubjectData(name = "Física", colorHex = "#BF092F")
    )
    private val sampleNotes = listOf(
        Note(id = "1", subject = "Matemáticas", title = "Álgebra Lineal", content = "Matrices y determinantes", tags = listOf("Examen", "Fórmula")),
        Note(id = "2", subject = "Física", title = "Cinemática", content = "Velocidad y aceleración", tags = listOf("Resumen"))
    )

    private fun createViewModel(): NotesViewModel = NotesViewModel(
        noteRepository,
        authRepository,
        evaluationRepository,
        scheduleRepository,
        calculateWeightedAverageUseCase,
        importSubjectPackageUseCase
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { authRepository.getUserProfile("user_123") } returns Result.success(User(firstName = "Test", lastName = "User", email = "test@example.com"))
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(sampleSubjects)
        coEvery { noteRepository.getNotes("user_123") } returns flowOf(sampleNotes)
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(emptyList())
        coEvery { scheduleRepository.getSchedules("user_123") } returns flowOf(emptyList())
    }

    @Test
    fun `when initialized, loads notes and populates availableTags and stats`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.rawNotes.size)
        assertEquals(2, state.totalNotesCount)
        assertEquals(2, state.totalSubjectsCount)
        assertEquals(listOf("Examen", "Fórmula", "Resumen"), state.availableTags)
    }

    @Test
    fun `when schedules exist for today, nextActivity is ClassActivity`() = runTest {
        val today = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)
        val todaySchedule = Schedule(
            id = "s1",
            subjectName = "Matemáticas",
            dayOfWeek = today,
            startTime = "08:00",
            endTime = "10:00",
            classroom = "Aula 101"
        )
        coEvery { scheduleRepository.getSchedules("user_123") } returns flowOf(listOf(todaySchedule))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.nextActivity is NextActivity.ClassActivity)
        val classActivity = state.nextActivity as NextActivity.ClassActivity
        assertEquals("Matemáticas", classActivity.schedule.subjectName)
        assertEquals("#3B9797", classActivity.colorHex)
    }

    @Test
    fun `when no schedules today but upcoming evaluation exists, nextActivity is EvaluationActivity`() = runTest {
        val futureDate = Date(System.currentTimeMillis() + 86400000)
        val eval = Evaluation(
            id = "e1",
            subjectName = "Física",
            title = "Parcial 1",
            date = futureDate,
            isPresented = false,
            weight = 30.0
        )
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(listOf(eval))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.nextActivity is NextActivity.EvaluationActivity)
        val evalActivity = state.nextActivity as NextActivity.EvaluationActivity
        assertEquals("Física", evalActivity.evaluation.subjectName)
        assertEquals("#BF092F", evalActivity.colorHex)
    }

    @Test
    fun `when no schedules and no upcoming evaluations, nextActivity is FreeDay`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.nextActivity is NextActivity.FreeDay)
    }

    @Test
    fun `when evaluations have grades, computes globalAverage correctly`() = runTest {
        val eval1 = Evaluation(id = "e1", subjectName = "Matemáticas", grade = 5.0, weight = 50.0, isPresented = true)
        val eval2 = Evaluation(id = "e2", subjectName = "Física", grade = 4.0, weight = 50.0, isPresented = true)
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(listOf(eval1, eval2))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(4.5, state.globalAverage)
    }

    @Test
    fun `when SelectTag event dispatched, filters notes matching tag`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(NotesEvent.SelectTag("Examen"))
        advanceUntilIdle()

        val filteredNotes = viewModel.uiState.value.notes.values.flatten()
        assertEquals(1, filteredNotes.size)
        assertEquals("Álgebra Lineal", filteredNotes.first().title)
    }

    @Test
    fun `when searching by tag or keyword, returns matching notes`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(NotesEvent.Search("aceleración"))
        advanceUntilIdle()

        val filteredNotes = viewModel.uiState.value.notes.values.flatten()
        assertEquals(1, filteredNotes.size)
        assertEquals("Cinemática", filteredNotes.first().title)
    }
}
