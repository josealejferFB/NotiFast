package com.example.notifast.presentation.ui.grades.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.notifast.data.model.Evaluation
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

@Composable
fun GradeRevampDialog(
    evaluation: Evaluation, 
    onDismiss: () -> Unit, 
    onSave: (String, Double, Boolean) -> Unit
) {
    var grade by remember { mutableStateOf(evaluation.grade?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var isPresented by remember { mutableStateOf(evaluation.isPresented) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(32.dp),
        containerColor = Color.White,
        title = { Text("Registrar Resultado", fontWeight = FontWeight.Black, color = Ruby) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(evaluation.title, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                
                OutlinedTextField(
                    value = grade,
                    onValueChange = { 
                        val parsed = it.toDoubleOrNull()
                        if (it.isEmpty() || (parsed != null && parsed in 0.0..10.0)) {
                            grade = it
                            if (it.isNotEmpty()) isPresented = true
                        }
                    },
                    label = { Text("Nota (1-10)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(16.dp),
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
                    onSave(evaluation.id, grade.toDoubleOrNull() ?: 0.0, isPresented)
                    onDismiss() 
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ruby)
            ) {
                Text("GUARDAR", fontWeight = FontWeight.Bold)
            }
        }
    )
}
