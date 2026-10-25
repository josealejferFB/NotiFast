package com.notifast.app.presentation.feature.addschedule

import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.Teacher
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.TeacherRepository
import com.notifast.app.domain.usecase.ValidateScheduleOverlapUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class AddScheduleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val scheduleRepository: ScheduleRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val teacherRepository: TeacherRepository = mockk(relaxed = true)
    private val validateScheduleOverlapUseCase = ValidateScheduleOverlapUseCase()
    private val scheduleClassAlertUseCase: com.notifast.app.domain.usecase.ScheduleClassAlertUseCase = mockk(relaxed = true)

    private val sampleSubjects = listOf(
        SubjectData(name = "Matemáticas", colorHex = "#3B9797")
    )
    private val sampleSchedules = listOf(
        Schedule(id = "1", subjectName = "Física", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
    )

    private fun createViewModel() = AddScheduleViewModel(
        scheduleRepository,
        authRepository,
        noteRepository,
        teacherRepository,
        validateScheduleOverlapUseCase,
        scheduleClassAlertUseCase
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(sampleSubjects)
        coEvery { scheduleRepository.getSchedules("user_123") } returns flowOf(sampleSchedules)
        coEvery { teacherRepository.getTeachersBySubject("user_123", any()) } returns flowOf(emptyList())
        coEvery { teacherRepository.getTeachers("user_123") } returns flowOf(emptyList())
    }

    @Test
    fun `when times set and overlap with existing, conflictingSchedules is populated`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(1))
        viewModel.onEvent(AddScheduleEvent.UpdateStartTime("09:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateEndTime("11:00"))

        assertEquals(1, viewModel.uiState.value.conflictingSchedules.size)
        assertEquals("1", viewModel.uiState.value.conflictingSchedules.first().id)
    }

    @Test
    fun `when times set and no overlap, conflictingSchedules is empty`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(1))
        viewModel.onEvent(AddScheduleEvent.UpdateStartTime("10:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateEndTime("12:00"))

        assertTrue(viewModel.uiState.value.conflictingSchedules.isEmpty())
    }

    @Test
    fun `when saving with valid data, calls repository addSchedule and sets isSaved`() = runTest {
        coEvery { scheduleRepository.addSchedule(any(), any()) } returns Result.success("sch-1")

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateSubjectName("Matemáticas"))
        viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(1))
        viewModel.onEvent(AddScheduleEvent.UpdateStartTime("10:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateEndTime("12:00"))
        viewModel.onEvent(AddScheduleEvent.SaveSchedule)
        advanceUntilIdle()

        coVerify { scheduleRepository.addSchedule("user_123", any()) }
        assertTrue(viewModel.uiState.value.isSaved)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `when endTime is less than or equal to startTime, autocorrects endTime`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateStartTime("10:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateEndTime("09:00"))

        assertEquals("10:00", viewModel.uiState.value.startTime)
        assertEquals("12:00", viewModel.uiState.value.endTime)
    }

    @Test
    fun `when updateTimeRange is called with invalid range, autocorrects endTime`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateTimeRange("14:00", "14:00"))

        assertEquals("14:00", viewModel.uiState.value.startTime)
        assertEquals("16:00", viewModel.uiState.value.endTime)
    }

    @Test
    fun `when dayOfWeek is updated, dayOfWeek in state reflects change`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(4)) // Viernes
        assertEquals(4, viewModel.uiState.value.dayOfWeek)
    }

    @Test
    fun `when ResetState is dispatched, resets form fields but preserves loaded subjects and schedules`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateSubjectName("Matemáticas"))
        viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(3))
        viewModel.onEvent(AddScheduleEvent.UpdateStartTime("08:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateEndTime("10:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateClassroom("Aula 101"))
        viewModel.onEvent(AddScheduleEvent.UpdateTeacher("Prof. Gómez"))

        viewModel.onEvent(AddScheduleEvent.ResetState)

        val state = viewModel.uiState.value
        assertEquals("", state.subjectName)
        assertEquals(1, state.dayOfWeek)
        assertEquals("", state.startTime)
        assertEquals("", state.endTime)
        assertEquals("", state.classroom)
        assertEquals("", state.teacher)
        assertFalse(state.isSaved)
        assertEquals(1, state.subjects.size)
        assertEquals(1, state.existingSchedules.size)
    }

    @Test
    fun `when RefreshSubjectsAndSelect is dispatched, reloads subjects and updates subjectName preserving other fields`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateStartTime("08:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateEndTime("10:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateClassroom("Aula 202"))

        val updatedSubjects = listOf(
            SubjectData(name = "Matemáticas", colorHex = "#3B9797"),
            SubjectData(name = "Química", colorHex = "#FF5722")
        )
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(updatedSubjects)

        viewModel.onEvent(AddScheduleEvent.RefreshSubjectsAndSelect("Química"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Química", state.subjectName)
        assertEquals(2, state.subjects.size)
        assertEquals("08:00", state.startTime)
        assertEquals("10:00", state.endTime)
        assertEquals("Aula 202", state.classroom)
    }

    @Test
    fun `when subject selected has no teachers, hasConfiguredTeacher is false`() = runTest {
        coEvery { teacherRepository.getTeachersBySubject("user_123", "Matemáticas") } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateSubjectName("Matemáticas"))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasConfiguredTeacher)
        assertTrue(viewModel.uiState.value.availableTeachers.isEmpty())
    }

    @Test
    fun `when subject selected has teachers, hasConfiguredTeacher is true and availableTeachers is populated`() = runTest {
        val teacher = Teacher(id = "t1", firstName = "Dr.", lastName = "Euler", subjectNames = listOf("Matemáticas"))
        coEvery { teacherRepository.getTeachersBySubject("user_123", "Matemáticas") } returns flowOf(listOf(teacher))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateSubjectName("Matemáticas"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasConfiguredTeacher)
        assertEquals(1, viewModel.uiState.value.availableTeachers.size)
        assertEquals("Dr. Euler", viewModel.uiState.value.availableTeachers.first().fullName)
    }

    @Test
    fun `when saving schedule with new teacher, invokes teacherRepository saveTeacher`() = runTest {
        coEvery { scheduleRepository.addSchedule(any(), any()) } returns Result.success("sch-1")
        coEvery { teacherRepository.getTeachers("user_123") } returns flowOf(emptyList())
        coEvery { teacherRepository.saveTeacher(any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateSubjectName("Matemáticas"))
        viewModel.onEvent(AddScheduleEvent.UpdateTeacher("Prof. Gauss"))
        viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(1))
        viewModel.onEvent(AddScheduleEvent.UpdateStartTime("10:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateEndTime("12:00"))
        viewModel.onEvent(AddScheduleEvent.SaveSchedule)
        advanceUntilIdle()

        coVerify {
            teacherRepository.saveTeacher(
                match { it.fullName == "Prof. Gauss" && it.subjectNames.contains("Matemáticas") }
            )
        }
    }

    @Test
    fun `when OpenAddTeacherSheet and CloseAddTeacherSheet are dispatched, toggles showAddTeacherSheet`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAddTeacherSheet)

        viewModel.onEvent(AddScheduleEvent.OpenAddTeacherSheet)
        assertTrue(viewModel.uiState.value.showAddTeacherSheet)

        viewModel.onEvent(AddScheduleEvent.CloseAddTeacherSheet)
        assertFalse(viewModel.uiState.value.showAddTeacherSheet)
    }

    @Test
    fun `when SaveTeacher is dispatched, saves to teacherRepository, updates teacher to fullName, and closes sheet`() = runTest {
        coEvery { teacherRepository.saveTeacher(any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.OpenAddTeacherSheet)
        assertTrue(viewModel.uiState.value.showAddTeacherSheet)

        val newTeacher = Teacher(
            firstName = "Marie",
            lastName = "Curie",
            email = "mcurie@univ.edu",
            subjectNames = listOf("Química")
        )

        viewModel.onEvent(AddScheduleEvent.SaveTeacher(newTeacher))
        advanceUntilIdle()

        coVerify {
            teacherRepository.saveTeacher(
                match { it.firstName == "Marie" && it.lastName == "Curie" && it.userId == "user_123" }
            )
        }
        assertEquals("Marie Curie", viewModel.uiState.value.teacher)
        assertFalse(viewModel.uiState.value.showAddTeacherSheet)
    }

    @Test
    fun `when allTeachers flow emits, uiState allTeachers is updated`() = runTest {
        val t1 = Teacher(id = "1", firstName = "Albert", lastName = "Einstein")
        val t2 = Teacher(id = "2", firstName = "Isaac", lastName = "Newton")
        coEvery { teacherRepository.getTeachers("user_123") } returns flowOf(listOf(t1, t2))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.allTeachers.size)
        assertEquals("Albert Einstein", viewModel.uiState.value.allTeachers[0].fullName)
        assertEquals("Isaac Newton", viewModel.uiState.value.allTeachers[1].fullName)
    }

    @Test
    fun `double click on SaveSchedule executes repository addSchedule exactly once`() = runTest {
        coEvery { scheduleRepository.addSchedule(any(), any()) } returns Result.success("sch-1")
        coEvery { teacherRepository.getTeachers("user_123") } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(AddScheduleEvent.UpdateSubjectName("Matemáticas"))
        viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(1))
        viewModel.onEvent(AddScheduleEvent.UpdateStartTime("10:00"))
        viewModel.onEvent(AddScheduleEvent.UpdateEndTime("12:00"))

        viewModel.onEvent(AddScheduleEvent.SaveSchedule)
        viewModel.onEvent(AddScheduleEvent.SaveSchedule)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            scheduleRepository.addSchedule(eq("user_123"), any())
        }
        assertTrue(viewModel.uiState.value.isSaved)
    }
}
