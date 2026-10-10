package com.notifast.app.presentation.feature.addschedule

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTextField
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.addschedule.components.ScheduleDaySegmentedBar
import com.notifast.app.presentation.feature.addschedule.components.ScheduleSubjectChips
import com.notifast.app.presentation.feature.addschedule.components.ScheduleTimeRangePicker
import com.notifast.app.presentation.feature.schedules.components.ScheduleConflictBanner
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby

@Deprecated(
    message = "Use ScheduleEditorBottomSheet instead for modal editing experience",
    replaceWith = ReplaceWith("ScheduleEditorBottomSheet(onDismiss = onBackClick, onSaved = onScheduleAdded, onCreateSubject = onNavigateToAddSubject)")
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(
    viewModel: AddScheduleViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onScheduleAdded: () -> Unit,
    onNavigateToAddSubject: () -> Unit = {}
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

    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = uiState.subjectName.isNotBlank() || uiState.startTime.isNotBlank() ||
            uiState.endTime.isNotBlank() || uiState.classroom.isNotBlank() || uiState.teacher.isNotBlank()

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
                NotiFastTopBar(
                    type = TopBarType.Detail(
                        title = stringResource(R.string.schedules_add_title),
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Conflict Banner
                ScheduleConflictBanner(conflicts = uiState.conflictingSchedules)

                NotiFastCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // 1-Tap Subject Chips + Create action
                        ScheduleSubjectChips(
                            subjects = uiState.subjects,
                            selectedSubject = uiState.subjectName,
                            onSubjectSelected = { viewModel.onEvent(AddScheduleEvent.UpdateSubjectName(it)) },
                            onCreateSubjectClick = onNavigateToAddSubject
                        )

                        // 7-day Segmented Bar
                        ScheduleDaySegmentedBar(
                            selectedDayIndex = uiState.dayOfWeek,
                            onDaySelected = { viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(it)) }
                        )

                        // M3 TimeRangePicker with presets + dialog
                        ScheduleTimeRangePicker(
                            startTime = uiState.startTime,
                            endTime = uiState.endTime,
                            onTimeRangeChanged = { start, end ->
                                viewModel.onEvent(AddScheduleEvent.UpdateTimeRange(start, end))
                            }
                        )

                        // Classroom
                        NotiFastTextField(
                            value = uiState.classroom,
                            onValueChange = { viewModel.onEvent(AddScheduleEvent.UpdateClassroom(it)) },
                            label = stringResource(R.string.schedules_field_classroom),
                            placeholder = "Ej: Aula 302 / Laboratorio",
                            leadingIcon = Icons.Default.MeetingRoom,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Teacher
                        NotiFastTextField(
                            value = uiState.teacher,
                            onValueChange = { viewModel.onEvent(AddScheduleEvent.UpdateTeacher(it)) },
                            label = stringResource(R.string.schedules_field_teacher),
                            placeholder = "Ej: Dr. Pérez",
                            leadingIcon = Icons.Default.Person,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                NotiFastButton(
                    text = stringResource(R.string.schedules_save_button),
                    onClick = { viewModel.onEvent(AddScheduleEvent.SaveSchedule) },
                    isLoading = uiState.isSaving,
                    backgroundColor = Aqua,
                    enabled = uiState.subjectName.isNotBlank() && uiState.startTime.isNotBlank() && uiState.endTime.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
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
