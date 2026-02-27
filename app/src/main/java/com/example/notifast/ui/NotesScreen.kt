package com.example.notifast.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notifast.model.Note
import com.example.notifast.viewmodel.AuthViewModel
import com.example.notifast.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notesViewModel: NotesViewModel,
    authViewModel: AuthViewModel,
    onAddNote: () -> Unit,
    onEditNote: (Note) -> Unit, // Nueva función para editar
    onLogout: () -> Unit
) {
    val notes by notesViewModel.notes.collectAsState()
    val syncMessage by notesViewModel.syncMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
        }
    }

    LaunchedEffect(Unit) {
        notesViewModel.loadNotes()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mis Notas") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNote) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Nota")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(notes, key = { it.id }) { note ->
                Card(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(note.title) },
                        supportingContent = { Text(note.content) },
                        trailingContent = {
                            Row {
                                // BOTÓN DE EDITAR
                                IconButton(onClick = { onEditNote(note) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar Nota", tint = MaterialTheme.colorScheme.primary)
                                }
                                // BOTÓN DE BORRAR
                                IconButton(onClick = { notesViewModel.deleteNote(note.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Borrar Nota", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
