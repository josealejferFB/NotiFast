package com.notifast.app.presentation.feature.addevaluation.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.notifast.app.presentation.feature.editevaluation.EditEvaluationEvent
import com.notifast.app.presentation.feature.editevaluation.EditEvaluationUiState
import com.notifast.app.presentation.feature.editevaluation.components.EditEvaluationGradeFields
import java.util.Date

@Composable
fun EvaluationEditorEditForm(
    uiState: EditEvaluationUiState,
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    onEvent: (EditEvaluationEvent) -> Unit,
    onCreateSubjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradeScale = GradeScale.fromId(uiState.gradeScaleId)
    val isGradeInvalid = uiState.grade.isNotBlank() && (uiState.grade.toDoubleOrNull()?.let {
        !GradePolicy.validateGrade(it, gradeScale)
    } ?: true)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NotiFastCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                EvaluationSubjectSelector(
                    selectedSubject = uiState.subjectName,
                    subjects = uiState.subjects,
                    onSubjectSelected = { onEvent(EditEvaluationEvent.SelectSubject(it)) },
                    onCreateSubjectClick = onCreateSubjectClick
                )

                NotiFastTextField(
                    value = uiState.title,
                    onValueChange = { onEvent(EditEvaluationEvent.UpdateTitle(it)) },
                    label = stringResource(R.string.evaluation_field_title),
                    placeholder = stringResource(R.string.evaluation_title_placeholder),
                    leadingIcon = Icons.Default.Title,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                EvaluationDatePickerField(
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected
                )

                EvaluationWeightSlider(
                    weight = uiState.weight,
                    onWeightChanged = { onEvent(EditEvaluationEvent.UpdateWeight(it)) }
                )

                EditEvaluationGradeFields(
                    isPresented = uiState.isPresented,
                    onTogglePresented = { onEvent(EditEvaluationEvent.TogglePresented) },
                    grade = uiState.grade,
                    onGradeChange = { onEvent(EditEvaluationEvent.UpdateGrade(it)) },
                    isGradeInvalid = isGradeInvalid,
                    weight = uiState.weight,
                    onWeightChange = { onEvent(EditEvaluationEvent.UpdateWeight(it)) },
                    existingImageUrl = uiState.existingImageUrl,
                    onImageSelected = onImageSelected,
                    gradeScale = gradeScale
                )

                NotiFastTextField(
                    value = uiState.description,
                    onValueChange = { onEvent(EditEvaluationEvent.UpdateDescription(it)) },
                    label = stringResource(R.string.evaluation_field_description),
                    placeholder = stringResource(R.string.evaluation_desc_placeholder),
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                NotiFastTextField(
                    value = uiState.quickTips,
                    onValueChange = { onEvent(EditEvaluationEvent.UpdateQuickTips(it)) },
                    label = stringResource(R.string.evaluation_field_quick_tips),
                    placeholder = stringResource(R.string.evaluation_quick_tips_placeholder),
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        uiState.error?.let {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                com.notifast.app.presentation.atoms.NotiFastAlertChip(
                    text = it,
                    severity = com.notifast.app.presentation.atoms.AlertSeverity.ERROR,
                    onDismiss = { onEvent(EditEvaluationEvent.ClearError) }
                )
            }
        }

        NotiFastButton(
            text = stringResource(R.string.evaluation_btn_save_changes),
            onClick = { onEvent(EditEvaluationEvent.Save(selectedDate, selectedImageUri)) },
            isLoading = uiState.isLoading || uiState.isSaving,
            enabled = !uiState.isLoading && !uiState.isSaving && uiState.title.isNotBlank() && !isGradeInvalid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
    }
}
