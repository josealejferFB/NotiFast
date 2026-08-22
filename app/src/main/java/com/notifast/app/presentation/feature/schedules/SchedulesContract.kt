package com.notifast.app.presentation.feature.schedules

import com.notifast.app.data.model.Schedule
import java.util.Calendar

data class SchedulesUiState(
    val isLoading: Boolean = false,
    val schedules: List<Schedule> = emptyList(),
    val groupedSchedules: Map<Int, List<Schedule>> = emptyMap(),
    val selectedDay: Int = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 0 else it },
    val error: String? = null
)

sealed interface SchedulesEvent {
    data object LoadData : SchedulesEvent
    data class SelectDay(val dayOfWeek: Int) : SchedulesEvent
    data class DeleteSchedule(val schedule: Schedule) : SchedulesEvent
    data object ClearError : SchedulesEvent
}
