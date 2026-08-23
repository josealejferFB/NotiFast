package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.atoms.NotiFastButton

@Composable
fun SubjectGradeQuickDialog(
    evaluation: Evaluation,
    scale: GradeScale,
    onSave: (grade: Double?, isPresented: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var gradeText by remember { mutableStateOf(evaluation.grade?.toString() ?: "") }
    var isPresented by remember { mutableStateOf(evaluation.isPresented || evaluation.grade != null) }

    val presetChips = remember(scale) {
        when (scale) {
            GradeScale.SCALE_0_20 -> listOf(20.0, 18.0, 16.0, 14.0, 11.0, 8.0)
            GradeScale.SCALE_0_100 -> listOf(100.0, 90.0, 80.0, 70.0, 60.0, 50.0)
            else -> listOf(10.0, 9.0, 8.0, 7.0, 6.0, 5.0)
        }
    }

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

                // Preset grade chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetChips.forEach { preset ->
                        FilterChip(
                            selected = gradeText == preset.toString() || (preset % 1.0 == 0.0 && gradeText == preset.toInt().toString()),
                            onClick = {
                                gradeText = if (preset % 1.0 == 0.0) preset.toInt().toString() else preset.toString()
                                isPresented = true
                            },
                            label = { Text(if (preset % 1.0 == 0.0) preset.toInt().toString() else preset.toString()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = gradeText,
                    onValueChange = {
                        gradeText = it
                        if (it.isNotBlank()) isPresented = true
                    },
                    label = { Text(stringResource(R.string.subject_detail_quick_grade_score_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
                    val parsed = gradeText.toDoubleOrNull()
                    onSave(parsed, isPresented)
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
