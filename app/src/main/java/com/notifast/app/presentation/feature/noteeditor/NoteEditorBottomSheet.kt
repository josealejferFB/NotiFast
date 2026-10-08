package com.notifast.app.presentation.feature.noteeditor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastDialog
import kotlinx.coroutines.launch

/**
 * Unified create/edit note editor as an expandable bottom sheet.
 * Registered as a Navigation Compose `dialog()` destination so every entry point
 * (FAB, edit card, SubjectDetail, Schedules, global search) shares one flow (R5).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorBottomSheet(
    noteId: String?,
    subject: String?,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var annotatingImageId by remember { mutableStateOf<String?>(null) }
    var annotatingImagePath by remember { mutableStateOf<String?>(null) }

    val hasUnsavedChanges = uiState.title.isNotBlank() ||
            uiState.blocks.any { block ->
                when (block) {
                    is com.notifast.app.data.model.NoteBlock.Paragraph -> block.text.isNotBlank()
                    is com.notifast.app.data.model.NoteBlock.Heading -> block.text.isNotBlank()
                    is com.notifast.app.data.model.NoteBlock.Quote -> block.text.isNotBlank()
                    is com.notifast.app.data.model.NoteBlock.BulletList -> block.items.isNotEmpty()
                    is com.notifast.app.data.model.NoteBlock.Checklist -> block.items.isNotEmpty()
                    is com.notifast.app.data.model.NoteBlock.Image -> true
                    is com.notifast.app.data.model.NoteBlock.MathBlock -> block.source.isNotBlank()
                    else -> false
                }
            }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            scope.launch { sheetState.hide() }.invokeOnCompletion { onSaved() }
        }
    }

    fun closeSheet(after: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { after() }
    }

    BackHandler(enabled = hasUnsavedChanges) { showDiscardDialog = true }

    if (showDiscardDialog) {
        NotiFastDialog(
            title = stringResource(R.string.notes_discard_title),
            message = stringResource(R.string.notes_discard_message),
            onConfirm = {
                showDiscardDialog = false
                closeSheet { onDismiss() }
            },
            onDismiss = { showDiscardDialog = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (hasUnsavedChanges) showDiscardDialog = true else closeSheet { onDismiss() }
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            SheetHeader(
                isEdit = noteId != null,
                title = uiState.title,
                onClose = {
                    if (hasUnsavedChanges) showDiscardDialog = true else closeSheet { onDismiss() }
                },
                onSave = { viewModel.onEvent(NoteEditorEvent.SaveClicked) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            NoteEditorContent(
                uiState = uiState,
                viewModel = viewModel,
                onNavigateToAddSubject = {},
                showImagePicker = showImagePicker,
                onShowImagePicker = { showImagePicker = true },
                onDismissImagePicker = { showImagePicker = false },
                onAnnotateImage = { blockId, path ->
                    annotatingImageId = blockId
                    annotatingImagePath = path
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // R6: nested sheet — dismiss picker before dispatching insert
    if (showImagePicker) {
        com.notifast.app.presentation.feature.noteeditor.components.ImageBlockPickerSheet(
            onImageSelected = { uri ->
                showImagePicker = false
                viewModel.onEvent(
                    NoteEditorEvent.ImageSelectedForBlock(uiState.focusedBlockId, uri)
                )
            },
            onDismiss = { showImagePicker = false }
        )
    }

    if (annotatingImagePath != null) {
        com.notifast.app.presentation.feature.noteeditor.annotator.PhotoAnnotatorScreen(
            imagePath = annotatingImagePath!!,
            onSave = { annotation ->
                val blockId = annotatingImageId
                if (blockId != null) {
                    val json = com.notifast.app.domain.util.PhotoAnnotationCodec.toJson(annotation)
                    viewModel.onEvent(NoteEditorEvent.ImageAnnotationSaved(blockId, json))
                }
                annotatingImageId = null
                annotatingImagePath = null
            },
            onCancel = {
                annotatingImageId = null
                annotatingImagePath = null
            }
        )
    }
}

@Composable
private fun SheetHeader(
    isEdit: Boolean,
    title: String,
    onClose: () -> Unit,
    onSave: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.notes_discard_title),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = if (isEdit) stringResource(R.string.edit_note_title)
            else stringResource(R.string.fab_add_note),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        NotiFastButton(
            text = stringResource(R.string.add_note_btn_save_note),
            onClick = onSave,
            modifier = Modifier.height(40.dp)
        )
    }
    if (title.isNotBlank()) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
