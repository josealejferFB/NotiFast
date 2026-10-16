package com.notifast.app.presentation.feature.addevaluation.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastImagePickerField
import com.notifast.app.presentation.atoms.NotiFastTextField
import com.notifast.app.presentation.feature.addevaluation.AddEvaluationEvent
import com.notifast.app.presentation.feature.addevaluation.AddEvaluationUiState
import java.util.Date

@Composable
fun EvaluationEditorAddForm(
    uiState: AddEvaluationUiState,
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    onEvent: (AddEvaluationEvent) -> Unit,
    onCreateSubjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    selectedSubject = uiState.selectedSubject,
                    subjects = uiState.subjects,
                    onSubjectSelected = { onEvent(AddEvaluationEvent.SelectSubject(it)) },
                    onCreateSubjectClick = onCreateSubjectClick
                )

                Column {
                    Text(
                        text = stringResource(R.string.evaluation_field_type),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    EvaluationTypeSelector(
                        selectedType = uiState.selectedType,
                        onTypeSelected = { onEvent(AddEvaluationEvent.SelectType(it)) }
                    )
                }

                NotiFastTextField(
                    value = uiState.title,
                    onValueChange = { onEvent(AddEvaluationEvent.UpdateTitle(it)) },
                    label = stringResource(R.string.evaluation_field_title),
                    placeholder = stringResource(R.string.evaluation_title_placeholder),
                    leadingIcon = Icons.Default.Title,
                    modifier = Modifier.fillMaxWidth()
                )

                EvaluationDatePickerField(
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected
                )

                EvaluationWeightSlider(
                    weight = uiState.weight,
                    onWeightChanged = { onEvent(AddEvaluationEvent.UpdateWeight(it)) }
                )

                NotiFastTextField(
                    value = uiState.description,
                    onValueChange = { onEvent(AddEvaluationEvent.UpdateDescription(it)) },
                    label = stringResource(R.string.evaluation_field_description),
                    placeholder = stringResource(R.string.evaluation_desc_placeholder),
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                NotiFastTextField(
                    value = uiState.quickTips,
                    onValueChange = { onEvent(AddEvaluationEvent.UpdateQuickTips(it)) },
                    label = stringResource(R.string.evaluation_field_quick_tips),
                    placeholder = stringResource(R.string.evaluation_quick_tips_placeholder),
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                NotiFastImagePickerField(
                    placeholderText = if (selectedImageUri == null) {
                        stringResource(R.string.evaluation_attach_placeholder)
                    } else {
                        stringResource(R.string.evaluation_change_attachment)
                    },
                    onImageSelected = onImageSelected
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
                    onDismiss = { onEvent(AddEvaluationEvent.ClearError) }
                )
            }
        }

        NotiFastButton(
            text = stringResource(R.string.evaluation_btn_save),
            onClick = { onEvent(AddEvaluationEvent.Save(selectedDate, selectedImageUri)) },
            isLoading = uiState.isLoading || uiState.isSaving,
            enabled = !uiState.isLoading && !uiState.isSaving && uiState.title.isNotBlank() && uiState.selectedSubject.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
    }
}
