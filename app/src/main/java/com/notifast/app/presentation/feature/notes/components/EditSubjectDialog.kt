package com.notifast.app.presentation.feature.notes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.SubjectData
import androidx.compose.material3.TextButton
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastFormField
import com.notifast.app.presentation.feature.addsubject.components.SubjectColorPickerBento
import com.notifast.app.presentation.feature.addsubject.components.SubjectIconGridSelector
import com.notifast.app.presentation.feature.addsubject.components.SubjectLivePreviewCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubjectDialog(
    subject: SubjectData,
    onDismiss: () -> Unit,
    onSave: (newName: String, newColorHex: String, newIcon: String) -> Unit,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var name by remember(subject) { mutableStateOf(subject.name) }
    var selectedColor by remember(subject) { mutableStateOf(subject.colorHex.ifBlank { "#BF092F" }) }
    var selectedIcon by remember(subject) { mutableStateOf(subject.icon.ifBlank { "📚" }) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.edit_subject_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SubjectLivePreviewCard(
                    subjectName = name,
                    iconName = selectedIcon,
                    hexColor = selectedColor
                )

                NotiFastFormField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.edit_subject_name_label)
                )

                SubjectColorPickerBento(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )

                SubjectIconGridSelector(
                    selectedIcon = selectedIcon,
                    selectedColor = selectedColor,
                    onIconSelected = { selectedIcon = it }
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving
                    ) {
                        Text(stringResource(R.string.btn_cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    NotiFastButton(
                        text = stringResource(R.string.edit_subject_btn_save),
                        onClick = { onSave(name.trim(), selectedColor, selectedIcon) },
                        isLoading = isSaving,
                        enabled = name.isNotBlank() && !isSaving
                    )
                }
            }
        }
    }
}
