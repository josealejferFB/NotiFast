package com.notifast.app.data.backup

import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.Teacher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SubjectSnapshotTeachersTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var codec: BackupArchiveCodec
    private lateinit var hasher: ContentHasher

    @Before
    fun setUp() {
        codec = BackupArchiveCodec(ImageOptimizer())
        hasher = ContentHasher()
    }

    @Test
    fun subject_round_trip_preserves_teachers() {
        val workDir = tempFolder.newFolder("work")
        val extractDir = tempFolder.newFolder("extract")
        val outFile = File(tempFolder.newFolder("out"), "subject.nfbak")

        val teacher1 = Teacher(
            id = "t1",
            userId = "u1",
            firstName = "Alan",
            lastName = "Turing",
            phoneNumber = "+584120000000",
            email = "alan@turing.org",
            subjectNames = listOf("Cálculo I", "Algoritmos"),
            notes = "Cubículo 4B"
        )
        val teacher2 = Teacher(
            id = "t2",
            userId = "u1",
            firstName = "Ada",
            lastName = "Lovelace",
            phoneNumber = "+584141111111",
            email = "ada@lovelace.org",
            subjectNames = listOf("Cálculo I"),
            notes = "Coordinadora"
        )

        val originalSnapshot = SubjectSnapshot(
            subject = SubjectData(id = "sub-101", name = "Cálculo I", colorHex = "#BF092F", icon = "📐"),
            teachers = listOf(teacher1, teacher2)
        )

        val writeResult = codec.writeSubject(originalSnapshot, outFile, workDir)
        assertTrue("writeSubject failed: ${writeResult.exceptionOrNull()}", writeResult.isSuccess)

        val readResult = codec.readSubject(outFile, extractDir)
        assertTrue("readSubject failed: ${readResult.exceptionOrNull()}", readResult.isSuccess)

        val restoredSnapshot = readResult.getOrThrow()
        assertEquals(2, restoredSnapshot.teachers.size)
        assertEquals("Alan", restoredSnapshot.teachers[0].firstName)
        assertEquals("Turing", restoredSnapshot.teachers[0].lastName)
        assertEquals("Ada", restoredSnapshot.teachers[1].firstName)
        assertEquals("Lovelace", restoredSnapshot.teachers[1].lastName)
    }

    @Test
    fun hashing_subject_with_teachers_is_deterministic_and_sensitive_to_teacher_changes() {
        val teacherA = Teacher(id = "t1", firstName = "Carl", lastName = "Gauss", email = "gauss@math.org")
        val teacherB = Teacher(id = "t2", firstName = "Leonhard", lastName = "Euler", email = "euler@math.org")

        val snap1 = SubjectSnapshot(
            subject = SubjectData(id = "sub-1", name = "Álgebra"),
            teachers = listOf(teacherA, teacherB)
        )
        val snapOrderSwapped = SubjectSnapshot(
            subject = SubjectData(id = "sub-1", name = "Álgebra"),
            teachers = listOf(teacherB, teacherA)
        )
        val snapModified = SubjectSnapshot(
            subject = SubjectData(id = "sub-1", name = "Álgebra"),
            teachers = listOf(teacherA.copy(phoneNumber = "123456"))
        )

        val hash1 = hasher.hash(snap1)
        val hashSwapped = hasher.hash(snapOrderSwapped)
        val hashModified = hasher.hash(snapModified)

        assertEquals("Hash must be order independent for teachers sorted by id", hash1, hashSwapped)
        assertNotEquals("Hash must change when teacher content changes", hash1, hashModified)
    }
}
