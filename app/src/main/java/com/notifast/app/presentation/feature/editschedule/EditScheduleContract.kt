package com.notifast.app.presentation.feature.editschedule

import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.Teacher

data class EditScheduleUiState(
    val subjectName: String = "",
    val modality: String = Schedule.MODALITY_PRESENCIAL,
    val dayOfWeek: Int = 1,
    val startTime: String = "",
    val endTime: String = "",
    val classroom: String = "",
    val teacher: String = "",
    val meetingUrl: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val subjects: List<SubjectData> = emptyList(),
    val existingSchedules: List<Schedule> = emptyList(),
    val conflictingSchedules: List<Schedule> = emptyList(),
    val availableTeachers: List<Teacher> = emptyList(),
    val hasConfiguredTeacher: Boolean = true
)

sealed interface EditScheduleEvent {
    data class Init(val scheduleId: String) : EditScheduleEvent
    data class UpdateSubjectName(val subjectName: String) : EditScheduleEvent
    data class UpdateModality(val modality: String) : EditScheduleEvent
    data class UpdateDayOfWeek(val day: Int) : EditScheduleEvent
    data class UpdateStartTime(val time: String) : EditScheduleEvent
    data class UpdateEndTime(val time: String) : EditScheduleEvent
    data class UpdateTimeRange(val startTime: String, val endTime: String) : EditScheduleEvent
    data class UpdateClassroom(val classroom: String) : EditScheduleEvent
    data class UpdateTeacher(val teacher: String) : EditScheduleEvent
    data class UpdateMeetingUrl(val url: String) : EditScheduleEvent
    data object SaveSchedule : EditScheduleEvent
    data object ClearError : EditScheduleEvent
}
