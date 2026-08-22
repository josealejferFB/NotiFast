package com.notifast.app.presentation.feature.addevaluation

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
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.addevaluation.components.EvaluationFormDetails
import com.notifast.app.presentation.feature.addevaluation.components.EvaluationSubjectSelector
import com.notifast.app.presentation.feature.addevaluation.components.EvaluationTypeSelector
import com.notifast.app.presentation.feature.addevaluation.components.EvaluationWeightAndImageFields
import com.notifast.app.presentation.feature.addevaluation.components.StepCard
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEvaluationScreen(
    viewModel: AddEvaluationViewModel = hiltViewModel(),
    subject: String = "",
    onEvaluationAdded: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onEvaluationAdded() }
    LaunchedEffect(subject) { viewModel.setInitialSubject(subject) }

    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var expandedStep by remember { mutableStateOf(1) }

    val hasUnsavedChanges = uiState.title.isNotBlank() || uiState.description.isNotBlank() ||
            uiState.quickTips.isNotBlank() || uiState.weight.isNotBlank() || selectedImageUri != null

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
                        title = "Añadir Evaluación",
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

                // PASO 1: Materia y Tipo
                StepCard(
                    title = "1. Materia y Tipo",
                    icon = Icons.Default.Category,
                    isCompleted = uiState.selectedSubject.isNotBlank(),
                    isExpanded = expandedStep == 1,
                    onHeaderClick = { expandedStep = 1 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
                        EvaluationSubjectSelector(
                            selectedSubject = uiState.selectedSubject,
                            subjects = uiState.subjects,
                            onSubjectSelected = { viewModel.onEvent(AddEvaluationEvent.SelectSubject(it)) }
                        )

                        Text("Tipo de Evaluación", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        EvaluationTypeSelector(
                            selectedType = uiState.selectedType,
                            onTypeSelected = { viewModel.onEvent(AddEvaluationEvent.SelectType(it)) }
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
                            onTitleChange = { viewModel.onEvent(AddEvaluationEvent.UpdateTitle(it)) },
                            date = selectedDate,
                            onDateChange = { selectedDate = it },
                            description = uiState.description,
                            onDescriptionChange = { viewModel.onEvent(AddEvaluationEvent.UpdateDescription(it)) },
                            quickTips = uiState.quickTips,
                            onQuickTipsChange = { viewModel.onEvent(AddEvaluationEvent.UpdateQuickTips(it)) }
                        )
                    }
                }

                // PASO 3: Ponderación y Archivos
                StepCard(
                    title = "3. Ponderación y Archivos",
                    icon = Icons.Default.Grade,
                    isCompleted = uiState.weight.isNotBlank() || selectedImageUri != null,
                    isExpanded = expandedStep == 3,
                    onHeaderClick = { expandedStep = 3 }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        EvaluationWeightAndImageFields(
                            weight = uiState.weight,
                            onWeightChange = { viewModel.onEvent(AddEvaluationEvent.UpdateWeight(it)) },
                            selectedImageUri = selectedImageUri,
                            onImageSelected = { selectedImageUri = it }
                        )
                    }
                }

                Button(
                    onClick = { viewModel.onEvent(AddEvaluationEvent.Save(selectedDate, selectedImageUri)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !uiState.isLoading && uiState.title.isNotBlank() && uiState.selectedSubject.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        NotiFastCircularLoading()
                    } else {
                        Text("Guardar Evaluación", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showDiscardDialog) {
            NotiFastDialog(
                title = "Descartar evaluación",
                message = "¿Estás seguro de que deseas salir? Se perderán los datos ingresados.",
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
