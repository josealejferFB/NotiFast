package com.notifast.app.presentation.feature.addsubject.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.notifast.app.presentation.atoms.NotiFastFormField

val SUBJECT_SUGGESTIONS = listOf(
    "Cálculo", "Física", "Programación", "Literatura",
    "Química", "Historia", "Álgebra", "Biología", "Economía"
)

@Composable
fun SubjectEditorFormFields(
    subjectName: String,
    section: String = "",
    selectedColor: String,
    selectedIcon: String,
    onNameChanged: (String) -> Unit,
    onSectionChanged: (String) -> Unit = {},
    onSuggestionSelected: (String) -> Unit,
    onColorSelected: (String) -> Unit,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.add_subject_suggestions_title),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SUBJECT_SUGGESTIONS.forEach { suggestion ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSuggestionSelected(suggestion)
                        }
                    ) {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        NotiFastFormField(
            value = subjectName,
            onValueChange = onNameChanged,
            label = stringResource(R.string.add_subject_name_label),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        NotiFastFormField(
            value = section,
            onValueChange = onSectionChanged,
            label = stringResource(R.string.subject_section_label),
            placeholder = stringResource(R.string.subject_section_placeholder),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        SubjectColorPickerBento(
            selectedColor = selectedColor,
            onColorSelected = onColorSelected
        )

        SubjectIconGridSelector(
            selectedIcon = selectedIcon,
            selectedColor = selectedColor,
            onIconSelected = onIconSelected
        )
    }
}
