package com.notifast.app.presentation.feature.subjectdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.CancelAlertUseCase
import com.notifast.app.domain.usecase.CalculateRequiredGradeUseCase
import com.notifast.app.domain.usecase.CalculateWeightedAverageUseCase
import com.notifast.app.domain.usecase.ExportSubjectPackageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
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
    private val cancelAlertUseCase: CancelAlertUseCase? = null
) : ViewModel() {

    private val subjectName: String = checkNotNull(savedStateHandle["subjectName"])
    private val _uiState = MutableStateFlow(SubjectDetailUiState(subjectName = subjectName))
    val uiState: StateFlow<SubjectDetailUiState> = _uiState.asStateFlow()

    // Single source of filter/UI inputs lives inside UiState (MVI). These private
    // flows only fan-in to one combine without being exposed as parallel state.
    // targetGradeOverride=null → simulator target follows the active scale minPassing.
    private val filterInputs = MutableStateFlow(
        FilterData(
            targetGradeOverride = null,
            quadrant = SubjectQuadrant.PANORAMA,
            query = "",
            tag = null
        )
    )

    private var loadJob: kotlinx.coroutines.Job? = null

    init { loadData() }

    private fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            _uiState.update { it.copy(userId = userId, isLoading = true) }

            val gradePrefs = combine(
                userPrefsRepository.defaultGradeScaleFlow,
                userPrefsRepository.minPassingGradeFlow
            ) { scaleId, minPassing -> ScalePrefs(scaleId, minPassing) }

            val dataFlow = combine(
                noteRepository.getNotesBySubject(userId, subjectName),
                evaluationRepository.getEvaluations(userId),
                scheduleRepository.getSchedules(userId),
                noteRepository.getSubjects(userId),
                gradePrefs
            ) { notes, evals, schedules, subjects, prefs ->
                DomainData(notes, evals, schedules, subjects, prefs.scaleId, prefs.minPassing)
            }

            combine(dataFlow, filterInputs) { domain, filter ->
                val subjectEvals = domain.evaluations.filter { it.subjectName.equals(subjectName, ignoreCase = true) }
                val subjectSchedules = domain.schedules.filter { it.subjectName.equals(subjectName, ignoreCase = true) }
                val subjectData = domain.subjects.find { it.name.equals(subjectName, ignoreCase = true) }
                    ?: SubjectData(name = subjectName)
                val scale = GradeScale.fromId(domain.scaleId)
                val customMinPassing = domain.minPassing.toDouble().takeIf {
                    it > scale.minGrade && it <= scale.maxGrade
                }
                val minPassing = GradePolicy.resolveMinPassing(scale, customMinPassing)
                val targetGrade = filter.targetGradeOverride
                    ?.let { GradePolicy.clampGrade(it, scale) }
                    ?: minPassing

                val avg = calculateWeightedAverageUseCase(subjectEvals, scale)
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

                _uiState.update { prev ->
                    prev.copy(
                        userId = userId,
                        isLoading = false,
                        error = prev.error,
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
                        averageGrade = avg?.let { scale.roundGrade(it) },
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
                        minPassingGrade = minPassing,
                        targetGrade = targetGrade,
                        simulationResult = calculateRequiredGradeUseCase(
                            evaluations = subjectEvals,
                            targetGrade = targetGrade,
                            scale = scale,
                            minPassingGrade = minPassing
                        ),
                        classroom = firstSchedule?.classroom.orEmpty(),
                        teacher = firstSchedule?.teacher.orEmpty(),
                        modality = firstSchedule?.modality ?: Schedule.MODALITY_PRESENCIAL,
                        meetingUrl = firstSchedule?.meetingUrl
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar") }
            }.collect { }
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
            is SubjectDetailEvent.SelectQuadrant -> filterInputs.update { it.copy(quadrant = event.quadrant) }
            is SubjectDetailEvent.UpdateSearchQuery -> filterInputs.update { it.copy(query = event.query) }
            is SubjectDetailEvent.SelectTag -> filterInputs.update { it.copy(tag = event.tag) }
            is SubjectDetailEvent.SaveGrade -> saveGrade(event.evaluationId, event.grade, event.isPresented)
            is SubjectDetailEvent.DeleteEvaluation -> deleteEvaluation(event.evaluationId)
            is SubjectDetailEvent.DeleteSchedule -> deleteSchedule(event.schedule)
            is SubjectDetailEvent.UpdateTargetGrade -> filterInputs.update { it.copy(targetGradeOverride = event.targetGrade) }
            is SubjectDetailEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun saveGrade(evalId: String, grade: Double?, isPresented: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val eval = _uiState.value.evaluations.find { it.id == evalId } ?: return@launch
            val activeScale = _uiState.value.gradeScale
            val updated = eval.copy(
                grade = grade?.let { activeScale.roundGrade(it) },
                isPresented = isPresented,
                gradeScaleId = activeScale.id
            )
            evaluationRepository.updateEvaluation(userId, updated, null)
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

    private data class DomainData(
        val notes: List<Note>,
        val evaluations: List<Evaluation>,
        val schedules: List<Schedule>,
        val subjects: List<SubjectData>,
        val scaleId: String,
        val minPassing: Float
    )

    private data class ScalePrefs(val scaleId: String, val minPassing: Float)

    private data class FilterData(
        val targetGradeOverride: Double?,
        val quadrant: SubjectQuadrant,
        val query: String,
        val tag: String?
    )
}