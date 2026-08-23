package com.notifast.app.presentation.feature.grades

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.AcademicStats
import com.notifast.app.domain.model.GradeScale

enum class GradesTab {
    SUBJECTS,
    ANALYTICS
}

data class SubjectGradeInfo(
    val subject: String,
    val evaluations: List<Evaluation>,
    val average: Double
)

data class GradesUiState(
    val isLoading: Boolean = false,
    val selectedTab: GradesTab = GradesTab.SUBJECTS,
    val evaluationsBySubject: Map<String, List<Evaluation>> = emptyMap(),
    val subjects: List<SubjectData> = emptyList(),
    val globalAverage: Double = 0.0,
    val gradeScale: GradeScale = GradeScale.SCALE_0_10,
    val subjectGrades: List<SubjectGradeInfo> = emptyList(),
    val academicStats: AcademicStats = AcademicStats(),
    val error: String? = null,
    val userFirstName: String = "",
    val userEmail: String = "",
    val userProfilePictureUrl: String? = null
)

sealed interface GradesEvent {
    data object LoadData : GradesEvent
    data class SelectTab(val tab: GradesTab) : GradesEvent
    data class UpdateGrade(val evaluationId: String, val grade: Double?, val isPresented: Boolean) : GradesEvent
    data class SetGradeScale(val scale: GradeScale) : GradesEvent
    data object ClearError : GradesEvent
}
