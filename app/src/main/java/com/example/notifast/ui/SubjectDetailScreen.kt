package com.example.notifast.ui

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
    val notesDisplay by viewModel.filteredNotes.collectAsState()
    val notes = notesDisplay[subjectName] ?: emptyList<Note>()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subjectName.uppercase()) },
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(notes, key = { _, note -> note.id }) { index, note ->
                    // USAMOS EL MISMO DISEÑO ESTÉTICO
                    NoteEstheticCard(
                        note = note,
                        index = index,
                        onEdit = { onEditNote(note) },
                        onDelete = { viewModel.deleteNote(note) }
                    )
                }
            }
        }
    }
}
