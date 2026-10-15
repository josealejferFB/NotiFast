package com.notifast.app.presentation.feature.notes

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.User
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
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
    private val userPrefsRepository: UserPrefsRepository = mockk(relaxed = true)
    private val calculateWeightedAverageUseCase = CalculateWeightedAverageUseCase()
    private val importSubjectPackageUseCase: com.notifast.app.domain.usecase.ImportSubjectPackageUseCase = mockk(relaxed = true)
    private val teacherRepository: com.notifast.app.domain.repository.TeacherRepository = mockk(relaxed = true)
    private val observeLocalSyncSummaryUseCase: com.notifast.app.domain.usecase.cloud.ObserveLocalSyncSummaryUseCase = mockk(relaxed = true)

    private val sampleSubjects = listOf(
        SubjectData(name = "Matemáticas", colorHex = "#3B9797"),
        SubjectData(name = "Física", colorHex = "#BF092F")
    )
    private val sampleNotes = listOf(
        Note(id = "1", subject = "Matemáticas", title = "Álgebra Lineal", content = "Matrices y determinantes", tags = listOf("Examen", "Fórmula")),
        Note(id = "2", subject = "Física", title = "Cinemática", content = "Velocidad y aceleración", tags = listOf("Resumen"))
    )
    private val sampleTeachers = listOf(
        com.notifast.app.data.model.Teacher(id = "t1", firstName = "Richard", lastName = "Feynman", subjectNames = listOf("Física"))
    )

    private fun createViewModel(): NotesViewModel = NotesViewModel(
        noteRepository,
        authRepository,
        evaluationRepository,
        scheduleRepository,
        userPrefsRepository,
        calculateWeightedAverageUseCase,
        importSubjectPackageUseCase,
        teacherRepository,
        observeLocalSyncSummaryUseCase
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { authRepository.getUserProfile("user_123") } returns Result.success(User(firstName = "Test", lastName = "User", email = "test@example.com"))
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(sampleSubjects)
        coEvery { noteRepository.getNotes("user_123") } returns flowOf(sampleNotes)
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(emptyList())
        coEvery { scheduleRepository.getSchedules("user_123") } returns flowOf(emptyList())
        coEvery { teacherRepository.getTeachers("user_123") } returns flowOf(sampleTeachers)
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-10")
        // 0.0 is invalid for any active scale → falls back to scale.minPassingGrade
        coEvery { userPrefsRepository.minPassingGradeFlow } returns flowOf(0f)
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
        assertEquals(4.5, state.globalAverage!!, 0.001)
        assertEquals(GradeScale.SCALE_0_10, state.gradeScale)
        assertEquals(6.0, state.minPassingGrade, 0.001)
    }

    @Test
    fun `globalAverage uses user grade scale 0-20`() = runTest {
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-20")
        coEvery { userPrefsRepository.minPassingGradeFlow } returns flowOf(0f)
        val eval = Evaluation(
            id = "e1",
            subjectName = "Matemáticas",
            grade = 14.0,
            weight = 100.0,
            isPresented = true,
            gradeScaleId = "0-20"
        )
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(listOf(eval))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(14.0, state.globalAverage!!, 0.001)
        assertEquals(GradeScale.SCALE_0_20, state.gradeScale)
        assertEquals(11.0, state.minPassingGrade, 0.001)
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

    @Test
    fun `when ToggleTaskCheck event dispatched, updates note via repository and optimistically updates state`() = runTest {
        val noteWithTasks = Note(
            id = "task_note_1",
            subject = "Matemáticas",
            title = "Tareas",
            content = "- [ ] Tarea 1\n- [ ] Tarea 2"
        )
        coEvery { noteRepository.getNotes("user_123") } returns flowOf(listOf(noteWithTasks))
        coEvery { noteRepository.updateNote(any(), any(), any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(NotesEvent.ToggleTaskCheck(noteWithTasks, taskIndex = 0, isChecked = true))
        val updatedFirst = viewModel.uiState.value.rawNotes.find { it.id == "task_note_1" }
        assertTrue(updatedFirst?.content?.contains("- [x] Tarea 1") == true)

        advanceUntilIdle()

        io.mockk.coVerify {
            noteRepository.updateNote(
                "user_123",
                match { it.content.contains("- [x] Tarea 1") },
                null
            )
        }
    }

    @Test
    fun `when ToggleTaskCheck dispatched multiple times with original note, both checks are preserved`() = runTest {
        val noteWithTasks = Note(
            id = "task_note_1",
            subject = "Matemáticas",
            title = "Tareas",
            content = "- [ ] Tarea 1\n- [ ] Tarea 2"
        )
        coEvery { noteRepository.getNotes("user_123") } returns flowOf(listOf(noteWithTasks))
        coEvery { noteRepository.updateNote(any(), any(), any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Toggle task 0
        viewModel.onEvent(NotesEvent.ToggleTaskCheck(noteWithTasks, taskIndex = 0, isChecked = true))
        // Toggle task 1 using the same original note reference (simulating rapid clicks)
        viewModel.onEvent(NotesEvent.ToggleTaskCheck(noteWithTasks, taskIndex = 1, isChecked = true))

        val current = viewModel.uiState.value.rawNotes.find { it.id == "task_note_1" }
        assertTrue(current?.content?.contains("- [x] Tarea 1") == true)
        assertTrue(current?.content?.contains("- [x] Tarea 2") == true)
    }

    @Test
    fun `when UpdateSubject event dispatched, invokes repository updateSubject`() = runTest {
        coEvery { noteRepository.updateSubject(any(), any(), any()) } returns Result.success(Unit)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(NotesEvent.UpdateSubject("Matemáticas", "Álgebra", "#112233", "Calculate"))
        advanceUntilIdle()

        io.mockk.coVerify {
            noteRepository.updateSubject(
                userId = "user_123",
                oldName = "Matemáticas",
                newSubject = match { it.name == "Álgebra" && it.colorHex == "#112233" && it.icon == "Calculate" }
            )
        }
    }

    @Test
    fun `when initialized, loads teachers and populates filteredTeachers`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.teachers.size)
        assertEquals(1, state.filteredTeachers.size)
        assertEquals("Richard Feynman", state.filteredTeachers.first().fullName)
    }

    @Test
    fun `when SearchTeachers query provided, filters teachers list`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(NotesEvent.SearchTeachers("Einstein"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.filteredTeachers.isEmpty())

        viewModel.onEvent(NotesEvent.SearchTeachers("Feynman"))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.filteredTeachers.size)
    }

    @Test
    fun `when SaveTeacher dispatched, invokes teacherRepository saveTeacher`() = runTest {
        coEvery { teacherRepository.saveTeacher(any()) } returns Result.success(Unit)
        val viewModel = createViewModel()
        advanceUntilIdle()

        val teacherToSave = com.notifast.app.data.model.Teacher(firstName = "Marie", lastName = "Curie")
        viewModel.onEvent(NotesEvent.SaveTeacher(teacherToSave))
        advanceUntilIdle()

        io.mockk.coVerify { teacherRepository.saveTeacher(match { it.fullName == "Marie Curie" }) }
    }

    @Test
    fun `when DeleteTeacher dispatched, invokes teacherRepository deleteTeacher`() = runTest {
        coEvery { teacherRepository.deleteTeacher(any(), any()) } returns Result.success(Unit)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(NotesEvent.DeleteTeacher("t1"))
        advanceUntilIdle()

        io.mockk.coVerify { teacherRepository.deleteTeacher("user_123", "t1") }
    }

    @Test
    fun `when CloudClick and DismissCloudHub dispatched, updates showCloudHub`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        org.junit.Assert.assertFalse(viewModel.uiState.value.showCloudHub)

        viewModel.onEvent(NotesEvent.CloudClick)
        assertTrue(viewModel.uiState.value.showCloudHub)

        viewModel.onEvent(NotesEvent.DismissCloudHub)
        org.junit.Assert.assertFalse(viewModel.uiState.value.showCloudHub)
    }
}
