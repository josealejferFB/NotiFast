package com.notifast.app.presentation.feature.addevaluation

import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.EvaluationType

data class AddEvaluationUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val subjects: List<SubjectData> = emptyList(),
    val selectedSubject: String = "",
    val title: String = "",
    val description: String = "",
    val quickTips: String = "",
    val selectedType: EvaluationType = EvaluationType.EXAM,
    val weight: String = "", // Ej: "20" para 20% de peso en el promedio
    val error: String? = null
)

sealed interface AddEvaluationEvent {
    data object LoadSubjects : AddEvaluationEvent
    data class SelectSubject(val subject: String) : AddEvaluationEvent
    data class UpdateTitle(val value: String) : AddEvaluationEvent
    data class UpdateDescription(val value: String) : AddEvaluationEvent
    data class UpdateQuickTips(val value: String) : AddEvaluationEvent
    data class UpdateWeight(val value: String) : AddEvaluationEvent
    data class SelectType(val type: EvaluationType) : AddEvaluationEvent
    data class Save(val date: java.util.Date, val imageUri: android.net.Uri?) : AddEvaluationEvent
    data object ClearError : AddEvaluationEvent
}
