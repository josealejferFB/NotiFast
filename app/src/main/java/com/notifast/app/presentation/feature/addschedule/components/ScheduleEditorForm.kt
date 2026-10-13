package com.notifast.app.presentation.feature.addschedule.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.Teacher
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastTextField
import com.notifast.app.presentation.feature.schedules.components.ScheduleConflictBanner
import com.notifast.app.presentation.theme.color.Aqua

@Composable
fun ScheduleEditorForm(
    subjects: List<SubjectData>,
    selectedSubject: String,
    onSubjectSelected: (String) -> Unit,
    onCreateSubjectClick: () -> Unit,
    dayOfWeek: Int,
    onDaySelected: (Int) -> Unit,
    startTime: String,
    endTime: String,
    onTimeRangeChanged: (String, String) -> Unit,
    classroom: String,
    onClassroomChange: (String) -> Unit,
    teacher: String,
    onTeacherChange: (String) -> Unit,
    availableTeachers: List<Teacher> = emptyList(),
    hasConfiguredTeacher: Boolean = true,
    conflictingSchedules: List<Schedule>,
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScheduleConflictBanner(conflicts = conflictingSchedules)

        NotiFastCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                ScheduleSubjectChips(
                    subjects = subjects,
                    selectedSubject = selectedSubject,
                    onSubjectSelected = onSubjectSelected,
                    onCreateSubjectClick = onCreateSubjectClick
                )

                ScheduleDaySegmentedBar(
                    selectedDayIndex = dayOfWeek,
                    onDaySelected = onDaySelected
                )

                ScheduleTimeRangePicker(
                    startTime = startTime,
                    endTime = endTime,
                    onTimeRangeChanged = onTimeRangeChanged
                )

                NotiFastTextField(
                    value = classroom,
                    onValueChange = onClassroomChange,
                    label = stringResource(R.string.schedules_field_classroom),
                    placeholder = stringResource(R.string.schedules_placeholder_classroom),
                    leadingIcon = Icons.Default.MeetingRoom,
                    modifier = Modifier.fillMaxWidth()
                )

                ScheduleTeacherSelector(
                    subjectName = selectedSubject,
                    selectedTeacher = teacher,
                    availableTeachers = availableTeachers,
                    hasConfiguredTeacher = hasConfiguredTeacher,
                    onTeacherSelected = onTeacherChange
                )
            }
        }

        NotiFastButton(
            text = stringResource(R.string.schedules_save_button),
            onClick = onSaveClick,
            isLoading = isSaving,
            backgroundColor = Aqua,
            enabled = selectedSubject.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}
