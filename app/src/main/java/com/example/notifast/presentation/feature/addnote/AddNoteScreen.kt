package com.example.notifast.presentation.feature.addnote

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifast.presentation.atoms.NotiFastButton
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.feature.notes.components.SubjectIconMapper
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
    var animateEntrance by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animateEntrance = true
    }

    val selectedSubjectData = uiState.subjects.find { it.name == uiState.subject }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                com.example.notifast.presentation.atoms.NotiFastTopBar(
                    type = com.example.notifast.presentation.atoms.TopBarType.Detail(
                        title = "Nuevo Apunte",
                        onBackClick = onBackClick
                    )
                )
            }
        ) { innerPadding ->
            AnimatedVisibility(
                visible = animateEntrance,
                enter = fadeIn(tween(500)) + slideInVertically(animationSpec = tween(500)) { it / 6 },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    if (uiState.subjects.isEmpty()) {
                        Text(
                            text = "No tienes materias creadas aún. Por favor crea una materia primero.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = uiState.subject,
                                onValueChange = { viewModel.onEvent(AddNoteEvent.SubjectChanged(it)) },
                                label = { Text("Materia") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Ruby,
                                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                                ),
                                leadingIcon = selectedSubjectData?.let { data ->
                                    {
                                        val subjectColor = try {
                                            if (data.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(data.colorHex))
                                            else Ruby
                                        } catch (e: Exception) { Ruby }
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 6.dp)
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(subjectColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = SubjectIconMapper.getIcon(data.icon),
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                uiState.subjects.forEach { subjectData ->
                                    val subjectColor = try {
                                        if (subjectData.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(subjectData.colorHex))
                                        else Ruby
                                    } catch (e: Exception) { Ruby }
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(subjectColor),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = SubjectIconMapper.getIcon(subjectData.icon),
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(Modifier.width(12.dp))
                                                Text(subjectData.name, fontWeight = FontWeight.Medium)
                                            }
                                        },
                                        onClick = {
                                            viewModel.onEvent(AddNoteEvent.SubjectChanged(subjectData.name))
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.onEvent(AddNoteEvent.TitleChanged(it)) },
                        label = { Text("Título del Apunte") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Ruby,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                        )
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = uiState.content,
                        onValueChange = { viewModel.onEvent(AddNoteEvent.ContentChanged(it)) },
                        label = { Text("Escribe tus notas aquí...") },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Ruby,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                        ),
                        singleLine = false
                    )
                    
                    Spacer(Modifier.height(20.dp))
                    
                    NotiFastButton(
                        text = if (uiState.imageUri == null) "ADJUNTAR IMAGEN (OPCIONAL)" else "CAMBIAR IMAGEN ADJUNTADA",
                        onClick = { imagePickerLauncher.launch("image/*") }
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    uiState.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    
                    NotiFastButton(
                        text = "GUARDAR APUNTE",
                        onClick = { viewModel.onEvent(AddNoteEvent.SaveClicked) },
                        isLoading = uiState.isLoading,
                        enabled = uiState.subjects.isNotEmpty()
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}