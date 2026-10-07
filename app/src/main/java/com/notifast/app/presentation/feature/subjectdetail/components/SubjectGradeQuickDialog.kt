package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastGradePresetChips
import com.notifast.app.presentation.atoms.presetText

@Composable
fun SubjectGradeQuickDialog(
    evaluation: Evaluation,
    scale: GradeScale,
    onSave: (grade: Double?, isPresented: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var gradeText by remember {
        mutableStateOf(
            evaluation.grade?.let { raw ->
                val converted = GradePolicy.convertGrade(
                    raw,
                    GradeScale.fromId(evaluation.gradeScaleId),
                    scale
                )
                presetText(scale.roundGrade(converted))
            } ?: ""
        )
    }
    var isPresented by remember { mutableStateOf(evaluation.isPresented || evaluation.grade != null) }
    var error by remember { mutableStateOf<String?>(null) }

    val invalidGradeMsg = stringResource(R.string.grades_dialog_grade_invalid)
    val gradeRangeMsg = stringResource(
        R.string.grades_dialog_grade_range,
        scale.minGrade.toString(),
        scale.maxGrade.toString()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.subject_detail_quick_grade_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.subject_detail_quick_grade_subtitle, evaluation.title, evaluation.weight ?: 0.0),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))

                NotiFastGradePresetChips(
                    scale = scale,
                    selectedText = gradeText,
                    onPresetClick = { value ->
                        gradeText = presetText(value)
                        isPresented = true
                        error = null
                    }
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = gradeText,
                    onValueChange = {
                        gradeText = it
                        error = null
                        if (it.isNotBlank()) isPresented = true
                    },
                    label = { Text(stringResource(R.string.subject_detail_quick_grade_score_label)) },
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.eval_grade_scale_hint, scale.label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.subject_detail_quick_grade_presented),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = isPresented,
                        onCheckedChange = { isPresented = it }
                    )
                }
            }
        },
        confirmButton = {
            NotiFastButton(
                text = stringResource(R.string.btn_save),
                onClick = {
                    if (gradeText.isBlank()) {
                        onSave(null, isPresented)
                    } else {
                        val parsed = gradeText.toDoubleOrNull()
                        when {
                            parsed == null -> error = invalidGradeMsg
                            !GradePolicy.validateGrade(parsed, scale) -> error = gradeRangeMsg
                            else -> onSave(parsed, isPresented)
                        }
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
