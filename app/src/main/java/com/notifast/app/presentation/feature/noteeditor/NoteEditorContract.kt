package com.notifast.app.presentation.feature.noteeditor

import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.SubjectData

data class NoteEditorUiState(
    val noteId: String? = null,
    val subject: String = "",
    val subjects: List<SubjectData> = emptyList(),
    val title: String = "",
    val blocks: List<NoteBlock> = emptyList(),
    val tags: List<String> = emptyList(),
    val focusedBlockId: String? = null,
    val isEditMode: Boolean = true,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val syncStatus: String? = null,
    val error: String? = null
)

sealed interface NoteEditorEvent {
    data class TitleChanged(val title: String) : NoteEditorEvent
    data class SubjectChanged(val subject: String) : NoteEditorEvent
    data class TagToggled(val tag: String) : NoteEditorEvent
    data class CustomTagAdded(val tag: String) : NoteEditorEvent
    data class CustomTagRemoved(val tag: String) : NoteEditorEvent

    data class BlockTextChanged(val blockId: String, val text: String) : NoteEditorEvent
    data class BlockSpansChanged(val blockId: String, val spans: List<com.notifast.app.data.model.TextSpan>) : NoteEditorEvent
    data class BlockFocused(val blockId: String?) : NoteEditorEvent
    data class BlockMoved(val fromIndex: Int, val toIndex: Int) : NoteEditorEvent
    data class BlockDeleted(val blockId: String) : NoteEditorEvent
    data class BlockInserted(val afterBlockId: String?, val type: BlockType) : NoteEditorEvent
    data class BlockTypeChanged(val blockId: String, val newType: BlockType) : NoteEditorEvent

    data class CheckItemToggled(val blockId: String, val itemIndex: Int) : NoteEditorEvent
    data class CheckItemTextChanged(val blockId: String, val itemIndex: Int, val text: String) : NoteEditorEvent
    data class CheckItemAdded(val blockId: String) : NoteEditorEvent
    data class CheckItemRemoved(val blockId: String, val itemIndex: Int) : NoteEditorEvent

    data class ListItemTextChanged(val blockId: String, val itemIndex: Int, val text: String) : NoteEditorEvent
    data class ListItemAdded(val blockId: String) : NoteEditorEvent
    data class ListItemRemoved(val blockId: String, val itemIndex: Int) : NoteEditorEvent
    data class ListItemOrderedChanged(val blockId: String, val isOrdered: Boolean) : NoteEditorEvent

    data class ImageBlockAdded(val afterBlockId: String?, val path: String) : NoteEditorEvent
    data class ImageSelectedForBlock(val afterBlockId: String?, val uri: android.net.Uri) : NoteEditorEvent
    data class ImageBlockRemoved(val blockId: String) : NoteEditorEvent
    data class ImageAnnotationSaved(val blockId: String, val annotationJson: String) : NoteEditorEvent

    data class MathSourceChanged(val blockId: String, val source: String) : NoteEditorEvent

    data object SaveClicked : NoteEditorEvent
    data object ClearError : NoteEditorEvent
    data object ResetState : NoteEditorEvent
}

enum class BlockType {
    HEADING, PARAGRAPH, BULLET_LIST, ORDERED_LIST, CHECKLIST, QUOTE, IMAGE, MATH, DIVIDER
}
