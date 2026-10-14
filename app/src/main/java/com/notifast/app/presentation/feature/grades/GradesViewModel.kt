package com.notifast.app.presentation.feature.grades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.CalculateAcademicStatsUseCase
import com.notifast.app.domain.usecase.CalculateWeightedAverageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val evaluationRepository: EvaluationRepository,
    private val noteRepository: NoteRepository,
    private val scheduleRepository: ScheduleRepository,
    private val authRepository: AuthRepository,
    private val userPrefsRepository: UserPrefsRepository,
    private val calculateWeightedAverageUseCase: CalculateWeightedAverageUseCase,
    private val calculateAcademicStatsUseCase: CalculateAcademicStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradesUiState())
    val uiState: StateFlow<GradesUiState> = _uiState.asStateFlow()

    private var loadJob: kotlinx.coroutines.Job? = null

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
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true) }
            combine(
                evaluationRepository.getEvaluations(userId),
                noteRepository.getSubjects(userId),
                scheduleRepository.getSchedules(userId),
                userPrefsRepository.defaultGradeScaleFlow,
                userPrefsRepository.minPassingGradeFlow
            ) { evaluations, subjects, schedules, scaleId, minPassing ->
                val evalsBySubject = evaluations.groupBy { it.subjectName }
                val scale = GradeScale.fromId(scaleId)
                // Only honor a custom passing grade if it is valid for the active scale;
                // the DataStore default (11.0) is a 0-20 threshold and must not apply to 0-10.
                val customMin = minPassing.toDouble().takeIf {
                    it > scale.minGrade && it <= scale.maxGrade
                }
                val globalAverage = calculateWeightedAverageUseCase(evaluations, scale) ?: 0.0
                val subjectGrades = evalsBySubject.map { (subject, evals) ->
                    val subjData = subjects.find { it.name.equals(subject, ignoreCase = true) }
                    SubjectGradeInfo(
                        subject = subject,
                        evaluations = evals,
                        average = calculateWeightedAverageUseCase(evals, scale) ?: 0.0,
                        icon = subjData?.icon ?: "📚",
                        colorHex = subjData?.colorHex.orEmpty()
                    )
                }.sortedBy { it.subject }

                val academicStats = calculateAcademicStatsUseCase(
                    evaluations = evaluations,
                    subjects = subjects,
                    schedules = schedules,
                    gradeScale = scale,
                    customMinPassingGrade = customMin
                )

                _uiState.update { prev ->
                    prev.copy(
                        evaluationsBySubject = evalsBySubject,
                        subjects = subjects,
                        globalAverage = globalAverage,
                        gradeScale = scale,
                        minPassingGrade = customMin ?: scale.minPassingGrade,
                        subjectGrades = subjectGrades,
                        academicStats = academicStats,
                        isLoading = false
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar datos") }
            }.collect { }
        }
    }

    fun onEvent(event: GradesEvent) {
        when (event) {
            is GradesEvent.LoadData -> loadData()
            is GradesEvent.SelectTab -> _uiState.update { it.copy(selectedTab = event.tab) }
            is GradesEvent.UpdateGrade -> updateGrade(event.evaluationId, event.grade, event.isPresented)
            is GradesEvent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun updateGrade(evaluationId: String, grade: Double?, isPresented: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val current = _uiState.value.evaluationsBySubject.values.flatten()
                .find { it.id == evaluationId } ?: return@launch
            val activeScale = _uiState.value.gradeScale
            val updated = current.copy(
                grade = grade?.let { activeScale.roundGrade(it) },
                isPresented = isPresented,
                gradeScaleId = activeScale.id
            )
            evaluationRepository.updateEvaluation(userId, updated, null).onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Error al actualizar nota") }
            }
        }
    }
}
