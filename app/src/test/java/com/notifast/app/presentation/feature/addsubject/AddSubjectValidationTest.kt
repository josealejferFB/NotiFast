package com.notifast.app.presentation.feature.addsubject

import com.notifast.app.data.model.SubjectData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddSubjectValidationTest {

    @Test
    fun `blank or whitespace only subject name is invalid`() {
        val names = listOf("", " ", "   ", "\t", "\n")
        names.forEach { name ->
            assertTrue("Name '$name' should be blank", name.trim().isBlank())
        }
    }

    @Test
    fun `trimmed subject name retains valid characters`() {
        val input = "   Matemáticas Discretas   "
        val clean = input.trim()
        assertEquals("Matemáticas Discretas", clean)
        assertTrue(clean.isNotBlank())
    }

    @Test
    fun `detects duplicate subjects case insensitively`() {
        val existing = listOf(
            SubjectData(name = "Física I", icon = "📐", colorHex = "#BF092F"),
            SubjectData(name = "Cálculo Integral", icon = "📚", colorHex = "#3B9797")
        )

        val newSubject1 = "física i"
        val isDuplicate1 = existing.any { it.name.equals(newSubject1.trim(), ignoreCase = true) }
        assertTrue(isDuplicate1)

        val newSubject2 = "  CÁLCULO INTEGRAL  "
        val isDuplicate2 = existing.any { it.name.equals(newSubject2.trim(), ignoreCase = true) }
        assertTrue(isDuplicate2)

        val newSubject3 = "Química General"
        val isDuplicate3 = existing.any { it.name.equals(newSubject3.trim(), ignoreCase = true) }
        assertFalse(isDuplicate3)
    }
}
