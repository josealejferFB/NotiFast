package com.notifast.app.presentation.feature.addschedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.usecase.ScheduleOverlapResult
import com.notifast.app.domain.usecase.ValidateScheduleOverlapUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository,
    private val validateScheduleOverlapUseCase: ValidateScheduleOverlapUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddScheduleUiState())
    val uiState: StateFlow<AddScheduleUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val subjects = noteRepository.getSubjects(userId).first()
            val schedules = scheduleRepository.getSchedules(userId).first()
            _uiState.update { it.copy(subjects = subjects, existingSchedules = schedules) }
        }
    }

    fun onEvent(event: AddScheduleEvent) {
        when (event) {
            is AddScheduleEvent.UpdateSubjectName -> _uiState.update { it.copy(subjectName = event.subjectName) }
            is AddScheduleEvent.UpdateModality -> _uiState.update {
                if (event.modality == Schedule.MODALITY_VIRTUAL) {
                    it.copy(modality = event.modality, dayOfWeek = 0, startTime = "", endTime = "", classroom = "", conflictingSchedules = emptyList())
                } else {
                    it.copy(modality = event.modality, meetingUrl = "")
                }
            }
            is AddScheduleEvent.UpdateDayOfWeek -> {
                _uiState.update { it.copy(dayOfWeek = event.day) }
                checkConflicts()
            }
            is AddScheduleEvent.UpdateStartTime -> {
                _uiState.update { it.copy(startTime = event.time) }
                checkConflicts()
            }
            is AddScheduleEvent.UpdateEndTime -> {
                _uiState.update { it.copy(endTime = event.time) }
                checkConflicts()
            }
            is AddScheduleEvent.UpdateClassroom -> _uiState.update { it.copy(classroom = event.classroom) }
            is AddScheduleEvent.UpdateTeacher -> _uiState.update { it.copy(teacher = event.teacher) }
            is AddScheduleEvent.UpdateMeetingUrl -> _uiState.update { it.copy(meetingUrl = event.url) }
            is AddScheduleEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is AddScheduleEvent.SaveSchedule -> saveSchedule()
        }
    }

    private fun checkConflicts() {
        val state = _uiState.value
        if (state.modality != Schedule.MODALITY_PRESENCIAL || state.startTime.isBlank() || state.endTime.isBlank()) {
            _uiState.update { it.copy(conflictingSchedules = emptyList()) }
            return
        }
        val tempSchedule = Schedule(
            subjectName = state.subjectName,
            dayOfWeek = state.dayOfWeek,
            startTime = state.startTime,
            endTime = state.endTime
        )
        val result = validateScheduleOverlapUseCase(state.existingSchedules, tempSchedule)
        val conflicts = when (result) {
            is ScheduleOverlapResult.Conflict -> result.conflictingSchedules
            is ScheduleOverlapResult.NoOverlap -> emptyList()
        }
        _uiState.update { it.copy(conflictingSchedules = conflicts) }
    }

    private fun saveSchedule() {
        val state = _uiState.value
        if (state.subjectName.isBlank()) {
            _uiState.update { it.copy(error = "El nombre de la materia es requerido") }
            return
        }
        if (state.modality == Schedule.MODALITY_PRESENCIAL) {
            if (state.startTime.isBlank() || state.endTime.isBlank()) {
                _uiState.update { it.copy(error = "Las horas de inicio y fin son requeridas") }
                return
            }
        }

        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isSaving = true, error = null) }

            val subjectColorHex = state.subjects.find { it.name == state.subjectName }?.colorHex
            val subjectColor = if (!subjectColorHex.isNullOrBlank()) {
                try {
                    android.graphics.Color.parseColor(subjectColorHex).toLong()
                } catch (e: Exception) { 0L }
            } else 0L

            val schedule = Schedule(
                userId = userId,
                subjectName = state.subjectName,
                modality = state.modality,
                dayOfWeek = state.dayOfWeek,
                startTime = state.startTime,
                endTime = state.endTime,
                classroom = state.classroom,
                teacher = state.teacher,
                meetingUrl = state.meetingUrl.takeIf { it.isNotBlank() },
                color = subjectColor,
                createdAt = Date()
            )

            scheduleRepository.addSchedule(userId, schedule).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaved = true, isSaving = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message ?: "Error al guardar horario", isSaving = false) }
                }
            )
        }
    }
}
