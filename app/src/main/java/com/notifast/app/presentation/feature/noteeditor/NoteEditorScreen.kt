package com.notifast.app.presentation.feature.noteeditor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.feature.noteeditor.components.DockedAccessoryToolbar
import com.notifast.app.presentation.feature.noteeditor.components.EditorTopBar
import com.notifast.app.presentation.feature.noteeditor.util.SmartMarkdownFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNoteSaved: () -> Unit,
    onNavigateToAddSubject: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.onEvent(NoteEditorEvent.ImageSelected(uri))
    }

    var contentFieldValue by remember {
        mutableStateOf(TextFieldValue(uiState.content, TextRange(uiState.content.length)))
    }

    LaunchedEffect(uiState.content) {
        if (uiState.content != contentFieldValue.text) {
            contentFieldValue = TextFieldValue(uiState.content, TextRange(uiState.content.length))
        }
    }

    val activeStyles = remember(contentFieldValue) {
        SmartMarkdownFormatter.detectActiveStyles(contentFieldValue)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNoteSaved()
    }

    val hasUnsavedChanges = uiState.title.isNotBlank() ||
            uiState.content.isNotBlank() ||
            uiState.imageUri != null

    var showDiscardDialog by remember { mutableStateOf(false) }
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
            containerColor = Color.Transparent,
            topBar = {
                EditorTopBar(
                    title = if (uiState.noteId != null) stringResource(R.string.edit_note_title)
                    else stringResource(R.string.fab_add_note),
                    syncStatus = uiState.syncStatus,
                    onBackClick = handleBack,
                    onSaveClick = { viewModel.onEvent(NoteEditorEvent.SaveClicked) },
                    isPreviewMode = uiState.isPreviewMode,
                    onTogglePreview = { viewModel.onEvent(NoteEditorEvent.TogglePreview(!uiState.isPreviewMode)) }
                )
            },
            bottomBar = {
                if (!uiState.isPreviewMode) {
                    DockedAccessoryToolbar(
                        activeStyles = activeStyles,
                        onToggleFormat = { style ->
                            val updated = SmartMarkdownFormatter.toggleFormat(contentFieldValue, style)
                            contentFieldValue = updated
                            viewModel.onEvent(NoteEditorEvent.ContentChanged(updated.text))
                            scope.launch { bringIntoViewRequester.bringIntoView() }
                        },
                        onInsertFormatting = { p, s -> viewModel.onEvent(NoteEditorEvent.InsertFormatting(p, s)) },
                        onAttachPhotoClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                NoteEditorContent(
                    uiState = uiState,
                    contentFieldValue = contentFieldValue,
                    onTitleChanged = { viewModel.onEvent(NoteEditorEvent.TitleChanged(it)) },
                    onContentFieldValueChange = { new ->
                        val handled = SmartMarkdownFormatter.handleSmartEnter(contentFieldValue, new)
                        contentFieldValue = handled
                        viewModel.onEvent(NoteEditorEvent.ContentChanged(handled.text))
                    },
                    onSubjectSelected = { viewModel.onEvent(NoteEditorEvent.SubjectChanged(it)) },
                    onTagToggled = { viewModel.onEvent(NoteEditorEvent.TagToggled(it)) },
                    onCustomTagAdded = { viewModel.onEvent(NoteEditorEvent.CustomTagAdded(it)) },
                    onCustomTagRemoved = { viewModel.onEvent(NoteEditorEvent.CustomTagRemoved(it)) },
                    onInsertFormatting = { p, s -> viewModel.onEvent(NoteEditorEvent.InsertFormatting(p, s)) },
                    onTogglePreview = { viewModel.onEvent(NoteEditorEvent.TogglePreview(it)) },
                    onTaskCheckToggled = { index, checked -> viewModel.onEvent(NoteEditorEvent.TaskCheckToggled(index, checked)) },
                    onImageSelected = { viewModel.onEvent(NoteEditorEvent.ImageSelected(it)) },
                    onCreateSubjectClick = onNavigateToAddSubject,
                    bringIntoViewRequester = bringIntoViewRequester
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotiFastButton(
                    text = stringResource(R.string.add_note_btn_save_note),
                    onClick = { viewModel.onEvent(NoteEditorEvent.SaveClicked) },
                    isLoading = uiState.isLoading,
                    enabled = uiState.subject.isNotBlank() && uiState.title.isNotBlank() && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )
            }
        }
    }
}
