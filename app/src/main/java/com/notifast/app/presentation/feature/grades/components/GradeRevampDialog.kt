package com.notifast.app.presentation.feature.grades.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale

/**
 * Dialog to record or edit an evaluation grade in NotiFast 3.0.
 */
@Composable
fun GradeRevampDialog(
    evaluation: Evaluation,
    onDismiss: () -> Unit,
    onSave: (String, Double?, Boolean) -> Unit
) {
    val scale = GradeScale.fromId(evaluation.gradeScaleId)
    var grade by remember {
        mutableStateOf(evaluation.grade?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var isPresented by remember { mutableStateOf(evaluation.isPresented) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Registrar Resultado",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    evaluation.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = grade,
                    onValueChange = {
                        grade = it
                        error = null
                        val parsed = it.toDoubleOrNull()
                        if (it.isNotEmpty() && parsed != null && GradePolicy.validateGrade(parsed, scale)) {
                            isPresented = true
                        }
                    },
                    label = { Text("Nota (${scale.minGrade} - ${scale.maxGrade})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPresented = !isPresented }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isPresented,
                        onCheckedChange = { isPresented = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        "Marcar como evaluada",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (grade.isBlank()) {
                        onSave(evaluation.id, null, false)
                        onDismiss()
                    } else {
                        val g = grade.toDoubleOrNull()
                        when {
                            g == null -> error = "Nota inválida"
                            !GradePolicy.validateGrade(g, scale) ->
                                error = "La nota debe estar entre ${scale.minGrade} y ${scale.maxGrade}"
                            else -> {
                                onSave(evaluation.id, g, isPresented)
                                onDismiss()
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(stringResource(R.string.btn_cancel), fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
