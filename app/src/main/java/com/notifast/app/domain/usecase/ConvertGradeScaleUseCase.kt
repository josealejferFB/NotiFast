package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale
import javax.inject.Inject

data class GradeScaleConversionResult(
    val convertedEvaluations: List<Evaluation>,
    val minPassingGrade: Double,
    val hasCustomMinPassing: Boolean,
    val convertedCount: Int
)

class ConvertGradeScaleUseCase @Inject constructor() {

    operator fun invoke(
        evaluations: List<Evaluation>,
        targetScale: GradeScale,
        currentScale: GradeScale,
        customMinPassing: Double?
    ): GradeScaleConversionResult {
        val converted = GradePolicy.convertEvaluations(evaluations, targetScale)
        val count = converted.count { it.grade != null }
        val hasCustom = customMinPassing != null
        val minPassing = if (hasCustom) {
            GradePolicy.convertPassingGrade(customMinPassing!!, currentScale, targetScale)
        } else {
            targetScale.minPassingGrade
        }
        return GradeScaleConversionResult(
            convertedEvaluations = converted,
            minPassingGrade = minPassing,
            hasCustomMinPassing = hasCustom,
            convertedCount = count
        )
    }
}
