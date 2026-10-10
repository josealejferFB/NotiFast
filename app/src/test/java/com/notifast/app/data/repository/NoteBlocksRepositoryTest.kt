package com.notifast.app.data.repository

import androidx.room.withTransaction
import com.notifast.app.data.local.NotiFastDatabase
import com.notifast.app.data.local.dao.EvaluationDao
import com.notifast.app.data.local.dao.NoteDao
import com.notifast.app.data.local.dao.ScheduleDao
import com.notifast.app.data.local.dao.SubjectDao
import com.notifast.app.data.local.entity.NoteEntity
import com.notifast.app.data.local.entity.SubjectEntity
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.Note
import com.notifast.app.domain.repository.ImageRepository
import com.notifast.app.domain.util.NoteBlockCodec
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class NoteBlocksRepositoryTest {

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
    fun `addNoteWithBlocks saves blocksJson and schemaVersion 2`() = runTest {
        coEvery { subjectDao.getSubjectByName("u1", "Física") } returns SubjectEntity(
            userId = "u1", id = "s1", name = "Física", icon = "⚡", colorHex = "#000", cycle = ""
        )
        coEvery { noteDao.insertNote(any()) } returns 1L

        val blocks = listOf(
            NoteBlock.Heading(id = "h1", text = "Título", level = 1),
            NoteBlock.Paragraph(id = "p1", text = "Contenido")
        )
        val result = repository.addNoteWithBlocks("u1", "Física", "Mi nota", blocks, listOf("tag"))
        assertTrue(result.isSuccess)

        val slotEntity = slot<NoteEntity>()
        coVerify { noteDao.insertNote(capture(slotEntity)) }
        val saved = slotEntity.captured
        assertEquals(2, saved.schemaVersion)
        assertEquals(NoteBlockCodec.toJson(blocks), saved.blocksJson)
        assertTrue(saved.content.contains("Título"))
        assertTrue(saved.content.contains("Contenido"))
    }

    @Test
    fun `legacy note schemaVersion 1 is converted to blocks on read`() = runTest {
        val legacy = NoteEntity(
            id = "n1", userId = "u1", subjectId = "s1", subject = "Física",
            title = "Legacy", content = "# Título\n- item1\n- item2",
            imageUrl = "file:///foto.jpg", tags = emptyList(),
            timestamp = Date(), updatedAt = Date(),
            blocksJson = "[]", schemaVersion = 1
        )
        coEvery { noteDao.getNotes("u1") } returns flowOf(listOf(legacy))

        val notes = repository.getNotes("u1").first()
        assertEquals(1, notes.size)
        val blocks = notes[0].blocks
        // heading + bullet list + image (from imageUrl)
        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is NoteBlock.Heading)
        assertTrue(blocks[1] is NoteBlock.BulletList)
        assertTrue(blocks[2] is NoteBlock.Image)
    }

    @Test
    fun `blocks from schemaVersion 2 are parsed from blocksJson`() = runTest {
        val blocks = listOf(NoteBlock.Paragraph(id = "p1", text = "Moderno"))
        val entity = NoteEntity(
            id = "n2", userId = "u1", subjectId = "s1", subject = "Física",
            title = "Nueva", content = "Moderno", imageUrl = null, tags = emptyList(),
            timestamp = Date(), updatedAt = Date(),
            blocksJson = NoteBlockCodec.toJson(blocks), schemaVersion = 2
        )
        coEvery { noteDao.getNotes("u1") } returns flowOf(listOf(entity))

        val notes = repository.getNotes("u1").first()
        assertEquals(1, notes.size)
        assertEquals(blocks, notes[0].blocks)
    }

    @Test
    fun `updateNote with blocks persists blocksJson and schemaVersion 2`() = runTest {
        val existing = NoteEntity(
            id = "n1", userId = "u1", subjectId = "s1", subject = "Física",
            title = "Vieja", content = "old", imageUrl = null, tags = emptyList(),
            timestamp = Date(), updatedAt = Date(),
            blocksJson = "[]", schemaVersion = 1
        )
        coEvery { noteDao.getNoteById("u1", "n1") } returns existing
        coEvery { noteDao.updateNote(any()) } returns 1

        val newBlocks = listOf(NoteBlock.Paragraph(id = "p1", text = "actualizado"))
        val note = Note(
            id = "n1", userId = "u1", subjectId = "s1", subject = "Física",
            title = "Nueva", content = "actualizado", imageUrl = null,
            tags = emptyList(), timestamp = Date(), updatedAt = Date(),
            blocks = newBlocks
        )
        val result = repository.updateNote("u1", note, imageUri = null)
        assertTrue(result.isSuccess)

        val slotEntity = slot<NoteEntity>()
        coVerify { noteDao.updateNote(capture(slotEntity)) }
        assertEquals(2, slotEntity.captured.schemaVersion)
        assertEquals(NoteBlockCodec.toJson(newBlocks), slotEntity.captured.blocksJson)
    }

    @Test
    fun `updateNote preserves markdown checkboxes in content instead of plain text`() = runTest {
        val existing = NoteEntity(
            id = "n1", userId = "u1", subjectId = "s1", subject = "Física",
            title = "Checks", content = "- [ ] Tarea 1\n- [ ] Tarea 2", imageUrl = null, tags = emptyList(),
            timestamp = Date(), updatedAt = Date(),
            blocksJson = "[]", schemaVersion = 1
        )
        coEvery { noteDao.getNoteById("u1", "n1") } returns existing
        coEvery { noteDao.updateNote(any()) } returns 1

        val markdownWithChecks = "- [x] Tarea 1\n- [ ] Tarea 2"
        val checklistBlocks = listOf(
            NoteBlock.Checklist(
                id = "c1",
                items = listOf(
                    com.notifast.app.data.model.CheckItem(text = "Tarea 1", isChecked = true),
                    com.notifast.app.data.model.CheckItem(text = "Tarea 2", isChecked = false)
                )
            )
        )
        val note = Note(
            id = "n1", userId = "u1", subjectId = "s1", subject = "Física",
            title = "Checks", content = markdownWithChecks, imageUrl = null,
            tags = emptyList(), timestamp = Date(), updatedAt = Date(),
            blocks = checklistBlocks
        )
        val result = repository.updateNote("u1", note, imageUri = null)
        assertTrue(result.isSuccess)

        val slotEntity = slot<NoteEntity>()
        coVerify { noteDao.updateNote(capture(slotEntity)) }
        assertEquals(markdownWithChecks, slotEntity.captured.content)
        assertTrue(slotEntity.captured.content.contains("- [x] Tarea 1"))
        assertTrue(slotEntity.captured.content.contains("- [ ] Tarea 2"))
    }

    @Test
    fun `getNotes self heals schemaVersion 2 notes where content lost checkboxes`() = runTest {
        val checklistBlocks = listOf(
            NoteBlock.Checklist(
                id = "c1",
                items = listOf(
                    com.notifast.app.data.model.CheckItem(text = "Tarea 1", isChecked = true),
                    com.notifast.app.data.model.CheckItem(text = "Tarea 2", isChecked = false)
                )
            )
        )
        // Corrupted entity: plain text content without markdown check markers, but blocksJson has checklist
        val corrupted = NoteEntity(
            id = "n1", userId = "u1", subjectId = "s1", subject = "Física",
            title = "Checks", content = "Tarea 1\nTarea 2", imageUrl = null, tags = emptyList(),
            timestamp = Date(), updatedAt = Date(),
            blocksJson = NoteBlockCodec.toJson(checklistBlocks), schemaVersion = 2
        )
        coEvery { noteDao.getNotes("u1") } returns flowOf(listOf(corrupted))

        val notes = repository.getNotes("u1").first()
        assertEquals(1, notes.size)
        assertTrue(notes[0].content.contains("- [x] Tarea 1"))
        assertTrue(notes[0].content.contains("- [ ] Tarea 2"))
    }
}
