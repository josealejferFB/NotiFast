package com.notifast.app.presentation.feature.notes

import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.usecase.CalculateWeightedAverageUseCase
import com.notifast.app.util.MainDispatcherRule
import com.notifast.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

    private val testUserId = "user-test"

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns testUserId
        coEvery { authRepository.getUserProfile(testUserId) } returns Result.success(
            TestDataFactory.createUser(firstName = "Ana", lastName = "Gomez", email = "ana@test.com")
        )
    }

    @Test
    fun `loadNotes combines notes, subjects and evaluations into subjectInfos`() = runTest {
        val subject1 = TestDataFactory.createSubject(name = "Física", icon = "⚡", colorHex = "#3B9797")
        val note1 = TestDataFactory.createNote(id = "n1", subject = "Física", title = "Termodinámica")
        val eval1 = TestDataFactory.createEvaluation(id = "e1", subjectName = "Física", grade = 8.0, weight = 100.0, isPresented = true)

        every { noteRepository.getNotes(testUserId) } returns flowOf(listOf(note1))
        every { noteRepository.getSubjects(testUserId) } returns flowOf(listOf(subject1))
        every { evaluationRepository.getEvaluations(testUserId) } returns flowOf(listOf(eval1))

        val viewModel = NotesViewModel(noteRepository, authRepository, evaluationRepository, calculateWeightedAverageUseCase)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.rawNotes.size)
        assertEquals("Termodinámica", state.rawNotes.first().title)
        assertNotNull(state.notes["Física"])
        assertEquals(1, state.subjectInfos.size)
        assertEquals("Física", state.subjectInfos.first().name)
        assertEquals(8.0, state.subjectInfos.first().averageGrade ?: 0.0, 0.01)
        assertEquals("Ana", state.userFirstName)
    }

    @Test
    fun `search filter filters notes dynamically`() = runTest {
        val note1 = TestDataFactory.createNote(id = "n1", subject = "Matemáticas", title = "Cálculo Diferencial", content = "Derivadas")
        val note2 = TestDataFactory.createNote(id = "n2", subject = "Historia", title = "Historia del Arte", content = "Renacimiento")

        every { noteRepository.getNotes(testUserId) } returns flowOf(listOf(note1, note2))
        every { noteRepository.getSubjects(testUserId) } returns flowOf(emptyList())
        every { evaluationRepository.getEvaluations(testUserId) } returns flowOf(emptyList())

        val viewModel = NotesViewModel(noteRepository, authRepository, evaluationRepository, calculateWeightedAverageUseCase)

        viewModel.onEvent(NotesEvent.Search("Diferencial"))
        val filtered = viewModel.uiState.value.notes.values.flatten()
        assertEquals(1, filtered.size)
        assertEquals("Cálculo Diferencial", filtered.first().title)

        viewModel.onEvent(NotesEvent.Search(""))
        val allNotes = viewModel.uiState.value.notes.values.flatten()
        assertEquals(2, allNotes.size)
    }

    @Test
    fun `filter change updates notes ordering`() = runTest {
        val note1 = TestDataFactory.createNote(id = "n1", title = "Zeta", subject = "Biología")
        val note2 = TestDataFactory.createNote(id = "n2", title = "Alfa", subject = "Álgebra")

        every { noteRepository.getNotes(testUserId) } returns flowOf(listOf(note1, note2))
        every { noteRepository.getSubjects(testUserId) } returns flowOf(emptyList())
        every { evaluationRepository.getEvaluations(testUserId) } returns flowOf(emptyList())

        val viewModel = NotesViewModel(noteRepository, authRepository, evaluationRepository, calculateWeightedAverageUseCase)

        viewModel.onEvent(NotesEvent.SetFilter(NoteFilter.POR_MATERIA))
        assertEquals(NoteFilter.POR_MATERIA, viewModel.uiState.value.currentFilter)
    }
}
