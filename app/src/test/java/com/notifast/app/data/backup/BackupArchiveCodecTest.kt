package com.notifast.app.data.backup

import android.graphics.Bitmap
import com.google.gson.Gson
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.Teacher
import com.notifast.app.domain.util.NoteBlockCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupArchiveCodecTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var optimizer: ImageOptimizer
    private lateinit var codec: BackupArchiveCodec

    @Before
    fun setUp() {
        optimizer = ImageOptimizer()
        codec = BackupArchiveCodec(optimizer)
    }

    private fun createTestImage(name: String, width: Int = 200, height: Int = 200): File {
        val file = tempFolder.newFile(name)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }
        return file
    }

    @Test
    fun subject_round_trip_keeps_ids_counts_and_blocks_and_resolves_image_files() {
        val evImg = createTestImage("ev.jpg")
        val noteImg = createTestImage("note.jpg")

        val blocks = listOf(
            NoteBlock.Paragraph(id = "b1", text = "Leyes de Newton"),
            NoteBlock.Image(
                id = "b2",
                path = evImg.absolutePath,
                caption = "Diagrama de fuerzas"
            )
        )

        val snapshot = SubjectSnapshot(
            subject = SubjectData(
                id = "sub-1",
                name = "Física Clásica",
                icon = "atom",
                colorHex = "#3B9797",
                section = "19"
            ),
            schedules = listOf(
                Schedule(id = "sch-1", subjectId = "sub-1", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
            ),
            evaluations = listOf(
                Evaluation(id = "ev-1", subjectId = "sub-1", title = "Parcial 1", weight = 30.0, imageUrl = evImg.absolutePath)
            ),
            notes = listOf(
                Note(
                    id = "n-1",
                    subjectId = "sub-1",
                    title = "Dinámica",
                    imageUrl = noteImg.absolutePath,
                    blocks = blocks
                )
            ),
            imageFiles = mapOf(
                evImg.absolutePath to evImg,
                noteImg.absolutePath to noteImg
            )
        )

        val zipFile = tempFolder.newFile("subject_backup.nfbak")
        val workDir = tempFolder.newFolder("work_write")
        val writeResult = codec.writeSubject(snapshot, zipFile, workDir)
        assertTrue(writeResult.isSuccess)

        val extractDir = tempFolder.newFolder("extract_sub")
        val readResult = codec.readSubject(zipFile, extractDir)
        assertTrue(readResult.isSuccess)

        val restored = readResult.getOrThrow()
        assertEquals("sub-1", restored.subject.id)
        assertEquals("Física Clásica", restored.subject.name)
        assertEquals("19", restored.subject.section)
        assertEquals(1, restored.schedules.size)
        assertEquals(1, restored.evaluations.size)
        assertEquals(1, restored.notes.size)

        // Check rewritten evaluation image
        val restoredEv = restored.evaluations.first()
        assertTrue(restoredEv.imageUrl?.startsWith("images/") == true)
        val evExtractedFile = restored.imageFiles[restoredEv.imageUrl]
        assertNotNull(evExtractedFile)
        assertTrue(evExtractedFile!!.exists())

        // Check rewritten note image & blocks
        val restoredNote = restored.notes.first()
        assertTrue(restoredNote.imageUrl?.startsWith("images/") == true)
        val noteExtractedFile = restored.imageFiles[restoredNote.imageUrl]
        assertNotNull(noteExtractedFile)
        assertTrue(noteExtractedFile!!.exists())

        val restoredBlocks = restoredNote.blocks
        assertEquals(2, restoredBlocks.size)
        val imageBlock = restoredBlocks[1] as NoteBlock.Image
        assertTrue(imageBlock.path.startsWith("images/"))
        val blockImgFile = restored.imageFiles[imageBlock.path]
        assertNotNull(blockImgFile)
        assertTrue(blockImgFile!!.exists())
    }

    @Test
    fun manifest_with_higher_schema_version_returns_UnsupportedBackupException() {
        val zipFile = tempFolder.newFile("future_backup.nfbak")
        val manifest = BackupManifest(format = "notifast-backup", schemaVersion = 2, kind = "subject")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(Gson().toJson(manifest).toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        val extractDir = tempFolder.newFolder("extract_future")
        val result = codec.readSubject(zipFile, extractDir)
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is UnsupportedBackupException)
        assertEquals(2, (ex as UnsupportedBackupException).foundVersion)
    }

    @Test
    fun zip_without_manifest_returns_failure() {
        val zipFile = tempFolder.newFile("no_manifest.nfbak")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            zos.putNextEntry(ZipEntry("dummy.txt"))
            zos.write("hello".toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        val extractDir = tempFolder.newFolder("extract_no_manifest")
        val result = codec.readSubject(zipFile, extractDir)
        assertTrue(result.isFailure)
    }

    @Test
    fun zip_slip_entry_returns_failure() {
        val zipFile = tempFolder.newFile("zip_slip.nfbak")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            zos.putNextEntry(ZipEntry("../evil.txt"))
            zos.write("evil".toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        val extractDir = tempFolder.newFolder("extract_slip")
        val result = codec.readSubject(zipFile, extractDir)
        assertTrue(result.isFailure)
    }

    @Test
    fun global_round_trip_keeps_prefs_teachers_and_avatar() {
        val avatarFile = createTestImage("avatar.jpg", 100, 100)
        val teachers = listOf(
            Teacher(
                id = "t-1",
                firstName = "Marie",
                lastName = "Curie",
                email = "marie@curie.org",
                subjectNames = listOf("Química")
            )
        )
        val prefs = mapOf("dark_mode" to "true", "academic_system" to "20")

        val snapshot = GlobalSnapshot(
            firstName = "Ada",
            lastName = "Lovelace",
            avatar = avatarFile,
            prefs = prefs,
            teachers = teachers
        )

        val zipFile = tempFolder.newFile("global_backup.nfbak")
        val workDir = tempFolder.newFolder("work_global")
        val writeResult = codec.writeGlobal(snapshot, zipFile, workDir)
        assertTrue(writeResult.isSuccess)

        val extractDir = tempFolder.newFolder("extract_global")
        val readResult = codec.readGlobal(zipFile, extractDir)
        assertTrue(readResult.isSuccess)

        val restored = readResult.getOrThrow()
        assertEquals("Ada", restored.firstName)
        assertEquals("Lovelace", restored.lastName)
        assertEquals("true", restored.prefs["dark_mode"])
        assertEquals("20", restored.prefs["academic_system"])
        assertEquals(1, restored.teachers.size)
        assertEquals("Marie", restored.teachers[0].firstName)
        assertNotNull(restored.avatar)
        assertTrue(restored.avatar!!.exists())
    }

    @Test
    fun legacy_archive_without_section_deserializes_safely_with_empty_section() {
        val zipFile = tempFolder.newFile("legacy_subject.nfbak")
        val manifest = BackupManifest(format = "notifast-backup", schemaVersion = 1, kind = "subject")
        val legacySubjectJson = """
            {
              "schemaVersion": 1,
              "exportTimestamp": 123456789,
              "subject": {
                "id": "legacy-sub-1",
                "name": "Historia Antigua",
                "icon": "School",
                "colorHex": "#3B9797",
                "cycle": "2025-1"
              },
              "schedules": [],
              "evaluations": [],
              "notes": [],
              "teachers": []
            }
        """.trimIndent()

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(Gson().toJson(manifest).toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("subject.json"))
            zos.write(legacySubjectJson.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        val extractDir = tempFolder.newFolder("extract_legacy")
        val result = codec.readSubject(zipFile, extractDir)
        assertTrue(result.isSuccess)

        val restored = result.getOrThrow()
        assertEquals("legacy-sub-1", restored.subject.id)
        assertEquals("Historia Antigua", restored.subject.name)
        assertEquals("", restored.subject.section.orEmpty())
    }
}
