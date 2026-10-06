package com.notifast.app.domain.model

import com.notifast.app.data.model.Evaluation

/**
 * Single source of truth for grade/weight business rules (D1-D6).
 *
 * D1: weights are percent of 100. Explicit weights renormalize if they exceed 100.
 *     If NO evaluation has an explicit weight, split 100/n equally.
 *     Null/zero weight does NOT contribute (never invents 1.0).
 * D2: an evaluation counts toward the average only if `isPresented && grade != null`.
 * D6: remainingWeight = sum of pending evaluations' weights (no phantom 100-evaluated).
 */
object GradePolicy {

    fun countsForAverage(e: Evaluation): Boolean =
        e.isPresented && e.grade != null

    fun isPending(e: Evaluation): Boolean =
        !e.isPresented || e.grade == null

    /**
     * Normalized percent weights keyed by evaluation id.
     * Only evaluations that have an explicit positive weight participate;
     * if none do, every evaluation gets 100/n.
     */
    fun effectiveWeights(evals: List<Evaluation>): Map<String, Double> {
        if (evals.isEmpty()) return emptyMap()
        val explicit = evals.mapNotNull { e ->
            val w = e.weight
            if (w != null && w > 0.0) e.id to w else null
        }
        return if (explicit.isEmpty()) {
            val equal = 100.0 / evals.size
            evals.associate { it.id to equal }
        } else {
            val sum = explicit.sumOf { it.second }
            if (sum > 100.0) {
                explicit.associate { (id, w) -> id to (w / sum * 100.0) }
            } else {
                explicit.toMap()
            }
        }
    }

    fun weightedAverage(evals: List<Evaluation>): Double? {
        val weights = effectiveWeights(evals)
        val counted = evals.filter { countsForAverage(it) }
        if (counted.isEmpty()) return null
        var weightedSum = 0.0
        var totalWeight = 0.0
        for (e in counted) {
            val w = weights[e.id] ?: 0.0
            weightedSum += (e.grade ?: 0.0) * w
            totalWeight += w
        }
        return if (totalWeight > 0.0) weightedSum / totalWeight else null
    }

    fun accumulatedPoints(evals: List<Evaluation>): Double {
        val weights = effectiveWeights(evals)
        return evals.filter { countsForAverage(it) }.sumOf { e ->
            (e.grade ?: 0.0) * ((weights[e.id] ?: 0.0) / 100.0)
        }
    }

    fun evaluatedWeight(evals: List<Evaluation>): Double {
        val weights = effectiveWeights(evals)
        return evals.filter { countsForAverage(it) }.sumOf { weights[it.id] ?: 0.0 }
    }

    fun remainingWeight(evals: List<Evaluation>): Double {
        val weights = effectiveWeights(evals)
        return evals.filter { isPending(it) }.sumOf { weights[it.id] ?: 0.0 }
    }

    fun resolveMinPassing(scale: GradeScale, custom: Double?): Double =
        custom ?: scale.minPassingGrade

    fun validateGrade(grade: Double, scale: GradeScale): Boolean =
        grade >= scale.minGrade && grade <= scale.maxGrade

    fun clampGrade(grade: Double, scale: GradeScale): Double =
        grade.coerceIn(scale.minGrade, scale.maxGrade)
}
