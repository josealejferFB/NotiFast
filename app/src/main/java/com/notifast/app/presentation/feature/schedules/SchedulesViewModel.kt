package com.notifast.app.presentation.feature.schedules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SchedulesViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchedulesUiState())
    val uiState: StateFlow<SchedulesUiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            scheduleRepository.getSchedules(userId).collect { schedules ->
                _uiState.value = SchedulesUiState(
                    schedules = schedules,
                    groupedSchedules = schedules.groupBy { it.dayOfWeek }
                )
            }
        }
    }

    fun onEvent(event: SchedulesEvent) {
        when (event) {
            is SchedulesEvent.LoadData -> loadData()
            is SchedulesEvent.DeleteSchedule -> deleteSchedule(event.schedule)
            is SchedulesEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            scheduleRepository.deleteSchedule(userId, schedule).onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Error al eliminar") }
            }
        }
    }
}
