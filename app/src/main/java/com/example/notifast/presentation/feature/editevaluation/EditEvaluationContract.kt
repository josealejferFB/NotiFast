package com.example.notifast.presentation.feature.editevaluation

data class EditEvaluationUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val evalId: String = "",
    val subjectName: String = "",
    val title: String = "",
    val description: String = "",
    val quickTips: String = "",
    val grade: String = "",
    val weight: String = "",
    val isPresented: Boolean = false,
    val existingImageUrl: String? = null,
    val date: java.util.Date? = null
)

sealed interface EditEvaluationEvent {
    data object LoadEvaluation : EditEvaluationEvent
    data class UpdateTitle(val value: String) : EditEvaluationEvent
    data class UpdateDescription(val value: String) : EditEvaluationEvent
    data class UpdateQuickTips(val value: String) : EditEvaluationEvent
    data class UpdateGrade(val value: String) : EditEvaluationEvent
    data class UpdateWeight(val value: String) : EditEvaluationEvent
    data object TogglePresented : EditEvaluationEvent
    data class Save(val date: java.util.Date, val imageUri: android.net.Uri?) : EditEvaluationEvent
}
