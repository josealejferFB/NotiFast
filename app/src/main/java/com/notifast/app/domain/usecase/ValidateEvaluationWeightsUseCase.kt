package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import javax.inject.Inject

/**
 * Validates that a candidate evaluation's weight keeps the subject's total at ≤ 100%.
 * A candidate with the same id replaces its previous weight (edit path).
 */
class ValidateEvaluationWeightsUseCase @Inject constructor() {

    operator fun invoke(existing: List<Evaluation>, candidate: Evaluation): Result<Unit> {
        val w = candidate.weight
        if (w != null && (w < 0.0 || w > 100.0)) {
            return Result.failure(IllegalArgumentException("El peso debe estar entre 0 y 100%"))
        }
        val others = existing.filterNot { it.id == candidate.id }
        val sumOthers = others.mapNotNull { it.weight }.filter { it > 0.0 }.sum()
        val total = sumOthers + (w?.takeIf { it > 0.0 } ?: 0.0)
        return if (total > 100.0 + 1e-9) {
            Result.failure(IllegalArgumentException("La suma de pesos de la materia supera el 100% ($total%)"))
        } else {
            Result.success(Unit)
        }
    }
}
