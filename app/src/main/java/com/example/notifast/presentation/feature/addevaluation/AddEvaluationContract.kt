package com.example.notifast.presentation.feature.addevaluation

import com.example.notifast.data.model.SubjectData

data class AddEvaluationUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val subjects: List<SubjectData> = emptyList(),
    val selectedSubject: String = "",
    val title: String = "",
    val description: String = "",
    val quickTips: String = "",
    val weight: String = "" // Ej: "20" para 20% de peso en el promedio
)

sealed interface AddEvaluationEvent {
    data object LoadSubjects : AddEvaluationEvent
    data class SelectSubject(val subject: String) : AddEvaluationEvent
    data class UpdateTitle(val value: String) : AddEvaluationEvent
    data class UpdateDescription(val value: String) : AddEvaluationEvent
    data class UpdateQuickTips(val value: String) : AddEvaluationEvent
    data class UpdateWeight(val value: String) : AddEvaluationEvent
    data class Save(val date: java.util.Date, val imageUri: android.net.Uri?) : AddEvaluationEvent
}
