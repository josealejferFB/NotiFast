package com.notifast.app.presentation.feature.addschedule

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
import com.notifast.app.data.model.Teacher
import com.notifast.app.presentation.atoms.AlertSeverity
import com.notifast.app.presentation.atoms.rememberNotiFastAlert
import com.notifast.app.presentation.feature.addschedule.components.ScheduleEditorForm
import com.notifast.app.presentation.feature.addschedule.components.ScheduleEditorSheetLayout
import com.notifast.app.presentation.feature.editschedule.EditScheduleEvent
import com.notifast.app.presentation.feature.editschedule.EditScheduleViewModel
import com.notifast.app.presentation.feature.notes.components.AddEditTeacherBottomSheet

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

    val showAlert = rememberNotiFastAlert()
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            showAlert(context.getString(R.string.schedules_saved_success), AlertSeverity.SUCCESS)
            viewModel.onEvent(AddScheduleEvent.ResetState)
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
        errorMessage = uiState.error,
        onErrorDismiss = { viewModel.onEvent(AddScheduleEvent.ClearError) },
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
            allTeachers = uiState.allTeachers,
            hasConfiguredTeacher = uiState.hasConfiguredTeacher,
            onAddNewTeacherClick = { viewModel.onEvent(AddScheduleEvent.OpenAddTeacherSheet) },
            conflictingSchedules = uiState.conflictingSchedules,
            isSaving = uiState.isSaving,
            onSaveClick = { viewModel.onEvent(AddScheduleEvent.SaveSchedule) }
        )
    }

    if (uiState.showAddTeacherSheet) {
        AddEditTeacherBottomSheet(
            teacher = Teacher(
                subjectNames = if (uiState.subjectName.isNotBlank()) listOf(uiState.subjectName) else emptyList()
            ),
            availableSubjects = uiState.subjects.map { it.name },
            onDismiss = { viewModel.onEvent(AddScheduleEvent.CloseAddTeacherSheet) },
            onSave = { viewModel.onEvent(AddScheduleEvent.SaveTeacher(it)) }
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

    val showAlert = rememberNotiFastAlert()
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            showAlert(context.getString(R.string.schedules_saved_success), AlertSeverity.SUCCESS)
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
        errorMessage = uiState.error,
        onErrorDismiss = { viewModel.onEvent(EditScheduleEvent.ClearError) },
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
            allTeachers = uiState.allTeachers,
            hasConfiguredTeacher = uiState.hasConfiguredTeacher,
            onAddNewTeacherClick = { viewModel.onEvent(EditScheduleEvent.OpenAddTeacherSheet) },
            conflictingSchedules = uiState.conflictingSchedules,
            isSaving = uiState.isSaving,
            onSaveClick = { viewModel.onEvent(EditScheduleEvent.SaveSchedule) }
        )
    }

    if (uiState.showAddTeacherSheet) {
        AddEditTeacherBottomSheet(
            teacher = Teacher(
                subjectNames = if (uiState.subjectName.isNotBlank()) listOf(uiState.subjectName) else emptyList()
            ),
            availableSubjects = uiState.subjects.map { it.name },
            onDismiss = { viewModel.onEvent(EditScheduleEvent.CloseAddTeacherSheet) },
            onSave = { viewModel.onEvent(EditScheduleEvent.SaveTeacher(it)) }
        )
    }
}
