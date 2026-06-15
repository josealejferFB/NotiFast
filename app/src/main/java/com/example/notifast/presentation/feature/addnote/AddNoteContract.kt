package com.example.notifast.presentation.feature.addnote

import android.net.Uri

data class AddNoteUiState(
    val subject: String = "",
    val title: String = "",
    val content: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

sealed interface AddNoteEvent {
    data class SubjectChanged(val value: String) : AddNoteEvent
    data class TitleChanged(val value: String) : AddNoteEvent
    data class ContentChanged(val value: String) : AddNoteEvent
    data class ImageSelected(val uri: Uri?) : AddNoteEvent
    data object SaveClicked : AddNoteEvent
    data object ClearError : AddNoteEvent
}
