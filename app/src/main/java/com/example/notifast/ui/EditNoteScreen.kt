package com.example.notifast.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notifast.model.Note
import com.example.notifast.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    note: Note, 
    viewModel: NotesViewModel, 
    onNoteUpdated: () -> Unit,
    onBack: () -> Unit
) {
    var subject by remember { mutableStateOf(note.subject) }
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Nota") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Materia") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenido") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = onBack) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        if (subject.isNotBlank() && title.isNotBlank()) {
                            viewModel.updateNote(note.id, subject, title, content)
                            onNoteUpdated()
                        }
                    }
                ) {
                    Text("Guardar Cambios")
                }
            }
        }
    }
}
