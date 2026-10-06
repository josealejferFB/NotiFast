package com.notifast.app.data.model

/**
 * Domain subject. [id] is the stable identity used for joins; [name] is display-only
 * and can be renamed without breaking children (D7). [cycle] is the academic term (D8).
 */
data class SubjectData(
    val id: String = "",
    val name: String,
    val icon: String = "📚",
    val colorHex: String = "",
    val cycle: String = ""
)
