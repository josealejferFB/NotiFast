package com.example.notifast.presentation.feature.schedules

import com.example.notifast.data.model.Schedule

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
