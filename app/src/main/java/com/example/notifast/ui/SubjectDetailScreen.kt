package com.example.notifast.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notifast.model.Note
import com.example.notifast.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectName: String,
    viewModel: NotesViewModel,
    onEditNote: (Note) -> Unit,
    onBack: () -> Unit
) {
    // CORRECCIÓN: Usamos el flujo que siempre está agrupado por materias
    val subjectsMap by viewModel.allNotesGroupedBySubject.collectAsState()
    
    // Obtenemos las notas de esta materia específica del mapa confiable
    val notes = subjectsMap[subjectName] ?: emptyList<Note>()
    
    var expandedNote by remember { mutableStateOf<Note?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(subjectName.uppercase(), style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding), 
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay notas en esta materia todavía.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(notes, key = { _, note -> note.id }) { index, note ->
                        NoteEstheticCard(
                            note = note,
                            index = index,
                            onClick = { expandedNote = note },
                            onEdit = { onEditNote(note) },
                            onDelete = { viewModel.deleteNote(note) }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = expandedNote != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            expandedNote?.let { note ->
                NoteDetailOverlay(
                    note = note,
                    onClose = { expandedNote = null }
                )
            }
        }
    }
}
