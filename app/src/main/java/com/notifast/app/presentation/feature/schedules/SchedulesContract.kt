package com.notifast.app.presentation.feature.schedules

import com.notifast.app.data.model.Schedule

data class SchedulesUiState(
    val isLoading: Boolean = false,
    val schedules: List<Schedule> = emptyList(),
    val groupedSchedules: Map<Int, List<Schedule>> = emptyMap(),
    val error: String? = null
)

sealed interface SchedulesEvent {
    data object LoadData : SchedulesEvent
    data class DeleteSchedule(val schedule: Schedule) : SchedulesEvent
    data object ClearError : SchedulesEvent
}
