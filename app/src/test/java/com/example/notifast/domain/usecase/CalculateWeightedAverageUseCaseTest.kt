package com.example.notifast.domain.usecase

import com.example.notifast.data.model.Evaluation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Date

class CalculateWeightedAverageUseCaseTest {

    private lateinit var useCase: CalculateWeightedAverageUseCase

    @Before
    fun setUp() {
        useCase = CalculateWeightedAverageUseCase()
    }

    @Test
    fun `invoke with empty list returns null`() {
        val result = useCase(emptyList())
        assertNull(result)
    }

    @Test
    fun `invoke with no graded evaluations returns null`() {
        val evaluations = listOf(
            createEvaluation(id = "1", grade = null),
            createEvaluation(id = "2", grade = null)
        )
        
        val result = useCase(evaluations)
        assertNull(result)
    }

    @Test
    fun `invoke with single graded evaluation and no weight returns the grade`() {
        val evaluations = listOf(
            createEvaluation(id = "1", grade = 8.5)
        )
        
        val result = useCase(evaluations)
        assertEquals(8.5, result!!, 0.001)
    }

    @Test
    fun `invoke with multiple evaluations defaults to weight 1 if weight is null or 0`() {
        val evaluations = listOf(
            createEvaluation(id = "1", grade = 8.0, weight = null),
            createEvaluation(id = "2", grade = 6.0, weight = 0.0)
        )
        
        // Both weights default to 1.0. Total weight = 2.0. Sum = 8.0 + 6.0 = 14.0. Avg = 7.0
        val result = useCase(evaluations)
        assertEquals(7.0, result!!, 0.001)
    }

    @Test
    fun `invoke calculates true weighted average with decimal factor weights`() {
        val evaluations = listOf(
            createEvaluation(id = "1", grade = 10.0, weight = 0.3), // 30% weight
            createEvaluation(id = "2", grade = 5.0, weight = 0.7)   // 70% weight
        )
        
        // Sum = (10 * 0.3) + (5 * 0.7) = 3 + 3.5 = 6.5. Total weight = 1.0. Avg = 6.5
        val result = useCase(evaluations)
        assertEquals(6.5, result!!, 0.001)
    }

    @Test
    fun `invoke calculates true weighted average with percentage weights`() {
        val evaluations = listOf(
            createEvaluation(id = "1", grade = 18.0, weight = 30.0), // 30% weight
            createEvaluation(id = "2", grade = 14.0, weight = 70.0)  // 70% weight
        )
        
        // Sum = (18 * 30) + (14 * 70) = 540 + 980 = 1520. Total weight = 100. Avg = 15.2
        val result = useCase(evaluations)
        assertEquals(15.2, result!!, 0.001)
    }

    @Test
    fun `invoke ignores evaluations that are graded but not presented`() {
        val evaluations = listOf(
            createEvaluation(id = "1", grade = 16.0, weight = 50.0, isPresented = true),
            createEvaluation(id = "2", grade = 10.0, weight = 50.0, isPresented = false) // Not presented yet
        )
        
        val result = useCase(evaluations)
        assertEquals(16.0, result!!, 0.001)
    }

    @Test
    fun `invoke calculates correctly when some evaluations have no grade`() {
        val evaluations = listOf(
            createEvaluation(id = "1", grade = 10.0, weight = 0.3),
            createEvaluation(id = "2", grade = null, weight = 0.4), // Should be ignored
            createEvaluation(id = "3", grade = 5.0, weight = 0.7)
        )
        
        // Sum = (10 * 0.3) + (5 * 0.7) = 3 + 3.5 = 6.5. Total weight = 1.0. Avg = 6.5
        val result = useCase(evaluations)
        assertEquals(6.5, result!!, 0.001)
    }

    private fun createEvaluation(
        id: String,
        grade: Double?,
        weight: Double? = null,
        isPresented: Boolean = grade != null
    ): Evaluation {
        return Evaluation(
            id = id,
            userId = "user",
            subjectName = "Math",
            title = "Test",
            description = "",
            date = Date(),
            imageUrl = null,
            quickTips = "",
            grade = grade,
            weight = weight,
            isPresented = isPresented
        )
    }
}
