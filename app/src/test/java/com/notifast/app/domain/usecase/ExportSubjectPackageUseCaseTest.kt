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

    @Test
    fun `export strips image blocks and local paths from exported notes`() = runTest {
        val noteWithImage = Note(
            id = "n2",
            userId = "u1",
            subject = "Física II",
            title = "Óptica",
            content = "Leyes de reflexión",
            imageUrl = "file:///data/user/0/com.notifast.app/files/images/legacy.jpg",
            blocks = listOf(
                com.notifast.app.data.model.NoteBlock.Paragraph(id = "b1", text = "Texto introductorio"),
                com.notifast.app.data.model.NoteBlock.Image(id = "b2", path = "file:///data/user/0/com.notifast.app/files/images/diagram.jpg", caption = "Diagrama")
            )
        )
        every { noteRepository.getNotesBySubject("u1", "Física II") } returns flowOf(listOf(noteWithImage))

        val result = exportSubjectPackageUseCase("u1", "Física II", includeNotes = true)
        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()

        val decoded = SubjectPackageCodec.decodeFromCompressedString(payload)
        val exportedNote = decoded.notes.first()
        org.junit.Assert.assertNull(exportedNote.imageUrl)
        assertEquals("", exportedNote.userId)
        assertTrue(exportedNote.blocks.none { it is com.notifast.app.data.model.NoteBlock.Image })
        assertEquals(1, exportedNote.blocks.size)
        assertTrue(exportedNote.blocks.first() is com.notifast.app.data.model.NoteBlock.Paragraph)
    }

    @Test
    fun `export then import roundtrip with notes preserves counts and decodes note blocks`() = runTest {
        val note = Note(
            id = "n-rt",
            userId = "u1",
            subject = "Física II",
            title = "Termodinámica",
            content = "Leyes térmicas",
            blocks = listOf(
                com.notifast.app.data.model.NoteBlock.Paragraph(id = "p1", text = "Primera ley de la termodinámica")
            )
        )
        every { noteRepository.getNotesBySubject("u1", "Física II") } returns flowOf(listOf(note))

        val exportResult = exportSubjectPackageUseCase("u1", "Física II", includeNotes = true)
        assertTrue(exportResult.isSuccess)
        val payload = exportResult.getOrThrow()

        val destNoteRepo = mockk<NoteRepository>()
        val destEvalRepo = mockk<EvaluationRepository>()
        val destSchedRepo = mockk<ScheduleRepository>()

        every { destNoteRepo.getSubjects("u2") } returns flowOf(emptyList())
        io.mockk.coEvery { destNoteRepo.addSubject("u2", "Física II", "⚡", "#3B9797", any()) } returns Result.success("sub-u2")
        io.mockk.coEvery { destSchedRepo.addSchedule("u2", any()) } returns Result.success("sch-u2")
        io.mockk.coEvery { destEvalRepo.addEvaluation("u2", any(), any()) } returns Result.success("ev-u2")
        io.mockk.coEvery { destNoteRepo.addNoteWithBlocks("u2", "Física II", any(), any(), any()) } returns Result.success("note-u2")

        val importer = ImportSubjectPackageUseCase(
            destNoteRepo,
            destEvalRepo,
            destSchedRepo
        )

        val importResult = importer("u2", payload, com.notifast.app.domain.model.package_data.ConflictStrategy.RENAME)
        assertTrue(importResult.isSuccess)
        assertEquals("Física II", importResult.getOrThrow())

        io.mockk.coVerify(exactly = 1) { destSchedRepo.addSchedule("u2", any()) }
        io.mockk.coVerify(exactly = 1) { destEvalRepo.addEvaluation("u2", any(), null) }
        io.mockk.coVerify(exactly = 1) { destNoteRepo.addNoteWithBlocks("u2", "Física II", "Termodinámica", any(), any()) }
    }
}
