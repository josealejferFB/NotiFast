package com.notifast.app.presentation.feature.schedules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.util.ScheduleCalculations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SchedulesViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository,
    private val userPrefsRepository: com.notifast.app.domain.repository.UserPrefsRepository? = null,
    private val cancelAlertUseCase: com.notifast.app.domain.usecase.CancelAlertUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchedulesUiState())
    val uiState: StateFlow<SchedulesUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    internal var currentTimeProvider: () -> Pair<Int, String> = {
        val cal = Calendar.getInstance()
        val day = (cal.get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 0 else it }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        day to String.format(Locale.US, "%02d:%02d", hour, min)
    }

    constructor(
        scheduleRepository: ScheduleRepository,
        evaluationRepository: EvaluationRepository,
        authRepository: AuthRepository,
        timeProvider: () -> Pair<Int, String>
    ) : this(scheduleRepository, evaluationRepository, authRepository, null, null) {
        this.currentTimeProvider = timeProvider
        if (_uiState.value.schedules.isNotEmpty() || _uiState.value.evaluations.isNotEmpty()) {
            updateState(
                schedules = _uiState.value.schedules,
                evaluations = _uiState.value.evaluations
            )
        }
    }

    init {
        loadData()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: return@launch
            authRepository.getUserProfile(uid).onSuccess { user ->
                _uiState.update { it.copy(
                    userFirstName = user.firstName,
                    userEmail = user.email,
                    userProfilePictureUrl = user.profilePictureUrl
                ) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            combine(
                scheduleRepository.getSchedules(userId),
                evaluationRepository.getEvaluations(userId)
            ) { schedules, evaluations ->
                schedules to evaluations
            }.collect { (schedules, evaluations) ->
                updateState(schedules = schedules, evaluations = evaluations)
            }
        }
        viewModelScope.launch {
            userPrefsRepository?.firstDayOfWeekFlow?.collect { day ->
                _uiState.update { it.copy(firstDayOfWeek = day) }
            }
        }
    }

    fun onEvent(event: SchedulesEvent) {
        when (event) {
            is SchedulesEvent.LoadData -> loadData()
            is SchedulesEvent.RefreshLiveStatus -> updateState(
                schedules = _uiState.value.schedules,
                evaluations = _uiState.value.evaluations
            )
            is SchedulesEvent.SelectDay -> selectDay(event.dayOfWeek)
            is SchedulesEvent.SelectDate -> selectDate(event.date)
            is SchedulesEvent.ChangeMonth -> changeMonth(event.amount)
            is SchedulesEvent.GoToToday -> goToToday()
            is SchedulesEvent.SetViewMode -> _uiState.update { it.copy(viewMode = event.mode) }
            is SchedulesEvent.DeleteSchedule -> deleteSchedule(event.schedule)
            is SchedulesEvent.DeleteEvaluation -> deleteEvaluation(event.evaluation)
            is SchedulesEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun selectDay(dayOfWeek: Int) {
        _uiState.update { current ->
            val daySchedules = current.groupedSchedules[dayOfWeek] ?: emptyList()
            val (currentDay, currentTime) = currentTimeProvider()
            val conflicts = ScheduleCalculations.calculateConflicts(daySchedules)
            val timelineItems = ScheduleCalculations.calculateTimelineItems(
                daySchedules,
                dayOfWeek,
                currentDay,
                currentTime,
                conflicts
            )

            val adjustedDate = (current.selectedDate.clone() as Calendar).apply {
                val currentDow = (get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 0 else it }
                add(Calendar.DAY_OF_YEAR, dayOfWeek - currentDow)
            }
            val key = dateFormat.format(adjustedDate.time)
            val dailyEvals = current.evaluationsByDate[key] ?: emptyList()

            current.copy(
                selectedDay = dayOfWeek,
                selectedDate = adjustedDate,
                dailyEvaluations = dailyEvals,
                conflicts = conflicts,
                timelineItems = timelineItems
            )
        }
    }

    private fun selectDate(date: Calendar) {
        _uiState.update { current ->
            val dayOfWeek = (date.get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 0 else it }
            val key = dateFormat.format(date.time)
            val dailyEvals = current.evaluationsByDate[key] ?: emptyList()

            val daySchedules = current.groupedSchedules[dayOfWeek] ?: emptyList()
            val (currentDay, currentTime) = currentTimeProvider()
            val conflicts = ScheduleCalculations.calculateConflicts(daySchedules)
            val timelineItems = ScheduleCalculations.calculateTimelineItems(
                daySchedules,
                dayOfWeek,
                currentDay,
                currentTime,
                conflicts
            )

            current.copy(
                selectedDate = date,
                selectedDay = dayOfWeek,
                dailyEvaluations = dailyEvals,
                conflicts = conflicts,
                timelineItems = timelineItems
            )
        }
    }

    private fun changeMonth(amount: Int) {
        _uiState.update { current ->
            val newCal = (current.currentMonth.clone() as Calendar).apply {
                add(Calendar.MONTH, amount)
            }
            current.copy(currentMonth = newCal)
        }
    }

    private fun goToToday() {
        val today = Calendar.getInstance()
        selectDate(today)
        _uiState.update { it.copy(currentMonth = today.clone() as Calendar) }
    }

    private fun updateState(
        schedules: List<Schedule>,
        evaluations: List<Evaluation>,
        selectedDay: Int = _uiState.value.selectedDay,
        selectedDate: Calendar = _uiState.value.selectedDate,
        viewMode: ScheduleViewMode = _uiState.value.viewMode
    ) {
        val grouped = schedules.groupBy { it.dayOfWeek }
        val (currentDay, currentTime) = currentTimeProvider()
        val todaySchedules = grouped[currentDay] ?: emptyList()
        val liveStatus = ScheduleCalculations.calculateLiveClassStatus(todaySchedules, currentTime)

        val selectedDaySchedules = grouped[selectedDay] ?: emptyList()
        val conflicts = ScheduleCalculations.calculateConflicts(selectedDaySchedules)
        val timelineItems = ScheduleCalculations.calculateTimelineItems(
            selectedDaySchedules,
            selectedDay,
            currentDay,
            currentTime,
            conflicts
        )
        val weeklySummary = ScheduleCalculations.calculateWeeklySummary(schedules)

        val evalsByDate = evaluations.filter { it.date != null }.groupBy {
            dateFormat.format(it.date!!)
        }
        val dateKey = dateFormat.format(selectedDate.time)
        val dailyEvals = evalsByDate[dateKey] ?: emptyList()

        _uiState.update { current ->
            current.copy(
                isLoading = false,
                schedules = schedules,
                groupedSchedules = grouped,
                evaluations = evaluations,
                evaluationsByDate = evalsByDate,
                dailyEvaluations = dailyEvals,
                selectedDay = selectedDay,
                selectedDate = selectedDate,
                viewMode = viewMode,
                liveClassStatus = liveStatus,
                conflicts = conflicts,
                timelineItems = timelineItems,
                weeklySummary = weeklySummary
            )
        }
    }

    private fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            scheduleRepository.deleteSchedule(userId, schedule).onSuccess {
                cancelAlertUseCase?.cancelClassAlert(schedule.id)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Error al eliminar horario") }
            }
        }
    }

    private fun deleteEvaluation(evaluation: Evaluation) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            evaluationRepository.deleteEvaluation(userId, evaluation).onSuccess {
                cancelAlertUseCase?.cancelEvaluationAlert(evaluation.id)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Error al eliminar evaluación") }
            }
        }
    }
}
