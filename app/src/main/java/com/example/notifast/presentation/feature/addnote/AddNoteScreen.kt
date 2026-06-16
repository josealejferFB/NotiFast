package com.example.notifast.presentation.feature.addnote

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifast.presentation.atoms.NotiFastButton
import com.example.notifast.presentation.atoms.NotiFastCenterAlignedTopBar
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    viewModel: AddNoteViewModel = hiltViewModel(),
    subject: String = "",
    onBackClick: () -> Unit,
    onNoteAdded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.isSaved) { onNoteAdded(); return }

    LaunchedEffect(subject) { viewModel.setInitialSubject(subject) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        viewModel.onEvent(AddNoteEvent.ImageSelected(uri))
    }

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(containerColor = Color.Transparent, topBar = { NotiFastCenterAlignedTopBar(title = "Nuevo Apunte") }) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
                if (uiState.subjects.isEmpty()) {
                    Text(
                        "No hay materias. Crea una primero.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = uiState.subject,
                            onValueChange = { viewModel.onEvent(AddNoteEvent.SubjectChanged(it)) },
                            label = { Text("Materia") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ruby),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            uiState.subjects.forEach { subjectData ->
                                DropdownMenuItem(
                                    text = { Text("${subjectData.icon}  ${subjectData.name}") },
                                    onClick = {
                                        viewModel.onEvent(AddNoteEvent.SubjectChanged(subjectData.name))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(AddNoteEvent.TitleChanged(it)) },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ruby)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.content,
                    onValueChange = { viewModel.onEvent(AddNoteEvent.ContentChanged(it)) },
                    label = { Text("Contenido") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ruby),
                    singleLine = false
                )
                Spacer(Modifier.height(12.dp))
                NotiFastButton(text = if (uiState.imageUri == null) "Agregar Imagen" else "Imagen seleccionada", onClick = { imagePickerLauncher.launch("image/*") })
                Spacer(Modifier.height(16.dp))
                uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (uiState.error != null) { Spacer(Modifier.height(8.dp)) }
                NotiFastButton(
                    text = "GUARDAR APUNTE",
                    onClick = { viewModel.onEvent(AddNoteEvent.SaveClicked) },
                    isLoading = uiState.isLoading,
                    enabled = uiState.subjects.isNotEmpty()
                )
            }
        }
    }
}