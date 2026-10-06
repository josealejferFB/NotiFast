package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.model.GradeScale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertGradeScaleUseCaseTest {

    private val useCase = ConvertGradeScaleUseCase()

    private fun eval(id: String, grade: Double?, scaleId: String) = Evaluation(
        id = id, grade = grade, isPresented = grade != null, gradeScaleId = scaleId
    )

    @Test
    fun `converts mixed source scales into target 0-100`() {
        val result = useCase(
            evaluations = listOf(eval("1", 15.0, "0-20"), eval("2", 7.5, "0-10")),
            targetScale = GradeScale.SCALE_0_100,
            currentScale = GradeScale.SCALE_0_10,
            customMinPassing = 6.0
        )
        assertEquals(75.0, result.convertedEvaluations[0].grade!!, 0.001)
        assertEquals(75.0, result.convertedEvaluations[1].grade!!, 0.001)
        assertEquals(2, result.convertedCount)
        assertTrue(result.hasCustomMinPassing)
        assertEquals(60.0, result.minPassingGrade, 0.001)
    }

    @Test
    fun `without custom passing uses target default`() {
        val result = useCase(
            evaluations = emptyList(),
            targetScale = GradeScale.SCALE_0_20,
            currentScale = GradeScale.SCALE_0_10,
            customMinPassing = null
        )
        assertFalse(result.hasCustomMinPassing)
        assertEquals(11.0, result.minPassingGrade, 0.001)
    }

    @Test
    fun `pending evaluations are restamped only`() {
        val result = useCase(
            listOf(eval("p", null, "0-10")),
            GradeScale.SCALE_0_20,
            GradeScale.SCALE_0_10,
            customMinPassing = null
        )
        assertEquals(0, result.convertedCount)
        assertEquals("0-20", result.convertedEvaluations[0].gradeScaleId)
    }
}
