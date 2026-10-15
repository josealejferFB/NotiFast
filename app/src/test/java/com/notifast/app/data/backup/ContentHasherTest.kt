package com.notifast.app.data.backup

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Date

class ContentHasherTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var hasher: ContentHasher

    @Before
    fun setUp() {
        hasher = ContentHasher()
    }

    @Test
    fun same_snapshot_with_different_list_orders_produces_equal_hash() {
        val s1 = Schedule(id = "s1", subjectName = "Cálculo", dayOfWeek = 1, startTime = "08:00", endTime = "10:00")
        val s2 = Schedule(id = "s2", subjectName = "Cálculo", dayOfWeek = 3, startTime = "10:00", endTime = "12:00")
        val e1 = Evaluation(id = "e1", subjectName = "Cálculo", title = "Parcial 1", weight = 30.0)
        val e2 = Evaluation(id = "e2", subjectName = "Cálculo", title = "Parcial 2", weight = 30.0)
        val n1 = Note(id = "n1", subject = "Cálculo", title = "Derivadas", content = "Reglas básicas")
        val n2 = Note(id = "n2", subject = "Cálculo", title = "Integrales", content = "Integración por partes")

        val snapA = SubjectSnapshot(
            subject = SubjectData(id = "sub-1", name = "Cálculo", colorHex = "#BF092F", icon = "📐"),
            schedules = listOf(s1, s2),
            evaluations = listOf(e1, e2),
            notes = listOf(n1, n2),
            imageFiles = emptyMap()
        )

        val snapB = SubjectSnapshot(
            subject = SubjectData(id = "sub-1", name = "Cálculo", colorHex = "#BF092F", icon = "📐"),
            schedules = listOf(s2, s1),
            evaluations = listOf(e2, e1),
            notes = listOf(n2, n1),
            imageFiles = emptyMap()
        )

        assertEquals(hasher.hash(snapA), hasher.hash(snapB))
    }

    @Test
    fun changing_userId_or_updatedAt_produces_equal_hash() {
        val n1 = Note(id = "n1", userId = "u1", subject = "Cálculo", title = "Derivadas", content = "Reglas", updatedAt = Date(1000L))
        val n2 = Note(id = "n1", userId = "u2", subject = "Cálculo", title = "Derivadas", content = "Reglas", updatedAt = Date(9999999L))

        val snapA = SubjectSnapshot(
            subject = SubjectData(id = "sub-1", name = "Cálculo"),
            schedules = emptyList(),
            evaluations = emptyList(),
            notes = listOf(n1),
            imageFiles = emptyMap()
        )

        val snapB = SubjectSnapshot(
            subject = SubjectData(id = "sub-1", name = "Cálculo"),
            schedules = emptyList(),
            evaluations = emptyList(),
            notes = listOf(n2),
            imageFiles = emptyMap()
        )

        assertEquals(hasher.hash(snapA), hasher.hash(snapB))
    }

    @Test
    fun changing_content_produces_different_hash() {
        val snapA = SubjectSnapshot(
            subject = SubjectData(id = "sub-1", name = "Cálculo"),
            schedules = emptyList(),
            evaluations = emptyList(),
            notes = listOf(Note(id = "n1", subject = "Cálculo", title = "Título Original")),
            imageFiles = emptyMap()
        )

        val snapB = SubjectSnapshot(
            subject = SubjectData(id = "sub-1", name = "Cálculo"),
            schedules = emptyList(),
            evaluations = emptyList(),
            notes = listOf(Note(id = "n1", subject = "Cálculo", title = "Título Modificado")),
            imageFiles = emptyMap()
        )

        assertNotEquals(hasher.hash(snapA), hasher.hash(snapB))
    }

    @Test
    fun same_image_bytes_at_different_paths_produces_equal_hash_and_different_bytes_differs() {
        val file1 = tempFolder.newFile("img1.jpg").apply { writeBytes(byteArrayOf(1, 2, 3, 4, 5)) }
        val file2 = tempFolder.newFile("img2.jpg").apply { writeBytes(byteArrayOf(1, 2, 3, 4, 5)) }
        val fileDiff = tempFolder.newFile("img3.jpg").apply { writeBytes(byteArrayOf(9, 9, 9)) }

        val snapA = SubjectSnapshot(
            subject = SubjectData(id = "sub-1", name = "Cálculo"),
            schedules = emptyList(),
            evaluations = emptyList(),
            notes = listOf(Note(id = "n1", subject = "Cálculo", imageUrl = file1.absolutePath)),
            imageFiles = mapOf(file1.absolutePath to file1)
        )

        val snapB = SubjectSnapshot(
            subject = SubjectData(id = "sub-1", name = "Cálculo"),
            schedules = emptyList(),
            evaluations = emptyList(),
            notes = listOf(Note(id = "n1", subject = "Cálculo", imageUrl = file2.absolutePath)),
            imageFiles = mapOf(file2.absolutePath to file2)
        )

        val snapC = SubjectSnapshot(
            subject = SubjectData(id = "sub-1", name = "Cálculo"),
            schedules = emptyList(),
            evaluations = emptyList(),
            notes = listOf(Note(id = "n1", subject = "Cálculo", imageUrl = fileDiff.absolutePath)),
            imageFiles = mapOf(fileDiff.absolutePath to fileDiff)
        )

        assertEquals(hasher.hash(snapA), hasher.hash(snapB))
        assertNotEquals(hasher.hash(snapA), hasher.hash(snapC))
    }

    @Test
    fun missing_image_file_does_not_throw_and_hashes_deterministically() {
        val missingPath = "/path/that/does/not/exist.jpg"
        val snap = SubjectSnapshot(
            subject = SubjectData(id = "sub-1", name = "Cálculo"),
            schedules = emptyList(),
            evaluations = emptyList(),
            notes = listOf(Note(id = "n1", subject = "Cálculo", imageUrl = missingPath)),
            imageFiles = emptyMap()
        )

        val hash = hasher.hash(snap)
        org.junit.Assert.assertNotNull(hash)
        assertEquals(64, hash.length) // SHA-256 hex length
    }
}
