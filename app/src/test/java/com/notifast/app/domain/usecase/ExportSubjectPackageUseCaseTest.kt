package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.EvaluationType
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.package_data.SubjectPackageCodec
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExportSubjectPackageUseCaseTest {

    private val noteRepository = mockk<NoteRepository>()
    private val evaluationRepository = mockk<EvaluationRepository>()
    private val scheduleRepository = mockk<ScheduleRepository>()
    private lateinit var exportSubjectPackageUseCase: ExportSubjectPackageUseCase

    private val sampleSubject = SubjectData(name = "Física II", icon = "⚡", colorHex = "#3B9797")
    private val sampleSchedules = listOf(
        Schedule(id = "s1", subjectName = "Física II", teacher = "Dr. Maxwell", dayOfWeek = 1, startTime = "07:00", endTime = "09:00")
    )
    private val sampleEvaluations = listOf(
        Evaluation(id = "e1", subjectName = "Física II", title = "Parcial Electromagnetismo", type = EvaluationType.EXAM.name, weight = 40.0)
    )
    private val sampleNotes = listOf(
        Note(id = "n1", subject = "Física II", title = "Ley de Gauss", content = "Flujo de campo eléctrico", tags = listOf("formulas"))
    )

    @Before
    fun setUp() {
        every { noteRepository.getSubjects("u1") } returns flowOf(listOf(sampleSubject))
        every { scheduleRepository.getSchedules("u1") } returns flowOf(sampleSchedules)
        every { evaluationRepository.getEvaluations("u1") } returns flowOf(sampleEvaluations)
        every { noteRepository.getNotesBySubject("u1", "Física II") } returns flowOf(sampleNotes)

        exportSubjectPackageUseCase = ExportSubjectPackageUseCase(
            noteRepository = noteRepository,
            evaluationRepository = evaluationRepository,
            scheduleRepository = scheduleRepository
        )
    }

    @Test
    fun `export existing subject produces valid compressed package payload`() = runTest {
        val result = exportSubjectPackageUseCase("u1", "Física II", includeNotes = true)
        assertTrue(result.isSuccess)

        val payload = result.getOrThrow()
        assertNotNull(payload)
        assertTrue(payload.startsWith(SubjectPackageCodec.NOTIFAST_MAGIC_PREFIX))

        val decoded = SubjectPackageCodec.decodeFromCompressedString(payload)
        assertEquals("Física II", decoded.subject.name)
        assertEquals(1, decoded.schedules.size)
        assertEquals(1, decoded.evaluations.size)
        assertEquals(1, decoded.notes.size)
    }

    @Test
    fun `export non-existing subject returns failure`() = runTest {
        val result = exportSubjectPackageUseCase("u1", "Inexistente")
        assertTrue(result.isFailure)
    }
}
