package com.notifast.app.presentation.feature.addnote

import android.net.Uri
import com.notifast.app.data.model.SubjectData

data class AddNoteUiState(
    val subject: String = "",
    val subjects: List<SubjectData> = emptyList(),
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
    data class SetSubjects(val subjects: List<SubjectData>) : AddNoteEvent
}
