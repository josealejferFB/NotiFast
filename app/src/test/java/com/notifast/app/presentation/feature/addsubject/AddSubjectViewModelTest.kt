package com.notifast.app.presentation.feature.addsubject

import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddSubjectViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private val existingSubjects = listOf(
        SubjectData(name = "Cálculo I", icon = "Calculate", colorHex = "#3B9797")
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_test_456"
        coEvery { noteRepository.getSubjects("user_test_456") } returns flowOf(existingSubjects)
    }

    @Test
    fun `when NameChanged and SuggestionSelected events received, updates subject name`() = runTest {
        val viewModel = AddSubjectViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(AddSubjectEvent.NameChanged("Física Mecánica"))
        assertEquals("Física Mecánica", viewModel.uiState.value.subjectName)

        viewModel.onEvent(AddSubjectEvent.SuggestionSelected("Programación"))
        assertEquals("Programación", viewModel.uiState.value.subjectName)
    }

    @Test
    fun `when ColorSelected and IconSelected events received, updates selection state`() = runTest {
        val viewModel = AddSubjectViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(AddSubjectEvent.ColorSelected("#4338CA"))
        assertEquals("#4338CA", viewModel.uiState.value.selectedColor)

        viewModel.onEvent(AddSubjectEvent.IconSelected("Computer"))
        assertEquals("Computer", viewModel.uiState.value.selectedIcon)
    }

    @Test
    fun `when saving with blank name, sets error message without calling repository`() = runTest {
        val viewModel = AddSubjectViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(AddSubjectEvent.NameChanged("   "))
        viewModel.onEvent(AddSubjectEvent.SaveClicked)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaved)
        coVerify(exactly = 0) { noteRepository.addSubject(any(), any(), any(), any()) }
    }

    @Test
    fun `when saving subject name exceeding 50 chars, sets too long error`() = runTest {
        val viewModel = AddSubjectViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(AddSubjectEvent.NameChanged("A".repeat(51)))
        viewModel.onEvent(AddSubjectEvent.SaveClicked)
        advanceUntilIdle()

        assertEquals("El nombre no puede superar los 50 caracteres", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaved)
        coVerify(exactly = 0) { noteRepository.addSubject(any(), any(), any(), any()) }
    }

    @Test
    fun `when saving duplicate subject name case insensitively, sets duplicate error`() = runTest {
        val viewModel = AddSubjectViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(AddSubjectEvent.NameChanged("cálculo i"))
        viewModel.onEvent(AddSubjectEvent.SaveClicked)
        advanceUntilIdle()

        assertEquals("Ya tienes una materia registrada con este nombre", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaved)
        coVerify(exactly = 0) { noteRepository.addSubject(any(), any(), any(), any()) }
    }

    @Test
    fun `when saving valid subject, calls repository and marks isSaved`() = runTest {
        coEvery {
            noteRepository.addSubject(
                userId = "user_test_456",
                subjectName = "Química General",
                icon = "Science",
                colorHex = "#047857",
                section = ""
            )
        } returns Result.success("sub-1")

        val viewModel = AddSubjectViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(AddSubjectEvent.NameChanged("Química General"))
        viewModel.onEvent(AddSubjectEvent.IconSelected("Science"))
        viewModel.onEvent(AddSubjectEvent.ColorSelected("#047857"))
        viewModel.onEvent(AddSubjectEvent.SaveClicked)
        advanceUntilIdle()

        coVerify {
            noteRepository.addSubject(
                userId = "user_test_456",
                subjectName = "Química General",
                icon = "Science",
                colorHex = "#047857",
                section = ""
            )
        }
        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals("Química General", viewModel.uiState.value.createdSubjectName)
    }

    @Test
    fun `when SectionChanged event received and saved, updates section and passes to repository`() = runTest {
        coEvery {
            noteRepository.addSubject(
                userId = "user_test_456",
                subjectName = "Física II",
                icon = "Calculate",
                colorHex = "#0369A1",
                section = "19"
            )
        } returns Result.success("sub-2")

        val viewModel = AddSubjectViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(AddSubjectEvent.NameChanged("Física II"))
        viewModel.onEvent(AddSubjectEvent.SectionChanged("19"))
        viewModel.onEvent(AddSubjectEvent.IconSelected("Calculate"))
        viewModel.onEvent(AddSubjectEvent.ColorSelected("#0369A1"))

        assertEquals("19", viewModel.uiState.value.section)

        viewModel.onEvent(AddSubjectEvent.SaveClicked)
        advanceUntilIdle()

        coVerify {
            noteRepository.addSubject(
                userId = "user_test_456",
                subjectName = "Física II",
                icon = "Calculate",
                colorHex = "#0369A1",
                section = "19"
            )
        }
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `when ResetState is called, resets state to default`() = runTest {
        val viewModel = AddSubjectViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(AddSubjectEvent.NameChanged("Historia"))
        viewModel.onEvent(AddSubjectEvent.SectionChanged("2"))
        viewModel.onEvent(AddSubjectEvent.ResetState)

        assertEquals("", viewModel.uiState.value.subjectName)
        assertEquals("", viewModel.uiState.value.section)
        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `double click on save executes repository addSubject exactly once`() = runTest {
        coEvery {
            noteRepository.addSubject(
                userId = "user_test_456",
                subjectName = "Álgebra Lineal",
                icon = "School",
                colorHex = "#3B9797",
                section = ""
            )
        } returns Result.success("sub-2")

        val viewModel = AddSubjectViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(AddSubjectEvent.NameChanged("Álgebra Lineal"))

        // First click
        viewModel.onEvent(AddSubjectEvent.SaveClicked)
        // Immediate second click
        viewModel.onEvent(AddSubjectEvent.SaveClicked)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            noteRepository.addSubject(
                userId = "user_test_456",
                subjectName = "Álgebra Lineal",
                icon = any(),
                colorHex = any(),
                section = any()
            )
        }
        assertTrue(viewModel.uiState.value.isSaved)
    }
}
