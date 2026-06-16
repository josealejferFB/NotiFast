package com.example.notifast.presentation.feature.editnote

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifast.presentation.atoms.NotiFastButton
import com.example.notifast.presentation.atoms.NotiFastCircularLoading
import com.example.notifast.presentation.atoms.NotiFastTextField
import com.example.notifast.presentation.atoms.NotiFastCenterAlignedTopBar
import com.example.notifast.presentation.atoms.NotiFastStudyBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    viewModel: EditNoteViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNoteSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.isSaved) { onNoteSaved(); return }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        viewModel.onEvent(EditNoteEvent.ImageSelected(uri))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(containerColor = Color.Transparent, topBar = { NotiFastCenterAlignedTopBar(title = "Editar Apunte") }) { innerPadding ->
        if (uiState.note == null) { NotiFastCircularLoading(); return@Scaffold }
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            NotiFastTextField(value = uiState.subject, onValueChange = { viewModel.onEvent(EditNoteEvent.SubjectChanged(it)) }, label = "Materia", modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            NotiFastTextField(value = uiState.title, onValueChange = { viewModel.onEvent(EditNoteEvent.TitleChanged(it)) }, label = "Título", modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            NotiFastTextField(value = uiState.content, onValueChange = { viewModel.onEvent(EditNoteEvent.ContentChanged(it)) }, label = "Contenido", modifier = Modifier.fillMaxWidth(), singleLine = false)
            Spacer(Modifier.height(12.dp))
            NotiFastButton(text = if (uiState.imageUri == null) "Cambiar Imagen" else "Imagen seleccionada", onClick = { imagePickerLauncher.launch("image/*") })
            Spacer(Modifier.height(16.dp))
            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (uiState.error != null) { Spacer(Modifier.height(8.dp)) }
            NotiFastButton(text = "GUARDAR CAMBIOS", onClick = { viewModel.onEvent(EditNoteEvent.SaveClicked) }, isLoading = uiState.isLoading)
        }
    }
    }
}
