package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Cross-use-case consistency: the same dataset must produce the same numbers
 * in weighted average, academic stats and the required-grade simulator (D1-D6).
 */
class FormulaConsistencyTest {

    // DS: Química — 4 evaluations
    private val ds = listOf(
        Evaluation(id = "e1", subjectName = "Química", grade = 8.0, weight = 30.0, isPresented = true),
        Evaluation(id = "e2", subjectName = "Química", grade = null, weight = 30.0, isPresented = true),
        Evaluation(id = "e3", subjectName = "Química", grade = 9.0, weight = 20.0, isPresented = false),
        Evaluation(id = "e4", subjectName = "Química", grade = null, weight = 20.0, isPresented = false)
    )

    @Test
    fun `same average on all three use cases`() {
        val avg = CalculateWeightedAverageUseCase()(ds)
        val stats = CalculateAcademicStatsUseCase()(
            ds, listOf(SubjectData(name = "Química")), emptyList(), GradeScale.SCALE_0_10
        )
        val sim = CalculateRequiredGradeUseCase()(ds, targetGrade = 6.0, scale = GradeScale.SCALE_0_10)

        assertNotNull(avg)
        assertEquals(avg!!, stats.subjectHealthList.first().currentAverage, 0.05)
        // simulator's accumulated must match GradePolicy
        assertEquals(GradePolicy.accumulatedPoints(ds), sim.currentAccumulated, 0.01)
    }

    @Test
    fun `DS average is 8_0 only e1 counts`() {
        assertEquals(8.0, CalculateWeightedAverageUseCase()(ds)!!, 0.01)
    }

    @Test
    fun `DS evaluatedWeight is 30 and remainingWeight is 70`() {
        assertEquals(30.0, GradePolicy.evaluatedWeight(ds), 0.01)
        // pending: e2 (w=30, sin nota) + e3 (w=20, sin presentar) + e4 (w=20) = 70
        assertEquals(70.0, GradePolicy.remainingWeight(ds), 0.01)
    }

    @Test
    fun `DS accumulatedPoints is 2_4`() {
        assertEquals(2.4, GradePolicy.accumulatedPoints(ds), 0.01)
    }

    @Test
    fun `requiredGrade with target 6_0 on DS`() {
        val sim = CalculateRequiredGradeUseCase()(ds, targetGrade = 6.0, scale = GradeScale.SCALE_0_10)
        // needed = (6.0 - 2.4) / 70 * 100 ≈ 5.143
        assertEquals(5.143, sim.requiredGrade!!, 0.01)
        assertEquals(70.0, sim.remainingWeight, 0.01)
    }
}
