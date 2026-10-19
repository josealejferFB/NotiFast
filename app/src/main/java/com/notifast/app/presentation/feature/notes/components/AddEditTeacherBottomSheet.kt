package com.notifast.app.presentation.feature.notes.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.Teacher
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastChip
import com.notifast.app.presentation.atoms.NotiFastCreationBottomSheet
import com.notifast.app.presentation.atoms.NotiFastFormField
import com.notifast.app.presentation.atoms.clearFocusOnTapOutside

/**
 * NotiFast 3.2 Add / Edit Teacher Modal BottomSheet.
 * Standardized using [NotiFastCreationBottomSheet] and [clearFocusOnTapOutside].
 */
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

    val hasUnsavedChanges = remember(teacher, firstName, lastName, phoneNumber, email, notes, selectedSubjects) {
        firstName != teacher?.firstName.orEmpty() ||
            lastName != teacher?.lastName.orEmpty() ||
            phoneNumber != teacher?.phoneNumber.orEmpty() ||
            email != teacher?.email.orEmpty() ||
            notes != teacher?.notes.orEmpty() ||
            selectedSubjects != teacher?.subjectNames.orEmpty().toSet()
    }

    NotiFastCreationBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        hasUnsavedChanges = hasUnsavedChanges,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .clearFocusOnTapOutside()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (teacher == null) stringResource(R.string.teachers_btn_add) else stringResource(R.string.btn_edit),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { requestDismiss() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.btn_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NotiFastFormField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "${stringResource(R.string.teacher_first_name_label)} *",
                    leadingIcon = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )
                NotiFastFormField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = stringResource(R.string.teacher_last_name_label),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )
            }

            NotiFastFormField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = stringResource(R.string.teacher_phone_label),
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )

            NotiFastFormField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.teacher_email_label),
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
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
                            NotiFastChip(
                                label = subject,
                                selected = isSelected,
                                onClick = {
                                    selectedSubjects = if (isSelected) {
                                        selectedSubjects - subject
                                    } else {
                                        selectedSubjects + subject
                                    }
                                }
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
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { requestDismiss() }) {
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
                        dismiss()
                    },
                    enabled = firstName.isNotBlank()
                )
            }
        }
    }
}
