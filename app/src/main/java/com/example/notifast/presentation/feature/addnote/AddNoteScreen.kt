package com.example.notifast.presentation.feature.addnote

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifast.presentation.atoms.NotiFastButton
import com.example.notifast.presentation.atoms.NotiFastTextField
import com.example.notifast.presentation.atoms.NotiFastTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    viewModel: AddNoteViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNoteAdded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.isSaved) { onNoteAdded(); return }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        viewModel.onEvent(AddNoteEvent.ImageSelected(uri))
    }

    Scaffold(topBar = { NotiFastTopBar(title = "Nuevo Apunte", onBackClick = onBackClick) }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            NotiFastTextField(value = uiState.subject, onValueChange = { viewModel.onEvent(AddNoteEvent.SubjectChanged(it)) }, label = "Materia", modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            NotiFastTextField(value = uiState.title, onValueChange = { viewModel.onEvent(AddNoteEvent.TitleChanged(it)) }, label = "Título", modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            NotiFastTextField(value = uiState.content, onValueChange = { viewModel.onEvent(AddNoteEvent.ContentChanged(it)) }, label = "Contenido", modifier = Modifier.fillMaxWidth(), singleLine = false)
            Spacer(Modifier.height(12.dp))
            NotiFastButton(text = if (uiState.imageUri == null) "Agregar Imagen" else "Imagen seleccionada", onClick = { imagePickerLauncher.launch("image/*") })
            Spacer(Modifier.height(16.dp))
            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (uiState.error != null) { Spacer(Modifier.height(8.dp)) }
            NotiFastButton(text = "GUARDAR APUNTE", onClick = { viewModel.onEvent(AddNoteEvent.SaveClicked) }, isLoading = uiState.isLoading)
        }
    }
}
