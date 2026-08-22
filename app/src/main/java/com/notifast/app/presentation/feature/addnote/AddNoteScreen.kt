package com.notifast.app.presentation.feature.addnote

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastFormField
import com.notifast.app.presentation.atoms.NotiFastImagePickerField
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.notes.components.SubjectIconMapper
import com.notifast.app.presentation.theme.color.Ruby

import androidx.activity.compose.BackHandler
import com.notifast.app.presentation.atoms.NotiFastDialog

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
    var expandedStep by remember { mutableStateOf(1) }
    var animateEntrance by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = uiState.title.isNotBlank() || uiState.content.isNotBlank() || uiState.imageUri != null
    val handleBack = {
        if (hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onBackClick()
        }
    }

    BackHandler(enabled = hasUnsavedChanges) {
        showDiscardDialog = true
    }

    LaunchedEffect(Unit) {
        animateEntrance = true
    }

    val selectedSubjectData = uiState.subjects.find { it.name == uiState.subject }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NotiFastTopBar(
                    type = TopBarType.Detail(
                        title = "Nuevo Apunte",
                        onBackClick = handleBack
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
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.height(8.dp))

                    if (uiState.subjects.isEmpty()) {
                        Text(
                            text = "No tienes materias creadas aún. Por favor crea una materia primero.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // --- PASO 1 ---
                        NotiFastCard(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { expandedStep = 1 },
                            contentPadding = 0.dp
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                        Icon(Icons.Default.Category, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("1. Materia", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.weight(1f))
                                        if (uiState.subject.isNotBlank() && expandedStep != 1) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }

                                AnimatedVisibility(visible = expandedStep == 1) {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
                                        ExposedDropdownMenuBox(
                                            expanded = expanded,
                                            onExpandedChange = { expanded = !expanded },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            TextField(
                                                value = uiState.subject,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Seleccionar Materia") },
                                                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                                shape = RoundedCornerShape(20.dp),
                                                colors = ExposedDropdownMenuDefaults.textFieldColors(
                                                    focusedContainerColor = Color(0xFFF5F5F5),
                                                    unfocusedContainerColor = Color(0xFFF5F5F5),
                                                    focusedIndicatorColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent
                                                ),
                                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                                            )

                                            ExposedDropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false },
                                                modifier = Modifier.background(Color.White, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp))
                                            ) {
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

                                        Button(
                                            onClick = { expandedStep = 2 },
                                            modifier = Modifier.fillMaxWidth().height(50.dp),
                                            enabled = uiState.subject.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                disabledContentColor = Color.LightGray
                                            )
                                        ) {
                                            Text("Siguiente", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                AnimatedVisibility(visible = expandedStep != 1) {
                                    Text(
                                        text = uiState.subject.ifBlank { "Sin seleccionar" },
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }

                        // --- PASO 2 ---
                        NotiFastCard(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { expandedStep = 2 },
                            contentPadding = 0.dp
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("2. Contenido del Apunte", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                    }
                                }

                                AnimatedVisibility(visible = expandedStep == 2) {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
                                        NotiFastFormField(
                                            value = uiState.title,
                                            onValueChange = { viewModel.onEvent(AddNoteEvent.TitleChanged(it)) },
                                            label = "Título del Apunte"
                                        )

                                        NotiFastFormField(
                                            value = uiState.content,
                                            onValueChange = { viewModel.onEvent(AddNoteEvent.ContentChanged(it)) },
                                            label = "Escribe tus notas aquí...",
                                            height = 180.dp,
                                            singleLine = false
                                        )

                                        NotiFastImagePickerField(
                                            placeholderText = if (uiState.imageUri == null) "Añadir foto de referencia" else "Cambiar foto adjuntada",
                                            onImageSelected = { viewModel.onEvent(AddNoteEvent.ImageSelected(it)) }
                                        )

                                        uiState.error?.let {
                                            Text(
                                                text = it,
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.padding(bottom = 12.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = { viewModel.onEvent(AddNoteEvent.SaveClicked) },
                                            modifier = Modifier.fillMaxWidth().height(60.dp),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                disabledContentColor = Color.LightGray
                                            ),
                                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                            enabled = uiState.subject.isNotBlank() && uiState.title.isNotBlank() && !uiState.isLoading
                                        ) {
                                            Text("GUARDAR APUNTE", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                        }
                                    }
                                }
                                
                                AnimatedVisibility(visible = expandedStep != 2) {
                                    Text(
                                        text = if (uiState.title.isNotBlank()) uiState.title else "Sin título",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }

        if (showDiscardDialog) {
            NotiFastDialog(
                title = "¿Descartar cambios?",
                message = "Tienes cambios sin guardar en este apunte. ¿Estás seguro de que deseas salir?",
                confirmText = "Descartar",
                dismissText = "Continuar editando",
                confirmColor = Ruby,
                confirmWeight = FontWeight.Bold,
                onConfirm = {
                    showDiscardDialog = false
                    onBackClick()
                },
                onDismiss = { showDiscardDialog = false }
            )
        }
    }
}