package com.notifast.app.presentation.feature.editnote

import androidx.lifecycle.SavedStateHandle
import com.notifast.app.data.model.Note
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
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditNoteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private val sampleSubjects = listOf(
        SubjectData(name = "Matemáticas", colorHex = "#3B9797")
    )
    private val sampleNotes = listOf(
        Note(id = "1", subject = "Matemáticas", title = "Álgebra", content = "Matrices", tags = listOf("Importante"))
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(sampleSubjects)
        coEvery { noteRepository.getNotes("user_123") } returns flowOf(sampleNotes)
        coEvery { noteRepository.updateNote(any(), any(), null) } returns Result.success(Unit)
    }

    @Test
    fun `when initialized, loads note data and tags`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("noteId" to "1"))
        val viewModel = EditNoteViewModel(savedStateHandle, noteRepository, authRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Álgebra", state.title)
        assertEquals("Matrices", state.content)
        assertEquals(listOf("Importante"), state.tags)
    }

    @Test
    fun `when AddTag and RemoveTag events received, updates tags and schedules save`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("noteId" to "1"))
        val viewModel = EditNoteViewModel(savedStateHandle, noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(EditNoteEvent.AddTag("Fórmula"))
        assertEquals(listOf("Importante", "Fórmula"), viewModel.uiState.value.tags)

        viewModel.onEvent(EditNoteEvent.RemoveTag("Importante"))
        assertEquals(listOf("Fórmula"), viewModel.uiState.value.tags)
    }

    @Test
    fun `when SaveClicked event received, calls updateNote and sets isSaved`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("noteId" to "1"))
        val viewModel = EditNoteViewModel(savedStateHandle, noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(EditNoteEvent.TitleChanged("Álgebra Lineal"))
        viewModel.onEvent(EditNoteEvent.SaveClicked)
        advanceUntilIdle()

        coVerify { noteRepository.updateNote("user_123", match { it.title == "Álgebra Lineal" }, null) }
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `when saveNote succeeds with imageUri, resets imageUri to null in uiState`() = runTest {
        val mockUri = mockk<Uri>()
        coEvery { noteRepository.updateNote(any(), any(), mockUri) } returns Result.success(Unit)

        val savedStateHandle = SavedStateHandle(mapOf("noteId" to "1"))
        val viewModel = EditNoteViewModel(savedStateHandle, noteRepository, authRepository)
        advanceUntilIdle()

        viewModel.onEvent(EditNoteEvent.ImageSelected(mockUri))
        assertEquals(mockUri, viewModel.uiState.value.imageUri)

        viewModel.onEvent(EditNoteEvent.SaveClicked)
        advanceUntilIdle()

        coVerify { noteRepository.updateNote("user_123", any(), mockUri) }
        assertNull(viewModel.uiState.value.imageUri)
        assertTrue(viewModel.uiState.value.isSaved)
    }
}
