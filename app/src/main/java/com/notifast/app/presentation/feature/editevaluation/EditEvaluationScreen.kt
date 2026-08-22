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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Title
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.data.model.EvaluationType
import com.notifast.app.domain.util.GradeConstants
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTextField
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.addevaluation.components.EvaluationDatePickerField
import com.notifast.app.presentation.feature.addevaluation.components.EvaluationSubjectSelector
import com.notifast.app.presentation.feature.addevaluation.components.EvaluationWeightSlider
import com.notifast.app.presentation.feature.editevaluation.components.EditEvaluationGradeFields
import com.notifast.app.presentation.theme.color.Ruby
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
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                NotiFastCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // 1. Subject Selector
                        EvaluationSubjectSelector(
                            selectedSubject = uiState.subjectName,
                            subjects = uiState.subjects,
                            onSubjectSelected = { viewModel.onEvent(EditEvaluationEvent.SelectSubject(it)) }
                        )

                        // 2. Title Field
                        NotiFastTextField(
                            value = uiState.title,
                            onValueChange = { viewModel.onEvent(EditEvaluationEvent.UpdateTitle(it)) },
                            label = stringResource(R.string.evaluation_field_title),
                            leadingIcon = Icons.Default.Title,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 3. Date Picker Field
                        EvaluationDatePickerField(
                            selectedDate = selectedDate,
                            onDateSelected = { selectedDate = it }
                        )

                        // 4. Weighting Slider
                        EvaluationWeightSlider(
                            weight = uiState.weight,
                            onWeightChanged = { viewModel.onEvent(EditEvaluationEvent.UpdateWeight(it)) }
                        )

                        // 5. Grade & Presented Fields
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

                        // 6. Optional Description
                        NotiFastTextField(
                            value = uiState.description,
                            onValueChange = { viewModel.onEvent(EditEvaluationEvent.UpdateDescription(it)) },
                            label = stringResource(R.string.evaluation_field_description),
                            placeholder = "Temas incluidos, requisitos...",
                            singleLine = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 7. Optional Quick Tips
                        NotiFastTextField(
                            value = uiState.quickTips,
                            onValueChange = { viewModel.onEvent(EditEvaluationEvent.UpdateQuickTips(it)) },
                            label = stringResource(R.string.evaluation_field_quick_tips),
                            placeholder = "Fórmulas clave, repasar capítulo 4...",
                            singleLine = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                NotiFastButton(
                    text = stringResource(R.string.evaluation_btn_save_changes),
                    onClick = { viewModel.onEvent(EditEvaluationEvent.Save(selectedDate, selectedImageUri)) },
                    isLoading = uiState.isLoading,
                    enabled = !uiState.isLoading && uiState.title.isNotBlank() && !isGradeInvalid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
            }
        }

        if (showDiscardDialog) {
            NotiFastDialog(
                title = stringResource(R.string.evaluation_discard_title),
                message = stringResource(R.string.evaluation_discard_message),
                confirmText = stringResource(R.string.schedules_discard_confirm),
                dismissText = stringResource(R.string.schedules_discard_cancel),
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
