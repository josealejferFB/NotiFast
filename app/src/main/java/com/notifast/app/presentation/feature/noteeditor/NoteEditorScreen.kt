package com.notifast.app.presentation.feature.noteeditor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.feature.noteeditor.components.BlockInsertMenu
import com.notifast.app.presentation.feature.noteeditor.components.BlockListEditor
import com.notifast.app.presentation.feature.noteeditor.components.EditorTopBar
import com.notifast.app.presentation.feature.noteeditor.components.ImageBlockPickerSheet
import com.notifast.app.presentation.feature.noteeditor.components.NoteSubjectSelectorChip

@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNoteSaved: () -> Unit,
    onNavigateToAddSubject: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    if (uiState.isSaved) {
        onNoteSaved()
        return
    }

    val hasUnsavedChanges = uiState.title.isNotBlank() ||
            uiState.blocks.any { block ->
                when (block) {
                    is com.notifast.app.data.model.NoteBlock.Paragraph -> block.text.isNotBlank()
                    is com.notifast.app.data.model.NoteBlock.Heading -> block.text.isNotBlank()
                    is com.notifast.app.data.model.NoteBlock.Quote -> block.text.isNotBlank()
                    is com.notifast.app.data.model.NoteBlock.BulletList -> block.items.isNotEmpty()
                    is com.notifast.app.data.model.NoteBlock.Checklist -> block.items.isNotEmpty()
                    else -> false
                }
            }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var annotatingImageId by remember { mutableStateOf<String?>(null) }
    var annotatingImagePath by remember { mutableStateOf<String?>(null) }
    val handleBack = { if (hasUnsavedChanges) showDiscardDialog = true else onBackClick() }
    BackHandler(enabled = hasUnsavedChanges) { showDiscardDialog = true }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(NoteEditorEvent.ClearError)
        }
    }

    if (showDiscardDialog) {
        NotiFastDialog(
            title = stringResource(R.string.notes_discard_title),
            message = stringResource(R.string.notes_discard_message),
            onConfirm = {
                showDiscardDialog = false
                onBackClick()
            },
            onDismiss = { showDiscardDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                EditorTopBar(
                    title = uiState.title.ifBlank { stringResource(R.string.fab_add_note) },
                    syncStatus = uiState.syncStatus,
                    onBackClick = handleBack,
                    onSaveClick = { viewModel.onEvent(NoteEditorEvent.SaveClicked) }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                NoteSubjectSelectorChip(
                    subjects = uiState.subjects,
                    selectedSubject = uiState.subject,
                    onSubjectSelected = { viewModel.onEvent(NoteEditorEvent.SubjectChanged(it)) },
                    onCreateSubjectClick = onNavigateToAddSubject
                )

                Spacer(modifier = Modifier.height(12.dp))

                TitleField(
                    title = uiState.title,
                    onTitleChange = { viewModel.onEvent(NoteEditorEvent.TitleChanged(it)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                            annotatingImageId = blockId
                            annotatingImagePath = block.path
                        }
                    },
                    onBlockDeleted = { viewModel.onEvent(NoteEditorEvent.BlockDeleted(it)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                BlockInsertMenu(
                    onInsert = { type ->
                        viewModel.onEvent(
                            NoteEditorEvent.BlockInserted(uiState.focusedBlockId, type)
                        )
                    },
                    onAddImage = { showImagePicker = true }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showImagePicker) {
        ImageBlockPickerSheet(
            onImageSelected = { uri ->
                viewModel.onEvent(
                    NoteEditorEvent.ImageBlockAdded(uiState.focusedBlockId, uri.toString())
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
private fun TitleField(title: String, onTitleChange: (String) -> Unit) {
    com.notifast.app.presentation.feature.noteeditor.components.BlockTextField(
        value = title,
        onValueChange = onTitleChange,
        onFocus = {},
        textStyle = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        ),
        isFocused = false,
        placeholder = stringResource(R.string.add_note_title_placeholder)
    )
}
