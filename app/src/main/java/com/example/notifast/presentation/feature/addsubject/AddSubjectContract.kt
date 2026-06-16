package com.example.notifast.presentation.feature.addsubject

data class AddSubjectUiState(
    val subjectName: String = "",
    val selectedIcon: String = "📚",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

sealed interface AddSubjectEvent {
    data class NameChanged(val name: String) : AddSubjectEvent
    data class IconSelected(val icon: String) : AddSubjectEvent
    data object SaveClicked : AddSubjectEvent
}
