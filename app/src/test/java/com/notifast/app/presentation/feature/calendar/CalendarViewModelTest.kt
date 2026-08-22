package com.notifast.app.presentation.feature.calendar

import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.util.MainDispatcherRule
import com.notifast.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val evaluationRepository: EvaluationRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)

    private val testUserId = "user-test"

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns testUserId
    }

    @Test
    fun `loadData groups evaluations by date correctly`() = runTest {
        val today = Date()
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val todayKey = format.format(today)

        val eval1 = TestDataFactory.createEvaluation(id = "e1", title = "Parcial 1", date = today)
        val eval2 = TestDataFactory.createEvaluation(id = "e2", title = "Parcial 2", date = today)

        every { evaluationRepository.getEvaluations(testUserId) } returns flowOf(listOf(eval1, eval2))

        val viewModel = CalendarViewModel(evaluationRepository, authRepository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.evaluationsByDate[todayKey])
        assertEquals(2, state.evaluationsByDate[todayKey]?.size)
        assertEquals(2, state.dailyEvaluations.size)
    }

    @Test
    fun `changeMonth increments and decrements month correctly`() = runTest {
        every { evaluationRepository.getEvaluations(testUserId) } returns flowOf(emptyList())

        val viewModel = CalendarViewModel(evaluationRepository, authRepository)
        val initialMonth = viewModel.uiState.value.currentMonth.get(Calendar.MONTH)

        viewModel.onEvent(CalendarEvent.ChangeMonth(1))
        val nextMonth = viewModel.uiState.value.currentMonth.get(Calendar.MONTH)
        assertEquals((initialMonth + 1) % 12, nextMonth)

        viewModel.onEvent(CalendarEvent.ChangeMonth(-1))
        val restoredMonth = viewModel.uiState.value.currentMonth.get(Calendar.MONTH)
        assertEquals(initialMonth, restoredMonth)
    }

    @Test
    fun `deleteEvaluation calls repository and updates error on failure`() = runTest {
        val eval = TestDataFactory.createEvaluation(id = "e1")
        every { evaluationRepository.getEvaluations(testUserId) } returns flowOf(listOf(eval))
        coEvery { evaluationRepository.deleteEvaluation(testUserId, eval) } returns Result.failure(Exception("Fallo de red"))

        val viewModel = CalendarViewModel(evaluationRepository, authRepository)
        viewModel.onEvent(CalendarEvent.DeleteEvaluation(eval))

        coVerify { evaluationRepository.deleteEvaluation(testUserId, eval) }
        assertEquals("Fallo de red", viewModel.uiState.value.error)

        viewModel.onEvent(CalendarEvent.ClearError)
        assertNull(viewModel.uiState.value.error)
    }
}
