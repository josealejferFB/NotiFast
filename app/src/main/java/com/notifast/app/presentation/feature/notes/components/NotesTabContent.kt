package com.notifast.app.presentation.feature.notes.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.Note
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import com.notifast.app.presentation.atoms.NotiFastListSkeleton
import com.notifast.app.presentation.feature.notes.NotesEvent
import com.notifast.app.presentation.feature.notes.NotesUiState
import com.notifast.app.presentation.feature.notes.NotesViewModel

@Composable
fun NotesTabContent(
    uiState: NotesUiState,
    viewModel: NotesViewModel,
    onAddNoteClick: (String?) -> Unit,
    onNoteClick: (String) -> Unit,
    onExpandNote: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onAddSubjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectColorMap = uiState.subjectInfos.associate { it.name to it.colorHex }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            NotesDashboardHero(
                nextActivity = uiState.nextActivity,
                totalNotes = uiState.totalNotesCount,
                totalSubjects = uiState.totalSubjectsCount,
                globalAverage = uiState.globalAverage,
                onAddNoteClick = onAddNoteClick,
                displayScale = uiState.gradeScale,
                minPassingGrade = uiState.minPassingGrade
            )
        }

        item {
            NotesFilterBar(
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.onEvent(NotesEvent.Search(it)) },
                availableTags = uiState.availableTags,
                selectedTag = uiState.selectedTag,
                onTagSelect = { viewModel.onEvent(NotesEvent.SelectTag(it)) }
            )
        }

        if (uiState.isLoading) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    NotiFastListSkeleton(itemCount = 3)
                }
            }
        } else if (uiState.notes.isEmpty()) {
            item {
                val isFiltering = uiState.searchQuery.isNotEmpty() || uiState.selectedTag != null
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NotiFastEmptyState(
                        title = stringResource(if (isFiltering) R.string.notes_no_matches else R.string.notes_empty_title),
                        message = stringResource(if (isFiltering) R.string.notes_no_tags_found else R.string.notes_empty_message),
                        actionLabel = if (isFiltering) null else stringResource(R.string.notes_add_button),
                        onActionClick = if (isFiltering) null else { { onAddNoteClick(null) } }
                    )
                }
            }
        } else {
            uiState.notes.forEach { (subjectName, notesInSubject) ->
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = subjectName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                itemsIndexed(notesInSubject, key = { _, note -> note.id }) { index, note ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        NoteEstheticCard(
                            note = note,
                            isEven = index % 2 == 0,
                            onClick = { onExpandNote(note) },
                            onEdit = { onNoteClick(note.id) },
                            onDelete = { onDeleteNote(note) },
                            subjectColorHex = subjectColorMap[note.subject].orEmpty()
                        )
                    }
                }
            }
        }
    }
}
