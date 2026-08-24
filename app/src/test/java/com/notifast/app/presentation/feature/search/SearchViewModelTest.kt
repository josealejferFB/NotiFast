package com.notifast.app.presentation.feature.search

import com.notifast.app.domain.model.search.SearchFilter
import com.notifast.app.domain.model.search.SearchResultItem
import com.notifast.app.domain.model.search.SearchResultType
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.usecase.SearchGlobalUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val searchGlobalUseCase = mockk<SearchGlobalUseCase>()
    private val authRepository = mockk<AuthRepository>()

    private val sampleResults = listOf(
        SearchResultItem(
            id = "s1",
            type = SearchResultType.SUBJECT,
            title = "Cálculo I",
            subtitle = "Asignatura",
            subjectName = "Cálculo I",
            targetRoute = "subject_detail/Calculo"
        ),
        SearchResultItem(
            id = "n1",
            type = SearchResultType.NOTE,
            title = "Derivadas",
            subtitle = "Cálculo I",
            subjectName = "Cálculo I",
            targetRoute = "edit_note/n1"
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { authRepository.getCurrentUserId() } returns "u1"
        every { searchGlobalUseCase("u1", any(), any()) } returns flowOf(sampleResults)
        every { searchGlobalUseCase("u1", "", any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty query and empty results`() = runTest {
        val viewModel = SearchViewModel(searchGlobalUseCase, authRepository)
        assertEquals("", viewModel.uiState.value.query)
        assertEquals(SearchFilter.ALL, viewModel.uiState.value.activeFilter)
        assertTrue(viewModel.uiState.value.results.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `updating query with debounce updates results and recent searches`() = runTest {
        val viewModel = SearchViewModel(searchGlobalUseCase, authRepository)

        viewModel.onEvent(SearchEvent.UpdateQuery("cal"))
        advanceTimeBy(350)
        advanceUntilIdle()

        assertEquals("cal", viewModel.uiState.value.query)
        assertEquals(sampleResults, viewModel.uiState.value.results)
        assertTrue(viewModel.uiState.value.recentSearches.contains("cal"))
    }

    @Test
    fun `changing filter triggers new search query with selected filter`() = runTest {
        val noteResults = listOf(sampleResults[1])
        every { searchGlobalUseCase("u1", "cal", SearchFilter.NOTES) } returns flowOf(noteResults)

        val viewModel = SearchViewModel(searchGlobalUseCase, authRepository)
        viewModel.onEvent(SearchEvent.UpdateQuery("cal"))
        advanceTimeBy(350)
        advanceUntilIdle()

        viewModel.onEvent(SearchEvent.SelectFilter(SearchFilter.NOTES))
        advanceUntilIdle()

        assertEquals(SearchFilter.NOTES, viewModel.uiState.value.activeFilter)
        assertEquals(noteResults, viewModel.uiState.value.results)
    }

    @Test
    fun `clearing query resets query and results`() = runTest {
        val viewModel = SearchViewModel(searchGlobalUseCase, authRepository)
        viewModel.onEvent(SearchEvent.UpdateQuery("cal"))
        advanceTimeBy(350)
        advanceUntilIdle()

        viewModel.onEvent(SearchEvent.ClearQuery)
        advanceTimeBy(350)
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.query)
        assertTrue(viewModel.uiState.value.results.isEmpty())
    }

    @Test
    fun `selecting recent search populates query and triggers search`() = runTest {
        val viewModel = SearchViewModel(searchGlobalUseCase, authRepository)
        viewModel.onEvent(SearchEvent.SelectRecentSearch("física"))
        advanceTimeBy(350)
        advanceUntilIdle()

        assertEquals("física", viewModel.uiState.value.query)
        assertEquals(sampleResults, viewModel.uiState.value.results)
    }
}
