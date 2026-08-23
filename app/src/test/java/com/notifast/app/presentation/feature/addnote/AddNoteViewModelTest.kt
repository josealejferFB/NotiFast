package com.notifast.app.presentation.feature.addnote

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddNoteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private val sampleSubjects = listOf(
        SubjectData(name = "Matemáticas", colorHex = "#3B9797")
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(sampleSubjects)
    }

    @Test
    fun `when AddTag and RemoveTag events received, updates tags list`() = runTest {
        val viewModel = AddNoteViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(AddNoteEvent.AddTag("Examen"))
        viewModel.onEvent(AddNoteEvent.AddTag("Fórmula"))
        assertEquals(listOf("Examen", "Fórmula"), viewModel.uiState.value.tags)

        viewModel.onEvent(AddNoteEvent.RemoveTag("Examen"))
        assertEquals(listOf("Fórmula"), viewModel.uiState.value.tags)
    }

    @Test
    fun `when ToggleLivePreview event received, updates isLivePreview`() = runTest {
        val viewModel = AddNoteViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLivePreview)
        viewModel.onEvent(AddNoteEvent.ToggleLivePreview(true))
        assertTrue(viewModel.uiState.value.isLivePreview)
    }

    @Test
    fun `when saving note with tags, calls repository addNote with tags`() = runTest {
        coEvery { noteRepository.addNote("user_123", "Matemáticas", "Derivadas", "Contenido", any(), listOf("Examen")) } returns Result.success(Unit)

        val viewModel = AddNoteViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(AddNoteEvent.SubjectChanged("Matemáticas"))
        viewModel.onEvent(AddNoteEvent.TitleChanged("Derivadas"))
        viewModel.onEvent(AddNoteEvent.ContentChanged("Contenido"))
        viewModel.onEvent(AddNoteEvent.AddTag("Examen"))
        viewModel.onEvent(AddNoteEvent.SaveClicked)
        advanceUntilIdle()

        coVerify { noteRepository.addNote("user_123", "Matemáticas", "Derivadas", "Contenido", any(), listOf("Examen")) }
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `when ToggleTag is called, adds tag if absent and removes if present`() = runTest {
        val viewModel = AddNoteViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        // Toggle on
        viewModel.onEvent(AddNoteEvent.ToggleTag("resumen"))
        assertTrue(viewModel.uiState.value.tags.contains("resumen"))

        // Toggle on second tag
        viewModel.onEvent(AddNoteEvent.ToggleTag("examen"))
        assertEquals(listOf("resumen", "examen"), viewModel.uiState.value.tags)

        // Toggle off first tag
        viewModel.onEvent(AddNoteEvent.ToggleTag("resumen"))
        assertEquals(listOf("examen"), viewModel.uiState.value.tags)
    }

    @Test
    fun `when ResetState is called, clears inputs but retains loaded subjects`() = runTest {
        val viewModel = AddNoteViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(AddNoteEvent.SubjectChanged("Matemáticas"))
        viewModel.onEvent(AddNoteEvent.TitleChanged("Derivadas"))
        viewModel.onEvent(AddNoteEvent.AddTag("resumen"))

        viewModel.onEvent(AddNoteEvent.ResetState)

        assertEquals("", viewModel.uiState.value.title)
        assertEquals("", viewModel.uiState.value.subject)
        assertTrue(viewModel.uiState.value.tags.isEmpty())
        assertEquals(sampleSubjects, viewModel.uiState.value.subjects)
    }

    @Test
    fun `when ClearError is called, resets error state to null`() = runTest {
        val viewModel = AddNoteViewModel(noteRepository, authRepository)
        advanceUntilIdle()

        // Trigger validation error
        viewModel.onEvent(AddNoteEvent.SaveClicked)
        assertTrue(viewModel.uiState.value.error != null)

        viewModel.onEvent(AddNoteEvent.ClearError)
        assertEquals(null, viewModel.uiState.value.error)
    }
}

