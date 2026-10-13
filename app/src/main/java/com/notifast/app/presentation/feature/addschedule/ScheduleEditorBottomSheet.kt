package com.notifast.app.presentation.feature.addschedule

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.feature.addschedule.components.ScheduleEditorForm
import com.notifast.app.presentation.feature.addschedule.components.ScheduleEditorSheetLayout
import com.notifast.app.presentation.feature.editschedule.EditScheduleEvent
import com.notifast.app.presentation.feature.editschedule.EditScheduleViewModel

@Composable
fun ScheduleEditorBottomSheet(
    scheduleId: String? = null,
    initialSubject: String = "",
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onCreateSubject: () -> Unit = {}
) {
    if (scheduleId != null) {
        EditScheduleEditorSheet(
            scheduleId = scheduleId,
            onDismiss = onDismiss,
            onSaved = onSaved,
            onCreateSubject = onCreateSubject
        )
    } else {
        AddScheduleEditorSheet(
            initialSubject = initialSubject,
            onDismiss = onDismiss,
            onSaved = onSaved,
            onCreateSubject = onCreateSubject
        )
    }
}

@Composable
private fun AddScheduleEditorSheet(
    initialSubject: String = "",
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onCreateSubject: () -> Unit,
    viewModel: AddScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(AddScheduleEvent.ResetState)
        if (initialSubject.isNotBlank()) {
            viewModel.onEvent(AddScheduleEvent.UpdateSubjectName(initialSubject))
        }
    }

    LaunchedEffect(initialSubject) {
        if (initialSubject.isNotBlank()) {
            viewModel.onEvent(AddScheduleEvent.RefreshSubjectsAndSelect(initialSubject))
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, R.string.schedules_saved_success, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(AddScheduleEvent.ResetState)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            viewModel.onEvent(AddScheduleEvent.ClearError)
        }
    }

    val hasChanges = uiState.subjectName.isNotBlank() ||
            uiState.startTime.isNotBlank() ||
            uiState.endTime.isNotBlank() ||
            uiState.classroom.isNotBlank() ||
            uiState.teacher.isNotBlank()

    ScheduleEditorSheetLayout(
        isEditing = false,
        isSaved = uiState.isSaved,
        hasUnsavedChanges = hasChanges,
        onDismiss = onDismiss,
        onSaved = onSaved,
        onCreateSubject = onCreateSubject
    ) { handleCreateSubject ->
        ScheduleEditorForm(
            subjects = uiState.subjects,
            selectedSubject = uiState.subjectName,
            onSubjectSelected = { viewModel.onEvent(AddScheduleEvent.UpdateSubjectName(it)) },
            onCreateSubjectClick = handleCreateSubject,
            dayOfWeek = uiState.dayOfWeek,
            onDaySelected = { viewModel.onEvent(AddScheduleEvent.UpdateDayOfWeek(it)) },
            startTime = uiState.startTime,
            endTime = uiState.endTime,
            onTimeRangeChanged = { s, e -> viewModel.onEvent(AddScheduleEvent.UpdateTimeRange(s, e)) },
            classroom = uiState.classroom,
            onClassroomChange = { viewModel.onEvent(AddScheduleEvent.UpdateClassroom(it)) },
            teacher = uiState.teacher,
            onTeacherChange = { viewModel.onEvent(AddScheduleEvent.UpdateTeacher(it)) },
            availableTeachers = uiState.availableTeachers,
            hasConfiguredTeacher = uiState.hasConfiguredTeacher,
            conflictingSchedules = uiState.conflictingSchedules,
            isSaving = uiState.isSaving,
            onSaveClick = { viewModel.onEvent(AddScheduleEvent.SaveSchedule) }
        )
    }
}

@Composable
private fun EditScheduleEditorSheet(
    scheduleId: String,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onCreateSubject: () -> Unit,
    viewModel: EditScheduleViewModel = hiltViewModel()
) {
    LaunchedEffect(scheduleId) {
        viewModel.onEvent(EditScheduleEvent.Init(scheduleId))
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, R.string.schedules_saved_success, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            viewModel.onEvent(EditScheduleEvent.ClearError)
        }
    }

    var initialSubject by remember(scheduleId) { mutableStateOf<String?>(null) }
    var initialDay by remember(scheduleId) { mutableStateOf<Int?>(null) }
    var initialStart by remember(scheduleId) { mutableStateOf<String?>(null) }
    var initialEnd by remember(scheduleId) { mutableStateOf<String?>(null) }
    var initialClassroom by remember(scheduleId) { mutableStateOf<String?>(null) }
    var initialTeacher by remember(scheduleId) { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.subjectName, uiState.startTime) {
        if (initialSubject == null && (uiState.subjectName.isNotBlank() || uiState.startTime.isNotBlank())) {
            initialSubject = uiState.subjectName
            initialDay = uiState.dayOfWeek
            initialStart = uiState.startTime
            initialEnd = uiState.endTime
            initialClassroom = uiState.classroom
            initialTeacher = uiState.teacher
        }
    }

    val hasChanges = if (initialSubject == null) {
        false
    } else {
        uiState.subjectName != initialSubject ||
                uiState.dayOfWeek != initialDay ||
                uiState.startTime != initialStart ||
                uiState.endTime != initialEnd ||
                uiState.classroom != initialClassroom ||
                uiState.teacher != initialTeacher
    }

    ScheduleEditorSheetLayout(
        isEditing = true,
        isSaved = uiState.isSaved,
        hasUnsavedChanges = hasChanges,
        onDismiss = onDismiss,
        onSaved = onSaved,
        onCreateSubject = onCreateSubject
    ) { handleCreateSubject ->
        ScheduleEditorForm(
            subjects = uiState.subjects,
            selectedSubject = uiState.subjectName,
            onSubjectSelected = { viewModel.onEvent(EditScheduleEvent.UpdateSubjectName(it)) },
            onCreateSubjectClick = handleCreateSubject,
            dayOfWeek = uiState.dayOfWeek,
            onDaySelected = { viewModel.onEvent(EditScheduleEvent.UpdateDayOfWeek(it)) },
            startTime = uiState.startTime,
            endTime = uiState.endTime,
            onTimeRangeChanged = { s, e -> viewModel.onEvent(EditScheduleEvent.UpdateTimeRange(s, e)) },
            classroom = uiState.classroom,
            onClassroomChange = { viewModel.onEvent(EditScheduleEvent.UpdateClassroom(it)) },
            teacher = uiState.teacher,
            onTeacherChange = { viewModel.onEvent(EditScheduleEvent.UpdateTeacher(it)) },
            availableTeachers = uiState.availableTeachers,
            hasConfiguredTeacher = uiState.hasConfiguredTeacher,
            conflictingSchedules = uiState.conflictingSchedules,
            isSaving = uiState.isSaving,
            onSaveClick = { viewModel.onEvent(EditScheduleEvent.SaveSchedule) }
        )
    }
}
