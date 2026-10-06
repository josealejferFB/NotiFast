package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.AcademicStats
import com.notifast.app.domain.model.GradeBandDistribution
import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.model.SubjectAcademicHealth
import com.notifast.app.domain.model.SubjectHealthStatus
import java.util.Locale
import javax.inject.Inject

/**
 * Academic health/stats per subject. All grade math delegates to [GradePolicy]
 * so this stays consistent with the dashboard average and the simulator (D1-D6).
 */
class CalculateAcademicStatsUseCase @Inject constructor() {

    operator fun invoke(
        evaluations: List<Evaluation>,
        subjects: List<SubjectData>,
        schedules: List<Schedule>,
        gradeScale: GradeScale,
        customMinPassingGrade: Double? = null
    ): AcademicStats {
        if (subjects.isEmpty() && evaluations.isEmpty()) {
            return AcademicStats()
        }

        val minPassingGrade = GradePolicy.resolveMinPassing(gradeScale, customMinPassingGrade)
        val maxGrade = gradeScale.maxGrade
        val minGrade = gradeScale.minGrade

        val subjectHealthList = subjects.map { subject ->
            calculateSubjectHealth(
                subject = subject,
                allEvaluations = evaluations,
                allSchedules = schedules,
                minPassingGrade = minPassingGrade,
                maxGrade = maxGrade,
                gradeScale = gradeScale
            )
        }

        val totalWeeklyHours = subjectHealthList.sumOf { it.weeklyHours }
        val approvedSubjects = subjectHealthList.filter { it.status == SubjectHealthStatus.APPROVED }
        val atRiskSubjects = subjectHealthList.filter {
            it.status == SubjectHealthStatus.AT_RISK || it.status == SubjectHealthStatus.CRITICAL_FAIL
        }
        val onTrackOrApproved = subjectHealthList.filter {
            it.status == SubjectHealthStatus.ON_TRACK || it.status == SubjectHealthStatus.APPROVED
        }

        val projectedPassingRate = if (subjectHealthList.isNotEmpty()) {
            (onTrackOrApproved.size.toDouble() / subjectHealthList.size.toDouble()) * 100.0
        } else 0.0

        val evaluatedSubjects = subjectHealthList.filter { it.evaluatedWeight > 0 }
        val globalAverage = if (evaluatedSubjects.isNotEmpty()) {
            evaluatedSubjects.map { it.currentAverage }.average()
        } else 0.0

        val bestSubject = evaluatedSubjects.maxByOrNull { it.currentAverage }
        val lowestSubject = evaluatedSubjects.minByOrNull { it.currentAverage }

        val gradeDistribution = calculateDistribution(evaluations, gradeScale)
        val recommendations = generateRecommendations(atRiskSubjects)

        return AcademicStats(
            globalAverage = globalAverage,
            projectedPassingRate = projectedPassingRate,
            approvedSubjectsCount = approvedSubjects.size,
            totalSubjectsCount = subjects.size,
            subjectHealthList = subjectHealthList,
            atRiskSubjects = atRiskSubjects,
            bestSubject = bestSubject,
            lowestSubject = lowestSubject,
            gradeDistribution = gradeDistribution,
            totalWeeklyClassHours = totalWeeklyHours,
            studyRecommendations = recommendations
        )
    }

    private fun calculateSubjectHealth(
        subject: SubjectData,
        allEvaluations: List<Evaluation>,
        allSchedules: List<Schedule>,
        minPassingGrade: Double,
        maxGrade: Double,
        gradeScale: GradeScale
    ): SubjectAcademicHealth {
        val subjectEvals = allEvaluations.filter { it.subjectName.equals(subject.name, ignoreCase = true) }
        val subjectSchedules = allSchedules.filter { it.subjectName.equals(subject.name, ignoreCase = true) }

        val weeklyHours = subjectSchedules.sumOf { schedule ->
            calculateHours(schedule.startTime, schedule.endTime)
        }

        val evaluatedWeight = GradePolicy.evaluatedWeight(subjectEvals)
        val remainingWeight = GradePolicy.remainingWeight(subjectEvals)
        val accumulatedPoints = GradePolicy.accumulatedPoints(subjectEvals, gradeScale)
        val currentAverage = GradePolicy.weightedAverage(subjectEvals, gradeScale) ?: 0.0

        val pointsNeeded = (minPassingGrade - accumulatedPoints).coerceAtLeast(0.0)
        val minGradeNeeded = if (accumulatedPoints >= minPassingGrade) {
            0.0
        } else if (remainingWeight > 0.0) {
            (pointsNeeded / remainingWeight) * 100.0
        } else {
            Double.POSITIVE_INFINITY
        }

        val status = when {
            accumulatedPoints >= minPassingGrade -> SubjectHealthStatus.APPROVED
            minGradeNeeded > maxGrade -> SubjectHealthStatus.CRITICAL_FAIL
            evaluatedWeight > 0.0 && currentAverage < minPassingGrade -> SubjectHealthStatus.AT_RISK
            else -> SubjectHealthStatus.ON_TRACK
        }

        return SubjectAcademicHealth(
            subjectName = subject.name,
            colorHex = subject.colorHex.ifBlank { null },
            currentAverage = currentAverage,
            evaluatedWeight = evaluatedWeight,
            remainingWeight = remainingWeight,
            accumulatedPoints = accumulatedPoints,
            minGradeNeededToPass = if (minGradeNeeded.isInfinite()) maxGrade + 1.0 else minGradeNeeded,
            status = status,
            weeklyHours = weeklyHours
        )
    }

    private fun calculateHours(startTime: String, endTime: String): Double {
        val startMins = parseMinutes(startTime) ?: return 0.0
        val endMins = parseMinutes(endTime) ?: return 0.0
        return if (endMins > startMins) (endMins - startMins) / 60.0 else 0.0
    }

    private fun parseMinutes(time: String): Int? {
        val parts = time.trim().split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }

    private fun calculateDistribution(
        evaluations: List<Evaluation>,
        gradeScale: GradeScale
    ): GradeBandDistribution {
        val graded = evaluations.filter { GradePolicy.countsForAverage(it) }
            .map { GradePolicy.toDisplayGrade(it, gradeScale) }
        if (graded.isEmpty()) return GradeBandDistribution()

        var excellent = 0
        var good = 0
        var regular = 0
        var atRisk = 0

        val minGrade = gradeScale.minGrade
        val maxGrade = gradeScale.maxGrade
        val minPassingGrade = gradeScale.minPassingGrade
        val excellentThreshold = minGrade + (maxGrade - minGrade) * 0.90
        val goodThreshold = minGrade + (maxGrade - minGrade) * 0.70

        graded.forEach { grade ->
            when {
                grade >= excellentThreshold -> excellent++
                grade >= goodThreshold -> good++
                grade >= minPassingGrade -> regular++
                else -> atRisk++
            }
        }

        return GradeBandDistribution(
            excellentCount = excellent,
            goodCount = good,
            regularCount = regular,
            atRiskCount = atRisk,
            totalEvaluated = graded.size
        )
    }

    private fun generateRecommendations(atRiskSubjects: List<SubjectAcademicHealth>): List<String> {
        return atRiskSubjects.map { subject ->
            if (subject.status == SubjectHealthStatus.CRITICAL_FAIL) {
                "En ${subject.subjectName} la meta mínima ya no es alcanzable. Consulta opciones de recuperación académica."
            } else {
                val neededStr = String.format(Locale.US, "%.1f", subject.minGradeNeededToPass)
                val remStr = String.format(Locale.US, "%.0f", subject.remainingWeight)
                "En ${subject.subjectName} necesitas promediar $neededStr en el $remStr% restante para aprobar."
            }
        }
    }
}
