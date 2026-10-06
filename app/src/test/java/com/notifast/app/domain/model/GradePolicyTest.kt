package com.notifast.app.domain.model

import com.notifast.app.data.model.Evaluation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GradePolicyTest {

    private fun eval(
        id: String,
        grade: Double? = null,
        presented: Boolean = false,
        weight: Double? = null
    ) = Evaluation(
        id = id,
        grade = grade,
        isPresented = presented,
        weight = weight
    )

    // region countsForAverage / isPending

    @Test
    fun `countsForAverage requires both flags`() {
        assertFalse(GradePolicy.countsForAverage(eval("1", grade = 8.0, presented = false)))
        assertFalse(GradePolicy.countsForAverage(eval("2", grade = null, presented = true)))
        assertTrue(GradePolicy.countsForAverage(eval("3", grade = 8.0, presented = true)))
    }

    @Test
    fun `isPending covers presented-without-grade and not-presented`() {
        assertTrue(GradePolicy.isPending(eval("1", grade = null, presented = true)))
        assertTrue(GradePolicy.isPending(eval("2", grade = 9.0, presented = false)))
        assertTrue(GradePolicy.isPending(eval("3", grade = null, presented = false)))
        assertFalse(GradePolicy.isPending(eval("4", grade = 9.0, presented = true)))
    }

    // endregion

    // region weightedAverage

    @Test
    fun `empty list returns null average`() {
        assertNull(GradePolicy.weightedAverage(emptyList()))
        assertEquals(0.0, GradePolicy.accumulatedPoints(emptyList()), 0.001)
    }

    @Test
    fun `presented without grade excluded from average`() {
        val evals = listOf(
            eval("a", grade = null, presented = true, weight = 50.0),
            eval("b", grade = 8.0, presented = true, weight = 50.0)
        )
        assertEquals(8.0, GradePolicy.weightedAverage(evals)!!, 0.001)
        assertEquals(50.0, GradePolicy.evaluatedWeight(evals), 0.001)
    }

    @Test
    fun `graded without presented excluded from average`() {
        val evals = listOf(
            eval("a", grade = 9.0, presented = false, weight = 40.0),
            eval("b", grade = 7.0, presented = true, weight = 60.0)
        )
        assertEquals(7.0, GradePolicy.weightedAverage(evals)!!, 0.001)
    }

    @Test
    fun `null and zero weight do not invent 1_0`() {
        val evals = listOf(
            eval("a", grade = 8.0, presented = true, weight = null),
            eval("b", grade = 6.0, presented = true, weight = 0.0),
            eval("c", grade = 10.0, presented = true, weight = 100.0)
        )
        assertEquals(10.0, GradePolicy.weightedAverage(evals)!!, 0.001)
    }

    @Test
    fun `equal split when no explicit weights`() {
        val evals = listOf(
            eval("a", grade = 8.0, presented = true, weight = null),
            eval("b", grade = 6.0, presented = true, weight = null)
        )
        assertEquals(7.0, GradePolicy.weightedAverage(evals)!!, 0.001)
    }

    @Test
    fun `mixed only explicit weights used`() {
        val evals = listOf(
            eval("a", grade = 8.0, presented = true, weight = 30.0),
            eval("b", grade = 6.0, presented = true, weight = null)
        )
        assertEquals(8.0, GradePolicy.weightedAverage(evals)!!, 0.001)
    }

    @Test
    fun `renormalize when sum exceeds 100`() {
        val evals = listOf(
            eval("a", grade = 10.0, presented = true, weight = 80.0),
            eval("b", grade = 6.0, presented = true, weight = 80.0)
        )
        assertEquals(8.0, GradePolicy.weightedAverage(evals)!!, 0.001)
    }

    @Test
    fun `all pending returns null average`() {
        val evals = listOf(
            eval("a", weight = null),
            eval("b", weight = null)
        )
        assertNull(GradePolicy.weightedAverage(evals))
        assertEquals(100.0, GradePolicy.remainingWeight(evals), 0.001)
    }

    // endregion

    // region accumulatedPoints / evaluatedWeight / remainingWeight

    @Test
    fun `accumulatedPoints is grade times weight over 100`() {
        val evals = listOf(
            eval("e1", grade = 8.0, presented = true, weight = 30.0),
            eval("e2", grade = null, presented = true, weight = 30.0),
            eval("e3", grade = 9.0, presented = false, weight = 20.0),
            eval("e4", grade = null, presented = false, weight = 20.0)
        )
        assertEquals(2.4, GradePolicy.accumulatedPoints(evals), 0.001)
        assertEquals(30.0, GradePolicy.evaluatedWeight(evals), 0.001)
    }

    @Test
    fun `remainingWeight is sum of pending weights`() {
        val evals = listOf(
            eval("e1", grade = 10.0, presented = true, weight = 30.0),
            eval("e2", weight = 20.0),
            eval("e3", weight = 25.0)
        )
        assertEquals(45.0, GradePolicy.remainingWeight(evals), 0.001)
    }

    @Test
    fun `remaining includes presented-ungraded`() {
        val evals = listOf(eval("e", grade = null, presented = true, weight = 40.0))
        assertEquals(40.0, GradePolicy.remainingWeight(evals), 0.001)
    }

    @Test
    fun `no phantom remaining when all graded and presented`() {
        val evals = listOf(
            eval("e1", grade = 10.0, presented = true, weight = 30.0),
            eval("e2", grade = 8.0, presented = true, weight = 30.0)
        )
        assertEquals(0.0, GradePolicy.remainingWeight(evals), 0.001)
    }

    // endregion

    // region validateGrade / clampGrade / resolveMinPassing

    @Test
    fun `validateGrade per scale`() {
        assertFalse(GradePolicy.validateGrade(15.0, GradeScale.SCALE_0_10))
        assertTrue(GradePolicy.validateGrade(15.0, GradeScale.SCALE_0_20))
        assertTrue(GradePolicy.validateGrade(15.0, GradeScale.SCALE_0_100))
        assertTrue(GradePolicy.validateGrade(0.0, GradeScale.SCALE_0_10))
        assertTrue(GradePolicy.validateGrade(10.0, GradeScale.SCALE_0_10))
        assertFalse(GradePolicy.validateGrade(-1.0, GradeScale.SCALE_0_20))
        assertFalse(GradePolicy.validateGrade(21.0, GradeScale.SCALE_0_20))
    }

    @Test
    fun `clampGrade bounds to scale`() {
        assertEquals(10.0, GradePolicy.clampGrade(15.0, GradeScale.SCALE_0_10), 0.001)
        assertEquals(0.0, GradePolicy.clampGrade(-1.0, GradeScale.SCALE_0_20), 0.001)
        assertEquals(8.5, GradePolicy.clampGrade(8.5, GradeScale.SCALE_0_10), 0.001)
    }

    @Test
    fun `resolveMinPassing custom overrides default`() {
        assertEquals(7.0, GradePolicy.resolveMinPassing(GradeScale.SCALE_0_10, 7.0), 0.001)
        assertEquals(6.0, GradePolicy.resolveMinPassing(GradeScale.SCALE_0_10, null), 0.001)
        assertEquals(11.0, GradePolicy.resolveMinPassing(GradeScale.SCALE_0_20, 11.0), 0.001)
    }

    // endregion
}
