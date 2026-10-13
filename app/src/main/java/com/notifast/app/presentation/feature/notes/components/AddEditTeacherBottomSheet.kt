package com.notifast.app.presentation.feature.notes.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.notifast.app.data.model.Teacher
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastFormField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTeacherBottomSheet(
    teacher: Teacher? = null,
    availableSubjects: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Teacher) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var firstName by remember(teacher) { mutableStateOf(teacher?.firstName.orEmpty()) }
    var lastName by remember(teacher) { mutableStateOf(teacher?.lastName.orEmpty()) }
    var phoneNumber by remember(teacher) { mutableStateOf(teacher?.phoneNumber.orEmpty()) }
    var email by remember(teacher) { mutableStateOf(teacher?.email.orEmpty()) }
    var notes by remember(teacher) { mutableStateOf(teacher?.notes.orEmpty()) }
    var selectedSubjects by remember(teacher) { mutableStateOf(teacher?.subjectNames.orEmpty().toSet()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (teacher == null) stringResource(R.string.teachers_btn_add) else stringResource(R.string.btn_edit),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NotiFastFormField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "${stringResource(R.string.teacher_first_name_label)} *",
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.weight(1f)
                )
                NotiFastFormField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = stringResource(R.string.teacher_last_name_label),
                    modifier = Modifier.weight(1f)
                )
            }

            NotiFastFormField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = stringResource(R.string.teacher_phone_label),
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            NotiFastFormField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.teacher_email_label),
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            if (availableSubjects.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.teacher_subjects_label),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableSubjects.forEach { subject ->
                            val isSelected = selectedSubjects.contains(subject)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedSubjects = if (isSelected) {
                                        selectedSubjects - subject
                                    } else {
                                        selectedSubjects + subject
                                    }
                                },
                                label = { Text(subject) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            NotiFastFormField(
                value = notes,
                onValueChange = { notes = it },
                label = stringResource(R.string.teacher_notes_label),
                leadingIcon = Icons.AutoMirrored.Filled.Notes,
                singleLine = false,
                maxLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.btn_cancel))
                }
                Spacer(Modifier.width(8.dp))
                NotiFastButton(
                    text = stringResource(R.string.btn_save),
                    onClick = {
                        val updated = (teacher ?: Teacher()).copy(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            phoneNumber = phoneNumber.trim(),
                            email = email.trim(),
                            notes = notes.trim(),
                            subjectNames = selectedSubjects.toList()
                        )
                        onSave(updated)
                        onDismiss()
                    },
                    enabled = firstName.isNotBlank()
                )
            }
        }
    }
}
