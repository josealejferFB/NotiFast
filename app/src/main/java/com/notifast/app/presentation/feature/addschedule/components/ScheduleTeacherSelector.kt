package com.notifast.app.presentation.feature.addschedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.notifast.app.data.model.Teacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTeacherSelector(
    subjectName: String,
    selectedTeacher: String,
    availableTeachers: List<Teacher>,
    allTeachers: List<Teacher> = emptyList(),
    hasConfiguredTeacher: Boolean = true,
    onTeacherSelected: (String) -> Unit,
    onAddNewTeacherClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (allTeachers.isEmpty() && availableTeachers.isEmpty()) {
        EmptyTeacherCard(
            onAddNewTeacherClick = onAddNewTeacherClick,
            modifier = modifier
        )
        return
    }

    var expanded by remember { mutableStateOf(false) }

    val otherTeachers = remember(allTeachers, availableTeachers) {
        allTeachers.filter { allT ->
            availableTeachers.none { availT ->
                (availT.id.isNotBlank() && availT.id == allT.id) ||
                    availT.fullName.equals(allT.fullName, ignoreCase = true)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedTeacher.ifBlank { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.schedules_field_teacher)) },
            placeholder = { Text(stringResource(R.string.schedule_select_teacher_hint)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (selectedTeacher.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedTeacher.isNotBlank()) {
                        IconButton(
                            onClick = { onTeacherSelected("") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.schedule_select_teacher_none),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.schedule_select_teacher_none),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PersonOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    onTeacherSelected("")
                    expanded = false
                }
            )

            if (subjectName.isNotBlank() && availableTeachers.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = stringResource(R.string.schedule_subject_teachers_header),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
                availableTeachers.forEach { teacher ->
                    TeacherDropdownMenuItem(
                        teacher = teacher,
                        isSelected = teacher.fullName.equals(selectedTeacher, ignoreCase = true),
                        onClick = {
                            onTeacherSelected(teacher.fullName)
                            expanded = false
                        },
                        iconTint = if (teacher.fullName.equals(selectedTeacher, ignoreCase = true)) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    )
                }
            }

            if (otherTeachers.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = stringResource(R.string.schedule_all_teachers_header),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
                otherTeachers.forEach { teacher ->
                    TeacherDropdownMenuItem(
                        teacher = teacher,
                        isSelected = teacher.fullName.equals(selectedTeacher, ignoreCase = true),
                        onClick = {
                            onTeacherSelected(teacher.fullName)
                            expanded = false
                        },
                        iconTint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(R.string.schedule_add_new_teacher_option),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {
                    expanded = false
                    onAddNewTeacherClick()
                }
            )
        }
    }
}
