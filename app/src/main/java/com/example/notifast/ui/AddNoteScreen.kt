package com.example.notifast.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.notifast.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(viewModel: NotesViewModel, onNoteAdded: () -> Unit) {
    val subjects by viewModel.subjects.collectAsState()
    
    var selectedSubject by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    var expanded by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nueva Nota") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SELECTOR DE MATERIA (DROPDOWN CORREGIDO)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = selectedSubject,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Selecciona una Materia") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (subjects.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Primero crea una materia en el inicio") },
                            onClick = { expanded = false }
                        )
                    } else {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject) },
                                onClick = {
                                    selectedSubject = subject
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título de la nota") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            selectedImageUri?.let { uri ->
                Card(
                    modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 8.dp)
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors() // Corregido el parámetro variant
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Seleccionar Imagen")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenido del apunte") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (selectedSubject.isNotBlank() && title.isNotBlank()) {
                        viewModel.addNote(selectedSubject, title, content, selectedImageUri)
                        onNoteAdded()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedSubject.isNotBlank() && title.isNotBlank()
            ) {
                Text("Guardar Nota")
            }
        }
    }
}
