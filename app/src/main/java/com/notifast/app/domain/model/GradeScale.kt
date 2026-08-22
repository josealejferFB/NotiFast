package com.notifast.app.domain.model

enum class GradeScale(
    val id: String,
    val label: String,
    val minGrade: Double,
    val maxGrade: Double,
    val minPassingGrade: Double
) {
    SCALE_0_10("0-10", "Escala 0 - 10", 0.0, 10.0, 6.0),
    SCALE_0_20("0-20", "Escala 0 - 20", 0.0, 20.0, 10.0),
    SCALE_0_100("0-100", "Escala 0 - 100", 0.0, 100.0, 60.0),
    SCALE_LETTER("A-F", "Escala Letras (A - F)", 0.0, 4.0, 2.0);

    fun formatGrade(grade: Double?): String {
        if (grade == null) return "-"
        return when (this) {
            SCALE_0_10, SCALE_0_20 -> String.format(java.util.Locale.US, "%.1f", grade)
            SCALE_0_100 -> String.format(java.util.Locale.US, "%.0f", grade)
            SCALE_LETTER -> when {
                grade >= 3.7 -> "A"
                grade >= 3.0 -> "B"
                grade >= 2.0 -> "C"
                grade >= 1.0 -> "D"
                else -> "F"
            }
        }
    }

    companion object {
        fun fromId(id: String?): GradeScale {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: SCALE_0_10
        }
    }
}
