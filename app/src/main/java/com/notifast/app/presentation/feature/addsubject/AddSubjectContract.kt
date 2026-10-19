package com.notifast.app.presentation.feature.addsubject

data class AddSubjectUiState(
    val subjectName: String = "",
    val section: String = "",
    val selectedIcon: String = "School",
    val selectedColor: String = "#3B9797",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val createdSubjectName: String = ""
)

sealed interface AddSubjectEvent {
    data class NameChanged(val name: String) : AddSubjectEvent
    data class SectionChanged(val section: String) : AddSubjectEvent
    data class SuggestionSelected(val name: String) : AddSubjectEvent
    data class IconSelected(val icon: String) : AddSubjectEvent
    data class ColorSelected(val colorHex: String) : AddSubjectEvent
    data object SaveClicked : AddSubjectEvent
    data object ClearError : AddSubjectEvent
    data object ResetState : AddSubjectEvent
}

