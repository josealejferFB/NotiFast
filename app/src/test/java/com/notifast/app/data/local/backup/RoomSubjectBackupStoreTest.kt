package com.notifast.app.data.local.backup

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Room
import com.notifast.app.data.backup.SubjectSnapshot
import com.notifast.app.data.local.NotiFastDatabase
import com.notifast.app.data.local.entity.EvaluationEntity
import com.notifast.app.data.local.entity.NoteEntity
import com.notifast.app.data.local.entity.ScheduleEntity
import com.notifast.app.data.local.entity.SubjectCloudStateEntity
import com.notifast.app.data.local.entity.SubjectEntity
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.util.NoteBlockCodec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomSubjectBackupStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var db: NotiFastDatabase
    private lateinit var store: RoomSubjectBackupStore

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, NotiFastDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        store = RoomSubjectBackupStore(
            context = context,
            db = db,
            subjectDao = db.subjectDao(),
            scheduleDao = db.scheduleDao(),
            evaluationDao = db.evaluationDao(),
            noteDao = db.noteDao(),
            subjectCloudStateDao = db.subjectCloudStateDao()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun createTestImage(name: String): File {
        val file = tempFolder.newFile(name)
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }
        return file
    }

    @Test
    fun read_insert_round_trip_into_second_user_preserves_subjectId_and_counts() = runTest {
        val subject = SubjectEntity(
            userId = "user1",
            id = "sub-1",
            name = "Álgebra Lineal",
            icon = "math",
            colorHex = "#3B9797",
            cycle = "2026-1",
            createdAt = Date()
        )
        val schedule = ScheduleEntity(
            id = "sch-1",
            userId = "user1",
            subjectId = "sub-1",
            subjectName = "Álgebra Lineal",
            dayOfWeek = 1,
            startTime = "08:00",
            endTime = "10:00"
        )
        val evaluation = EvaluationEntity(
            id = "ev-1",
            userId = "user1",
            subjectId = "sub-1",
            subjectName = "Álgebra Lineal",
            title = "Parcial 1",
            weight = 30.0
        )
        val note = NoteEntity(
            id = "n-1",
            userId = "user1",
            subjectId = "sub-1",
            subject = "Álgebra Lineal",
            title = "Matrices",
            blocksJson = NoteBlockCodec.toJson(listOf(NoteBlock.Paragraph("b1", "Texto"))),
            schemaVersion = 2
        )

        db.subjectDao().insertSubject(subject)
        db.scheduleDao().insertSchedule(schedule)
        db.evaluationDao().insertEvaluation(evaluation)
        db.noteDao().insertNote(note)

        // Read snapshot from user1
        val readResult = store.readSnapshot("user1", "sub-1")
        assertTrue(readResult.isSuccess)
        val snapshot = readResult.getOrThrow()
        assertEquals("sub-1", snapshot.subject.id)
        assertEquals(1, snapshot.schedules.size)
        assertEquals(1, snapshot.evaluations.size)
        assertEquals(1, snapshot.notes.size)

        // Insert into user2
        val insertResult = store.insertSnapshot("user2", snapshot)
        assertTrue(insertResult.isSuccess)
        assertEquals("sub-1", insertResult.getOrThrow())

        // Verify user2 entities
        val user2Subject = db.subjectDao().getSubjectById("user2", "sub-1")
        assertNotNull(user2Subject)
        assertEquals("Álgebra Lineal", user2Subject!!.name)

        val user2Schedules = db.scheduleDao().getSchedulesBySubjectId("user2", "sub-1").first()
        assertEquals(1, user2Schedules.size)
        assertEquals("user2", user2Schedules.first().userId)

        val user2Evals = db.evaluationDao().getEvaluationsBySubjectId("user2", "sub-1").first()
        assertEquals(1, user2Evals.size)
        assertEquals("user2", user2Evals.first().userId)

        val user2Notes = db.noteDao().getNotesBySubjectId("user2", "sub-1").first()
        assertEquals(1, user2Notes.size)
        assertEquals("user2", user2Notes.first().userId)
    }

    @Test
    fun insertSnapshot_with_newSubjectId_and_newName_assigns_new_id_and_name_to_children() = runTest {
        val snapshot = SubjectSnapshot(
            subject = SubjectData(id = "sub-orig", name = "Cálculo"),
            schedules = listOf(
                Schedule(id = "sch-orig", subjectId = "sub-orig", subjectName = "Cálculo", dayOfWeek = 2, startTime = "10:00", endTime = "12:00")
            ),
            evaluations = listOf(
                Evaluation(id = "ev-orig", subjectId = "sub-orig", subjectName = "Cálculo", title = "Quiz 1", weight = 15.0)
            ),
            notes = listOf(
                Note(id = "n-orig", subjectId = "sub-orig", subject = "Cálculo", title = "Límites")
            )
        )

        val result = store.insertSnapshot("user1", snapshot, newSubjectId = "sub-renamed", newName = "Cálculo (1)")
        assertTrue(result.isSuccess)
        assertEquals("sub-renamed", result.getOrThrow())

        val sub = db.subjectDao().getSubjectById("user1", "sub-renamed")
        assertNotNull(sub)
        assertEquals("Cálculo (1)", sub!!.name)

        val schedules = db.scheduleDao().getSchedulesBySubjectId("user1", "sub-renamed").first()
        assertEquals(1, schedules.size)
        assertEquals("Cálculo (1)", schedules.first().subjectName)
        assertNotEquals("sch-orig", schedules.first().id) // new UUID

        val evals = db.evaluationDao().getEvaluationsBySubjectId("user1", "sub-renamed").first()
        assertEquals(1, evals.size)
        assertEquals("Cálculo (1)", evals.first().subjectName)
        assertNotEquals("ev-orig", evals.first().id)

        val notes = db.noteDao().getNotesBySubjectId("user1", "sub-renamed").first()
        assertEquals(1, notes.size)
        assertEquals("Cálculo (1)", notes.first().subject)
        assertNotEquals("n-orig", notes.first().id)
    }

    @Test
    fun replaceSnapshot_removes_old_notes_and_images_and_inserts_new_ones_and_cleans_cloud_state() = runTest {
        val oldImg = createTestImage("old_img.jpg")
        val oldEval = EvaluationEntity(
            id = "ev-old",
            userId = "user1",
            subjectId = "sub-1",
            subjectName = "Química",
            title = "Lab 1",
            imageUrl = oldImg.absolutePath
        )
        val oldSub = SubjectEntity(userId = "user1", id = "sub-1", name = "Química")
        db.subjectDao().insertSubject(oldSub)
        db.evaluationDao().insertEvaluation(oldEval)

        db.subjectCloudStateDao().upsert(
            SubjectCloudStateEntity(
                accountId = "acc-1",
                subjectId = "sub-1",
                driveFileId = "drive-1",
                lastUploadedHash = "old-hash",
                lastSyncedAt = 1000L
            )
        )

        val newImg = createTestImage("new_img.jpg")
        val newSnapshot = SubjectSnapshot(
            subject = SubjectData(id = "sub-1", name = "Química"),
            evaluations = listOf(
                Evaluation(id = "ev-new", subjectId = "sub-1", subjectName = "Química", title = "Lab Nuevo", imageUrl = newImg.absolutePath)
            ),
            imageFiles = mapOf(newImg.absolutePath to newImg)
        )

        val replaceResult = store.replaceSnapshot("user1", "sub-1", newSnapshot)
        assertTrue(replaceResult.isSuccess)

        // Verify old image was deleted from disk
        assertFalse(oldImg.exists())

        // Verify cloud state row for sub-1 was removed
        val cloudStates = db.subjectCloudStateDao().getAll("acc-1")
        assertTrue(cloudStates.isEmpty())

        // Verify new evaluation is present and points to a new copied file
        val evals = db.evaluationDao().getEvaluationsBySubjectId("user1", "sub-1").first()
        assertEquals(1, evals.size)
        assertEquals("ev-new", evals.first().id)
        val newEvalImgUri = evals.first().imageUrl
        assertNotNull(newEvalImgUri)
        val newFile = File(newEvalImgUri!!.removePrefix("file://"))
        assertTrue(newFile.exists())
    }

    @Test
    fun forced_failure_due_to_duplicate_name_rolls_back_rows_and_deletes_copied_files() = runTest {
        // Pre-create subject with name "Física"
        db.subjectDao().insertSubject(SubjectEntity(userId = "user1", id = "sub-phys", name = "Física"))

        val testImg = createTestImage("temp_img.jpg")
        val snapshot = SubjectSnapshot(
            subject = SubjectData(id = "sub-2", name = "Física"),
            imageFiles = mapOf(testImg.absolutePath to testImg),
            evaluations = listOf(
                Evaluation(id = "ev-2", subjectId = "sub-2", title = "P1", imageUrl = testImg.absolutePath)
            )
        )

        val imagesDir = File(context.filesDir, "images")
        val filesBefore = imagesDir.listFiles()?.size ?: 0

        // Attempt to insert with duplicate name "Física" on user1
        val result = store.insertSnapshot("user1", snapshot, newSubjectId = "sub-2", newName = "Física")
        assertTrue(result.isFailure)

        // Verify rollback: no "sub-2" in DB
        val sub = db.subjectDao().getSubjectById("user1", "sub-2")
        assertNull(sub)

        // Verify copied files were cleaned up
        val filesAfter = imagesDir.listFiles()?.size ?: 0
        assertEquals(filesBefore, filesAfter)
    }

    @Test
    fun note_whose_image_ref_has_no_file_is_inserted_with_null_imageUrl_and_dropped_block() = runTest {
        val missingImagePath = "file:///non/existent/image.jpg"
        val snapshot = SubjectSnapshot(
            subject = SubjectData(id = "sub-missing", name = "Biología"),
            notes = listOf(
                Note(
                    id = "n-1",
                    subjectId = "sub-missing",
                    title = "Célula",
                    imageUrl = missingImagePath,
                    blocks = listOf(
                        NoteBlock.Paragraph("b1", "Introducción a la célula"),
                        NoteBlock.Image("b2", path = missingImagePath, caption = "Diagrama faltante")
                    )
                )
            ),
            imageFiles = emptyMap() // No files supplied
        )

        val result = store.insertSnapshot("user1", snapshot)
        assertTrue(result.isSuccess)

        val notes = db.noteDao().getNotesBySubjectId("user1", "sub-missing").first()
        assertEquals(1, notes.size)
        val note = notes.first()
        assertNull(note.imageUrl)

        val blocks = NoteBlockCodec.fromJson(note.blocksJson)
        assertEquals(1, blocks.size)
        assertTrue(blocks.first() is NoteBlock.Paragraph)
    }

    @Test
    fun readSnapshot_includes_schedules_and_evaluations_with_blank_subjectId_matching_subject_name() = runTest {
        val subject = SubjectEntity(
            userId = "user1",
            id = "sub-1",
            name = "Álgebra Lineal",
            icon = "math",
            colorHex = "#3B9797",
            cycle = "2026-1",
            createdAt = Date()
        )
        db.subjectDao().insertSubject(subject)

        val schedule = ScheduleEntity(
            id = "sch-blank",
            userId = "user1",
            subjectId = "",
            subjectName = "Álgebra Lineal",
            dayOfWeek = 1,
            startTime = "08:00",
            endTime = "10:00",
            classroom = "A-101",
            teacher = "Prof. Gauss",
            createdAt = Date()
        )
        db.scheduleDao().insertSchedule(schedule)

        val evaluation = EvaluationEntity(
            id = "eval-blank",
            userId = "user1",
            subjectId = "",
            subjectName = "Álgebra Lineal",
            title = "Examen Parcial",
            description = "Temas 1 al 4",
            date = Date(),
            createdAt = Date()
        )
        db.evaluationDao().insertEvaluation(evaluation)

        val result = store.readSnapshot("user1", "sub-1")
        assertTrue(result.isSuccess)
        val snapshot = result.getOrThrow()

        assertEquals("Should include schedule matching by subjectName", 1, snapshot.schedules.size)
        assertEquals("sch-blank", snapshot.schedules.first().id)
        assertEquals("Should include evaluation matching by subjectName", 1, snapshot.evaluations.size)
        assertEquals("eval-blank", snapshot.evaluations.first().id)
    }
}

