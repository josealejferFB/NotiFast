package com.notifast.app.presentation.feature.noteeditor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.clearFocusOnTapOutside
import com.notifast.app.presentation.atoms.rememberClearFocusAction
import com.notifast.app.presentation.feature.noteeditor.components.DockedAccessoryToolbar
import com.notifast.app.presentation.feature.noteeditor.components.NoteEditorDiscardDialog
import com.notifast.app.presentation.feature.noteeditor.components.NoteEditorSheetHeader
import com.notifast.app.presentation.feature.noteeditor.util.SmartMarkdownFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun NoteEditorBottomSheet(
    noteId: String?,
    subject: String?,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onCreateSubject: () -> Unit = {},
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isImeVisible = WindowInsets.isImeVisible

    val hasUnsavedChanges = uiState.title.isNotBlank() || uiState.content.isNotBlank() || uiState.imageUri != null
    var showDiscardDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newState ->
            if (hasUnsavedChanges && newState == SheetValue.Hidden) {
                showDiscardDialog = true
                false
            } else {
                true
            }
        }
    )

    LaunchedEffect(noteId, subject) {
        viewModel.onEvent(NoteEditorEvent.Init(noteId, subject))
    }

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

    fun dismissSheet(action: (() -> Unit)? = null) {
        scope.launch {
            runCatching { sheetState.hide() }
            if (action != null) action() else onDismiss()
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            dismissSheet(action = onSaved)
        }
    }

    BackHandler(enabled = true) {
        when {
            isImeVisible -> {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
            hasUnsavedChanges -> showDiscardDialog = true
            else -> dismissSheet()
        }
    }

    if (showDiscardDialog) {
        NoteEditorDiscardDialog(
            onConfirm = {
                showDiscardDialog = false
                dismissSheet()
            },
            onDismiss = {
                showDiscardDialog = false
            }
        )
    }

    val handleCreateSubject = {
        keyboardController?.hide()
        focusManager.clearFocus()
        onCreateSubject()
    }

    val canSave = uiState.subject.isNotBlank() && uiState.title.isNotBlank() && !uiState.isLoading

    ModalBottomSheet(
        onDismissRequest = {
            if (hasUnsavedChanges) showDiscardDialog = true else dismissSheet()
        },
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = false
        ),
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        contentWindowInsets = { WindowInsets.statusBars }
    ) {
        val clearFocusAndKeyboard = rememberClearFocusAction()

        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight().clearFocusOnTapOutside()) {
            NoteEditorSheetHeader(
                modifier = Modifier.clearFocusOnTapOutside(),
                isEditing = noteId != null,
                isPreviewMode = uiState.isPreviewMode,
                canSave = canSave,
                isLoading = uiState.isLoading,
                onCloseClick = {
                    clearFocusAndKeyboard()
                    if (hasUnsavedChanges) showDiscardDialog = true else dismissSheet()
                },
                onTogglePreview = { viewModel.onEvent(NoteEditorEvent.TogglePreview(!uiState.isPreviewMode)) },
                onSaveClick = { viewModel.onEvent(NoteEditorEvent.SaveClicked) }
            )

            // Scrollable Editor Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clearFocusOnTapOutside()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                if (!uiState.error.isNullOrBlank()) {
                    com.notifast.app.presentation.atoms.NotiFastAlertBanner(
                        message = uiState.error ?: "",
                        onDismiss = { viewModel.onEvent(NoteEditorEvent.ClearError) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

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
                    onTaskCheckToggled = { i, c -> viewModel.onEvent(NoteEditorEvent.TaskCheckToggled(i, c)) },
                    onImageSelected = { viewModel.onEvent(NoteEditorEvent.ImageSelected(it)) },
                    onCreateSubjectClick = handleCreateSubject,
                    bringIntoViewRequester = bringIntoViewRequester,
                    onTapOutside = clearFocusAndKeyboard
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Docked accessory toolbar when in edit mode
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
                        .navigationBarsPadding()
                        .imePadding()
                )
            } else {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}
