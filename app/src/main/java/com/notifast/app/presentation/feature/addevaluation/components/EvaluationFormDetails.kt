package com.notifast.app.presentation.feature.addevaluation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Title
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.notifast.app.presentation.atoms.NotiFastDatePickerField
import com.notifast.app.presentation.atoms.NotiFastFormField
import java.util.Date

@Composable
fun EvaluationFormDetails(
    title: String,
    onTitleChange: (String) -> Unit,
    date: Date,
    onDateChange: (Date) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    quickTips: String,
    onQuickTipsChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NotiFastFormField(
            value = title,
            onValueChange = onTitleChange,
            label = "Título de la evaluación",
            leadingIcon = Icons.Default.Title
        )

        NotiFastDatePickerField(
            date = date,
            onDateSelected = onDateChange,
            label = "Fecha de la evaluación"
        )

        NotiFastFormField(
            value = description,
            onValueChange = onDescriptionChange,
            label = "Temas a evaluar / Descripción",
            singleLine = false,
            maxLines = 3
        )

        NotiFastFormField(
            value = quickTips,
            onValueChange = onQuickTipsChange,
            label = "Tips rápidos para estudiar",
            leadingIcon = Icons.Default.Lightbulb,
            singleLine = false,
            maxLines = 2
        )
    }
}
