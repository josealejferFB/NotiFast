package com.notifast.app.presentation.feature.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.data.model.Note
import com.notifast.app.domain.util.MarkdownToBlocksParser
import com.notifast.app.presentation.atoms.FabAction
import com.notifast.app.presentation.atoms.MarkdownParser
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastExpandableFab
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.notes.components.NoteDetailExpandable
import com.notifast.app.presentation.feature.notes.components.NotesTabContent
import com.notifast.app.presentation.feature.notes.components.SubjectGridBento
import java.util.Date

@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    onAddNoteClick: (String?) -> Unit = {},
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onAddEvaluationClick: (String) -> Unit,
    onSubjectDetailClick: (String) -> Unit = {},
    onEditProfile: () -> Unit,
    onSearchClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var subjectToDelete by remember { mutableStateOf<String?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var expandedNote by remember { mutableStateOf<Note?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    var showAddSubjectSheet by remember { mutableStateOf(false) }
    var showImportSubjectSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NotiFastTopBar(
                    type = TopBarType.Home(
                        firstName = uiState.userFirstName,
                        email = uiState.userEmail,
                        profilePictureUrl = uiState.userProfilePictureUrl,
                        onProfileClick = onEditProfile,
                        onSearchClick = onSearchClick
                    )
                )
            },
            floatingActionButton = {
                NotiFastExpandableFab(
                    actions = listOf(
                        FabAction(stringResource(R.string.fab_add_subject), Icons.Default.CreateNewFolder, { showAddSubjectSheet = true }, stringResource(R.string.fab_add_subject), MaterialTheme.colorScheme.secondary),
                        FabAction(stringResource(R.string.import_subject_title), androidx.compose.material.icons.Icons.Default.QrCode2, { showImportSubjectSheet = true }, stringResource(R.string.import_subject_title), MaterialTheme.colorScheme.tertiary),
                        FabAction(stringResource(R.string.fab_add_note), Icons.AutoMirrored.Filled.NoteAdd, { onAddNoteClick(null) }, stringResource(R.string.fab_add_note), MaterialTheme.colorScheme.primary)
                    ),
                    modifier = Modifier.padding(bottom = 72.dp)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), color = MaterialTheme.colorScheme.primary)
                        }
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text(stringResource(R.string.notes_tab_notes), fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium) }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text(stringResource(R.string.notes_tab_subjects), fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium) }
                        )
                    }

                    if (selectedTabIndex == 0) {
                        NotesTabContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            onAddNoteClick = onAddNoteClick,
                            onNoteClick = onNoteClick,
                            onExpandNote = { expandedNote = it },
                            onDeleteNote = { noteToDelete = it },
                            onAddSubjectClick = { showAddSubjectSheet = true }
                        )
                    } else {
                        SubjectGridBento(
                            subjectInfos = uiState.subjectInfos,
                            onSubjectClick = onSubjectClick,
                            onAddNoteClick = { onAddNoteClick(it) },
                            onAddEvaluationClick = onAddEvaluationClick,
                            onDeleteSubject = { subjectToDelete = it },
                            onAddSubjectClick = { showAddSubjectSheet = true },
                            displayScale = uiState.gradeScale,
                            minPassingGrade = uiState.minPassingGrade
                        )
                    }
                }
            }
        }

        if (showAddSubjectSheet) {
            com.notifast.app.presentation.feature.addsubject.AddSubjectBottomSheet(
                onDismissRequest = { showAddSubjectSheet = false },
                onSubjectCreated = { name ->
                    showAddSubjectSheet = false
                    viewModel.onEvent(NotesEvent.LoadNotes)
                    onSubjectClick(name)
                }
            )
        }

        subjectToDelete?.let { name ->
            NotiFastDialog(
                title = stringResource(R.string.delete_subject_title),
                message = stringResource(R.string.delete_subject_message, name),
                confirmText = stringResource(R.string.delete_subject_confirm),
                dismissText = stringResource(R.string.delete_subject_cancel),
                confirmColor = MaterialTheme.colorScheme.error,
                confirmWeight = FontWeight.Bold,
                onConfirm = { viewModel.onEvent(NotesEvent.DeleteSubject(name)); subjectToDelete = null },
                onDismiss = { subjectToDelete = null }
            )
        }

        noteToDelete?.let { note ->
            NotiFastDialog(
                title = stringResource(R.string.delete_note_title),
                message = stringResource(R.string.delete_note_message, note.title),
                confirmText = stringResource(R.string.delete_note_confirm),
                dismissText = stringResource(R.string.delete_note_cancel),
                confirmColor = MaterialTheme.colorScheme.error,
                confirmWeight = FontWeight.Bold,
                onConfirm = { viewModel.onEvent(NotesEvent.DeleteNote(note)); noteToDelete = null },
                onDismiss = { noteToDelete = null }
            )
        }

        AnimatedVisibility(
            visible = expandedNote != null,
            enter = fadeIn(tween(250)) + scaleIn(animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f), initialScale = 0.85f),
            exit = fadeOut(tween(250)) + scaleOut(animationSpec = spring(stiffness = 300f), targetScale = 0.85f)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 40.dp), contentAlignment = Alignment.Center) {
                expandedNote?.let { note ->
                    NoteDetailExpandable(
                        note = note,
                        onClose = { expandedNote = null },
                        onEdit = {
                            onNoteClick(note.id)
                            expandedNote = null
                        },
                        onTaskCheckedChange = { taskIndex, isChecked ->
                            val updatedContent = MarkdownParser.toggleTaskCheckbox(note.content, taskIndex, isChecked)
                            val updatedNote = note.copy(
                                content = updatedContent,
                                updatedAt = Date(),
                                blocks = MarkdownToBlocksParser.parse(updatedContent, note.imageUrl)
                            )
                            expandedNote = updatedNote
                            viewModel.onEvent(NotesEvent.ToggleTaskCheck(note, taskIndex, isChecked))
                        }
                    )
                }
            }
        }

        if (showImportSubjectSheet) {
            com.notifast.app.presentation.feature.share.ImportSubjectBottomSheet(
                userId = uiState.userId,
                importUseCase = viewModel.importSubjectPackageUseCase,
                onDismiss = { showImportSubjectSheet = false },
                onSubjectImported = { importedName ->
                    viewModel.onEvent(NotesEvent.LoadNotes)
                    onSubjectClick(importedName)
                }
            )
        }
    }
}

