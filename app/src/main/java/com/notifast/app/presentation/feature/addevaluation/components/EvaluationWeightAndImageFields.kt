package com.notifast.app.presentation.feature.addevaluation.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Scale
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.notifast.app.presentation.atoms.NotiFastFormField
import com.notifast.app.presentation.atoms.NotiFastImagePickerField

@Composable
fun EvaluationWeightAndImageFields(
    weight: String,
    onWeightChange: (String) -> Unit,
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NotiFastFormField(
            value = weight,
            onValueChange = onWeightChange,
            label = "Ponderación / Peso % (ej. 20)",
            leadingIcon = Icons.Default.Scale,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        NotiFastImagePickerField(
            placeholderText = "Foto o guía de examen adjunta (Opcional)",
            onImageSelected = onImageSelected
        )
    }
}
