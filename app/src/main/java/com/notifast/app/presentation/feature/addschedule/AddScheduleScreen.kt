package com.notifast.app.presentation.feature.addschedule

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.data.model.Schedule
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTextField
import com.notifast.app.presentation.feature.schedules.components.ScheduleConflictBanner
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(
    viewModel: AddScheduleViewModel = hiltViewModel(),
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
            viewModel.onEvent(AddScheduleEvent.ClearError)
        }
    }

    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showDayDropdown by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = uiState.subjectName.isNotBlank() || uiState.startTime.isNotBlank() || uiState.endTime.isNotBlank() || uiState.classroom.isNotBlank() || uiState.teacher.isNotBlank()
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
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.schedules_add_title), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = handleBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Conflict Banner
                ScheduleConflictBanner(conflicts = uiState.conflictingSchedules)

                // Subject Dropdown
                ExposedDropdownMenuBox(
                    expanded = showSubjectDropdown,
                    onExpandedChange = { showSubjectDropdown = it }
                ) {
                    NotiFastTextField(
                        value = uiState.subjectName,
                        onValueChange = {},
                        label = stringResource(R.string.schedules_field_subject),
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
                                    viewModel.onEvent(AddScheduleEvent.UpdateSubjectName(subject.name))
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
                        label = stringResource(R.string.schedules_field_day),
                        readOnly = true,
                        leadingIcon = Icons.Default.Schedule,
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showDayDropdown,
                        onDismissRequest = { showDayDropdown = false }
                    ) {
                        Schedule.DAY_NAMES.forEachIndexed { index, day ->
                            DropdownMenuItem(
                                text = { Text(day) },
                                onClick = {
                                    viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(index))
                                    showDayDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    NotiFastTextField(
                        value = uiState.startTime,
                        onValueChange = { viewModel.onEvent(AddScheduleEvent.UpdateStartTime(it)) },
                        label = stringResource(R.string.schedules_field_start_time),
                        placeholder = "Ej: 08:00",
                        modifier = Modifier.weight(1f)
                    )
                    NotiFastTextField(
                        value = uiState.endTime,
                        onValueChange = { viewModel.onEvent(AddScheduleEvent.UpdateEndTime(it)) },
                        label = stringResource(R.string.schedules_field_end_time),
                        placeholder = "Ej: 10:00",
                        modifier = Modifier.weight(1f)
                    )
                }

                NotiFastTextField(
                    value = uiState.classroom,
                    onValueChange = { viewModel.onEvent(AddScheduleEvent.UpdateClassroom(it)) },
                    label = stringResource(R.string.schedules_field_classroom),
                    leadingIcon = Icons.Default.MeetingRoom
                )

                NotiFastTextField(
                    value = uiState.teacher,
                    onValueChange = { viewModel.onEvent(AddScheduleEvent.UpdateTeacher(it)) },
                    label = stringResource(R.string.schedules_field_teacher),
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.weight(1f))

                NotiFastButton(
                    text = stringResource(R.string.schedules_save_button),
                    onClick = { viewModel.onEvent(AddScheduleEvent.SaveSchedule) },
                    isLoading = uiState.isSaving,
                    backgroundColor = Aqua,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )
            }
        }

        if (showDiscardDialog) {
            NotiFastDialog(
                title = stringResource(R.string.schedules_discard_title),
                message = stringResource(R.string.schedules_discard_message),
                confirmText = stringResource(R.string.schedules_discard_confirm),
                dismissText = stringResource(R.string.schedules_discard_cancel),
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
