package com.example.notifast.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notifast.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectScreen(viewModel: NotesViewModel, onSubjectAdded: () -> Unit) {
    var name by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nueva Materia") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la materia (ej: Historia)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addSubject(name)
                        onSubjectAdded()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear Materia")
            }
        }
    }
}
