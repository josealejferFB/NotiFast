package com.notifast.app.presentation.feature.addsubject

data class AddSubjectUiState(
    val subjectName: String = "",
    val selectedIcon: String = "School",
    val selectedColor: String = "#3B9797",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

sealed interface AddSubjectEvent {
    data class NameChanged(val name: String) : AddSubjectEvent
    data class IconSelected(val icon: String) : AddSubjectEvent
    data class ColorSelected(val colorHex: String) : AddSubjectEvent
    data object SaveClicked : AddSubjectEvent
}
