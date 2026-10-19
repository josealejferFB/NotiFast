package com.notifast.app.data.local.backup

import android.content.Context
import androidx.room.Room
import com.notifast.app.data.backup.SubjectSnapshot
import com.notifast.app.data.local.NotiFastDatabase
import com.notifast.app.data.local.entity.SubjectEntity
import com.notifast.app.data.local.entity.TeacherEntity
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.Teacher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomSubjectBackupStoreTeachersTest {

    private lateinit var context: Context
    private lateinit var db: NotiFastDatabase
    private lateinit var store: RoomSubjectBackupStore

    private val userId = "user-1"
    private val subjectId = "sub-calculo"
    private val subjectName = "Cálculo I"

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
            subjectCloudStateDao = db.subjectCloudStateDao(),
            teacherDao = db.teacherDao()
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun readSnapshot_includesTeachersAssociatedWithSubject() = runTest {
        // Setup subject
        db.subjectDao().insertSubject(
            SubjectEntity(userId = userId, id = subjectId, name = subjectName, icon = "📐", colorHex = "#BF092F")
        )

        // Teacher 1 teaches Cálculo I
        db.teacherDao().insertTeacher(
            TeacherEntity(
                userId = userId,
                id = "t1",
                firstName = "Euler",
                lastName = "Leonhard",
                subjectNamesJson = "[\"Cálculo I\",\"Álgebra\"]"
            )
        )

        // Teacher 2 teaches only Química (should not be in Cálculo I snapshot)
        db.teacherDao().insertTeacher(
            TeacherEntity(
                userId = userId,
                id = "t2",
                firstName = "Curie",
                lastName = "Marie",
                subjectNamesJson = "[\"Química\"]"
            )
        )

        val snapshotResult = store.readSnapshot(userId, subjectId)
        assertTrue(snapshotResult.isSuccess)
        val snapshot = snapshotResult.getOrThrow()

        assertEquals(1, snapshot.teachers.size)
        assertEquals("Euler", snapshot.teachers[0].firstName)
        assertEquals("Leonhard", snapshot.teachers[0].lastName)
    }

    @Test
    fun insertSnapshot_mergesTeachersWithoutDuplication() = runTest {
        val targetUser = "user-2"

        // Existing teacher on target user
        db.teacherDao().insertTeacher(
            TeacherEntity(
                userId = targetUser,
                id = "local-t1",
                firstName = "Euler",
                lastName = "Leonhard",
                phoneNumber = "+584120000000",
                subjectNamesJson = "[\"Física\"]"
            )
        )

        val incomingTeacher = Teacher(
            id = "remote-t1",
            userId = "user-1",
            firstName = "Euler",
            lastName = "Leonhard",
            phoneNumber = "+584120000000",
            subjectNames = listOf("Cálculo I")
        )

        val newTeacher = Teacher(
            id = "remote-t2",
            userId = "user-1",
            firstName = "Newton",
            lastName = "Isaac",
            phoneNumber = "+584149999999",
            subjectNames = listOf("Cálculo I")
        )

        val snapshot = SubjectSnapshot(
            subject = SubjectData(id = "sub-new", name = "Cálculo I"),
            teachers = listOf(incomingTeacher, newTeacher)
        )

        val insertResult = store.insertSnapshot(targetUser, snapshot, null, null)
        assertTrue(insertResult.isSuccess)

        val teachers = db.teacherDao().getTeachers(targetUser).first()
        assertEquals(2, teachers.size)

        val mergedEuler = teachers.first { it.firstName == "Euler" }
        assertTrue(mergedEuler.subjectNamesJson.contains("Física"))
        assertTrue(mergedEuler.subjectNamesJson.contains("Cálculo I"))

        val createdNewton = teachers.first { it.firstName == "Newton" }
        assertEquals(targetUser, createdNewton.userId)
        assertTrue(createdNewton.subjectNamesJson.contains("Cálculo I"))
    }
}
