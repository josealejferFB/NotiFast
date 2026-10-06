package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale
import javax.inject.Inject

/**
 * Weighted average of a subject's (or set's) evaluations.
 * Delegates to [GradePolicy]: only presented+graded evaluations count;
 * weights are percent of 100 (D1-D2). Grades from mixed scales are
 * converted to [displayScale] before averaging (D5).
 */
class CalculateWeightedAverageUseCase @Inject constructor() {

    operator fun invoke(
        evaluations: List<Evaluation>,
        displayScale: GradeScale = GradeScale.SCALE_0_10
    ): Double? = GradePolicy.weightedAverage(evaluations, displayScale)
}
