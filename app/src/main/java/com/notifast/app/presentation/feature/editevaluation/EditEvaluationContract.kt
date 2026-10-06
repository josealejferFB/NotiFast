package com.notifast.app.presentation.feature.editevaluation

import com.notifast.app.data.model.EvaluationType
import com.notifast.app.data.model.SubjectData

data class EditEvaluationUiState(
    val isLoading: Boolean = false,
    val isLoadingSubjects: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val evalId: String = "",
    val type: String = EvaluationType.EXAM.name,
    val subjectName: String = "",
    val title: String = "",
    val description: String = "",
    val quickTips: String = "",
    val grade: String = "",
    val weight: String = "",
    val isPresented: Boolean = false,
    val gradeScaleId: String = "0-10",
    val existingImageUrl: String? = null,
    val date: java.util.Date? = null,
    val subjects: List<SubjectData> = emptyList()
)

sealed interface EditEvaluationEvent {
    data object LoadEvaluation : EditEvaluationEvent
    data class SelectSubject(val subject: String) : EditEvaluationEvent
    data class UpdateTitle(val value: String) : EditEvaluationEvent
    data class UpdateDescription(val value: String) : EditEvaluationEvent
    data class UpdateQuickTips(val value: String) : EditEvaluationEvent
    data class UpdateGrade(val value: String) : EditEvaluationEvent
    data class UpdateWeight(val value: String) : EditEvaluationEvent
    data object TogglePresented : EditEvaluationEvent
    data class Save(val date: java.util.Date, val imageUri: android.net.Uri?) : EditEvaluationEvent
    data object ClearError : EditEvaluationEvent
}
