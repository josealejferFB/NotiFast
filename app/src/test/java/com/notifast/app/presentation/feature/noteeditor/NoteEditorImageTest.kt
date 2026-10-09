package com.notifast.app.presentation.feature.noteeditor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.ImageRepository
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorImageTest {

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

    private fun createViewModel(): NoteEditorViewModel {
        return NoteEditorViewModel(SavedStateHandle(), noteRepository, authRepository, imageRepository, context = context)
    }

    @Test
    fun `image selected updates imageUri in state`() = runTest(testDispatcher) {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val mockUri = mockk<Uri>()
        vm.onEvent(NoteEditorEvent.ImageSelected(mockUri))

        assertEquals(mockUri, vm.uiState.value.imageUri)
    }

    @Test
    fun `image selection can be cleared with null`() = runTest(testDispatcher) {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val mockUri = mockk<Uri>()
        vm.onEvent(NoteEditorEvent.ImageSelected(mockUri))
        assertEquals(mockUri, vm.uiState.value.imageUri)

        vm.onEvent(NoteEditorEvent.ImageSelected(null))
        assertNull(vm.uiState.value.imageUri)
    }
}
