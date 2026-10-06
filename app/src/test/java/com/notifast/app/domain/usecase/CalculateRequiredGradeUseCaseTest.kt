package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.model.GradeScale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CalculateRequiredGradeUseCaseTest {

    private lateinit var useCase: CalculateRequiredGradeUseCase

    @Before
    fun setUp() {
        useCase = CalculateRequiredGradeUseCase()
    }

    @Test
    fun `when already passed, returns ALREADY_PASSED status`() {
        // Presented: 10 with 70% weight -> 7.0 accumulated. Target: 6.0
        val evaluations = listOf(
            Evaluation(isPresented = true, grade = 10.0, weight = 70.0),
            Evaluation(isPresented = false, weight = 30.0)
        )

        val result = useCase(evaluations, targetGrade = 6.0, scale = GradeScale.SCALE_0_10)

        assertEquals(SimulatorStatus.ALREADY_PASSED, result.status)
        assertEquals(7.0, result.currentAccumulated, 0.01)
        assertEquals(30.0, result.remainingWeight, 0.01)
        assertEquals(0.0, result.requiredGrade ?: 0.0, 0.01)
    }

    @Test
    fun `when achievable, calculates required grade correctly`() {
        // Presented: 5.0 with 50% weight -> 2.5 accumulated. Target: 6.0.
        // Remaining weight: 50%. Needed: 3.5 points. Required grade: 3.5 / 0.5 = 7.0
        val evaluations = listOf(
            Evaluation(isPresented = true, grade = 5.0, weight = 50.0),
            Evaluation(isPresented = false, weight = 50.0)
        )

        val result = useCase(evaluations, targetGrade = 6.0, scale = GradeScale.SCALE_0_10)

        assertEquals(SimulatorStatus.AT_RISK, result.status) // 7.0 > 6.0 minPassing
        assertEquals(2.5, result.currentAccumulated, 0.01)
        assertEquals(50.0, result.remainingWeight, 0.01)
        assertEquals(7.0, result.requiredGrade ?: 0.0, 0.01)
    }

    @Test
    fun `when easy achievable below passing threshold, returns ACHIEVABLE`() {
        // Presented: 8.0 with 50% weight -> 4.0 accumulated. Target: 6.0.
        // Remaining: 50%. Needed: 2.0. Required grade: 4.0 (<= 6.0 passing threshold)
        val evaluations = listOf(
            Evaluation(isPresented = true, grade = 8.0, weight = 50.0),
            Evaluation(isPresented = false, weight = 50.0)
        )

        val result = useCase(evaluations, targetGrade = 6.0, scale = GradeScale.SCALE_0_10)

        assertEquals(SimulatorStatus.ACHIEVABLE, result.status)
        assertEquals(4.0, result.requiredGrade ?: 0.0, 0.01)
    }

    @Test
    fun `when mathematically impossible, returns IMPOSSIBLE`() {
        // Presented: 2.0 with 70% weight -> 1.4 accumulated. Target: 6.0.
        // Remaining: 30%. Max possible points: 1.4 + 3.0 = 4.4 (< 6.0)
        // Required grade would be 4.6 / 0.3 = 15.33 (> 10.0 max)
        val evaluations = listOf(
            Evaluation(isPresented = true, grade = 2.0, weight = 70.0),
            Evaluation(isPresented = false, weight = 30.0)
        )

        val result = useCase(evaluations, targetGrade = 6.0, scale = GradeScale.SCALE_0_10)

        assertEquals(SimulatorStatus.IMPOSSIBLE, result.status)
        assertNotNull(result.requiredGrade)
        assertEquals(15.33, result.requiredGrade ?: 0.0, 0.05)
    }

    @Test
    fun `when no pending evaluations and not passed, returns NO_PENDING_EVALUATIONS`() {
        val evaluations = listOf(
            Evaluation(isPresented = true, grade = 4.0, weight = 100.0)
        )

        val result = useCase(evaluations, targetGrade = 6.0, scale = GradeScale.SCALE_0_10)

        assertEquals(SimulatorStatus.NO_PENDING_EVALUATIONS, result.status)
        assertNull(result.requiredGrade)
    }

    @Test
    fun `ACHIEVABLE uses custom minPassing not scale default`() {
        // Grade 10 recorded on default 0-10, displayed on 0-20 → 20. Weight 50% → accumulated 10.
        // Target 11. Remaining 50% → required = 2.0
        val evals = listOf(
            Evaluation(id = "1", grade = 10.0, weight = 50.0, isPresented = true),
            Evaluation(id = "2", grade = null, weight = 50.0, isPresented = false)
        )
        val result = CalculateRequiredGradeUseCase()(
            evals, targetGrade = 11.0, scale = GradeScale.SCALE_0_20, minPassingGrade = 10.0
        )
        // required 2.0 <= custom minPassing 10.0 → ACHIEVABLE
        assertEquals(SimulatorStatus.ACHIEVABLE, result.status)
        assertEquals(2.0, result.requiredGrade ?: 0.0, 0.01)
    }

    @Test
    fun `status honors custom minPassing when it differs from scale default`() {
        // required = 10.5 on 0-20: between custom min 10.0 and scale default 11.0
        // grade 11.5 weight 50% → accumulated 5.75; target 11 → needed 5.25; required 10.5
        val evals = listOf(
            Evaluation(id = "1", grade = 11.5, weight = 50.0, isPresented = true, gradeScaleId = "0-20"),
            Evaluation(id = "2", grade = null, weight = 50.0, isPresented = false, gradeScaleId = "0-20")
        )
        val withCustom = CalculateRequiredGradeUseCase()(
            evals, targetGrade = 11.0, scale = GradeScale.SCALE_0_20, minPassingGrade = 10.0
        )
        assertEquals(10.5, withCustom.requiredGrade ?: 0.0, 0.01)
        // 10.5 > custom 10.0 → AT_RISK (stricter custom minPassing is honored)
        assertEquals(SimulatorStatus.AT_RISK, withCustom.status)

        val withScaleDefault = CalculateRequiredGradeUseCase()(
            evals, targetGrade = 11.0, scale = GradeScale.SCALE_0_20
        )
        // 10.5 <= scale default 11.0 → ACHIEVABLE without custom override
        assertEquals(SimulatorStatus.ACHIEVABLE, withScaleDefault.status)
    }
}
