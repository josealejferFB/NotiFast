package com.notifast.app.data.model

import java.util.Date

data class Schedule(
    val id: String = "",
    val userId: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val modality: String = MODALITY_PRESENCIAL,
    val dayOfWeek: Int = 0,
    val startTime: String = "",
    val endTime: String = "",
    val classroom: String = "",
    val teacher: String = "",
    val meetingUrl: String? = null,
    val color: Long = 0L,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
) {
    companion object {
        val DAY_NAMES = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
        const val MODALITY_PRESENCIAL = "presencial"
        const val MODALITY_VIRTUAL = "virtual"
    }
}
