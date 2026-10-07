package com.notifast.app.presentation.feature.noteeditor

import androidx.lifecycle.SavedStateHandle
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
import io.mockk.coEvery
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorImageTest {

    private val testDispatcher = StandardTestDispatcher()
    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

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

    private fun createViewModel(): NoteEditorViewModel {
        return NoteEditorViewModel(SavedStateHandle(), noteRepository, authRepository)
    }

    @Test
    fun `image block added inserts image block`() = runTest(testDispatcher) {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val firstId = vm.uiState.value.blocks[0].id
        vm.onEvent(NoteEditorEvent.ImageBlockAdded(firstId, "file:///foto.jpg"))
        val blocks = vm.uiState.value.blocks
        assertEquals(2, blocks.size)
        assertTrue(blocks[1] is NoteBlock.Image)
        assertEquals("file:///foto.jpg", (blocks[1] as NoteBlock.Image).path)
    }

    @Test
    fun `image block removed deletes image block`() = runTest(testDispatcher) {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val firstId = vm.uiState.value.blocks[0].id
        vm.onEvent(NoteEditorEvent.ImageBlockAdded(firstId, "file:///foto.jpg"))
        val imgId = vm.uiState.value.blocks[1].id
        vm.onEvent(NoteEditorEvent.ImageBlockRemoved(imgId))
        assertEquals(1, vm.uiState.value.blocks.size)
    }

    @Test
    fun `multiple images can be added`() = runTest(testDispatcher) {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        val firstId = vm.uiState.value.blocks[0].id
        vm.onEvent(NoteEditorEvent.ImageBlockAdded(firstId, "file:///foto1.jpg"))
        vm.onEvent(NoteEditorEvent.ImageBlockAdded(firstId, "file:///foto2.jpg"))
        vm.onEvent(NoteEditorEvent.ImageBlockAdded(firstId, "file:///foto3.jpg"))
        val images = vm.uiState.value.blocks.filterIsInstance<NoteBlock.Image>()
        assertEquals(3, images.size)
    }
}
