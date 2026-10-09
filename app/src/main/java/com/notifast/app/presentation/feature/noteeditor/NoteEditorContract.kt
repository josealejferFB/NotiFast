package com.notifast.app.presentation.feature.noteeditor

import android.net.Uri
import com.notifast.app.data.model.SubjectData

data class NoteEditorUiState(
    val noteId: String? = null,
    val subject: String = "",
    val subjects: List<SubjectData> = emptyList(),
    val title: String = "",
    val content: String = "",
    val tags: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val imageUrl: String? = null,
    val imageUri: Uri? = null,
    val isPreviewMode: Boolean = false,
    val wordCount: Int = 0,
    val readingTimeMinutes: Int = 0,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val syncStatus: String? = null,
    val error: String? = null
)

sealed interface NoteEditorEvent {
    data class Init(val noteId: String?, val subject: String?) : NoteEditorEvent
    data class TitleChanged(val title: String) : NoteEditorEvent
    data class ContentChanged(val content: String) : NoteEditorEvent
    data class SubjectChanged(val subject: String) : NoteEditorEvent
    data class TagToggled(val tag: String) : NoteEditorEvent
    data class CustomTagAdded(val tag: String) : NoteEditorEvent
    data class CustomTagRemoved(val tag: String) : NoteEditorEvent
    data class ImageSelected(val uri: Uri?) : NoteEditorEvent
    data class InsertFormatting(val prefix: String, val suffix: String = "") : NoteEditorEvent
    data class TogglePreview(val isPreview: Boolean) : NoteEditorEvent
    data class TaskCheckToggled(val taskIndex: Int, val isChecked: Boolean) : NoteEditorEvent
    data object SaveClicked : NoteEditorEvent
    data object ClearError : NoteEditorEvent
    data object ResetState : NoteEditorEvent
}
