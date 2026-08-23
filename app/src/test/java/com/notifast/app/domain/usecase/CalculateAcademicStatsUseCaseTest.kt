package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
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
        assertEquals(0, stats.approvedSubjectsCount)
        assertTrue(stats.subjectHealthList.isEmpty())
        assertTrue(stats.atRiskSubjects.isEmpty())
        assertEquals(0.0, stats.totalWeeklyClassHours, 0.01)
    }

    @Test
    fun `calculates subject health status accurately for on track, approved, at risk and critical fail`() {
        val subjects = listOf(
            SubjectData(name = "Matemáticas", colorHex = "#BF092F"),
            SubjectData(name = "Física", colorHex = "#3B9797"),
            SubjectData(name = "Química", colorHex = "#4338CA"),
            SubjectData(name = "Historia", colorHex = "#B45309")
        )

        val evaluations = listOf(
            // Matemáticas: 20 con peso 50% -> acumula 10 puntos (min passing 10.0 en 0-20, ya aprobó)
            Evaluation(id = "e1", subjectName = "Matemáticas", grade = 20.0, weight = 50.0, isPresented = true),
            // Física: 8.0 con peso 50% -> promedio actual 8.0 < 10.0, falta 6.0 puntos en 50% restante (necesita 12.0 <= 20) -> AT_RISK
            Evaluation(id = "e2", subjectName = "Física", grade = 8.0, weight = 50.0, isPresented = true),
            // Química: 0.0 con peso 60% -> acumula 0 puntos, en 40% restante el maximo posible es 0.40 * 20 = 8 < 10 -> CRITICAL_FAIL
            Evaluation(id = "e3", subjectName = "Química", grade = 0.0, weight = 60.0, isPresented = true),
            // Historia: 14.0 con peso 50% -> promedio 14.0 >= 10.0, no ha alcanzado 10 acumulados (lleva 7) -> ON_TRACK
            Evaluation(id = "e4", subjectName = "Historia", grade = 14.0, weight = 50.0, isPresented = true)
        )

        val stats = useCase(
            evaluations = evaluations,
            subjects = subjects,
            schedules = emptyList(),
            gradeScale = GradeScale.SCALE_0_20
        )

        assertEquals(4, stats.totalSubjectsCount)
        val mateHealth = stats.subjectHealthList.find { it.subjectName == "Matemáticas" }
        assertNotNull(mateHealth)
        assertEquals(SubjectHealthStatus.APPROVED, mateHealth?.status)
        assertEquals(0.0, mateHealth?.minGradeNeededToPass ?: 0.0, 0.01)

        val fisicaHealth = stats.subjectHealthList.find { it.subjectName == "Física" }
        assertNotNull(fisicaHealth)
        assertEquals(SubjectHealthStatus.AT_RISK, fisicaHealth?.status)
        assertEquals(12.0, fisicaHealth?.minGradeNeededToPass ?: 0.0, 0.01)

        val quimicaHealth = stats.subjectHealthList.find { it.subjectName == "Química" }
        assertNotNull(quimicaHealth)
        assertEquals(SubjectHealthStatus.CRITICAL_FAIL, quimicaHealth?.status)

        val historiaHealth = stats.subjectHealthList.find { it.subjectName == "Historia" }
        assertNotNull(historiaHealth)
        assertEquals(SubjectHealthStatus.ON_TRACK, historiaHealth?.status)

        assertEquals(2, stats.atRiskSubjects.size) // Física y Química
        assertEquals(50.0, stats.projectedPassingRate, 0.01) // 2 de 4 (Mate + Historia)
    }

    @Test
    fun `calculates grade band distribution across all evaluated grades`() {
        val evaluations = listOf(
            Evaluation(id = "1", subjectName = "A", grade = 19.0, isPresented = true), // Sobresaliente (>= 18)
            Evaluation(id = "2", subjectName = "A", grade = 15.0, isPresented = true), // Notable (>= 14 y < 18)
            Evaluation(id = "3", subjectName = "B", grade = 11.0, isPresented = true), // Regular (>= 10 y < 14)
            Evaluation(id = "4", subjectName = "C", grade = 7.0, isPresented = true)   // En Riesgo (< 10)
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
    fun `calculates weekly class hours correlation from schedules`() {
        val subjects = listOf(SubjectData(name = "Programación"))
        val schedules = listOf(
            // Lunes 08:00 - 10:00 (2.0 hrs)
            Schedule(id = "s1", subjectName = "Programación", dayOfWeek = 1, startTime = "08:00", endTime = "10:00"),
            // Miércoles 14:00 - 15:30 (1.5 hrs)
            Schedule(id = "s2", subjectName = "Programación", dayOfWeek = 3, startTime = "14:00", endTime = "15:30")
        )

        val stats = useCase(
            evaluations = emptyList(),
            subjects = subjects,
            schedules = schedules,
            gradeScale = GradeScale.SCALE_0_20
        )

        assertEquals(3.5, stats.totalWeeklyClassHours, 0.01)
        val progHealth = stats.subjectHealthList.find { it.subjectName == "Programación" }
        assertEquals(3.5, progHealth?.weeklyHours ?: 0.0, 0.01)
    }

    @Test
    fun `supports scale 0-100 and custom min passing grade`() {
        val subjects = listOf(SubjectData(name = "Inglés"))
        val evaluations = listOf(
            Evaluation(id = "e1", subjectName = "Inglés", grade = 55.0, weight = 50.0, isPresented = true)
        )

        // Min passing 70.0 en 0-100. Lleva 55 * 0.5 = 27.5 puntos. Faltan 42.5 en 50% -> necesita 85.0
        val stats = useCase(
            evaluations = evaluations,
            subjects = subjects,
            schedules = emptyList(),
            gradeScale = GradeScale.SCALE_0_100,
            customMinPassingGrade = 70.0
        )

        val inglesHealth = stats.subjectHealthList.first()
        assertEquals(SubjectHealthStatus.AT_RISK, inglesHealth.status)
        assertEquals(85.0, inglesHealth.minGradeNeededToPass, 0.01)
    }
}
