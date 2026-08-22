package com.notifast.app.presentation.feature.editschedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle

@HiltViewModel
class EditScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val scheduleId: String = checkNotNull(savedStateHandle["scheduleId"])
    private var originalSchedule: Schedule? = null

    private val _uiState = MutableStateFlow(EditScheduleUiState())
    val uiState: StateFlow<EditScheduleUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
        loadSchedule()
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            // We get the schedule by filtering the schedules stream
            val schedules = scheduleRepository.getSchedules(userId).first()
            val schedule = schedules.find { it.id == scheduleId }
            if (schedule != null) {
                originalSchedule = schedule
                _uiState.update { 
                    it.copy(
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
                _uiState.update { it.copy(error = "Horario no encontrado") }
            }
        }
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val subjects = noteRepository.getSubjects(userId).first()
            _uiState.update { it.copy(subjects = subjects) }
        }
    }

    fun onEvent(event: EditScheduleEvent) {
        when (event) {
            is EditScheduleEvent.UpdateSubjectName -> _uiState.update { it.copy(subjectName = event.subjectName) }
            is EditScheduleEvent.UpdateModality -> _uiState.update {
                // When switching to virtual, clear time/day/classroom fields
                if (event.modality == Schedule.MODALITY_VIRTUAL) {
                    it.copy(modality = event.modality, dayOfWeek = 0, startTime = "", endTime = "", classroom = "")
                } else {
                    it.copy(modality = event.modality, meetingUrl = "")
                }
            }
            is EditScheduleEvent.UpdateDayOfWeek -> _uiState.update { it.copy(dayOfWeek = event.day) }
            is EditScheduleEvent.UpdateStartTime -> _uiState.update { it.copy(startTime = event.time) }
            is EditScheduleEvent.UpdateEndTime -> _uiState.update { it.copy(endTime = event.time) }
            is EditScheduleEvent.UpdateClassroom -> _uiState.update { it.copy(classroom = event.classroom) }
            is EditScheduleEvent.UpdateTeacher -> _uiState.update { it.copy(teacher = event.teacher) }
            is EditScheduleEvent.UpdateMeetingUrl -> _uiState.update { it.copy(meetingUrl = event.url) }
            is EditScheduleEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is EditScheduleEvent.SaveSchedule -> saveSchedule()
        }
    }

    private fun saveSchedule() {
        val state = _uiState.value
        if (state.subjectName.isBlank()) {
            _uiState.update { it.copy(error = "El nombre de la materia es requerido") }
            return
        }
        // Presencial requires time and day; virtual does not
        if (state.modality == Schedule.MODALITY_PRESENCIAL) {
            if (state.startTime.isBlank() || state.endTime.isBlank()) {
                _uiState.update { it.copy(error = "Las horas de inicio y fin son requeridas para clases presenciales") }
                return
            }
        }

        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isSaving = true, error = null) }

            // Find subject color
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
