package com.example.notifast.presentation.feature.editnote

import android.net.Uri
import com.example.notifast.data.model.Note

data class EditNoteUiState(
    val note: Note? = null,
    val subject: String = "",
    val title: String = "",
    val content: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

sealed interface EditNoteEvent {
    data class SubjectChanged(val value: String) : EditNoteEvent
    data class TitleChanged(val value: String) : EditNoteEvent
    data class ContentChanged(val value: String) : EditNoteEvent
    data class ImageSelected(val uri: Uri?) : EditNoteEvent
    data object SaveClicked : EditNoteEvent
    data object ClearError : EditNoteEvent
}
