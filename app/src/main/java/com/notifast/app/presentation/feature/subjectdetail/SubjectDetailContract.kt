package com.notifast.app.presentation.feature.subjectdetail

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.usecase.RequiredGradeResult

enum class SubjectQuadrant {
    PANORAMA,
    EVALUATIONS,
    NOTES,
    SCHEDULES
}

sealed interface SubjectMilestone {
    data class Exam(val evaluation: Evaluation, val daysRemaining: Long) : SubjectMilestone
    data class Class(val schedule: Schedule, val isToday: Boolean, val dayName: String) : SubjectMilestone
    data object None : SubjectMilestone
}

data class SubjectDetailUiState(
    val userId: String = "",
    val isLoading: Boolean = true,
    val subjectName: String = "",
    val subjectIcon: String = "📚",
    val subjectColorHex: String = "",
    val selectedQuadrant: SubjectQuadrant = SubjectQuadrant.PANORAMA,
    val notes: List<Note> = emptyList(),
    val filteredNotes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val selectedTag: String? = null,
    val availableTags: List<String> = emptyList(),
    val evaluations: List<Evaluation> = emptyList(),
    val schedules: List<Schedule> = emptyList(),
    val averageGrade: Double? = null,
    val completedEvalCount: Int = 0,
    val pendingEvalCount: Int = 0,
    val upcomingEvaluations: List<Evaluation> = emptyList(),
    val nextMilestone: SubjectMilestone = SubjectMilestone.None,
    val progressPercent: Float = 0f,
    val totalAssignedWeight: Double = 0.0,
    val evaluatedWeight: Double = 0.0,
    val pendingWeight: Double = 0.0,
    val availableWeight: Double = 100.0,
    val gradeScale: GradeScale = GradeScale.SCALE_0_10,
    val targetGrade: Double = 6.0,
    val simulationResult: RequiredGradeResult? = null,
    val classroom: String = "",
    val teacher: String = "",
    val modality: String = Schedule.MODALITY_PRESENCIAL,
    val meetingUrl: String? = null,
    val error: String? = null
)

sealed interface SubjectDetailEvent {
    data object LoadData : SubjectDetailEvent
    data class SelectQuadrant(val quadrant: SubjectQuadrant) : SubjectDetailEvent
    data class UpdateSearchQuery(val query: String) : SubjectDetailEvent
    data class SelectTag(val tag: String?) : SubjectDetailEvent
    data class SaveGrade(val evaluationId: String, val grade: Double?, val isPresented: Boolean) : SubjectDetailEvent
    data class DeleteEvaluation(val evaluationId: String) : SubjectDetailEvent
    data class DeleteSchedule(val schedule: Schedule) : SubjectDetailEvent
    data class UpdateTargetGrade(val targetGrade: Double) : SubjectDetailEvent
    data class SetGradeScale(val scale: GradeScale) : SubjectDetailEvent
    data object ClearError : SubjectDetailEvent
}
