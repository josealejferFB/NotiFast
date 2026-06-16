package com.example.notifast.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Schedule(
    val id: String = "",
    val userId: String = "",
    val subjectName: String = "",
    val dayOfWeek: Int = 0,
    val startTime: String = "",
    val endTime: String = "",
    val classroom: String = "",
    val teacher: String = "",
    val color: Long = 0L,
    @ServerTimestamp val createdAt: Date? = null
) {
    companion object {
        val DAY_NAMES = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    }
}
