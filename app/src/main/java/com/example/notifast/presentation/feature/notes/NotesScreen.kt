package com.example.notifast.presentation.feature.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifast.data.model.Note
import com.example.notifast.presentation.atoms.NotiFastCircularLoading
import com.example.notifast.presentation.atoms.NotiFastEmptyState
import com.example.notifast.presentation.atoms.NotiFastTopBar
import com.example.notifast.presentation.feature.notes.components.NoteCard
import com.example.notifast.presentation.feature.notes.components.NoteFilterBar
import com.example.notifast.presentation.feature.notes.components.NoteSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    onNoteClick: (Note) -> Unit,
    onAddNoteClick: () -> Unit,
    onSubjectClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { NotiFastTopBar(title = "NotiFast") }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NoteSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onEvent(NotesEvent.Search(it)) }
            )
            NoteFilterBar(
                currentFilter = uiState.currentFilter,
                onFilterChange = { viewModel.onEvent(NotesEvent.SetFilter(it)) }
            )
            if (uiState.isLoading) {
                NotiFastCircularLoading()
            } else if (uiState.notes.isEmpty()) {
                NotiFastEmptyState(message = "No hay apuntes aún")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    uiState.notes.forEach { (subject, notes) ->
                        item {
                            TextButton(onClick = { onSubjectClick(subject) }) {
                                Text(subject, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        items(notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onNoteClick(note) },
                                onDelete = { viewModel.onEvent(NotesEvent.DeleteNote(note)) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
