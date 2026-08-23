package com.notifast.app.presentation.feature.addnote

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.feature.addnote.components.NoteEditorFormFields
import com.notifast.app.presentation.feature.addnote.components.NotePreviewCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteBottomSheet(
    onDismissRequest: () -> Unit,
    onNoteAdded: () -> Unit,
    initialSubject: String = "",
    onNavigateToAddSubject: () -> Unit = {},
    viewModel: AddNoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(initialSubject) {
        if (initialSubject.isNotBlank()) viewModel.setInitialSubject(initialSubject)
    }
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.onEvent(AddNoteEvent.ResetState)
            onNoteAdded()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.add_note_quick_capture_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.add_note_quick_capture_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.btn_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            TabRow(
                selectedTabIndex = if (uiState.isLivePreview) 1 else 0,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[if (uiState.isLivePreview) 1 else 0]), color = MaterialTheme.colorScheme.primary)
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

            Spacer(Modifier.height(10.dp))

            AnimatedContent(
                targetState = uiState.isLivePreview,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "SheetTransition"
            ) { isPreview ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(370.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            onCreateSubjectClick = {
                                onDismissRequest()
                                onNavigateToAddSubject()
                            },
                            contentHeight = 130.dp
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            uiState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            NotiFastButton(
                text = stringResource(R.string.add_note_btn_save_note),
                onClick = { viewModel.onEvent(AddNoteEvent.SaveClicked) },
                isLoading = uiState.isLoading,
                enabled = uiState.subject.isNotBlank() && uiState.title.isNotBlank() && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }
    }
}
