package com.notifast.app.domain.model.search

data class SearchResultItem(
    val id: String,
    val type: SearchResultType,
    val title: String,
    val subtitle: String,
    val snippet: String? = null,
    val subjectName: String,
    val subjectColorHex: String = "",
    val badgeText: String? = null,
    val targetRoute: String
)
