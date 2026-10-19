package com.notifast.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class UnergSchedulePdfParserTest {

    private val parser = UnergSchedulePdfParser()

    @Test
    fun `parse unerg pdf 1 extracts correct metadata and only presencial subjects`() {
        val stream = javaClass.classLoader?.getResourceAsStream("unerg_fixtures/horario_1.pdf")
        assertNotNull("Fixture horario_1.pdf not found", stream)

        val result = parser.parse(stream!!)
        assertTrue("Parser returned failure: ${result.exceptionOrNull()}", result.isSuccess)

        val data = result.getOrThrow()
        assertEquals("30961367", data.studentCi)
        assertTrue("Academic period should contain 2026", data.academicPeriod.contains("2026"))

        val subjectNames = data.subjects.map { it.subjectName }
        assertEquals("Expected 6 presencial subjects", 6, data.subjects.size)
        assertTrue("Expected ignored non-presencial count == 6", data.ignoredNonPresencialCount == 6)

        // Verify presencial subjects
        assertTrue(subjectNames.any { it.contains("ELECTIVA LIBRE", ignoreCase = true) })
        assertTrue(subjectNames.any { it.contains("PROYECTO DE GRADO", ignoreCase = true) })
        assertTrue(subjectNames.any { it.contains("INFORMATICA EDUCATIVA", ignoreCase = true) })
        assertTrue(subjectNames.any { it.contains("GERENCIA DE PROYECTO", ignoreCase = true) })
        assertTrue(subjectNames.any { it.contains("ELECTIVA DE AREA", ignoreCase = true) })
        assertTrue(subjectNames.any { it.contains("SISTEMAS DISTRIBUIDOS", ignoreCase = true) })

        // Check section propagation and schedules
        data.subjects.forEach { subject ->
            assertTrue("Section must not be empty", subject.section.isNotBlank())
            assertTrue("Subject ${subject.subjectName} must have schedules", subject.schedules.isNotEmpty())
            subject.schedules.forEach { schedule ->
                assertTrue("Day of week must be between 1 and 7", schedule.dayOfWeek in 1..7)
                assertTrue("Start time must match HH:mm", schedule.startTime.matches(Regex("\\d{2}:\\d{2}")))
                assertTrue("End time must match HH:mm", schedule.endTime.matches(Regex("\\d{2}:\\d{2}")))
                assertTrue("Classroom should not be empty", schedule.classroom.isNotBlank())
                assertFalse("Classroom cannot be A distancia", schedule.classroom.contains("distancia", ignoreCase = true))
            }
        }
    }

    @Test
    fun `parse unerg pdf 2 extracts correct metadata and valid schedules`() {
        val stream = javaClass.classLoader?.getResourceAsStream("unerg_fixtures/horario_2.pdf")
        assertNotNull("Fixture horario_2.pdf not found", stream)

        val result = parser.parse(stream!!)
        assertTrue("Parser returned failure: ${result.exceptionOrNull()}", result.isSuccess)

        val data = result.getOrThrow()
        assertEquals("31053939", data.studentCi)
        assertEquals("Expected 4 presencial subjects", 4, data.subjects.size)
        data.subjects.forEach { subject ->
            assertTrue("Section must not be empty", subject.section.isNotBlank())
            assertTrue(subject.schedules.isNotEmpty())
        }
    }

    @Test
    fun `parse invalid input returns failure`() {
        val invalidBytes = "This is not a PDF file".toByteArray()
        val result = parser.parse(ByteArrayInputStream(invalidBytes))
        assertTrue("Expected failure for invalid PDF", result.isFailure)
    }
}
