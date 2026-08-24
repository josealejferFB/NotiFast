package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.EvaluationType
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.search.SearchFilter
import com.notifast.app.domain.model.search.SearchResultType
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchGlobalUseCaseTest {

    private val noteRepository = mockk<NoteRepository>()
    private val evaluationRepository = mockk<EvaluationRepository>()
    private val scheduleRepository = mockk<ScheduleRepository>()
    private lateinit var searchGlobalUseCase: SearchGlobalUseCase

    private val testSubjects = listOf(
        SubjectData(name = "Cálculo Diferencial", colorHex = "#3B9797"),
        SubjectData(name = "Física Clásica", colorHex = "#BF092F")
    )

    private val testNotes = listOf(
        Note(id = "n1", subject = "Cálculo Diferencial", title = "Límites y Continuidad", content = "Definición epsilon-delta de límite", tags = listOf("teoría", "examen")),
        Note(id = "n2", subject = "Física Clásica", title = "Leyes de Newton", content = "Fuerza igual a masa por aceleración", tags = listOf("resumen"))
    )

    private val testEvaluations = listOf(
        Evaluation(id = "e1", subjectName = "Cálculo Diferencial", title = "Parcial 1: Derivadas", description = "Regla de la cadena y optimización", type = EvaluationType.EXAM.name, weight = 30.0),
        Evaluation(id = "e2", subjectName = "Física Clásica", title = "Taller de Dinámica", description = "Plano inclinado con rozamiento", type = EvaluationType.PROJECT.name, weight = 20.0)
    )

    private val testSchedules = listOf(
        Schedule(id = "s1", subjectName = "Cálculo Diferencial", teacher = "Dr. Gauss", classroom = "Aula 301", dayOfWeek = 1, startTime = "08:00", endTime = "10:00"),
        Schedule(id = "s2", subjectName = "Física Clásica", teacher = "Dra. Curie", classroom = "Lab 102", dayOfWeek = 3, startTime = "14:00", endTime = "16:00")
    )

    @Before
    fun setUp() {
        every { noteRepository.getSubjects("u1") } returns flowOf(testSubjects)
        every { noteRepository.getNotes("u1") } returns flowOf(testNotes)
        every { evaluationRepository.getEvaluations("u1") } returns flowOf(testEvaluations)
        every { scheduleRepository.getSchedules("u1") } returns flowOf(testSchedules)

        searchGlobalUseCase = SearchGlobalUseCase(
            noteRepository = noteRepository,
            evaluationRepository = evaluationRepository,
            scheduleRepository = scheduleRepository
        )
    }

    @Test
    fun `empty query returns empty results`() = runTest {
        val results = searchGlobalUseCase("u1", "", SearchFilter.ALL).first()
        assertTrue(results.isEmpty())
    }

    @Test
    fun `search with diacritics normalizes correctly and finds matches`() = runTest {
        // Query without tildes "calculo" should find "Cálculo Diferencial" across subjects, notes, evals, schedules
        val results = searchGlobalUseCase("u1", "calculo", SearchFilter.ALL).first()
        assertEquals(4, results.size)
        assertTrue(results.any { it.type == SearchResultType.SUBJECT && it.title == "Cálculo Diferencial" })
        assertTrue(results.any { it.type == SearchResultType.NOTE && it.title == "Límites y Continuidad" })
        assertTrue(results.any { it.type == SearchResultType.EVALUATION && it.title == "Parcial 1: Derivadas" })
        assertTrue(results.any { it.type == SearchResultType.SCHEDULE && it.title.contains("Cálculo Diferencial") })
    }

    @Test
    fun `search with specific filter returns only that entity`() = runTest {
        val noteResults = searchGlobalUseCase("u1", "newton", SearchFilter.NOTES).first()
        assertEquals(1, noteResults.size)
        assertEquals(SearchResultType.NOTE, noteResults.first().type)
        assertEquals("Leyes de Newton", noteResults.first().title)

        val evalResults = searchGlobalUseCase("u1", "derivadas", SearchFilter.EVALUATIONS).first()
        assertEquals(1, evalResults.size)
        assertEquals(SearchResultType.EVALUATION, evalResults.first().type)
        assertEquals("Parcial 1: Derivadas", evalResults.first().title)
    }

    @Test
    fun `search by teacher name finds schedule`() = runTest {
        val results = searchGlobalUseCase("u1", "curie", SearchFilter.SCHEDULES).first()
        assertEquals(1, results.size)
        assertEquals(SearchResultType.SCHEDULE, results.first().type)
        assertTrue(results.first().subtitle.contains("Dra. Curie"))
    }

    @Test
    fun `search by tag finds corresponding note`() = runTest {
        val results = searchGlobalUseCase("u1", "teoria", SearchFilter.NOTES).first()
        assertEquals(1, results.size)
        assertEquals("Límites y Continuidad", results.first().title)
    }
}
