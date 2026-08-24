package com.notifast.app.presentation.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.model.search.SearchFilter
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.usecase.SearchGlobalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchGlobalUseCase: SearchGlobalUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private val filterFlow = MutableStateFlow(SearchFilter.ALL)

    init {
        combine(
            queryFlow.debounce(300).distinctUntilChanged(),
            filterFlow
        ) { query, filter ->
            query to filter
        }.flatMapLatest { (query, filter) ->
            val userId = authRepository.getCurrentUserId().orEmpty()
            if (query.isBlank() || userId.isBlank()) {
                _uiState.update { it.copy(isLoading = false, results = emptyList(), totalCount = 0) }
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                _uiState.update { it.copy(isLoading = true) }
                searchGlobalUseCase(userId, query, filter)
            }
        }.onEach { results ->
            _uiState.update { current ->
                val updatedRecent = if (current.query.trim().length >= 2) {
                    val filtered = current.recentSearches.filterNot { it.equals(current.query.trim(), ignoreCase = true) }
                    (listOf(current.query.trim()) + filtered).take(5)
                } else {
                    current.recentSearches
                }

                current.copy(
                    results = results,
                    totalCount = results.size,
                    isLoading = false,
                    recentSearches = updatedRecent
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.UpdateQuery -> {
                _uiState.update { it.copy(query = event.query) }
                queryFlow.value = event.query
            }
            is SearchEvent.SelectFilter -> {
                _uiState.update { it.copy(activeFilter = event.filter) }
                filterFlow.value = event.filter
            }
            is SearchEvent.ClearQuery -> {
                _uiState.update { it.copy(query = "", results = emptyList(), totalCount = 0) }
                queryFlow.value = ""
            }
            is SearchEvent.SelectRecentSearch -> {
                _uiState.update { it.copy(query = event.query) }
                queryFlow.value = event.query
            }
            is SearchEvent.DeleteRecentSearch -> {
                _uiState.update { current ->
                    current.copy(recentSearches = current.recentSearches.filterNot { it.equals(event.query, ignoreCase = true) })
                }
            }
            is SearchEvent.ClearRecentSearches -> {
                _uiState.update { it.copy(recentSearches = emptyList()) }
            }
        }
    }
}
