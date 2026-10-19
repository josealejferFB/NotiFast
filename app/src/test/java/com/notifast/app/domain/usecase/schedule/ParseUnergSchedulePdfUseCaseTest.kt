package com.notifast.app.domain.usecase.schedule

import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.schedule.UnergImportPreviewData
import com.notifast.app.domain.model.schedule.UnergImportPreviewSubject
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.util.UnergSchedulePdfParser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class ParseUnergSchedulePdfUseCaseTest {

    private val parser = mockk<UnergSchedulePdfParser>()
    private val noteRepository = mockk<NoteRepository>()
    private lateinit var useCase: ParseUnergSchedulePdfUseCase

    @Before
    fun setUp() {
        useCase = ParseUnergSchedulePdfUseCase(parser, noteRepository)
    }

    @Test
    fun `when parser succeeds and subject matches existing subject, marks as existing and reuses color and icon`() = runTest {
        val existingSubject = SubjectData(
            id = "subj-123",
            name = "Sistemas Distribuidos",
            icon = "Computer",
            colorHex = "#15803D",
            section = "01"
        )
        coEvery { noteRepository.getSubjects("u1") } returns flowOf(listOf(existingSubject))

        val parsedSubject = UnergImportPreviewSubject(
            subjectName = "SISTEMAS DISTRIBUIDOS",
            section = "1",
            hoursPerWeek = 4,
            classroomSummary = "Aula 1",
            schedules = emptyList(),
            isExistingSubject = false,
            colorHex = "#4338CA",
            icon = "School"
        )
        val previewData = UnergImportPreviewData(
            studentCi = "30961367",
            career = "Ingenieria",
            academicPeriod = "20262",
            subjects = listOf(parsedSubject)
        )

        every { parser.parse(any()) } returns Result.success(previewData)

        val result = useCase("u1", ByteArrayInputStream(ByteArray(10)))
        assertTrue(result.isSuccess)

        val data = result.getOrThrow()
        val subject = data.subjects.first()
        assertTrue(subject.isExistingSubject)
        assertEquals("subj-123", subject.existingSubjectId)
        assertEquals("#15803D", subject.colorHex)
        assertEquals("Computer", subject.icon)
    }

    @Test
    fun `when parser fails, returns failure`() = runTest {
        coEvery { noteRepository.getSubjects("u1") } returns flowOf(emptyList())
        every { parser.parse(any()) } returns Result.failure(Exception("PDF corrupto"))

        val result = useCase("u1", ByteArrayInputStream(ByteArray(10)))
        assertFalse(result.isSuccess)
    }
}
