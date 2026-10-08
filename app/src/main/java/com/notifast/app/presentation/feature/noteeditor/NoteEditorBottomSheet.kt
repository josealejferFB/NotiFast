package com.notifast.app.presentation.feature.noteeditor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.feature.noteeditor.components.DockedAccessoryToolbar
import kotlinx.coroutines.launch

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
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.onEvent(NoteEditorEvent.ImageSelected(uri))
    }

    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = uiState.title.isNotBlank() || uiState.content.isNotBlank() || uiState.imageUri != null

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
            onConfirm = { showDiscardDialog = false; closeSheet { onDismiss() } },
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
        ) {
            val canSave = uiState.subject.isNotBlank() && uiState.title.isNotBlank() && !uiState.isLoading

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(38.dp)
                ) {
                    IconButton(
                        onClick = { if (hasUnsavedChanges) showDiscardDialog = true else closeSheet { onDismiss() } },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.notes_discard_title),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = if (noteId != null) stringResource(R.string.edit_note_title) else stringResource(R.string.fab_add_note),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    onClick = { viewModel.onEvent(NoteEditorEvent.TogglePreview(!uiState.isPreviewMode)) }
                ) {
                    Icon(
                        imageVector = if (uiState.isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                        contentDescription = stringResource(
                            if (uiState.isPreviewMode) R.string.editor_toggle_edit_mode else R.string.editor_toggle_reading_mode
                        ),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (canSave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                    border = BorderStroke(1.dp, if (canSave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.height(38.dp).clickable(enabled = canSave) { viewModel.onEvent(NoteEditorEvent.SaveClicked) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = if (canSave) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.btn_save),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (canSave) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                NoteEditorContent(
                    uiState = uiState,
                    onTitleChanged = { viewModel.onEvent(NoteEditorEvent.TitleChanged(it)) },
                    onContentChanged = { viewModel.onEvent(NoteEditorEvent.ContentChanged(it)) },
                    onSubjectSelected = { viewModel.onEvent(NoteEditorEvent.SubjectChanged(it)) },
                    onTagToggled = { viewModel.onEvent(NoteEditorEvent.TagToggled(it)) },
                    onCustomTagAdded = { viewModel.onEvent(NoteEditorEvent.CustomTagAdded(it)) },
                    onCustomTagRemoved = { viewModel.onEvent(NoteEditorEvent.CustomTagRemoved(it)) },
                    onTaskCheckToggled = { index, checked -> viewModel.onEvent(NoteEditorEvent.TaskCheckToggled(index, checked)) },
                    onImageSelected = { viewModel.onEvent(NoteEditorEvent.ImageSelected(it)) },
                    onCreateSubjectClick = {}
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotiFastButton(
                    text = stringResource(R.string.add_note_btn_save_note),
                    onClick = { viewModel.onEvent(NoteEditorEvent.SaveClicked) },
                    isLoading = uiState.isLoading,
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(28.dp))
            }

            if (!uiState.isPreviewMode) {
                DockedAccessoryToolbar(
                    onInsertFormatting = { p, s -> viewModel.onEvent(NoteEditorEvent.InsertFormatting(p, s)) },
                    onAttachPhotoClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
