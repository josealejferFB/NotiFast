package com.example.notifast.presentation.feature.addschedule

import com.example.notifast.data.model.SubjectData

data class AddScheduleUiState(
    val subjectName: String = "",
    val dayOfWeek: Int = 1,
    val startTime: String = "",
    val endTime: String = "",
    val classroom: String = "",
    val teacher: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val subjects: List<SubjectData> = emptyList()
)

sealed interface AddScheduleEvent {
    data class UpdateSubjectName(val subjectName: String) : AddScheduleEvent
    data class UpdateDayOfWeek(val day: Int) : AddScheduleEvent
    data class UpdateStartTime(val time: String) : AddScheduleEvent
    data class UpdateEndTime(val time: String) : AddScheduleEvent
    data class UpdateClassroom(val classroom: String) : AddScheduleEvent
    data class UpdateTeacher(val teacher: String) : AddScheduleEvent
    data object SaveSchedule : AddScheduleEvent
    data object ClearError : AddScheduleEvent
}
