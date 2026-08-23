package com.notifast.app.presentation.feature.addnote

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.addnote.components.NoteEditorFormFields
import com.notifast.app.presentation.feature.addnote.components.NotePreviewCard
import com.notifast.app.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    viewModel: AddNoteViewModel = hiltViewModel(),
    subject: String = "",
    onBackClick: () -> Unit,
    onNoteAdded: () -> Unit,
    onNavigateToAddSubject: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.isSaved) { onNoteAdded(); return }

    LaunchedEffect(subject) { viewModel.setInitialSubject(subject) }

    var showDiscardDialog by remember { mutableStateOf(false) }
    val hasUnsavedChanges = uiState.title.isNotBlank() || uiState.content.isNotBlank() || uiState.imageUri != null
    val handleBack = { if (hasUnsavedChanges) showDiscardDialog = true else onBackClick() }

    BackHandler(enabled = hasUnsavedChanges) { showDiscardDialog = true }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    NotiFastTopBar(
                        type = TopBarType.Detail(
                            title = stringResource(R.string.fab_add_note),
                            onBackClick = handleBack
                        )
                    )
                    TabRow(
                        selectedTabIndex = if (uiState.isLivePreview) 1 else 0,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[if (uiState.isLivePreview) 1 else 0]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        Tab(
                            selected = !uiState.isLivePreview,
                            onClick = { viewModel.onEvent(AddNoteEvent.ToggleLivePreview(false)) },
                            text = { Text(stringResource(R.string.notes_tab_edit), fontWeight = if (!uiState.isLivePreview) FontWeight.Bold else FontWeight.Medium) }
                        )
                        Tab(
                            selected = uiState.isLivePreview,
                            onClick = { viewModel.onEvent(AddNoteEvent.ToggleLivePreview(true)) },
                            text = { Text(stringResource(R.string.notes_tab_preview), fontWeight = if (uiState.isLivePreview) FontWeight.Bold else FontWeight.Medium) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = uiState.isLivePreview,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenEditPreview",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) { isPreview ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isPreview) {
                        NotePreviewCard(
                            subject = uiState.subject,
                            title = uiState.title,
                            content = uiState.content
                        )
                    } else {
                        NoteEditorFormFields(
                            subjects = uiState.subjects,
                            selectedSubject = uiState.subject,
                            title = uiState.title,
                            content = uiState.content,
                            tags = uiState.tags,
                            imageUri = uiState.imageUri,
                            onSubjectSelected = { viewModel.onEvent(AddNoteEvent.SubjectChanged(it)) },
                            onTitleChanged = { viewModel.onEvent(AddNoteEvent.TitleChanged(it)) },
                            onContentChanged = { viewModel.onEvent(AddNoteEvent.ContentChanged(it)) },
                            onTagToggled = { viewModel.onEvent(AddNoteEvent.ToggleTag(it)) },
                            onAddCustomTag = { viewModel.onEvent(AddNoteEvent.AddTag(it)) },
                            onRemoveTag = { viewModel.onEvent(AddNoteEvent.RemoveTag(it)) },
                            onImageSelected = { viewModel.onEvent(AddNoteEvent.ImageSelected(it)) },
                            onCreateSubjectClick = onNavigateToAddSubject,
                            contentHeight = 200.dp
                        )

                        Spacer(Modifier.height(8.dp))

                        NotiFastButton(
                            text = stringResource(R.string.add_note_btn_save_note),
                            onClick = { viewModel.onEvent(AddNoteEvent.SaveClicked) },
                            isLoading = uiState.isLoading,
                            enabled = uiState.subject.isNotBlank() && uiState.title.isNotBlank() && !uiState.isLoading,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                        )
                    }
                }
            }
        }

        if (showDiscardDialog) {
            NotiFastDialog(
                title = stringResource(R.string.notes_discard_title),
                message = stringResource(R.string.notes_discard_message),
                confirmText = stringResource(R.string.schedules_discard_confirm),
                dismissText = stringResource(R.string.schedules_discard_cancel),
                confirmColor = Ruby,
                confirmWeight = FontWeight.Bold,
                onConfirm = { showDiscardDialog = false; onBackClick() },
                onDismiss = { showDiscardDialog = false }
            )
        }
    }
}