package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.model.GradePolicy
import javax.inject.Inject

enum class SimulatorStatus {
    ALREADY_PASSED,
    ACHIEVABLE,
    AT_RISK,
    IMPOSSIBLE,
    NO_PENDING_EVALUATIONS
}

data class RequiredGradeResult(
    val currentAccumulated: Double,
    val remainingWeight: Double,
    val requiredGrade: Double?,
    val targetGrade: Double,
    val status: SimulatorStatus
)

/**
 * Required-grade simulator. Accumulated points and remaining weight come from
 * [GradePolicy] (D1/D2/D6): remaining is the real pending weight, not 100-evaluated.
 */
class CalculateRequiredGradeUseCase @Inject constructor() {

    operator fun invoke(
        evaluations: List<Evaluation>,
        targetGrade: Double,
        scale: GradeScale = GradeScale.SCALE_0_10
    ): RequiredGradeResult {
        val currentAccumulated = GradePolicy.accumulatedPoints(evaluations, scale)
        val remainingWeight = GradePolicy.remainingWeight(evaluations)

        if (currentAccumulated >= targetGrade) {
            return RequiredGradeResult(
                currentAccumulated = currentAccumulated,
                remainingWeight = remainingWeight,
                requiredGrade = 0.0,
                targetGrade = targetGrade,
                status = SimulatorStatus.ALREADY_PASSED
            )
        }

        if (remainingWeight <= 0.0) {
            return RequiredGradeResult(
                currentAccumulated = currentAccumulated,
                remainingWeight = 0.0,
                requiredGrade = null,
                targetGrade = targetGrade,
                status = SimulatorStatus.NO_PENDING_EVALUATIONS
            )
        }

        val neededPoints = targetGrade - currentAccumulated
        val requiredGrade = (neededPoints / remainingWeight) * 100.0

        val status = when {
            requiredGrade <= scale.minPassingGrade -> SimulatorStatus.ACHIEVABLE
            requiredGrade <= scale.maxGrade -> SimulatorStatus.AT_RISK
            else -> SimulatorStatus.IMPOSSIBLE
        }

        return RequiredGradeResult(
            currentAccumulated = currentAccumulated,
            remainingWeight = remainingWeight,
            requiredGrade = requiredGrade,
            targetGrade = targetGrade,
            status = status
        )
    }
}
