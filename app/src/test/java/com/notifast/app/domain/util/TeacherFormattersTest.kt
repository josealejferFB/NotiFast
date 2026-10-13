package com.notifast.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TeacherFormattersTest {

    @Test
    fun cleanPhoneNumber_removes_spaces_dashes_and_parentheses() {
        val raw = "+58 (412) 555-0192"
        val cleaned = TeacherFormatters.cleanPhoneNumber(raw)
        assertEquals("+584125550192", cleaned)
    }

    @Test
    fun cleanPhoneNumber_keeps_leading_plus() {
        val raw = "+1-800-555-0199"
        val cleaned = TeacherFormatters.cleanPhoneNumber(raw)
        assertEquals("+18005550199", cleaned)
    }

    @Test
    fun cleanPhoneNumber_handles_empty_or_blank() {
        assertEquals("", TeacherFormatters.cleanPhoneNumber("   "))
        assertEquals("", TeacherFormatters.cleanPhoneNumber(""))
    }

    @Test
    fun buildWhatsAppMessage_with_teacher_and_subject() {
        val msg = TeacherFormatters.buildWhatsAppMessage(
            teacherName = "Carlos Mendoza",
            subjectName = "Cálculo I"
        )
        assertEquals(
            "Hola Profesor/a Carlos Mendoza, le escribo con respecto a la materia Cálculo I.",
            msg
        )
    }

    @Test
    fun buildWhatsAppMessage_without_subject() {
        val msg = TeacherFormatters.buildWhatsAppMessage(
            teacherName = "Elena Rivas",
            subjectName = ""
        )
        assertEquals(
            "Hola Profesor/a Elena Rivas, le escribo por un asunto académico de su materia.",
            msg
        )
    }

    @Test
    fun buildWhatsAppUrl_encodes_phone_and_text() {
        val url = TeacherFormatters.buildWhatsAppUrl(
            phoneNumber = "+58 412 1234567",
            teacherName = "Carlos",
            subjectName = "Física"
        )
        assertTrue(url.startsWith("https://wa.me/584121234567?text="))
        assertFalse(url.contains(" "))
    }
}
