package com.notifast.app.data.repository

import android.net.Uri
import androidx.room.withTransaction
import com.notifast.app.data.local.NotiFastDatabase
import com.notifast.app.data.local.dao.EvaluationDao
import com.notifast.app.data.local.dao.NoteDao
import com.notifast.app.data.local.dao.ScheduleDao
import com.notifast.app.data.local.dao.SubjectDao
import com.notifast.app.data.local.entity.EvaluationEntity
import com.notifast.app.data.local.entity.NoteEntity
import com.notifast.app.data.local.entity.SubjectEntity
import com.notifast.app.data.model.Note
import com.notifast.app.domain.repository.ImageRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class LocalNoteRepositoryImplTest {

    private val db: NotiFastDatabase = mockk(relaxed = true)
    private val noteDao: NoteDao = mockk(relaxed = true)
    private val subjectDao: SubjectDao = mockk(relaxed = true)
    private val evaluationDao: EvaluationDao = mockk(relaxed = true)
    private val scheduleDao: ScheduleDao = mockk(relaxed = true)
    private val imageRepository: ImageRepository = mockk(relaxed = true)

    private lateinit var repository: LocalNoteRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionSlot = slot<suspend () -> Any?>()
        coEvery { db.withTransaction(capture(transactionSlot)) } coAnswers {
            transactionSlot.captured.invoke()
        }

        repository = LocalNoteRepositoryImpl(
            db = db,
            noteDao = noteDao,
            subjectDao = subjectDao,
            imageRepository = imageRepository,
            evaluationDao = evaluationDao,
            scheduleDao = scheduleDao
        )
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `addSubject succeeds when subject does not exist`() = runTest {
        coEvery { subjectDao.getSubjectByName("u1", "Física") } returns null
        coEvery { subjectDao.insertSubject(any()) } returns 1L

        val result = repository.addSubject("u1", "Física", "⚡", "#3B9797")
        assertTrue(result.isSuccess)
        coVerify { subjectDao.insertSubject(match { it.name == "Física" && it.userId == "u1" }) }
    }

    @Test
    fun `addSubject fails when subject already exists`() = runTest {
        coEvery { subjectDao.getSubjectByName("u1", "Física") } returns SubjectEntity(
            userId = "u1",
            name = "Física",
            icon = "⚡",
            colorHex = "#3B9797",
            createdAt = Date()
        )

        val result = repository.addSubject("u1", "Física", "⚡", "#3B9797")
        assertTrue(result.isFailure)
        assertEquals("La materia ya existe", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getSubjects maps entities to SubjectData`() = runTest {
        val entities = listOf(
            SubjectEntity(userId = "u1", name = "Matemáticas", icon = "📐", colorHex = "#FF0000", createdAt = Date())
        )
        coEvery { subjectDao.getSubjects("u1") } returns flowOf(entities)

        val subjects = repository.getSubjects("u1").first()
        assertEquals(1, subjects.size)
        assertEquals("Matemáticas", subjects[0].name)
        assertEquals("📐", subjects[0].icon)
    }

    @Test
    fun `getNotes maps entities to domain Note`() = runTest {
        val entities = listOf(
            NoteEntity(id = "n1", userId = "u1", subject = "Matemáticas", title = "Tema 1", content = "Texto")
        )
        coEvery { noteDao.getNotes("u1") } returns flowOf(entities)

        val notes = repository.getNotes("u1").first()
        assertEquals(1, notes.size)
        assertEquals("n1", notes[0].id)
        assertEquals("Tema 1", notes[0].title)
    }

    @Test
    fun `addNote uploads image if provided and inserts into dao`() = runTest {
        val mockUri = mockk<Uri>()
        coEvery { imageRepository.uploadNoteImage("u1", mockUri) } returns Result.success("file:///img1.jpg")
        coEvery { noteDao.insertNote(any()) } returns 1L

        val result = repository.addNote("u1", "Matemáticas", "Nota 1", "Contenido", mockUri)
        assertTrue(result.isSuccess)
        coVerify {
            noteDao.insertNote(match {
                it.userId == "u1" && it.subject == "Matemáticas" && it.imageUrl == "file:///img1.jpg" && it.id.isNotBlank()
            })
        }
    }

    @Test
    fun `deleteNote deletes image if present and deletes note from dao`() = runTest {
        val note = Note(id = "n1", userId = "u1", subject = "Matemáticas", imageUrl = "file:///img1.jpg")
        coEvery { imageRepository.deleteImage("file:///img1.jpg") } returns Result.success(Unit)
        coEvery { noteDao.deleteNoteById("u1", "n1") } returns 1

        val result = repository.deleteNote("u1", note)
        assertTrue(result.isSuccess)
        coVerify { imageRepository.deleteImage("file:///img1.jpg") }
        coVerify { noteDao.deleteNoteById("u1", "n1") }
    }

    @Test
    fun `deleteSubject cascades in transaction and deletes orphan images`() = runTest {
        val notes = listOf(NoteEntity(id = "n1", userId = "u1", subject = "Física", imageUrl = "file:///note.jpg"))
        val evals = listOf(EvaluationEntity(id = "e1", userId = "u1", subjectName = "Física", imageUrl = "file:///eval.jpg"))

        coEvery { noteDao.getNotesBySubject("u1", "Física") } returns flowOf(notes)
        coEvery { evaluationDao.getEvaluationsBySubject("u1", "Física") } returns flowOf(evals)
        coEvery { noteDao.deleteNotesBySubject("u1", "Física") } returns 1
        coEvery { evaluationDao.deleteEvaluationsBySubject("u1", "Física") } returns 1
        coEvery { scheduleDao.deleteSchedulesBySubject("u1", "Física") } returns 1
        coEvery { subjectDao.deleteSubject("u1", "Física") } returns 1
        coEvery { imageRepository.deleteImage(any()) } returns Result.success(Unit)

        val result = repository.deleteSubject("u1", "Física")
        assertTrue(result.isSuccess)

        coVerify { noteDao.deleteNotesBySubject("u1", "Física") }
        coVerify { evaluationDao.deleteEvaluationsBySubject("u1", "Física") }
        coVerify { scheduleDao.deleteSchedulesBySubject("u1", "Física") }
        coVerify { subjectDao.deleteSubject("u1", "Física") }
        coVerify { imageRepository.deleteImage("file:///note.jpg") }
        coVerify { imageRepository.deleteImage("file:///eval.jpg") }
    }

    @Test
    fun `getNoteById returns Note when found`() = runTest {
        val entity = NoteEntity(id = "n1", userId = "u1", subject = "Física", title = "Title")
        coEvery { noteDao.getNoteById("u1", "n1") } returns entity

        val result = repository.getNoteById("u1", "Física", "n1")
        assertTrue(result.isSuccess)
        assertEquals("Title", result.getOrNull()?.title)
    }

    @Test
    fun `getNoteById returns failure when not found`() = runTest {
        coEvery { noteDao.getNoteById("u1", "unknown") } returns null

        val result = repository.getNoteById("u1", "Física", "unknown")
        assertTrue(result.isFailure)
        assertEquals("Note not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateNote deletes old image if new image is provided`() = runTest {
        val existingEntity = NoteEntity(id = "n1", userId = "u1", subject = "Física", imageUrl = "file:///old.jpg")
        val updatedNote = Note(id = "n1", userId = "u1", subject = "Física", title = "New Title")
        val mockUri = mockk<Uri>()

        coEvery { noteDao.getNoteById("u1", "n1") } returns existingEntity
        coEvery { imageRepository.uploadNoteImage("u1", mockUri) } returns Result.success("file:///new.jpg")
        coEvery { imageRepository.deleteImage("file:///old.jpg") } returns Result.success(Unit)
        coEvery { noteDao.updateNote(any()) } returns 1

        val result = repository.updateNote("u1", updatedNote, mockUri)
        assertTrue(result.isSuccess)
        coVerify { imageRepository.deleteImage("file:///old.jpg") }
        coVerify {
            noteDao.updateNote(match {
                it.id == "n1" && it.imageUrl == "file:///new.jpg" && it.title == "New Title"
            })
        }
    }

    @Test
    fun `getNotesBySubject maps entities to domain Note`() = runTest {
        val entities = listOf(
            NoteEntity(id = "n1", userId = "u1", subject = "Física", title = "T1")
        )
        coEvery { noteDao.getNotesBySubject("u1", "Física") } returns flowOf(entities)

        val notes = repository.getNotesBySubject("u1", "Física").first()
        assertEquals(1, notes.size)
        assertEquals("T1", notes[0].title)
    }
}
