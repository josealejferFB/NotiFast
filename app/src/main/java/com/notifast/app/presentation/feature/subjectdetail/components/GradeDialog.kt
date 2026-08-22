package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.util.GradeConstants
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby

@Composable
fun GradeDialog(
    evaluation: Evaluation,
    onSave: (Double?, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var gradeText by remember { mutableStateOf(evaluation.grade?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var isPresented by remember { mutableStateOf(evaluation.isPresented) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calificar evaluación") },
        text = {
            Column {
                Text(evaluation.title, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = gradeText,
                    onValueChange = {
                        gradeText = it
                        error = null
                        val parsed = it.toDoubleOrNull()
                        if (it.isNotEmpty() && parsed != null && parsed in GradeConstants.MIN_GRADE..GradeConstants.MAX_GRADE) {
                            isPresented = true
                        }
                    },
                    label = { Text("Nota (${GradeConstants.MIN_GRADE} - ${GradeConstants.MAX_GRADE})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = Ruby) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPresented = !isPresented }
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = isPresented,
                        onCheckedChange = { isPresented = it },
                        colors = CheckboxDefaults.colors(checkedColor = Aqua)
                    )
                    Text("Marcar como presentada", fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (gradeText.isBlank()) {
                        onSave(null, false)
                    } else {
                        val grade = gradeText.toDoubleOrNull()
                        when {
                            grade == null -> error = "Nota inválida"
                            grade < GradeConstants.MIN_GRADE || grade > GradeConstants.MAX_GRADE -> error = "La nota debe estar entre ${GradeConstants.MIN_GRADE} y ${GradeConstants.MAX_GRADE}"
                            else -> onSave(grade, isPresented)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Ruby)
            ) { Text("Guardar", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
