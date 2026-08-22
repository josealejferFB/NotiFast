package com.notifast.app.presentation.feature.editevaluation

import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.data.model.EvaluationType
import com.notifast.app.domain.util.GradeConstants
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.addevaluation.components.EvaluationFormDetails
import com.notifast.app.presentation.feature.addevaluation.components.EvaluationSubjectSelector
import com.notifast.app.presentation.feature.addevaluation.components.StepCard
import com.notifast.app.presentation.feature.editevaluation.components.EditEvaluationGradeFields
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEvaluationScreen(
    viewModel: EditEvaluationViewModel = hiltViewModel(),
    onEvaluationUpdated: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onEvaluationUpdated() }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedDate by remember { mutableStateOf(uiState.date ?: Date()) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var expandedStep by remember { mutableStateOf(1) }

    LaunchedEffect(uiState.date) {
        uiState.date?.let { selectedDate = it }
    }

    val evaluationType = EvaluationType.fromName(uiState.type)
    val isGradeInvalid = uiState.grade.isNotBlank() && (uiState.grade.toDoubleOrNull()?.let {
        it < GradeConstants.MIN_GRADE || it > GradeConstants.MAX_GRADE
    } ?: true)

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

                // PASO 1: Materia
                StepCard(
                    title = "1. Materia",
                    icon = Icons.Default.Category,
                    isCompleted = uiState.subjectName.isNotBlank(),
                    isExpanded = expandedStep == 1,
                    onHeaderClick = { expandedStep = 1 }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        EvaluationSubjectSelector(
                            selectedSubject = uiState.subjectName,
                            subjects = uiState.subjects,
                            onSubjectSelected = { viewModel.onEvent(EditEvaluationEvent.SelectSubject(it)) }
                        )
                    }
                }

                // PASO 2: Detalles
                StepCard(
                    title = "2. Detalles de Evaluación",
                    icon = Icons.Default.Description,
                    isCompleted = uiState.title.isNotBlank(),
                    isExpanded = expandedStep == 2,
                    onHeaderClick = { expandedStep = 2 }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        EvaluationFormDetails(
                            title = uiState.title,
                            onTitleChange = { viewModel.onEvent(EditEvaluationEvent.UpdateTitle(it)) },
                            date = selectedDate,
                            onDateChange = { selectedDate = it },
                            description = uiState.description,
                            onDescriptionChange = { viewModel.onEvent(EditEvaluationEvent.UpdateDescription(it)) },
                            quickTips = uiState.quickTips,
                            onQuickTipsChange = { viewModel.onEvent(EditEvaluationEvent.UpdateQuickTips(it)) }
                        )
                    }
                }

                // PASO 3: Calificación y Ponderación
                StepCard(
                    title = "3. Calificación y Ponderación",
                    icon = Icons.Default.Grade,
                    isCompleted = uiState.isPresented && uiState.grade.isNotBlank(),
                    isExpanded = expandedStep == 3,
                    onHeaderClick = { expandedStep = 3 }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        EditEvaluationGradeFields(
                            isPresented = uiState.isPresented,
                            onTogglePresented = { viewModel.onEvent(EditEvaluationEvent.TogglePresented) },
                            grade = uiState.grade,
                            onGradeChange = { viewModel.onEvent(EditEvaluationEvent.UpdateGrade(it)) },
                            isGradeInvalid = isGradeInvalid,
                            weight = uiState.weight,
                            onWeightChange = { viewModel.onEvent(EditEvaluationEvent.UpdateWeight(it)) },
                            existingImageUrl = uiState.existingImageUrl,
                            onImageSelected = { selectedImageUri = it }
                        )
                    }
                }

                Button(
                    onClick = { viewModel.onEvent(EditEvaluationEvent.Save(selectedDate, selectedImageUri)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !uiState.isLoading && uiState.title.isNotBlank() && !isGradeInvalid
                ) {
                    if (uiState.isLoading) {
                        NotiFastCircularLoading()
                    } else {
                        Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showDiscardDialog) {
            NotiFastDialog(
                title = "Descartar cambios",
                message = "¿Estás seguro de que deseas salir? Se perderán los cambios sin guardar.",
                confirmText = "Descartar",
                dismissText = "Continuar editando",
                onConfirm = {
                    showDiscardDialog = false
                    onBack()
                },
                onDismiss = { showDiscardDialog = false },
                confirmColor = MaterialTheme.colorScheme.error,
                confirmWeight = FontWeight.Bold
            )
        }
    }
}
