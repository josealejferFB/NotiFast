package com.notifast.app.presentation.feature.schedules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.CancelAlertUseCase
import com.notifast.app.domain.util.ErrorSanitizer
import com.notifast.app.domain.util.ScheduleCalculations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
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
    private val userPrefsRepository: UserPrefsRepository? = null,
    private val cancelAlertUseCase: CancelAlertUseCase? = null,
    private val noteRepository: NoteRepository? = null,
    private val googleAccountRepository: com.notifast.app.domain.repository.GoogleAccountRepository? = null,
    private val parseUnergSchedulePdfUseCase: com.notifast.app.domain.usecase.schedule.ParseUnergSchedulePdfUseCase? = null,
    private val importUnergScheduleUseCase: com.notifast.app.domain.usecase.schedule.ImportUnergScheduleUseCase? = null
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
    ) : this(scheduleRepository, evaluationRepository, authRepository, null, null, null, null, null, null) {
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
            val accountFlow = googleAccountRepository?.account ?: kotlinx.coroutines.flow.flowOf(null)
            combine(
                authRepository.observeUserProfile(uid),
                accountFlow
            ) { user, account ->
                user to account
            }.collect { (user, account) ->
                val firstName = user?.firstName?.takeIf { it.isNotBlank() }
                    ?: account?.displayName?.split(" ")?.firstOrNull().orEmpty()
                val email = user?.email?.takeIf { !it.endsWith("@local.notifast") && it.isNotBlank() }
                    ?: account?.email.orEmpty()
                val photo = user?.profilePictureUrl?.takeIf { it.isNotBlank() } ?: account?.photoUrl

                _uiState.update { it.copy(
                    userFirstName = firstName,
                    userEmail = email,
                    userProfilePictureUrl = photo
                ) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            val subjectsFlow = noteRepository?.getSubjects(userId) ?: flowOf(emptyList())
            combine(
                scheduleRepository.getSchedules(userId),
                evaluationRepository.getEvaluations(userId),
                subjectsFlow
            ) { schedules, evaluations, subjects ->
                Triple(schedules, evaluations, subjects)
            }.collect { (schedules, evaluations, subjects) ->
                updateState(schedules = schedules, evaluations = evaluations, subjects = subjects)
            }
        }
        viewModelScope.launch {
            userPrefsRepository?.firstDayOfWeekFlow?.collect { day ->
                _uiState.update { current ->
                    val evalCounts = ScheduleCalculations.calculateEvaluationCountsForWeek(
                        evaluations = current.evaluations,
                        referenceDate = current.selectedDate,
                        firstDayOfWeek = day
                    )
                    current.copy(firstDayOfWeek = day, evaluationCountsByDay = evalCounts)
                }
            }
        }
        viewModelScope.launch {
            userPrefsRepository?.defaultGradeScaleFlow?.collect { scaleId ->
                _uiState.update { it.copy(gradeScale = GradeScale.fromId(scaleId)) }
            }
        }
    }

    fun onEvent(event: SchedulesEvent) {
        when (event) {
            is SchedulesEvent.LoadData -> loadData()
            is SchedulesEvent.RefreshLiveStatus -> updateState(
                schedules = _uiState.value.schedules,
                evaluations = _uiState.value.evaluations,
                subjects = _uiState.value.subjects
            )
            is SchedulesEvent.SelectDay -> selectDay(event.dayOfWeek)
            is SchedulesEvent.SelectDate -> selectDate(event.date)
            is SchedulesEvent.ChangeMonth -> changeMonth(event.amount)
            is SchedulesEvent.GoToToday -> goToToday()
            is SchedulesEvent.SetViewMode -> _uiState.update { it.copy(viewMode = event.mode) }
            is SchedulesEvent.DeleteSchedule -> deleteSchedule(event.schedule)
            is SchedulesEvent.DeleteEvaluation -> deleteEvaluation(event.evaluation)
            is SchedulesEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is SchedulesEvent.ParseUnergPdf -> parseUnergPdf(event.uri, event.context)
            is SchedulesEvent.DismissUnergImportSheet -> _uiState.update { it.copy(isUnergImportSheetVisible = false) }
            is SchedulesEvent.ToggleUnergSubjectSelected -> toggleUnergSubjectSelected(event.subjectName)
            is SchedulesEvent.SetUnergSelectAll -> setUnergSelectAll(event.selectAll)
            is SchedulesEvent.SetUnergConflictStrategy -> _uiState.update { it.copy(unergImportStrategy = event.strategy) }
            is SchedulesEvent.ConfirmUnergImport -> confirmUnergImport()
            is SchedulesEvent.ClearImportSuccess -> _uiState.update { it.copy(importSuccessMessage = null) }
        }
    }

    private fun parseUnergPdf(uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val parserUseCase = parseUnergSchedulePdfUseCase ?: return@launch
            _uiState.update { it.copy(isParsingPdf = true, error = null) }
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw java.lang.IllegalArgumentException("No se pudo abrir el archivo PDF.")
                val result = inputStream.use { stream ->
                    parserUseCase(userId, stream)
                }
                result.fold(
                    onSuccess = { previewData ->
                        _uiState.update {
                            it.copy(
                                isParsingPdf = false,
                                unergImportPreview = previewData,
                                isUnergImportSheetVisible = true
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isParsingPdf = false,
                                error = ErrorSanitizer.sanitize(e, "Error al procesar el horario UNERG")
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isParsingPdf = false,
                        error = ErrorSanitizer.sanitize(e, "Error al abrir el archivo PDF")
                    )
                }
            }
        }
    }

    private fun toggleUnergSubjectSelected(subjectName: String) {
        _uiState.update { state ->
            val preview = state.unergImportPreview ?: return@update state
            val updatedSubjects = preview.subjects.map {
                if (it.subjectName.equals(subjectName, ignoreCase = true)) {
                    it.copy(selected = !it.selected)
                } else it
            }
            state.copy(unergImportPreview = preview.copy(subjects = updatedSubjects))
        }
    }

    private fun setUnergSelectAll(selectAll: Boolean) {
        _uiState.update { state ->
            val preview = state.unergImportPreview ?: return@update state
            val updatedSubjects = preview.subjects.map { it.copy(selected = selectAll) }
            state.copy(unergImportPreview = preview.copy(subjects = updatedSubjects))
        }
    }

    private fun confirmUnergImport() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val importerUseCase = importUnergScheduleUseCase ?: return@launch
            val preview = _uiState.value.unergImportPreview ?: return@launch
            val selected = preview.subjects.filter { it.selected }
            if (selected.isEmpty()) {
                _uiState.update { it.copy(error = "Selecciona al menos una materia para importar") }
                return@launch
            }

            _uiState.update { it.copy(isImportingPdf = true, error = null) }
            importerUseCase(userId, selected, _uiState.value.unergImportStrategy).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isImportingPdf = false,
                            isUnergImportSheetVisible = false,
                            unergImportPreview = null,
                            importSuccessMessage = "Horario importado: ${result.createdSubjectsCount + result.reusedSubjectsCount} materias y ${result.createdSchedulesCount} clases"
                        )
                    }
                    loadData()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isImportingPdf = false,
                            error = ErrorSanitizer.sanitize(e, "Error al importar el horario")
                        )
                    }
                }
            )
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
                conflicts,
                current.subjectMap
            )

            val startOfWeek = ScheduleCalculations.getStartOfWeek(current.selectedDate, current.firstDayOfWeek)
            val dayOffset = if (current.firstDayOfWeek.equals("SUNDAY", ignoreCase = true)) {
                dayOfWeek
            } else {
                (dayOfWeek - 1 + 7) % 7
            }
            val adjustedDate = (startOfWeek.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val key = dateFormat.format(adjustedDate.time)
            val dailyEvals = current.evaluationsByDate[key] ?: emptyList()
            val evalCounts = ScheduleCalculations.calculateEvaluationCountsForWeek(
                evaluations = current.evaluations,
                referenceDate = adjustedDate,
                firstDayOfWeek = current.firstDayOfWeek
            )

            current.copy(
                selectedDay = dayOfWeek,
                selectedDate = adjustedDate,
                dailyEvaluations = dailyEvals,
                conflicts = conflicts,
                timelineItems = timelineItems,
                evaluationCountsByDay = evalCounts
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
                conflicts,
                current.subjectMap
            )
            val evalCounts = ScheduleCalculations.calculateEvaluationCountsForWeek(
                evaluations = current.evaluations,
                referenceDate = date,
                firstDayOfWeek = current.firstDayOfWeek
            )

            current.copy(
                selectedDate = date,
                selectedDay = dayOfWeek,
                dailyEvaluations = dailyEvals,
                conflicts = conflicts,
                timelineItems = timelineItems,
                evaluationCountsByDay = evalCounts
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
        subjects: List<SubjectData> = _uiState.value.subjects,
        selectedDay: Int = _uiState.value.selectedDay,
        selectedDate: Calendar = _uiState.value.selectedDate,
        viewMode: ScheduleViewMode = _uiState.value.viewMode
    ) {
        val subjectMap = subjects.associateBy { it.name.lowercase().trim() }
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
            conflicts,
            subjectMap
        )
        val weeklySummary = ScheduleCalculations.calculateWeeklySummary(schedules)

        val evalsByDate = evaluations.filter { it.date != null }.groupBy {
            dateFormat.format(it.date!!)
        }
        val dateKey = dateFormat.format(selectedDate.time)
        val dailyEvals = evalsByDate[dateKey] ?: emptyList()
        val evalCounts = ScheduleCalculations.calculateEvaluationCountsForWeek(
            evaluations = evaluations,
            referenceDate = selectedDate,
            firstDayOfWeek = _uiState.value.firstDayOfWeek
        )

        _uiState.update { current ->
            current.copy(
                isLoading = false,
                schedules = schedules,
                groupedSchedules = grouped,
                evaluations = evaluations,
                evaluationsByDate = evalsByDate,
                dailyEvaluations = dailyEvals,
                subjects = subjects,
                subjectMap = subjectMap,
                selectedDay = selectedDay,
                selectedDate = selectedDate,
                viewMode = viewMode,
                liveClassStatus = liveStatus,
                conflicts = conflicts,
                timelineItems = timelineItems,
                weeklySummary = weeklySummary,
                evaluationCountsByDay = evalCounts
            )
        }
    }

    private fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            scheduleRepository.deleteSchedule(userId, schedule).onSuccess {
                cancelAlertUseCase?.cancelClassAlert(schedule.id)
            }.onFailure { e ->
                _uiState.update { it.copy(error = ErrorSanitizer.sanitize(e, "Error al eliminar horario")) }
            }
        }
    }

    private fun deleteEvaluation(evaluation: Evaluation) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            evaluationRepository.deleteEvaluation(userId, evaluation).onSuccess {
                cancelAlertUseCase?.cancelEvaluationAlert(evaluation.id)
            }.onFailure { e ->
                _uiState.update { it.copy(error = ErrorSanitizer.sanitize(e, "Error al eliminar evaluación")) }
            }
        }
    }
}
