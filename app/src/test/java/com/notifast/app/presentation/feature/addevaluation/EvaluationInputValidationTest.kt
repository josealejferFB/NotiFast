package com.notifast.app.presentation.feature.addevaluation

import com.notifast.app.domain.util.GradeConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluationInputValidationTest {

    @Test
    fun `valid weight values are accepted between 0 and 100`() {
        val validWeights = listOf("0", "10", "25.5", "50", "99.9", "100")
        validWeights.forEach { v ->
            val parsed = v.toDoubleOrNull()
            assertTrue("Weight '$v' should be valid", parsed != null && parsed in 0.0..100.0)
        }
    }

    @Test
    fun `invalid weight values are rejected`() {
        val invalidWeights = listOf("-5", "-0.1", "100.1", "150", "abc", "NaN", "Infinity")
        invalidWeights.forEach { v ->
            val parsed = v.toDoubleOrNull()
            val isValid = parsed != null && !parsed.isNaN() && !parsed.isInfinite() && parsed in 0.0..100.0
            assertFalse("Weight '$v' should be invalid", isValid)
        }
    }

    @Test
    fun `valid grades are between GradeConstants MIN_GRADE and MAX_GRADE`() {
        val validGrades = listOf("0", "0.0", "1", "1.0", "5.5", "7", "9.8", "10", "10.0")
        validGrades.forEach { g ->
            val parsed = g.toDoubleOrNull()
            assertTrue("Grade '$g' should be within scale", parsed != null && parsed in GradeConstants.MIN_GRADE..GradeConstants.MAX_GRADE)
        }
    }

    @Test
    fun `invalid grades outside scale are rejected`() {
        val invalidGrades = listOf("-0.1", "-2", "10.1", "15", "xyz")
        invalidGrades.forEach { g ->
            val parsed = g.toDoubleOrNull()
            val isValid = parsed != null && parsed in GradeConstants.MIN_GRADE..GradeConstants.MAX_GRADE
            assertFalse("Grade '$g' should be rejected", isValid)
        }
    }

    @Test
    fun `weight coercion clamps safely`() {
        assertEquals(100.0, 150.0.coerceIn(0.0, 100.0), 0.001)
        assertEquals(0.0, (-10.0).coerceIn(0.0, 100.0), 0.001)
        assertEquals(35.5, 35.5.coerceIn(0.0, 100.0), 0.001)
    }
}
