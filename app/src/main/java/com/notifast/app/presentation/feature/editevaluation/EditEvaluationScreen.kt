package com.notifast.app.presentation.feature.editevaluation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Title
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.data.model.EvaluationType
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastDatePickerField
import com.notifast.app.presentation.atoms.NotiFastFormField
import com.notifast.app.presentation.atoms.NotiFastImagePickerField
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import java.util.Date

import androidx.activity.compose.BackHandler
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEvaluationScreen(
    viewModel: EditEvaluationViewModel = hiltViewModel(),
    onEvaluationUpdated: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onEvaluationUpdated() }

    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedDate by remember { mutableStateOf(uiState.date ?: Date()) }
    var expanded by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val evaluationType = EvaluationType.fromName(uiState.type)
    
    var expandedStep by remember { mutableStateOf(1) }

    // Validation
    val isGradeInvalid = uiState.grade.isNotBlank() && (uiState.grade.toDoubleOrNull()?.let { it < com.notifast.app.domain.util.GradeConstants.MIN_GRADE || it > com.notifast.app.domain.util.GradeConstants.MAX_GRADE } ?: true)

    val hasUnsavedChanges = uiState.title.isNotBlank() || selectedImageUri != null
    val handleBack = {
        if (hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(enabled = hasUnsavedChanges) {
        showDiscardDialog = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NotiFastTopBar(
                    type = TopBarType.Detail(
                        title = "Editar ${evaluationType.label}",
                        onBackClick = handleBack
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

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
                                if (uiState.subjectName.isNotBlank() && expandedStep != 1) {
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
                                        value = uiState.subjectName,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Materia") },
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
                                            DropdownMenuItem(
                                                text = { Text(subjectData.name, fontWeight = FontWeight.SemiBold) },
                                                onClick = {
                                                    viewModel.onEvent(EditEvaluationEvent.SelectSubject(subjectData.name))
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = { expandedStep = 2 },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    enabled = uiState.subjectName.isNotBlank(),
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
                            Text(text = uiState.subjectName.ifBlank { "Sin seleccionar" }, color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
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
                                Icon(Icons.Default.Title, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("2. Detalles Principales", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                if (uiState.title.isNotBlank() && !isGradeInvalid && expandedStep != 2) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        AnimatedVisibility(visible = expandedStep == 2) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
                                NotiFastFormField(
                                    value = uiState.title,
                                    onValueChange = { viewModel.onEvent(EditEvaluationEvent.UpdateTitle(it)) },
                                    label = "Título de la Evaluación",
                                    leadingIcon = Icons.Default.Title
                                )

                                if (evaluationType.showGrade) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        NotiFastFormField(
                                            value = uiState.grade,
                                            onValueChange = { viewModel.onEvent(EditEvaluationEvent.UpdateGrade(it)) },
                                            label = "Nota (1-10)",
                                            leadingIcon = Icons.Default.Grade,
                                            cursorColor = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.weight(1f),
                                            isError = isGradeInvalid,
                                            supportingText = if (isGradeInvalid) {
                                                { Text("La nota debe estar entre 1 y 10", color = MaterialTheme.colorScheme.error) }
                                            } else null
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .clickable { viewModel.onEvent(EditEvaluationEvent.TogglePresented) }
                                        ) {
                                            Switch(
                                                checked = uiState.isPresented,
                                                onCheckedChange = { viewModel.onEvent(EditEvaluationEvent.TogglePresented) },
                                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.secondary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                if (uiState.isPresented) "Presentado" else "Pendiente",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (uiState.isPresented) MaterialTheme.colorScheme.secondary else Color.Gray
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = { expandedStep = 3 },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    enabled = uiState.title.isNotBlank() && !isGradeInvalid,
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

                // --- PASO 3 ---
                NotiFastCard(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { expandedStep = 3 },
                    contentPadding = 0.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Surface(color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("3. Opcionales", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            }
                        }

                        AnimatedVisibility(visible = expandedStep == 3) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
                                NotiFastDatePickerField(
                                    date = selectedDate,
                                    onDateSelected = { selectedDate = it },
                                    label = "Fecha de Evaluación"
                                )

                                NotiFastImagePickerField(
                                    existingImageUrl = uiState.existingImageUrl,
                                    placeholderText = "Cambiar imagen de referencia",
                                    onImageSelected = { selectedImageUri = it }
                                )

                                NotiFastFormField(
                                    value = uiState.description,
                                    onValueChange = { viewModel.onEvent(EditEvaluationEvent.UpdateDescription(it)) },
                                    label = "Descripción",
                                    height = 100.dp
                                )

                                NotiFastFormField(
                                    value = uiState.quickTips,
                                    onValueChange = { viewModel.onEvent(EditEvaluationEvent.UpdateQuickTips(it)) },
                                    label = "Tips / Apuntes Rápidos",
                                    leadingIcon = Icons.Default.Lightbulb,
                                    cursorColor = MaterialTheme.colorScheme.secondary
                                )

                                if (evaluationType.showWeight) {
                                    NotiFastFormField(
                                        value = uiState.weight,
                                        onValueChange = { viewModel.onEvent(EditEvaluationEvent.UpdateWeight(it)) },
                                        label = "Peso / Porcentaje (opcional)",
                                        leadingIcon = Icons.Default.Scale,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        cursorColor = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (uiState.title.isNotBlank() && !isGradeInvalid) {
                                            viewModel.onEvent(EditEvaluationEvent.Save(selectedDate, selectedImageUri))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(60.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        disabledContentColor = Color.LightGray
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                    enabled = uiState.title.isNotBlank() && !isGradeInvalid && !uiState.isSaving
                                ) {
                                    Text("ACTUALIZAR EVALUACIÓN", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showDiscardDialog) {
            NotiFastDialog(
                title = "¿Descartar cambios?",
                message = "Tienes cambios sin guardar en esta evaluación. ¿Estás seguro de que deseas salir?",
                confirmText = "Descartar",
                dismissText = "Continuar editando",
                confirmColor = Ruby,
                confirmWeight = FontWeight.Bold,
                onConfirm = {
                    showDiscardDialog = false
                    onBack()
                },
                onDismiss = { showDiscardDialog = false }
            )
        }
    }
}
