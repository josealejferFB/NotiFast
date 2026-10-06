package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.EvaluationType
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.package_data.SubjectPackage
import com.notifast.app.data.model.package_data.SubjectPackageCodec
import com.notifast.app.domain.model.package_data.ConflictStrategy
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImportSubjectPackageUseCaseTest {

    private val noteRepository = mockk<NoteRepository>()
    private val evaluationRepository = mockk<EvaluationRepository>()
    private val scheduleRepository = mockk<ScheduleRepository>()
    private lateinit var importSubjectPackageUseCase: ImportSubjectPackageUseCase

    private val samplePkg = SubjectPackage(
        schemaVersion = 1,
        subject = SubjectData(name = "Química Orgánica", icon = "🧪", colorHex = "#15803D"),
        schedules = listOf(Schedule(id = "s1", subjectName = "Química Orgánica", dayOfWeek = 2, startTime = "08:00", endTime = "10:00")),
        evaluations = listOf(Evaluation(id = "e1", subjectName = "Química Orgánica", title = "Lab 1", type = EvaluationType.PROJECT.name, weight = 20.0)),
        notes = listOf(Note(id = "n1", subject = "Química Orgánica", title = "Alcanos", content = "Hidrocarburos saturados"))
    )

    private val validCompressedPayload = SubjectPackageCodec.encodeToCompressedString(samplePkg)

    @Before
    fun setUp() {
        coEvery { noteRepository.addSubject(any(), any(), any(), any()) } returns Result.success("sub-1")
        coEvery { scheduleRepository.addSchedule(any(), any()) } returns Result.success("sch-1")
        coEvery { evaluationRepository.addEvaluation(any(), any(), any()) } returns Result.success("ev-1")
        coEvery { noteRepository.addNote(any(), any(), any(), any(), any(), any()) } returns Result.success("note-1")

        importSubjectPackageUseCase = ImportSubjectPackageUseCase(
            noteRepository = noteRepository,
            evaluationRepository = evaluationRepository,
            scheduleRepository = scheduleRepository
        )
    }

    @Test
    fun `import new subject succeeds and persists all entities`() = runTest {
        every { noteRepository.getSubjects("u1") } returns flowOf(emptyList())

        val result = importSubjectPackageUseCase("u1", validCompressedPayload, ConflictStrategy.RENAME)
        assertTrue(result.isSuccess)
        assertEquals("Química Orgánica", result.getOrThrow())

        coVerify { noteRepository.addSubject("u1", "Química Orgánica", "🧪", "#15803D") }
        coVerify(exactly = 1) { scheduleRepository.addSchedule("u1", any()) }
        coVerify(exactly = 1) { evaluationRepository.addEvaluation("u1", any(), null) }
        coVerify(exactly = 1) { noteRepository.addNote("u1", "Química Orgánica", "Alcanos", "Hidrocarburos saturados", null, any()) }
    }

    @Test
    fun `import existing subject with RENAME strategy generates unique name`() = runTest {
        every { noteRepository.getSubjects("u1") } returns flowOf(listOf(SubjectData(name = "Química Orgánica")))

        val result = importSubjectPackageUseCase("u1", validCompressedPayload, ConflictStrategy.RENAME)
        assertTrue(result.isSuccess)
        val importedName = result.getOrThrow()
        assertTrue(importedName.startsWith("Química Orgánica (Copia"))

        coVerify { noteRepository.addSubject("u1", importedName, "🧪", "#15803D") }
    }

    @Test
    fun `import with invalid payload returns failure`() = runTest {
        val result = importSubjectPackageUseCase("u1", "INVALID_DATA")
        assertTrue(result.isFailure)
    }
}
