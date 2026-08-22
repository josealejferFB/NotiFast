package com.notifast.app.presentation.feature.notes.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import com.notifast.app.presentation.feature.notes.NotesEvent
import com.notifast.app.presentation.feature.notes.NotesUiState
import com.notifast.app.presentation.feature.notes.NotesViewModel

@Composable
fun SubjectsTabContent(
    uiState: NotesUiState,
    viewModel: NotesViewModel,
    onAddNoteClick: () -> Unit,
    onAddEvaluationClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onDeleteSubject: (String) -> Unit,
    onSubjectDetailClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.subjectInfos.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp)) {
                    NotiFastEmptyState(
                        icon = Icons.Default.CreateNewFolder,
                        title = stringResource(R.string.subjects_empty_title),
                        message = stringResource(R.string.subjects_empty_message)
                    )
                }
            }
        } else {
            items(uiState.subjectInfos, key = { it.name }) { info ->
                Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)) {
                    SubjectCard(
                        info = info,
                        isExpanded = uiState.expandedSubject == info.name,
                        onToggle = { viewModel.onEvent(NotesEvent.ToggleSubject(info.name)) },
                        onAddNote = { onAddNoteClick() },
                        onAddEvaluation = { subject -> onAddEvaluationClick(subject) },
                        onNoteClick = { note -> onNoteClick(note.id) },
                        onDeleteSubject = { onDeleteSubject(info.name) },
                        onSubjectDetail = { onSubjectDetailClick(info.name) }
                    )
                }
            }
        }
    }
}
