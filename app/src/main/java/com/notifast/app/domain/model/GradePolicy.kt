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
 *
 * Weights are returned as a list aligned with the input (avoids id collisions
 * on evaluations not yet persisted).
 */
object GradePolicy {

    fun countsForAverage(e: Evaluation): Boolean =
        e.isPresented && e.grade != null

    fun isPending(e: Evaluation): Boolean =
        !e.isPresented || e.grade == null

    /**
     * Normalized percent weights, same size/order as [evals].
     * Only evaluations with an explicit positive weight participate;
     * if none do, every evaluation gets 100/n.
     */
    fun effectiveWeights(evals: List<Evaluation>): List<Double> {
        if (evals.isEmpty()) return emptyList()
        val explicit = evals.map { it.weight?.takeIf { w -> w > 0.0 } }
        val hasAny = explicit.any { it != null }
        return if (!hasAny) {
            val equal = 100.0 / evals.size
            List(evals.size) { equal }
        } else {
            val sum = explicit.filterNotNull().sum()
            if (sum > 100.0) {
                explicit.map { w -> if (w == null) 0.0 else w / sum * 100.0 }
            } else {
                explicit.map { it ?: 0.0 }
            }
        }
    }

    /**
     * Converts a grade to the display scale using the evaluation's own scale (D5).
     * 15 in 0-20 → 7.5 in 0-10 → 75 in 0-100.
     */
    fun toDisplayGrade(e: Evaluation, displayScale: GradeScale): Double {
        val grade = e.grade ?: 0.0
        val src = GradeScale.fromId(e.gradeScaleId)
        if (src.maxGrade <= 0.0) return grade
        return grade / src.maxGrade * displayScale.maxGrade
    }

    fun weightedAverage(evals: List<Evaluation>, displayScale: GradeScale = GradeScale.SCALE_0_10): Double? {
        val weights = effectiveWeights(evals)
        var weightedSum = 0.0
        var totalWeight = 0.0
        evals.forEachIndexed { i, e ->
            if (countsForAverage(e)) {
                weightedSum += toDisplayGrade(e, displayScale) * weights[i]
                totalWeight += weights[i]
            }
        }
        return if (totalWeight > 0.0) weightedSum / totalWeight else null
    }

    fun accumulatedPoints(evals: List<Evaluation>, displayScale: GradeScale = GradeScale.SCALE_0_10): Double {
        val weights = effectiveWeights(evals)
        var points = 0.0
        evals.forEachIndexed { i, e ->
            if (countsForAverage(e)) {
                points += toDisplayGrade(e, displayScale) * (weights[i] / 100.0)
            }
        }
        return points
    }

    fun evaluatedWeight(evals: List<Evaluation>): Double {
        val weights = effectiveWeights(evals)
        var sum = 0.0
        evals.forEachIndexed { i, e ->
            if (countsForAverage(e)) sum += weights[i]
        }
        return sum
    }

    fun remainingWeight(evals: List<Evaluation>): Double {
        val weights = effectiveWeights(evals)
        var sum = 0.0
        evals.forEachIndexed { i, e ->
            if (isPending(e)) sum += weights[i]
        }
        return sum
    }

    fun resolveMinPassing(scale: GradeScale, custom: Double?): Double =
        custom ?: scale.minPassingGrade

    fun validateGrade(grade: Double, scale: GradeScale): Boolean =
        grade >= scale.minGrade && grade <= scale.maxGrade

    fun clampGrade(grade: Double, scale: GradeScale): Double =
        grade.coerceIn(scale.minGrade, scale.maxGrade)
}
