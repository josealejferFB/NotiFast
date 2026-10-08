package com.notifast.app.presentation.feature.noteeditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.presentation.feature.noteeditor.components.BlockInsertMenu
import com.notifast.app.presentation.feature.noteeditor.components.BlockListEditor
import com.notifast.app.presentation.feature.noteeditor.components.NoteSubjectSelectorChip
import com.notifast.app.presentation.feature.noteeditor.components.NoteTagsEditor
import com.notifast.app.presentation.feature.noteeditor.components.TextFormatToolbar

/**
 * Content-only editor body (no Scaffold/TopBar). Owned by [NoteEditorBottomSheet].
 */
@Composable
fun NoteEditorContent(
    uiState: NoteEditorUiState,
    viewModel: NoteEditorViewModel,
    onNavigateToAddSubject: () -> Unit,
    showImagePicker: Boolean,
    onShowImagePicker: () -> Unit,
    onDismissImagePicker: () -> Unit,
    onAnnotateImage: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        NoteSubjectSelectorChip(
            subjects = uiState.subjects,
            selectedSubject = uiState.subject,
            onSubjectSelected = { viewModel.onEvent(NoteEditorEvent.SubjectChanged(it)) },
            onCreateSubjectClick = onNavigateToAddSubject
        )

        Spacer(modifier = Modifier.height(12.dp))

        NoteTagsEditor(
            selectedTags = uiState.tags,
            availableTags = emptyList(),
            onTagToggled = { viewModel.onEvent(NoteEditorEvent.TagToggled(it)) },
            onCustomTagAdded = { viewModel.onEvent(NoteEditorEvent.CustomTagAdded(it)) },
            onCustomTagRemoved = { viewModel.onEvent(NoteEditorEvent.CustomTagRemoved(it)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        TitleField(
            title = uiState.title,
            onTitleChange = { viewModel.onEvent(NoteEditorEvent.TitleChanged(it)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        val hasSelection = uiState.selectionBlockId != null && uiState.selectionEnd > uiState.selectionStart
        val activeStyles = if (hasSelection && uiState.selectionBlockId != null) {
            val selBlock = uiState.blocks.find { it.id == uiState.selectionBlockId }
            val selSpans = when (selBlock) {
                is com.notifast.app.data.model.NoteBlock.Heading -> selBlock.spans
                is com.notifast.app.data.model.NoteBlock.Paragraph -> selBlock.spans
                is com.notifast.app.data.model.NoteBlock.Quote -> selBlock.spans
                else -> emptyList()
            }
            com.notifast.app.domain.util.TextSpanOps.stylesAt(
                selSpans, uiState.selectionStart, uiState.selectionEnd
            )
        } else emptySet()

        TextFormatToolbar(
            visible = hasSelection,
            activeStyles = activeStyles,
            onToggleStyle = { style ->
                val blockId = uiState.selectionBlockId
                if (blockId != null) {
                    viewModel.onEvent(
                        NoteEditorEvent.ToggleSpanStyle(
                            blockId, uiState.selectionStart, uiState.selectionEnd, style
                        )
                    )
                }
            }
        )

        BlockListEditor(
            blocks = uiState.blocks,
            focusedBlockId = uiState.focusedBlockId,
            onBlockTextChanged = { id, text ->
                viewModel.onEvent(NoteEditorEvent.BlockTextChanged(id, text))
            },
            onBlockFocused = { viewModel.onEvent(NoteEditorEvent.BlockFocused(it)) },
            onCheckToggle = { id, index ->
                viewModel.onEvent(NoteEditorEvent.CheckItemToggled(id, index))
            },
            onCheckTextChange = { id, index, text ->
                viewModel.onEvent(NoteEditorEvent.CheckItemTextChanged(id, index, text))
            },
            onImageClick = { blockId ->
                val block = uiState.blocks.find { it.id == blockId }
                if (block is com.notifast.app.data.model.NoteBlock.Image) {
                    onAnnotateImage(blockId, block.path)
                }
            },
            onBlockDeleted = { viewModel.onEvent(NoteEditorEvent.BlockDeleted(it)) },
            onBlockSpansChanged = { id, spans ->
                viewModel.onEvent(NoteEditorEvent.BlockSpansChanged(id, spans))
            },
            onBlockSelectionChanged = { id, s, e ->
                viewModel.onEvent(NoteEditorEvent.SelectionChanged(id, s, e))
            },
            onListItemTextChange = { id, index, text ->
                viewModel.onEvent(NoteEditorEvent.ListItemTextChanged(id, index, text))
            },
            onListItemAdded = { viewModel.onEvent(NoteEditorEvent.ListItemAdded(it)) },
            onListItemRemoved = { id, index ->
                viewModel.onEvent(NoteEditorEvent.ListItemRemoved(id, index))
            },
            onCheckItemAdded = { viewModel.onEvent(NoteEditorEvent.CheckItemAdded(it)) },
            onCheckItemRemoved = { id, index ->
                viewModel.onEvent(NoteEditorEvent.CheckItemRemoved(id, index))
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        BlockInsertMenu(
            onInsert = { type ->
                viewModel.onEvent(NoteEditorEvent.BlockInserted(uiState.focusedBlockId, type))
            },
            onAddImage = onShowImagePicker
        )
    }
}

@Composable
private fun TitleField(title: String, onTitleChange: (String) -> Unit) {
    com.notifast.app.presentation.feature.noteeditor.components.BlockTextField(
        value = title,
        onValueChange = onTitleChange,
        onFocus = {},
        textStyle = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        ),
        isFocused = false,
        placeholder = stringResource(com.notifast.app.R.string.add_note_title_placeholder)
    )
}
