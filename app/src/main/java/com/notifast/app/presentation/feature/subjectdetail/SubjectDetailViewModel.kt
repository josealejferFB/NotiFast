package com.notifast.app.presentation.feature.subjectdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.repository.*
import com.notifast.app.domain.usecase.CalculateRequiredGradeUseCase
import com.notifast.app.domain.usecase.CalculateWeightedAverageUseCase
import com.notifast.app.domain.usecase.ExportSubjectPackageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SubjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val authRepository: AuthRepository,
    private val userPrefsRepository: UserPrefsRepository,
    private val calculateWeightedAverageUseCase: CalculateWeightedAverageUseCase,
    private val calculateRequiredGradeUseCase: CalculateRequiredGradeUseCase,
    val exportSubjectPackageUseCase: ExportSubjectPackageUseCase,
    private val cancelAlertUseCase: com.notifast.app.domain.usecase.CancelAlertUseCase? = null
) : ViewModel() {

    private val subjectName: String = checkNotNull(savedStateHandle["subjectName"])
    private val _uiState = MutableStateFlow(SubjectDetailUiState(subjectName = subjectName))
    val uiState: StateFlow<SubjectDetailUiState> = _uiState.asStateFlow()

    private val _targetGrade = MutableStateFlow(6.0)
    private val _selectedQuadrant = MutableStateFlow(SubjectQuadrant.PANORAMA)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedTag = MutableStateFlow<String?>(null)

    init { loadData() }

    private fun loadData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            _uiState.update { it.copy(userId = userId, isLoading = true) }

            val dataFlow = combine(
                noteRepository.getNotesBySubject(userId, subjectName),
                evaluationRepository.getEvaluations(userId),
                scheduleRepository.getSchedules(userId),
                noteRepository.getSubjects(userId),
                userPrefsRepository.defaultGradeScaleFlow
            ) { notes, evals, schedules, subjects, scaleId -> DomainData(notes, evals, schedules, subjects, scaleId) }

            val filterFlow = combine(_targetGrade, _selectedQuadrant, _searchQuery, _selectedTag) { tg, quad, q, tag -> FilterData(tg, quad, q, tag) }

            combine(dataFlow, filterFlow) { domain, filter ->
                val subjectEvals = domain.evaluations.filter { it.subjectName == subjectName }
                val subjectSchedules = domain.schedules.filter { it.subjectName == subjectName }
                val subjectData = domain.subjects.find { it.name == subjectName } ?: SubjectData(name = subjectName)
                val scale = GradeScale.fromId(domain.scaleId)

                val avg = calculateWeightedAverageUseCase(subjectEvals)
                val upcoming = subjectEvals.filter { !it.isPresented && it.date != null }.sortedBy { it.date }.take(5)
                val completed = subjectEvals.count { it.isPresented }
                val total = subjectEvals.size

                val totalWeight = subjectEvals.mapNotNull { it.weight }.sum()
                val evalWeight = subjectEvals.filter { it.isPresented }.mapNotNull { it.weight }.sum()
                val pendWeight = subjectEvals.filter { !it.isPresented }.mapNotNull { it.weight }.sum()

                val allTags = domain.notes.flatMap { it.tags }.distinct()
                val filteredNotes = domain.notes.filter { n ->
                    (filter.tag == null || n.tags.contains(filter.tag)) &&
                    (filter.query.isBlank() || n.title.contains(filter.query, true) || n.content.contains(filter.query, true))
                }

                val milestone = calculateMilestone(upcoming.firstOrNull(), subjectSchedules)
                val firstSchedule = subjectSchedules.firstOrNull()

                SubjectDetailUiState(
                    userId = userId,
                    isLoading = false,
                    subjectName = subjectName,
                    subjectIcon = subjectData.icon,
                    subjectColorHex = subjectData.colorHex,
                    selectedQuadrant = filter.quadrant,
                    notes = domain.notes,
                    filteredNotes = filteredNotes,
                    searchQuery = filter.query,
                    selectedTag = filter.tag,
                    availableTags = allTags,
                    evaluations = subjectEvals,
                    schedules = subjectSchedules,
                    averageGrade = avg?.let { kotlin.math.round(it * 10) / 10.0 },
                    completedEvalCount = completed,
                    pendingEvalCount = total - completed,
                    upcomingEvaluations = upcoming,
                    nextMilestone = milestone,
                    progressPercent = if (total > 0) completed.toFloat() / total else 0f,
                    totalAssignedWeight = totalWeight,
                    evaluatedWeight = evalWeight,
                    pendingWeight = pendWeight,
                    availableWeight = (100.0 - totalWeight).coerceAtLeast(0.0),
                    gradeScale = scale,
                    targetGrade = filter.targetGrade,
                    simulationResult = calculateRequiredGradeUseCase(subjectEvals, filter.targetGrade, scale),
                    classroom = firstSchedule?.classroom.orEmpty(),
                    teacher = firstSchedule?.teacher.orEmpty(),
                    modality = firstSchedule?.modality ?: Schedule.MODALITY_PRESENCIAL,
                    meetingUrl = firstSchedule?.meetingUrl
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    private fun calculateMilestone(nextEval: Evaluation?, schedules: List<Schedule>): SubjectMilestone {
        val now = System.currentTimeMillis()
        if (nextEval?.date != null) {
            val diffDays = maxOf(0L, (nextEval.date.time - now) / (1000L * 60 * 60 * 24))
            return SubjectMilestone.Exam(nextEval, diffDays)
        }
        val cal = Calendar.getInstance()
        val today = (cal.get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 0 else it }
        val nextClass = schedules.sortedBy { (it.dayOfWeek - today + 7) % 7 }.firstOrNull()
        return if (nextClass != null) {
            SubjectMilestone.Class(nextClass, nextClass.dayOfWeek == today, Schedule.DAY_NAMES.getOrElse(nextClass.dayOfWeek) { "" })
        } else SubjectMilestone.None
    }

    fun onEvent(event: SubjectDetailEvent) {
        when (event) {
            is SubjectDetailEvent.LoadData -> loadData()
            is SubjectDetailEvent.SelectQuadrant -> _selectedQuadrant.value = event.quadrant
            is SubjectDetailEvent.UpdateSearchQuery -> _searchQuery.value = event.query
            is SubjectDetailEvent.SelectTag -> _selectedTag.value = event.tag
            is SubjectDetailEvent.SaveGrade -> saveGrade(event.evaluationId, event.grade, event.isPresented)
            is SubjectDetailEvent.DeleteEvaluation -> deleteEvaluation(event.evaluationId)
            is SubjectDetailEvent.DeleteSchedule -> deleteSchedule(event.schedule)
            is SubjectDetailEvent.UpdateTargetGrade -> _targetGrade.value = event.targetGrade
            is SubjectDetailEvent.SetGradeScale -> viewModelScope.launch { userPrefsRepository.setDefaultGradeScale(event.scale.id) }
            is SubjectDetailEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun saveGrade(evalId: String, grade: Double?, isPresented: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val eval = _uiState.value.evaluations.find { it.id == evalId } ?: return@launch
            evaluationRepository.updateEvaluation(userId, eval.copy(grade = grade, isPresented = isPresented), null)
                .onSuccess {
                    if (grade != null || isPresented) {
                        cancelAlertUseCase?.cancelEvaluationAlert(evalId)
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message ?: "Error al actualizar nota") } }
        }
    }

    private fun deleteEvaluation(evalId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val eval = _uiState.value.evaluations.find { it.id == evalId } ?: return@launch
            evaluationRepository.deleteEvaluation(userId, eval)
                .onSuccess {
                    cancelAlertUseCase?.cancelEvaluationAlert(evalId)
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message ?: "Error al eliminar evaluación") } }
        }
    }

    private fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            scheduleRepository.deleteSchedule(userId, schedule)
                .onSuccess {
                    cancelAlertUseCase?.cancelClassAlert(schedule.id)
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message ?: "Error al eliminar horario") } }
        }
    }

    private data class DomainData(val notes: List<Note>, val evaluations: List<Evaluation>, val schedules: List<Schedule>, val subjects: List<SubjectData>, val scaleId: String)
    private data class FilterData(val targetGrade: Double, val quadrant: SubjectQuadrant, val query: String, val tag: String?)
}
