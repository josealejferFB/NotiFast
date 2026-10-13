package com.notifast.app.domain.util

import java.net.URLEncoder

object TeacherFormatters {

    fun cleanPhoneNumber(phone: String): String {
        if (phone.isBlank()) return ""
        val trimmed = phone.trim()
        val hasLeadingPlus = trimmed.startsWith("+")
        val digits = trimmed.filter { it.isDigit() }
        return if (hasLeadingPlus) "+$digits" else digits
    }

    fun buildWhatsAppMessage(teacherName: String, subjectName: String): String {
        val cleanTeacher = teacherName.trim()
        val cleanSubject = subjectName.trim()
        return if (cleanSubject.isNotBlank()) {
            "Hola Profesor/a $cleanTeacher, le escribo con respecto a la materia $cleanSubject."
        } else {
            "Hola Profesor/a $cleanTeacher, le escribo por un asunto académico de su materia."
        }
    }

    fun buildWhatsAppUrl(phoneNumber: String, teacherName: String, subjectName: String): String {
        val digits = phoneNumber.filter { it.isDigit() }
        val message = buildWhatsAppMessage(teacherName, subjectName)
        val encodedMessage = URLEncoder.encode(message, "UTF-8").replace("+", "%20")
        return "https://wa.me/$digits?text=$encodedMessage"
    }
}
