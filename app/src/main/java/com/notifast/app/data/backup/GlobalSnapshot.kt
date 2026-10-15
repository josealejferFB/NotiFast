package com.notifast.app.data.backup

import com.notifast.app.data.model.Teacher
import java.io.File

data class GlobalSnapshot(
    val firstName: String = "",
    val lastName: String = "",
    val avatar: File? = null,
    val prefs: Map<String, String> = emptyMap(),
    val teachers: List<Teacher> = emptyList()
)
