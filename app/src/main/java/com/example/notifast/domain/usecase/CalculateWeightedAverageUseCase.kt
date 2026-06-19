package com.example.notifast.domain.usecase

import com.example.notifast.data.model.Evaluation
import javax.inject.Inject

class CalculateWeightedAverageUseCase @Inject constructor() {
    
    /**
     * Calculates the weighted average of a list of evaluations.
     * Only evaluations with a non-null grade AND [Evaluation.isPresented] == true are considered.
     * If an evaluation has no weight or weight is 0.0, it defaults to a weight of 1.0.
     *
     * Business rule: an evaluation that has been graded but not yet presented is still
     * "pending" — it should not influence the final average until the student confirms
     * it was actually taken.
     *
     * @param evaluations List of evaluations to calculate the average from.
     * @return The weighted average, or null if there are no presented, graded evaluations.
     */
    operator fun invoke(evaluations: List<Evaluation>): Double? {
        val graded = evaluations.filter { it.grade != null && it.isPresented }
        if (graded.isEmpty()) return null
        
        val weightedSum = graded.sumOf { eval ->
            val w = if (eval.weight == null || eval.weight == 0.0) 1.0 else eval.weight
            (eval.grade ?: 0.0) * w
        }
        
        val totalWeight = graded.sumOf { eval ->
            if (eval.weight == null || eval.weight == 0.0) 1.0 else eval.weight
        }
        
        return if (totalWeight > 0.0) weightedSum / totalWeight else null
    }
}
