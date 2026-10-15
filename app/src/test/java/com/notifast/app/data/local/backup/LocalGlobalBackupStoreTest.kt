package com.notifast.app.data.local.backup

import android.net.Uri
import com.notifast.app.data.backup.GlobalSnapshot
import com.notifast.app.data.model.Teacher
import com.notifast.app.data.model.User
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.TeacherRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocalGlobalBackupStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var authRepository: AuthRepository
    private lateinit var userPrefsRepository: UserPrefsRepository
    private lateinit var teacherRepository: TeacherRepository
    private lateinit var store: LocalGlobalBackupStore

    @Before
    fun setUp() {
        authRepository = mockk(relaxed = true)
        userPrefsRepository = mockk(relaxed = true)
        teacherRepository = mockk(relaxed = true)

        store = LocalGlobalBackupStore(
            authRepository = authRepository,
            userPrefsRepository = userPrefsRepository,
            teacherRepository = teacherRepository
        )
    }

    @Test
    fun prefs_round_trip() = runTest {
        val testPrefs = mapOf(
            "theme_accent" to "ruby",
            "haptics_enabled" to "false"
        )
        coEvery { userPrefsRepository.exportAll() } returns testPrefs
        coEvery { userPrefsRepository.importAll(testPrefs) } returns Result.success(Unit)

        val user = User(
            uid = "u1",
            firstName = "John",
            lastName = "Doe",
            profilePictureUrl = null
        )
        coEvery { authRepository.getUserProfile("u1") } returns Result.success(user)
        coEvery { teacherRepository.getTeachers("u1") } returns flowOf(emptyList())

        // Read
        val readResult = store.read("u1")
        assertTrue(readResult.isSuccess)
        val snapshot = readResult.getOrThrow()
        assertEquals("ruby", snapshot.prefs["theme_accent"])
        assertEquals("false", snapshot.prefs["haptics_enabled"])

        // Apply
        val applyResult = store.apply("u1", snapshot)
        assertTrue(applyResult.isSuccess)
        coVerify { userPrefsRepository.importAll(testPrefs) }
    }

    @Test
    fun teacher_merge_one_new_one_matching_merges_subjects_without_duplication() = runTest {
        val localTeacher = Teacher(
            id = "t-local",
            userId = "u1",
            firstName = "Juan",
            lastName = "Pérez",
            subjectNames = listOf("Física", "Química")
        )
        coEvery { teacherRepository.getTeachers("u1") } returns flowOf(listOf(localTeacher))

        val savedTeachers = mutableListOf<Teacher>()
        coEvery { teacherRepository.saveTeacher(capture(savedTeachers)) } returns Result.success(Unit)
        coEvery { userPrefsRepository.importAll(any()) } returns Result.success(Unit)
        coEvery { authRepository.updateProfile(any(), any(), any(), any()) } returns Result.success(Unit)

        val remoteMatching = Teacher(
            id = "t-remote-match",
            userId = "other-user",
            firstName = "  juan  ",
            lastName = "  PÉREZ  ",
            subjectNames = listOf("química", "Biología")
        )
        val remoteNew = Teacher(
            id = "t-remote-new",
            userId = "other-user",
            firstName = "Albert",
            lastName = "Einstein",
            subjectNames = listOf("Relatividad")
        )

        val snapshot = GlobalSnapshot(
            firstName = "Juan",
            lastName = "Perez",
            teachers = listOf(remoteMatching, remoteNew)
        )

        val result = store.apply("u1", snapshot)
        assertTrue(result.isSuccess)

        // 2 calls to saveTeacher: 1 for merged local, 1 for new remote
        assertEquals(2, savedTeachers.size)

        // Find merged teacher
        val merged = savedTeachers.find { it.firstName == "Juan" && it.lastName == "Pérez" }
        assertNotNull(merged)
        assertEquals("t-local", merged!!.id)
        assertEquals("u1", merged.userId)
        // Subjects: Física, Química, Biología (deduped ignoring case)
        assertEquals(listOf("Física", "Química", "Biología"), merged.subjectNames)

        // Find new teacher
        val newTeacher = savedTeachers.find { it.firstName == "Albert" && it.lastName == "Einstein" }
        assertNotNull(newTeacher)
        assertEquals("u1", newTeacher!!.userId)
        assertTrue(newTeacher.id.isNotBlank())
        assertEquals(listOf("Relatividad"), newTeacher.subjectNames)
    }

    @Test
    fun profile_name_is_updated_and_userId_is_unchanged() = runTest {
        val avatarFile = tempFolder.newFile("avatar.jpg").apply { writeText("fake-image") }
        val snapshot = GlobalSnapshot(
            firstName = "Grace",
            lastName = "Hopper",
            avatar = avatarFile
        )

        val uidSlot = slot<String>()
        val firstSlot = slot<String>()
        val lastSlot = slot<String>()
        val uriSlot = slot<Uri?>()

        coEvery {
            authRepository.updateProfile(
                capture(uidSlot),
                capture(firstSlot),
                capture(lastSlot),
                captureNullable(uriSlot)
            )
        } returns Result.success(Unit)
        coEvery { userPrefsRepository.importAll(any()) } returns Result.success(Unit)
        coEvery { teacherRepository.getTeachers("u1") } returns flowOf(emptyList())

        val result = store.apply("u1", snapshot)
        assertTrue(result.isSuccess)

        assertEquals("u1", uidSlot.captured)
        assertEquals("Grace", firstSlot.captured)
        assertEquals("Hopper", lastSlot.captured)
        assertNotNull(uriSlot.captured)
    }
}
