package com.notifast.app.presentation.feature.search

import com.notifast.app.domain.model.search.SearchFilter
import com.notifast.app.domain.model.search.SearchResultItem

data class SearchUiState(
    val query: String = "",
    val activeFilter: SearchFilter = SearchFilter.ALL,
    val results: List<SearchResultItem> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val totalCount: Int = 0
)

sealed interface SearchEvent {
    data class UpdateQuery(val query: String) : SearchEvent
    data class SelectFilter(val filter: SearchFilter) : SearchEvent
    data object ClearQuery : SearchEvent
    data class SelectRecentSearch(val query: String) : SearchEvent
    data class DeleteRecentSearch(val query: String) : SearchEvent
    data object ClearRecentSearches : SearchEvent
}
