package com.notifast.app.presentation.feature.editevaluation.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.notifast.app.presentation.atoms.NotiFastFormField
import com.notifast.app.presentation.atoms.NotiFastImagePickerField

@Composable
fun EditEvaluationGradeFields(
    isPresented: Boolean,
    onTogglePresented: () -> Unit,
    grade: String,
    onGradeChange: (String) -> Unit,
    isGradeInvalid: Boolean,
    weight: String,
    onWeightChange: (String) -> Unit,
    existingImageUrl: String?,
    onImageSelected: (Uri?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Evaluación Presentada",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Activa para ingresar la nota obtenida",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isPresented,
                onCheckedChange = { onTogglePresented() }
            )
        }

        if (isPresented) {
            NotiFastFormField(
                value = grade,
                onValueChange = onGradeChange,
                label = "Calificación obtenida (0.0 - 10.0)",
                leadingIcon = Icons.Default.Star,
                isError = isGradeInvalid,
                supportingText = if (isGradeInvalid) {
                    { Text("La nota debe estar entre 0.0 y 10.0", color = MaterialTheme.colorScheme.error) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        NotiFastFormField(
            value = weight,
            onValueChange = onWeightChange,
            label = "Ponderación / Peso % (ej. 20)",
            leadingIcon = Icons.Default.Scale,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        NotiFastImagePickerField(
            existingImageUrl = existingImageUrl,
            placeholderText = "Foto o guía adjunta (Opcional)",
            onImageSelected = onImageSelected
        )
    }
}
