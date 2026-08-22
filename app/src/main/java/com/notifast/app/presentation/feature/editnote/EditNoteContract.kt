package com.notifast.app.presentation.feature.editnote

import android.net.Uri
import com.notifast.app.data.model.Note

data class EditNoteUiState(
    val note: Note? = null,
    val subjectColorHex: String = "",
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val isLivePreview: Boolean = false,
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val syncStatus: String? = null,
    val isSaved: Boolean = false
)

sealed interface EditNoteEvent {
    data class SubjectChanged(val value: String) : EditNoteEvent
    data class TitleChanged(val value: String) : EditNoteEvent
    data class ContentChanged(val value: String) : EditNoteEvent
    data class AddTag(val tag: String) : EditNoteEvent
    data class RemoveTag(val tag: String) : EditNoteEvent
    data class ToggleTag(val tag: String) : EditNoteEvent
    data class ToggleLivePreview(val enabled: Boolean) : EditNoteEvent
    data class ImageSelected(val uri: Uri?) : EditNoteEvent
    data object SaveClicked : EditNoteEvent
    data object ClearError : EditNoteEvent
}
