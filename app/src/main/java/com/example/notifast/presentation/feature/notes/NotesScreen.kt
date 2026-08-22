package com.example.notifast.presentation.feature.notes

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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.notifast.R
import com.example.notifast.data.model.Note
import com.example.notifast.presentation.atoms.FabAction
import com.example.notifast.presentation.atoms.NotiFastDialog
import com.example.notifast.presentation.atoms.NotiFastExpandableFab
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.atoms.NotiFastTopBar
import com.example.notifast.presentation.atoms.TopBarType
import com.example.notifast.presentation.feature.notes.components.NoteDetailExpandable
import com.example.notifast.presentation.feature.notes.components.NotesTabContent
import com.example.notifast.presentation.feature.notes.components.SubjectsTabContent
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    onAddNoteClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onAddEvaluationClick: (String) -> Unit,
    onSubjectDetailClick: (String) -> Unit = {},
    onEditProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var subjectToDelete by remember { mutableStateOf<String?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var expandedNote by remember { mutableStateOf<Note?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

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
                        onProfileClick = onEditProfile
                    )
                )
            },
            floatingActionButton = {
                NotiFastExpandableFab(
                    actions = listOf(
                        FabAction(
                            label = stringResource(R.string.fab_add_subject),
                            icon = Icons.Default.CreateNewFolder,
                            onClick = onAddSubjectClick,
                            contentDescription = stringResource(R.string.fab_add_subject),
                            color = Aqua
                        ),
                        FabAction(
                            label = stringResource(R.string.fab_add_note),
                            icon = Icons.AutoMirrored.Filled.NoteAdd,
                            onClick = onAddNoteClick,
                            contentDescription = stringResource(R.string.fab_add_note),
                            color = Ruby
                        )
                    ),
                    modifier = Modifier.padding(bottom = 72.dp)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = Ruby,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = Ruby
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = {
                                Text(
                                    stringResource(R.string.notes_tab_notes),
                                    fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = {
                                Text(
                                    stringResource(R.string.notes_tab_subjects),
                                    fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }

                    if (selectedTabIndex == 0) {
                        NotesTabContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            onSubjectClick = onSubjectClick,
                            onNoteClick = onNoteClick,
                            onExpandNote = { expandedNote = it },
                            onDeleteNote = { noteToDelete = it }
                        )
                    } else {
                        SubjectsTabContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            onAddNoteClick = onAddNoteClick,
                            onAddEvaluationClick = onAddEvaluationClick,
                            onNoteClick = onNoteClick,
                            onDeleteSubject = { subjectToDelete = it },
                            onSubjectDetailClick = onSubjectDetailClick
                        )
                    }
                }
            }
        }

        subjectToDelete?.let { name ->
            NotiFastDialog(
                title = stringResource(R.string.delete_subject_title),
                message = stringResource(R.string.delete_subject_message, name),
                confirmText = stringResource(R.string.delete_subject_confirm),
                dismissText = stringResource(R.string.delete_subject_cancel),
                confirmColor = Ruby,
                confirmWeight = FontWeight.Bold,
                onConfirm = {
                    viewModel.onEvent(NotesEvent.DeleteSubject(name))
                    subjectToDelete = null
                },
                onDismiss = { subjectToDelete = null }
            )
        }

        noteToDelete?.let { note ->
            NotiFastDialog(
                title = stringResource(R.string.delete_note_title),
                message = stringResource(R.string.delete_note_message, note.title),
                confirmText = stringResource(R.string.delete_note_confirm),
                dismissText = stringResource(R.string.delete_note_cancel),
                confirmColor = Ruby,
                confirmWeight = FontWeight.Bold,
                onConfirm = {
                    viewModel.onEvent(NotesEvent.DeleteNote(note))
                    noteToDelete = null
                },
                onDismiss = { noteToDelete = null }
            )
        }

        AnimatedVisibility(
            visible = expandedNote != null,
            enter = fadeIn(tween(250)) + scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
            ),
            exit = fadeOut(tween(250)) + scaleOut(
                targetScale = 0.85f,
                animationSpec = spring(stiffness = 300f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                expandedNote?.let { note ->
                    NoteDetailExpandable(
                        note = note,
                        onClose = { expandedNote = null },
                        onEdit = { onNoteClick(note.id); expandedNote = null }
                    )
                }
            }
        }
    }
}
