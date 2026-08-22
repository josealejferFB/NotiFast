package com.notifast.app.presentation.feature.editschedule

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.*
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.data.model.Schedule

import androidx.activity.compose.BackHandler
import com.notifast.app.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleScreen(
    viewModel: EditScheduleViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onScheduleAdded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, "Horario guardado", Toast.LENGTH_SHORT).show()
            onScheduleAdded()
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
            viewModel.onEvent(EditScheduleEvent.ClearError)
        }
    }

    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showDayDropdown by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = uiState.subjectName.isNotBlank() || uiState.startTime.isNotBlank() || uiState.endTime.isNotBlank()
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

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { EditScheduleTopBar(handleBack) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Subject Dropdown
                ExposedDropdownMenuBox(
                    expanded = showSubjectDropdown,
                    onExpandedChange = { showSubjectDropdown = it }
                ) {
                    NotiFastTextField(
                        value = uiState.subjectName,
                        onValueChange = {},
                        label = "Materia",
                        readOnly = true,
                        leadingIcon = Icons.Default.School,
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showSubjectDropdown,
                        onDismissRequest = { showSubjectDropdown = false }
                    ) {
                        uiState.subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.name) },
                                onClick = {
                                    viewModel.onEvent(EditScheduleEvent.UpdateSubjectName(subject.name))
                                    showSubjectDropdown = false
                                }
                            )
                        }
                        if (uiState.subjects.isEmpty()) {
                            DropdownMenuItem(text = { Text("No hay materias") }, onClick = { showSubjectDropdown = false })
                        }
                    }
                }

                // Day of week Dropdown
                ExposedDropdownMenuBox(
                    expanded = showDayDropdown,
                    onExpandedChange = { showDayDropdown = it }
                ) {
                    val dayName = Schedule.DAY_NAMES.getOrElse(uiState.dayOfWeek) { "Día ${uiState.dayOfWeek}" }
                    NotiFastTextField(
                        value = dayName,
                        onValueChange = {},
                        label = "Día de la semana",
                        readOnly = true,
                        leadingIcon = Icons.Default.Schedule,
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showDayDropdown,
                        onDismissRequest = { showDayDropdown = false }
                    ) {
                        Schedule.DAY_NAMES.forEachIndexed { index, day ->
                            if (index > 0) {
                                DropdownMenuItem(
                                    text = { Text(day) },
                                    onClick = {
                                        viewModel.onEvent(EditScheduleEvent.UpdateDayOfWeek(index))
                                        showDayDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    NotiFastTextField(
                        value = uiState.startTime,
                        onValueChange = { viewModel.onEvent(EditScheduleEvent.UpdateStartTime(it)) },
                        label = "Hora Inicio",
                        placeholder = "Ej: 08:00",
                        modifier = Modifier.weight(1f)
                    )
                    NotiFastTextField(
                        value = uiState.endTime,
                        onValueChange = { viewModel.onEvent(EditScheduleEvent.UpdateEndTime(it)) },
                        label = "Hora Fin",
                        placeholder = "Ej: 10:00",
                        modifier = Modifier.weight(1f)
                    )
                }

                NotiFastTextField(
                    value = uiState.classroom,
                    onValueChange = { viewModel.onEvent(EditScheduleEvent.UpdateClassroom(it)) },
                    label = "Salón (Opcional)",
                    leadingIcon = Icons.Default.MeetingRoom
                )

                NotiFastTextField(
                    value = uiState.teacher,
                    onValueChange = { viewModel.onEvent(EditScheduleEvent.UpdateTeacher(it)) },
                    label = "Profesor (Opcional)",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.weight(1f))

                NotiFastButton(
                    text = "Guardar Horario",
                    onClick = { viewModel.onEvent(EditScheduleEvent.SaveSchedule) },
                    isLoading = uiState.isSaving,
                    backgroundColor = Aqua,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )
            }
        }

        if (showDiscardDialog) {
            NotiFastDialog(
                title = "¿Descartar cambios?",
                message = "Tienes cambios sin guardar en este horario. ¿Estás seguro de que deseas salir?",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditScheduleTopBar(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text("Editar Horario", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
    )
}
