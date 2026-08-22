package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.model.GradeScale
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

class CalculateRequiredGradeUseCase @Inject constructor() {

    operator fun invoke(
        evaluations: List<Evaluation>,
        targetGrade: Double,
        scale: GradeScale = GradeScale.SCALE_0_10
    ): RequiredGradeResult {
        val presentedEvals = evaluations.filter { it.isPresented && it.grade != null }
        val pendingEvals = evaluations.filter { !it.isPresented }

        val currentAccumulated = presentedEvals.sumOf { (it.grade ?: 0.0) * ((it.weight ?: 0.0) / 100.0) }
        val remainingWeight = pendingEvals.sumOf { it.weight ?: 0.0 }

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
