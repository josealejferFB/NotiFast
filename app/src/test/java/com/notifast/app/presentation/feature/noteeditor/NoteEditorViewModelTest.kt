package com.notifast.app.presentation.feature.noteeditor

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.ImageRepository
import com.notifast.app.domain.repository.NoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val imageRepository: ImageRepository = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { authRepository.getCurrentUserId() } returns "u1"
        coEvery { noteRepository.getSubjects("u1") } returns flowOf(
            listOf(SubjectData(id = "s1", name = "Física", icon = "⚡", colorHex = "#000"))
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(noteId: String? = null, subject: String = ""): NoteEditorViewModel {
        val handle = SavedStateHandle().apply {
            noteId?.let { set("noteId", it) }
            if (subject.isNotBlank()) set("subject", subject)
        }
        return NoteEditorViewModel(handle, noteRepository, authRepository, imageRepository, context = context)
    }

    @Test
    fun `new note starts with empty content and zero words`() = runTest(testDispatcher) {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = vm.uiState.value

        assertNull(state.noteId)
        assertEquals("", state.title)
        assertEquals("", state.content)
        assertEquals(0, state.wordCount)
        assertEquals(0, state.readingTimeMinutes)
        assertFalse(state.isPreviewMode)
    }

    @Test
    fun `new note with initial subject sets subject`() = runTest(testDispatcher) {
        val vm = createViewModel(subject = "Química")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Química", vm.uiState.value.subject)
    }

    @Test
    fun `title change updates state`() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.onEvent(NoteEditorEvent.TitleChanged("Termodinámica"))

        assertEquals("Termodinámica", vm.uiState.value.title)
    }

    @Test
    fun `content change updates content and calculates word count and reading time`() = runTest(testDispatcher) {
        val vm = createViewModel()
        val sampleText = "Primera ley de la termodinámica conservación de energía"
        vm.onEvent(NoteEditorEvent.ContentChanged(sampleText))

        assertEquals(sampleText, vm.uiState.value.content)
        assertEquals(8, vm.uiState.value.wordCount)
        assertEquals(1, vm.uiState.value.readingTimeMinutes)
    }

    @Test
    fun `insert formatting appends markdown syntax smoothly`() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.onEvent(NoteEditorEvent.InsertFormatting("# "))

        assertEquals("# ", vm.uiState.value.content)

        vm.onEvent(NoteEditorEvent.InsertFormatting("**", "**"))
        assertEquals("# ****", vm.uiState.value.content)
    }

    @Test
    fun `toggle preview switches mode`() = runTest(testDispatcher) {
        val vm = createViewModel()
        assertFalse(vm.uiState.value.isPreviewMode)

        vm.onEvent(NoteEditorEvent.TogglePreview(true))
        assertTrue(vm.uiState.value.isPreviewMode)

        vm.onEvent(NoteEditorEvent.TogglePreview(false))
        assertFalse(vm.uiState.value.isPreviewMode)
    }

    @Test
    fun `task check toggled modifies markdown checkbox syntax reactively`() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.onEvent(NoteEditorEvent.ContentChanged("- [ ] Estudiar para el parcial\n- [ ] Hacer guía"))

        vm.onEvent(NoteEditorEvent.TaskCheckToggled(taskIndex = 0, isChecked = true))

        assertTrue(vm.uiState.value.content.contains("- [x] Estudiar para el parcial"))
        assertTrue(vm.uiState.value.content.contains("- [ ] Hacer guía"))
    }

    @Test
    fun `tag operations add toggle and remove tags cleanly`() = runTest(testDispatcher) {
        val vm = createViewModel()

        vm.onEvent(NoteEditorEvent.TagToggled("resumen"))
        assertTrue(vm.uiState.value.tags.contains("resumen"))

        vm.onEvent(NoteEditorEvent.CustomTagAdded("examen"))
        assertTrue(vm.uiState.value.tags.contains("examen"))

        vm.onEvent(NoteEditorEvent.CustomTagRemoved("resumen"))
        assertFalse(vm.uiState.value.tags.contains("resumen"))
        assertTrue(vm.uiState.value.tags.contains("examen"))
    }

    @Test
    fun `save requires non-blank subject and non-blank title`() = runTest(testDispatcher) {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(NoteEditorEvent.SaveClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(vm.uiState.value.error)
        assertFalse(vm.uiState.value.isSaved)

        vm.onEvent(NoteEditorEvent.SubjectChanged("Física"))
        vm.onEvent(NoteEditorEvent.SaveClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(vm.uiState.value.error)
        assertFalse(vm.uiState.value.isSaved)
    }

    @Test
    fun `successful save calls noteRepository addNote for new note`() = runTest(testDispatcher) {
        coEvery {
            noteRepository.addNote(any(), any(), any(), any(), any(), any())
        } returns Result.success("new_note_123")

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(NoteEditorEvent.SubjectChanged("Física"))
        vm.onEvent(NoteEditorEvent.TitleChanged("Leyes de Newton"))
        vm.onEvent(NoteEditorEvent.ContentChanged("F = m * a"))
        vm.onEvent(NoteEditorEvent.SaveClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.isSaved)
        assertEquals("new_note_123", vm.uiState.value.noteId)
        coVerify(exactly = 1) {
            noteRepository.addNote("u1", "Física", "Leyes de Newton", "F = m * a", null, emptyList())
        }
    }

    @Test
    fun `loading existing note populates title content and tags`() = runTest(testDispatcher) {
        val existing = Note(
            id = "note_99",
            subject = "Física",
            title = "Cinemática",
            content = "# Movimiento Rectilíneo",
            tags = listOf("fórmulas")
        )
        coEvery { noteRepository.getNotes("u1") } returns flowOf(listOf(existing))

        val vm = createViewModel(noteId = "note_99")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("note_99", state.noteId)
        assertEquals("Cinemática", state.title)
        assertEquals("Física", state.subject)
        assertEquals("# Movimiento Rectilíneo", state.content)
        assertEquals(listOf("fórmulas"), state.tags)
    }

    @Test
    fun `init event cleanses state and resets isSaved flag for new note`() = runTest(testDispatcher) {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Simulate dirty state with title and content
        vm.onEvent(NoteEditorEvent.TitleChanged("Viejo"))
        vm.onEvent(NoteEditorEvent.ContentChanged("Contenido viejo"))
        assertEquals("Viejo", vm.uiState.value.title)

        // Dispatch Init for new note
        vm.onEvent(NoteEditorEvent.Init(noteId = null, subject = "Biología"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.noteId)
        assertEquals("Biología", state.subject)
        assertEquals("", state.title)
        assertEquals("", state.content)
        assertFalse(state.isSaved)
        assertNull(state.error)
    }

    @Test
    fun `init event loads existing note and resets isSaved`() = runTest(testDispatcher) {
        val existing = Note(
            id = "note_101",
            subject = "Historia",
            title = "Revolución Industrial",
            content = "Máquina de vapor",
            tags = listOf("siglo_xix")
        )
        coEvery { noteRepository.getNotes("u1") } returns flowOf(listOf(existing))

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(NoteEditorEvent.Init(noteId = "note_101", subject = "Historia"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("note_101", state.noteId)
        assertEquals("Historia", state.subject)
        assertEquals("Revolución Industrial", state.title)
        assertEquals("Máquina de vapor", state.content)
        assertEquals(listOf("siglo_xix"), state.tags)
        assertFalse(state.isSaved)
    }
}

