package com.notifast.app.presentation.feature.addschedule

import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.Teacher

data class AddScheduleUiState(
    val subjectName: String = "",
    val modality: String = Schedule.MODALITY_PRESENCIAL,
    val dayOfWeek: Int = 1,
    val startTime: String = "",
    val endTime: String = "",
    val classroom: String = "",
    val teacher: String = "",
    val meetingUrl: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val subjects: List<SubjectData> = emptyList(),
    val existingSchedules: List<Schedule> = emptyList(),
    val conflictingSchedules: List<Schedule> = emptyList(),
    val availableTeachers: List<Teacher> = emptyList(),
    val hasConfiguredTeacher: Boolean = true
)

sealed interface AddScheduleEvent {
    data class UpdateSubjectName(val subjectName: String) : AddScheduleEvent
    data class UpdateModality(val modality: String) : AddScheduleEvent
    data class UpdateDayOfWeek(val day: Int) : AddScheduleEvent
    data class UpdateStartTime(val time: String) : AddScheduleEvent
    data class UpdateEndTime(val time: String) : AddScheduleEvent
    data class UpdateTimeRange(val startTime: String, val endTime: String) : AddScheduleEvent
    data class UpdateClassroom(val classroom: String) : AddScheduleEvent
    data class UpdateTeacher(val teacher: String) : AddScheduleEvent
    data class UpdateMeetingUrl(val url: String) : AddScheduleEvent
    data class RefreshSubjectsAndSelect(val subjectName: String) : AddScheduleEvent
    data object SaveSchedule : AddScheduleEvent
    data object ClearError : AddScheduleEvent
    data object ResetState : AddScheduleEvent
}
