package com.notifast.app.presentation.feature.editschedule

import androidx.lifecycle.SavedStateHandle
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
class EditScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository,
    private val validateScheduleOverlapUseCase: ValidateScheduleOverlapUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val scheduleId: String = checkNotNull(savedStateHandle["scheduleId"])
    private var originalSchedule: Schedule? = null

    private val _uiState = MutableStateFlow(EditScheduleUiState())
    val uiState: StateFlow<EditScheduleUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val subjects = noteRepository.getSubjects(userId).first()
            val schedules = scheduleRepository.getSchedules(userId).first()
            val schedule = schedules.find { it.id == scheduleId }
            if (schedule != null) {
                originalSchedule = schedule
                _uiState.update { 
                    it.copy(
                        subjects = subjects,
                        existingSchedules = schedules,
                        subjectName = schedule.subjectName,
                        modality = schedule.modality,
                        dayOfWeek = schedule.dayOfWeek,
                        startTime = schedule.startTime,
                        endTime = schedule.endTime,
                        classroom = schedule.classroom,
                        teacher = schedule.teacher,
                        meetingUrl = schedule.meetingUrl ?: ""
                    ) 
                }
            } else {
                _uiState.update { it.copy(subjects = subjects, existingSchedules = schedules, error = "Horario no encontrado") }
            }
        }
    }

    fun onEvent(event: EditScheduleEvent) {
        when (event) {
            is EditScheduleEvent.UpdateSubjectName -> _uiState.update { it.copy(subjectName = event.subjectName) }
            is EditScheduleEvent.UpdateModality -> _uiState.update {
                if (event.modality == Schedule.MODALITY_VIRTUAL) {
                    it.copy(modality = event.modality, dayOfWeek = 0, startTime = "", endTime = "", classroom = "", conflictingSchedules = emptyList())
                } else {
                    it.copy(modality = event.modality, meetingUrl = "")
                }
            }
            is EditScheduleEvent.UpdateDayOfWeek -> {
                _uiState.update { it.copy(dayOfWeek = event.day) }
                checkConflicts()
            }
            is EditScheduleEvent.UpdateStartTime -> {
                _uiState.update { it.copy(startTime = event.time) }
                checkConflicts()
            }
            is EditScheduleEvent.UpdateEndTime -> {
                _uiState.update { it.copy(endTime = event.time) }
                checkConflicts()
            }
            is EditScheduleEvent.UpdateClassroom -> _uiState.update { it.copy(classroom = event.classroom) }
            is EditScheduleEvent.UpdateTeacher -> _uiState.update { it.copy(teacher = event.teacher) }
            is EditScheduleEvent.UpdateMeetingUrl -> _uiState.update { it.copy(meetingUrl = event.url) }
            is EditScheduleEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is EditScheduleEvent.SaveSchedule -> saveSchedule()
        }
    }

    private fun checkConflicts() {
        val state = _uiState.value
        if (state.modality != Schedule.MODALITY_PRESENCIAL || state.startTime.isBlank() || state.endTime.isBlank()) {
            _uiState.update { it.copy(conflictingSchedules = emptyList()) }
            return
        }
        val tempSchedule = Schedule(
            id = scheduleId,
            subjectName = state.subjectName,
            dayOfWeek = state.dayOfWeek,
            startTime = state.startTime,
            endTime = state.endTime
        )
        val result = validateScheduleOverlapUseCase(
            existingSchedules = state.existingSchedules,
            newSchedule = tempSchedule,
            excludeScheduleId = scheduleId
        )
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
                id = scheduleId,
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
                createdAt = originalSchedule?.createdAt ?: Date()
            )

            scheduleRepository.updateSchedule(userId, schedule).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaved = true, isSaving = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message ?: "Error al actualizar horario", isSaving = false) }
                }
            )
        }
    }
}
