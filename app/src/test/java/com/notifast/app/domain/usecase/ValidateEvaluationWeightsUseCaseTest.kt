package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateEvaluationWeightsUseCaseTest {

    private lateinit var useCase: ValidateEvaluationWeightsUseCase

    @Before
    fun setUp() {
        useCase = ValidateEvaluationWeightsUseCase()
    }

    private fun eval(id: String, weight: Double?) = Evaluation(id = id, weight = weight)

    @Test
    fun `sum exactly 100 is allowed`() {
        val existing = listOf(eval("a", 30.0), eval("b", 30.0), eval("c", 20.0))
        val result = useCase(existing, eval("d", 20.0))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `sum over 100 is rejected`() {
        val existing = listOf(eval("a", 30.0), eval("b", 30.0), eval("c", 20.0))
        val result = useCase(existing, eval("d", 30.0))
        assertTrue(result.isFailure)
    }

    @Test
    fun `replace existing weight is allowed`() {
        val existing = listOf(eval("a", 30.0), eval("b", 30.0))
        val result = useCase(existing, eval("a", 40.0))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `null weight does not count toward sum`() {
        val existing = listOf(eval("a", 30.0))
        val result = useCase(existing, eval("b", null))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `negative weight rejected`() {
        val result = useCase(emptyList(), eval("a", -5.0))
        assertTrue(result.isFailure)
    }

    @Test
    fun `weight over 100 on single eval rejected`() {
        val result = useCase(emptyList(), eval("a", 150.0))
        assertTrue(result.isFailure)
    }
}
