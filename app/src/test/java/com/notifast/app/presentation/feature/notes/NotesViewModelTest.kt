package com.notifast.app.presentation.feature.notes

import com.notifast.app.data.model.Note
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.User
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.usecase.CalculateWeightedAverageUseCase
import com.notifast.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val noteRepository: NoteRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val evaluationRepository: EvaluationRepository = mockk(relaxed = true)
    private val calculateWeightedAverageUseCase = CalculateWeightedAverageUseCase()

    private val sampleSubjects = listOf(
        SubjectData(name = "Matemáticas", colorHex = "#3B9797"),
        SubjectData(name = "Física", colorHex = "#BF092F")
    )
    private val sampleNotes = listOf(
        Note(id = "1", subject = "Matemáticas", title = "Álgebra Lineal", content = "Matrices y determinantes", tags = listOf("Examen", "Fórmula")),
        Note(id = "2", subject = "Física", title = "Cinemática", content = "Velocidad y aceleración", tags = listOf("Resumen"))
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { authRepository.getUserProfile("user_123") } returns Result.success(User(firstName = "Test", lastName = "User", email = "test@example.com"))
        coEvery { noteRepository.getSubjects("user_123") } returns flowOf(sampleSubjects)
        coEvery { noteRepository.getNotes("user_123") } returns flowOf(sampleNotes)
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(emptyList())
    }

    @Test
    fun `when initialized, loads notes and populates availableTags`() = runTest {
        val viewModel = NotesViewModel(noteRepository, authRepository, evaluationRepository, calculateWeightedAverageUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.rawNotes.size)
        assertEquals(listOf("Examen", "Fórmula", "Resumen"), state.availableTags)
    }

    @Test
    fun `when SelectTag event dispatched, filters notes matching tag`() = runTest {
        val viewModel = NotesViewModel(noteRepository, authRepository, evaluationRepository, calculateWeightedAverageUseCase)
        advanceUntilIdle()

        viewModel.onEvent(NotesEvent.SelectTag("Examen"))
        advanceUntilIdle()

        val filteredNotes = viewModel.uiState.value.notes.values.flatten()
        assertEquals(1, filteredNotes.size)
        assertEquals("Álgebra Lineal", filteredNotes.first().title)
    }

    @Test
    fun `when searching by tag or keyword, returns matching notes`() = runTest {
        val viewModel = NotesViewModel(noteRepository, authRepository, evaluationRepository, calculateWeightedAverageUseCase)
        advanceUntilIdle()

        viewModel.onEvent(NotesEvent.Search("aceleración"))
        advanceUntilIdle()

        val filteredNotes = viewModel.uiState.value.notes.values.flatten()
        assertEquals(1, filteredNotes.size)
        assertEquals("Cinemática", filteredNotes.first().title)
    }
}
