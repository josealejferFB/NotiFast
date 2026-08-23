package com.notifast.app.domain.model

enum class SubjectHealthStatus {
    APPROVED,     // Ya alcanzó la nota mínima aprobatoria o completó 100% aprobado
    ON_TRACK,     // Promedio actual >= minPassingGrade y factible de aprobar
    AT_RISK,      // Promedio actual < minPassingGrade pero aún matemáticamente recuperable
    CRITICAL_FAIL // Matemáticamente imposible alcanzar minPassingGrade con el peso restante
}

data class SubjectAcademicHealth(
    val subjectName: String,
    val colorHex: String?,
    val currentAverage: Double,
    val evaluatedWeight: Double,      // 0..100%
    val remainingWeight: Double,      // 0..100%
    val accumulatedPoints: Double,    // Puntos obtenidos (ej. 14 * 0.3 = 4.2)
    val minGradeNeededToPass: Double, // Nota promedio necesaria en lo que queda para aprobar (0 si ya aprobó)
    val status: SubjectHealthStatus,
    val weeklyHours: Double           // Horas de clase a la semana
)

data class GradeBandDistribution(
    val excellentCount: Int = 0,  // Sobresaliente (>= 90% escala)
    val goodCount: Int = 0,       // Notable / Bueno (>= 70% escala)
    val regularCount: Int = 0,    // Regular / Aprobado (>= minPassingGrade)
    val atRiskCount: Int = 0,     // En Riesgo / Desaprobado (< minPassingGrade)
    val totalEvaluated: Int = 0
)

data class AcademicStats(
    val globalAverage: Double = 0.0,
    val projectedPassingRate: Double = 0.0, // % de materias en ON_TRACK o APPROVED (0..100)
    val approvedSubjectsCount: Int = 0,
    val totalSubjectsCount: Int = 0,
    val subjectHealthList: List<SubjectAcademicHealth> = emptyList(),
    val atRiskSubjects: List<SubjectAcademicHealth> = emptyList(),
    val bestSubject: SubjectAcademicHealth? = null,
    val lowestSubject: SubjectAcademicHealth? = null,
    val gradeDistribution: GradeBandDistribution = GradeBandDistribution(),
    val totalWeeklyClassHours: Double = 0.0,
    val studyRecommendations: List<String> = emptyList()
)
