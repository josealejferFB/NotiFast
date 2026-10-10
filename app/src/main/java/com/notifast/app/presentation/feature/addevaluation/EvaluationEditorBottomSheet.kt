package com.notifast.app.presentation.feature.addevaluation

import android.net.Uri
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
import com.notifast.app.presentation.feature.addevaluation.components.EvaluationEditorAddForm
import com.notifast.app.presentation.feature.addevaluation.components.EvaluationEditorEditForm
import com.notifast.app.presentation.feature.addevaluation.components.EvaluationEditorSheetLayout
import com.notifast.app.presentation.feature.editevaluation.EditEvaluationEvent
import com.notifast.app.presentation.feature.editevaluation.EditEvaluationViewModel
import java.util.Date

@Composable
fun EvaluationEditorBottomSheet(
    evalId: String? = null,
    initialSubject: String = "",
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onCreateSubject: () -> Unit = {}
) {
    if (evalId != null) {
        EditEvaluationEditorSheet(
            evalId = evalId,
            onDismiss = onDismiss,
            onSaved = onSaved,
            onCreateSubject = onCreateSubject
        )
    } else {
        AddEvaluationEditorSheet(
            initialSubject = initialSubject,
            onDismiss = onDismiss,
            onSaved = onSaved,
            onCreateSubject = onCreateSubject
        )
    }
}

@Composable
private fun AddEvaluationEditorSheet(
    initialSubject: String,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onCreateSubject: () -> Unit,
    viewModel: AddEvaluationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(AddEvaluationEvent.ResetState)
        if (initialSubject.isNotBlank()) {
            viewModel.setInitialSubject(initialSubject)
        }
    }

    LaunchedEffect(initialSubject) {
        if (initialSubject.isNotBlank()) {
            viewModel.onEvent(AddEvaluationEvent.RefreshSubjectsAndSelect(initialSubject))
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, R.string.evaluation_saved_success, Toast.LENGTH_SHORT).show()
            selectedDate = Date()
            selectedImageUri = null
            viewModel.onEvent(AddEvaluationEvent.ResetState)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            viewModel.onEvent(AddEvaluationEvent.ClearError)
        }
    }

    val hasChanges = uiState.title.isNotBlank() ||
        uiState.description.isNotBlank() ||
        uiState.quickTips.isNotBlank() ||
        uiState.weight.isNotBlank() ||
        selectedImageUri != null

    EvaluationEditorSheetLayout(
        isEditing = false,
        isSaved = uiState.isSaved,
        hasUnsavedChanges = hasChanges,
        onDismiss = onDismiss,
        onSaved = onSaved,
        onCreateSubject = onCreateSubject
    ) { handleCreateSubject ->
        EvaluationEditorAddForm(
            uiState = uiState,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            selectedImageUri = selectedImageUri,
            onImageSelected = { selectedImageUri = it },
            onEvent = { viewModel.onEvent(it) },
            onCreateSubjectClick = handleCreateSubject
        )
    }
}

@Composable
private fun EditEvaluationEditorSheet(
    evalId: String,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    onCreateSubject: () -> Unit,
    viewModel: EditEvaluationViewModel = hiltViewModel()
) {
    LaunchedEffect(evalId) {
        viewModel.onEvent(EditEvaluationEvent.Init(evalId))
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedImageUri by remember(evalId) { mutableStateOf<Uri?>(null) }
    var selectedDate by remember(evalId) { mutableStateOf(uiState.date ?: Date()) }

    LaunchedEffect(uiState.date) {
        uiState.date?.let { selectedDate = it }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, R.string.evaluation_saved_success, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            viewModel.onEvent(EditEvaluationEvent.ClearError)
        }
    }

    var initialSubject by remember(evalId) { mutableStateOf<String?>(null) }
    var initialTitle by remember(evalId) { mutableStateOf<String?>(null) }
    var initialDescription by remember(evalId) { mutableStateOf<String?>(null) }
    var initialQuickTips by remember(evalId) { mutableStateOf<String?>(null) }
    var initialGrade by remember(evalId) { mutableStateOf<String?>(null) }
    var initialWeight by remember(evalId) { mutableStateOf<String?>(null) }
    var initialPresented by remember(evalId) { mutableStateOf<Boolean?>(null) }
    var initialDate by remember(evalId) { mutableStateOf<Date?>(null) }

    LaunchedEffect(uiState.title, uiState.subjectName) {
        if (initialTitle == null && (uiState.title.isNotBlank() || uiState.subjectName.isNotBlank())) {
            initialSubject = uiState.subjectName
            initialTitle = uiState.title
            initialDescription = uiState.description
            initialQuickTips = uiState.quickTips
            initialGrade = uiState.grade
            initialWeight = uiState.weight
            initialPresented = uiState.isPresented
            initialDate = uiState.date
        }
    }

    val hasChanges = if (initialTitle == null) {
        selectedImageUri != null
    } else {
        selectedImageUri != null ||
            uiState.subjectName != initialSubject ||
            uiState.title != initialTitle ||
            uiState.description != initialDescription ||
            uiState.quickTips != initialQuickTips ||
            uiState.grade != initialGrade ||
            uiState.weight != initialWeight ||
            uiState.isPresented != initialPresented ||
            selectedDate != initialDate
    }

    EvaluationEditorSheetLayout(
        isEditing = true,
        isSaved = uiState.isSaved,
        hasUnsavedChanges = hasChanges,
        onDismiss = onDismiss,
        onSaved = onSaved,
        onCreateSubject = onCreateSubject
    ) { handleCreateSubject ->
        EvaluationEditorEditForm(
            uiState = uiState,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            selectedImageUri = selectedImageUri,
            onImageSelected = { selectedImageUri = it },
            onEvent = { viewModel.onEvent(it) },
            onCreateSubjectClick = handleCreateSubject
        )
    }
}
