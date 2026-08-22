package com.notifast.app.domain.usecase

import app.cash.turbine.test
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.repository.NoteRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetNotesWithSubjectInfoUseCaseTest {

    private lateinit var noteRepository: NoteRepository
    private lateinit var getNotesWithSubjectInfoUseCase: GetNotesWithSubjectInfoUseCase

    @Before
    fun setup() {
        noteRepository = mockk(relaxed = true)
        getNotesWithSubjectInfoUseCase = GetNotesWithSubjectInfoUseCase(noteRepository)
    }

    @Test
    fun `invoke maps subject icon and colorHex to notes correctly`() = runTest {
        val userId = "user123"
        val note1 = Note(id = "n1", title = "Nota 1", subject = "Química")
        val note2 = Note(id = "n2", title = "Nota 2", subject = "Sin Materia")
        val subject = SubjectData(name = "Química", icon = "🧪", colorHex = "#00BCD4")

        every { noteRepository.getNotes(userId) } returns flowOf(listOf(note1, note2))
        every { noteRepository.getSubjects(userId) } returns flowOf(listOf(subject))

        getNotesWithSubjectInfoUseCase(userId).test {
            val list = awaitItem()
            assertEquals(2, list.size)

            val item1 = list[0]
            assertEquals("n1", item1.note.id)
            assertEquals("🧪", item1.subjectIcon)
            assertEquals("#00BCD4", item1.subjectColorHex)

            val item2 = list[1]
            assertEquals("n2", item2.note.id)
            assertEquals("📚", item2.subjectIcon)
            assertEquals("", item2.subjectColorHex)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
