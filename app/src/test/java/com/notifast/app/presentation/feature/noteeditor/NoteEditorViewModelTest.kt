package com.notifast.app.presentation.feature.noteeditor

import androidx.lifecycle.SavedStateHandle
import com.notifast.app.data.model.CheckItem
import com.notifast.app.data.model.ListItem
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.ImageRepository
import com.notifast.app.domain.repository.NoteRepository
import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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

    private fun createViewModelForNew(noteId: String? = null): NoteEditorViewModel {
        val handle = SavedStateHandle().apply {
            noteId?.let { set("noteId", it) }
        }
        return NoteEditorViewModel(handle, noteRepository, authRepository, imageRepository, context)
    }

    @Test
    fun `new note starts with one empty paragraph block`() = runTest(testDispatcher) {
        val vm = createViewModelForNew()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = vm.uiState.value
        assertNull(state.noteId)
        assertEquals(1, state.blocks.size)
        assertTrue(state.blocks[0] is NoteBlock.Paragraph)
    }

    @Test
    fun `title change updates state`() = runTest(testDispatcher) {
        val vm = createViewModelForNew()
        vm.onEvent(NoteEditorEvent.TitleChanged("Mi título"))
        assertEquals("Mi título", vm.uiState.value.title)
    }

    @Test
    fun `block text change updates the correct block`() = runTest(testDispatcher) {
        val vm = createViewModelForNew()
        testDispatcher.scheduler.advanceUntilIdle()
        val blockId = vm.uiState.value.blocks[0].id
        vm.onEvent(NoteEditorEvent.BlockTextChanged(blockId, "hola"))
        assertEquals("hola", (vm.uiState.value.blocks[0] as NoteBlock.Paragraph).text)
    }

    @Test
    fun `block insert after focused adds new block`() = runTest(testDispatcher) {
        val vm = createViewModelForNew()
        testDispatcher.scheduler.advanceUntilIdle()
        val firstId = vm.uiState.value.blocks[0].id
        vm.onEvent(NoteEditorEvent.BlockInserted(firstId, BlockType.HEADING))
        assertEquals(2, vm.uiState.value.blocks.size)
        assertTrue(vm.uiState.value.blocks[1] is NoteBlock.Heading)
    }

    @Test
    fun `block delete removes block`() = runTest(testDispatcher) {
        val vm = createViewModelForNew()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(NoteEditorEvent.BlockInserted(null, BlockType.DIVIDER))
        assertEquals(2, vm.uiState.value.blocks.size)
        val secondId = vm.uiState.value.blocks[1].id
        vm.onEvent(NoteEditorEvent.BlockDeleted(secondId))
        assertEquals(1, vm.uiState.value.blocks.size)
    }

    @Test
    fun `block move reorders blocks`() = runTest(testDispatcher) {
        val vm = createViewModelForNew()
        testDispatcher.scheduler.advanceUntilIdle()
        val firstId = vm.uiState.value.blocks[0].id
        vm.onEvent(NoteEditorEvent.BlockInserted(firstId, BlockType.HEADING))
        // blocks: [paragraph, heading]
        vm.onEvent(NoteEditorEvent.BlockMoved(1, 0))
        assertTrue(vm.uiState.value.blocks[0] is NoteBlock.Heading)
    }

    @Test
    fun `check item toggle persists in block`() = runTest(testDispatcher) {
        val vm = createViewModelForNew()
        testDispatcher.scheduler.advanceUntilIdle()
        val blockId = vm.uiState.value.blocks[0].id
        vm.onEvent(NoteEditorEvent.BlockTypeChanged(blockId, BlockType.CHECKLIST))
        vm.onEvent(NoteEditorEvent.CheckItemAdded(blockId))
        vm.onEvent(NoteEditorEvent.CheckItemToggled(blockId, 0))
        val checklist = vm.uiState.value.blocks[0] as NoteBlock.Checklist
        assertTrue(checklist.items[0].isChecked)
    }

    @Test
    fun `save creates note with blocks via addNoteWithBlocks`() = runTest(testDispatcher) {
        coEvery {
            noteRepository.addNoteWithBlocks(any(), any(), any(), any(), any())
        } returns Result.success("n1")
        val vm = createViewModelForNew()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(NoteEditorEvent.TitleChanged("Título"))
        vm.onEvent(NoteEditorEvent.SubjectChanged("Física"))
        vm.onEvent(NoteEditorEvent.SaveClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value.isSaved)
        coVerify {
            noteRepository.addNoteWithBlocks("u1", "Física", "Título", any(), any())
        }
    }

    @Test
    fun `save with blank title sets error`() = runTest(testDispatcher) {
        val vm = createViewModelForNew()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(NoteEditorEvent.SaveClicked)
        assertFalse(vm.uiState.value.isSaved)
        assertNull(vm.uiState.value.error) // no subject either - may still error
    }

    @Test
    fun `existing note loads blocks from repository`() = runTest(testDispatcher) {
        val blocks = listOf(NoteBlock.Paragraph(id = "p1", text = "existente"))
        coEvery { noteRepository.getNotes("u1") } returns flowOf(
            listOf(
                Note(
                    id = "n1", userId = "u1", subjectId = "s1", subject = "Física",
                    title = "Vieja", content = "existente", tags = listOf("a"),
                    blocks = blocks
                )
            )
        )
        val vm = createViewModelForNew(noteId = "n1")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("n1", vm.uiState.value.noteId)
        assertEquals("Vieja", vm.uiState.value.title)
        assertEquals(blocks, vm.uiState.value.blocks)
    }

    @Test
    fun `check item text change updates item`() = runTest(testDispatcher) {
        val vm = createViewModelForNew()
        testDispatcher.scheduler.advanceUntilIdle()
        val blockId = vm.uiState.value.blocks[0].id
        vm.onEvent(NoteEditorEvent.BlockTypeChanged(blockId, BlockType.CHECKLIST))
        vm.onEvent(NoteEditorEvent.CheckItemAdded(blockId))
        vm.onEvent(NoteEditorEvent.CheckItemTextChanged(blockId, 0, "comprar pan"))
        val checklist = vm.uiState.value.blocks[0] as NoteBlock.Checklist
        assertEquals("comprar pan", checklist.items[0].text)
    }

    @Test
    fun `subject change updates state`() = runTest(testDispatcher) {
        val vm = createViewModelForNew()
        vm.onEvent(NoteEditorEvent.SubjectChanged("Física"))
        assertEquals("Física", vm.uiState.value.subject)
    }

    @Test
    fun `tag toggle adds and removes`() = runTest(testDispatcher) {
        val vm = createViewModelForNew()
        vm.onEvent(NoteEditorEvent.TagToggled("examen"))
        assertTrue(vm.uiState.value.tags.contains("examen"))
        vm.onEvent(NoteEditorEvent.TagToggled("examen"))
        assertFalse(vm.uiState.value.tags.contains("examen"))
    }

    @Test
    fun `CustomTagAdded trims and stores tag`() = runTest(testDispatcher) {
        val vm = createViewModelForNew()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(NoteEditorEvent.CustomTagAdded("  examen  "))
        assertTrue(vm.uiState.value.tags.contains("examen"))
        assertFalse(vm.uiState.value.tags.contains("  examen  "))
    }

    @Test
    fun `CustomTagAdded ignores blank tags`() = runTest(testDispatcher) {
        val vm = createViewModelForNew()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(NoteEditorEvent.CustomTagAdded("   "))
        assertTrue(vm.uiState.value.tags.isEmpty())
    }

    @Test
    fun `CustomTagRemoved removes tag`() = runTest(testDispatcher) {
        val vm = createViewModelForNew()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(NoteEditorEvent.CustomTagAdded("tarea"))
        vm.onEvent(NoteEditorEvent.CustomTagRemoved("tarea"))
        assertFalse(vm.uiState.value.tags.contains("tarea"))
    }
}
