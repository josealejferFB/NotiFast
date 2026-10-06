package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.model.SubjectHealthStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateAcademicStatsUseCaseTest {

    private lateinit var useCase: CalculateAcademicStatsUseCase

    @Before
    fun setUp() {
        useCase = CalculateAcademicStatsUseCase()
    }

    @Test
    fun `empty inputs returns safe default stats`() {
        val stats = useCase(
            evaluations = emptyList(),
            subjects = emptyList(),
            schedules = emptyList(),
            gradeScale = GradeScale.SCALE_0_20
        )

        assertEquals(0.0, stats.globalAverage, 0.01)
        assertEquals(0.0, stats.projectedPassingRate, 0.01)
        assertEquals(0, stats.totalSubjectsCount)
        assertTrue(stats.subjectHealthList.isEmpty())
    }

    @Test
    fun `subject health statuses with pending weights (D6 remaining)`() {
        val subjects = listOf(
            SubjectData(name = "Matemáticas"),
            SubjectData(name = "Física"),
            SubjectData(name = "Química"),
            SubjectData(name = "Historia")
        )
        val evaluations = listOf(
            // Mate: 20 con 50% + pendiente 50% → acumula 10 < 11 → ON_TRACK (avg 20 ≥ 11)
            Evaluation(id = "m1", subjectName = "Matemáticas", grade = 20.0, weight = 50.0, isPresented = true, gradeScaleId = "0-20"),
            Evaluation(id = "m2", subjectName = "Matemáticas", weight = 50.0, isPresented = false),
            // Física: 8 con 50% + pendiente 50% → acumula 4, necesita 7 en 50% → 14.0 ≤ 20 → AT_RISK
            Evaluation(id = "f1", subjectName = "Física", grade = 8.0, weight = 50.0, isPresented = true, gradeScaleId = "0-20"),
            Evaluation(id = "f2", subjectName = "Física", weight = 50.0, isPresented = false),
            // Química: 0 con 60% + pendiente 40% → acumula 0, necesita 11 en 40% → 27.5 > 20 → CRITICAL_FAIL
            Evaluation(id = "q1", subjectName = "Química", grade = 0.0, weight = 60.0, isPresented = true, gradeScaleId = "0-20"),
            Evaluation(id = "q2", subjectName = "Química", weight = 40.0, isPresented = false),
            // Historia: 14 con 50% + pendiente 50% → acumula 7, necesita 4 en 50% → 8.0 ≤ 20, avg 14 ≥ 11 → ON_TRACK
            Evaluation(id = "h1", subjectName = "Historia", grade = 14.0, weight = 50.0, isPresented = true, gradeScaleId = "0-20"),
            Evaluation(id = "h2", subjectName = "Historia", weight = 50.0, isPresented = false)
        )

        val stats = useCase(
            evaluations = evaluations,
            subjects = subjects,
            schedules = emptyList(),
            gradeScale = GradeScale.SCALE_0_20
        )

        val mate = stats.subjectHealthList.first { it.subjectName == "Matemáticas" }
        assertEquals(SubjectHealthStatus.ON_TRACK, mate.status)
        assertEquals(50.0, mate.remainingWeight, 0.01)

        val fisica = stats.subjectHealthList.first { it.subjectName == "Física" }
        assertEquals(SubjectHealthStatus.AT_RISK, fisica.status)
        assertEquals(50.0, fisica.remainingWeight, 0.01)
        assertEquals(14.0, fisica.minGradeNeededToPass, 0.01)

        val quimica = stats.subjectHealthList.first { it.subjectName == "Química" }
        assertEquals(SubjectHealthStatus.CRITICAL_FAIL, quimica.status)

        val historia = stats.subjectHealthList.first { it.subjectName == "Historia" }
        assertEquals(SubjectHealthStatus.ON_TRACK, historia.status)
    }

    @Test
    fun `no phantom remaining when all evaluations are graded and presented`() {
        val subjects = listOf(SubjectData(name = "Física"))
        val evaluations = listOf(
            Evaluation(id = "f1", subjectName = "Física", grade = 10.0, weight = 30.0, isPresented = true),
            Evaluation(id = "f2", subjectName = "Física", grade = 8.0, weight = 30.0, isPresented = true)
        )

        val stats = useCase(
            evaluations = evaluations,
            subjects = subjects,
            schedules = emptyList(),
            gradeScale = GradeScale.SCALE_0_10
        )

        val health = stats.subjectHealthList.first()
        assertEquals(0.0, health.remainingWeight, 0.01)
        // accumulated = 10*0.3 + 8*0.3 = 5.4 < 6.0 and no remaining → CRITICAL_FAIL
        assertEquals(SubjectHealthStatus.CRITICAL_FAIL, health.status)
    }

    @Test
    fun `presented-without-grade is pending not zero`() {
        val subjects = listOf(SubjectData(name = "Física"))
        val evaluations = listOf(
            Evaluation(id = "f1", subjectName = "Física", grade = 8.0, weight = 50.0, isPresented = true),
            Evaluation(id = "f2", subjectName = "Física", grade = null, weight = 50.0, isPresented = true)
        )

        val stats = useCase(
            evaluations = evaluations,
            subjects = subjects,
            schedules = emptyList(),
            gradeScale = GradeScale.SCALE_0_10
        )

        val health = stats.subjectHealthList.first()
        assertEquals(8.0, health.currentAverage, 0.01) // NOT dragged to 0 by f2
        assertEquals(50.0, health.evaluatedWeight, 0.01)
        assertEquals(50.0, health.remainingWeight, 0.01)
    }

    @Test
    fun `calculates grade band distribution only from counted evaluations`() {
        val evaluations = listOf(
            Evaluation(id = "1", subjectName = "A", grade = 19.0, isPresented = true, gradeScaleId = "0-20"),
            Evaluation(id = "2", subjectName = "A", grade = 15.0, isPresented = true, gradeScaleId = "0-20"),
            Evaluation(id = "3", subjectName = "B", grade = 11.0, isPresented = true, gradeScaleId = "0-20"),
            Evaluation(id = "4", subjectName = "C", grade = 7.0, isPresented = true, gradeScaleId = "0-20"),
            // not counted (not presented)
            Evaluation(id = "5", subjectName = "C", grade = 20.0, isPresented = false, gradeScaleId = "0-20")
        )
        val subjects = listOf(SubjectData(name = "A"), SubjectData(name = "B"), SubjectData(name = "C"))

        val stats = useCase(
            evaluations = evaluations,
            subjects = subjects,
            schedules = emptyList(),
            gradeScale = GradeScale.SCALE_0_20
        )

        assertEquals(1, stats.gradeDistribution.excellentCount)
        assertEquals(1, stats.gradeDistribution.goodCount)
        assertEquals(1, stats.gradeDistribution.regularCount)
        assertEquals(1, stats.gradeDistribution.atRiskCount)
        assertEquals(4, stats.gradeDistribution.totalEvaluated)
    }

    @Test
    fun `calculates weekly class hours from schedules`() {
        val subjects = listOf(SubjectData(name = "Programación"))
        val schedules = listOf(
            Schedule(id = "s1", subjectName = "Programación", dayOfWeek = 1, startTime = "08:00", endTime = "10:00"),
            Schedule(id = "s2", subjectName = "Programación", dayOfWeek = 3, startTime = "14:00", endTime = "15:30")
        )

        val stats = useCase(
            evaluations = emptyList(),
            subjects = subjects,
            schedules = schedules,
            gradeScale = GradeScale.SCALE_0_20
        )

        assertEquals(3.5, stats.totalWeeklyClassHours, 0.01)
    }

    @Test
    fun `supports scale 0-100 and custom min passing grade`() {
        val subjects = listOf(SubjectData(name = "Inglés"))
        val evaluations = listOf(
            Evaluation(id = "e1", subjectName = "Inglés", grade = 55.0, weight = 50.0, isPresented = true, gradeScaleId = "0-100"),
            Evaluation(id = "e2", subjectName = "Inglés", weight = 50.0, isPresented = false)
        )

        val stats = useCase(
            evaluations = evaluations,
            subjects = subjects,
            schedules = emptyList(),
            gradeScale = GradeScale.SCALE_0_100,
            customMinPassingGrade = 70.0
        )

        val ingles = stats.subjectHealthList.first()
        assertEquals(SubjectHealthStatus.AT_RISK, ingles.status)
        // accumulated = 27.5, need 42.5 in 50% → 85.0
        assertEquals(85.0, ingles.minGradeNeededToPass, 0.01)
    }

    @Test
    fun `remainingWeight matches GradePolicy`() {
        val evals = listOf(
            Evaluation(id = "e1", subjectName = "X", grade = 10.0, weight = 30.0, isPresented = true),
            Evaluation(id = "e2", subjectName = "X", weight = 20.0),
            Evaluation(id = "e3", subjectName = "X", weight = 25.0)
        )
        val subjects = listOf(SubjectData(name = "X"))
        val stats = useCase(evals, subjects, emptyList(), GradeScale.SCALE_0_10)
        assertEquals(GradePolicy.remainingWeight(evals), stats.subjectHealthList.first().remainingWeight, 0.01)
        assertEquals(45.0, stats.subjectHealthList.first().remainingWeight, 0.01)
    }
}
