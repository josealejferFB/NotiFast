package com.notifast.app.data.repository

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.notifast.app.data.local.dao.UserDao
import com.notifast.app.data.local.entity.UserEntity
import com.notifast.app.domain.repository.ImageRepository
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocalAuthRepositoryImplTest {

    private val userDao: UserDao = mockk(relaxed = true)
    private val dataStore: DataStore<Preferences> = mockk(relaxed = true)
    private val imageRepository: ImageRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: LocalAuthRepositoryImpl

    private val keyActiveUserId = stringPreferencesKey("active_local_user_id")

    @Before
    fun setUp() {
        coEvery { dataStore.updateData(any()) } coAnswers {
            val transform = firstArg<suspend (Preferences) -> Preferences>()
            val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
            transform(mutablePrefs)
        }
        every { dataStore.data } returns flowOf(emptyPreferences())

        repository = LocalAuthRepositoryImpl(
            userDao = userDao,
            dataStore = dataStore,
            imageRepository = imageRepository,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `getCurrentUserId returns active id from dataStore and memory cache`() = runTest(testDispatcher) {
        val prefs = mockk<Preferences>()
        every { prefs[keyActiveUserId] } returns "user-123"
        every { dataStore.data } returns flowOf(prefs)

        clearMocks(dataStore, answers = false)

        val uidFirst = repository.getCurrentUserId()
        assertEquals("user-123", uidFirst)

        val uidSecond = repository.getCurrentUserId()
        assertEquals("user-123", uidSecond)

        verify(exactly = 1) { dataStore.data }
    }

    @Test
    fun `getCurrentUserId returns null when dataStore is empty`() = runTest(testDispatcher) {
        every { dataStore.data } returns flowOf(emptyPreferences())

        val uid = repository.getCurrentUserId()
        assertNull(uid)
    }

    @Test
    fun `createLocalProfile stores user in Room and updates dataStore`() = runTest(testDispatcher) {
        coEvery { userDao.insertUser(any()) } returns 1L

        val result = repository.createLocalProfile("Carlos", "Gómez", null)
        assertTrue(result.isSuccess)
        val uid = result.getOrNull()!!
        assertTrue(uid.isNotBlank())

        coVerify {
            userDao.insertUser(
                match {
                    it.uid == uid &&
                        it.firstName == "Carlos" &&
                        it.lastName == "Gómez" &&
                        it.email == "$uid@local.notifast" &&
                        it.profilePictureUrl == null
                }
            )
        }
        coVerify { dataStore.updateData(any()) }
        assertEquals(uid, repository.getCurrentUserId())
        assertEquals(true, repository.authState.value)
    }

    @Test
    fun `createLocalProfile uploads image when profileImageUri is provided`() = runTest(testDispatcher) {
        val mockUri = mockk<Uri>()
        coEvery { imageRepository.uploadProfileImage(any(), mockUri) } returns Result.success("file://avatar.jpg")
        coEvery { userDao.insertUser(any()) } returns 1L

        val result = repository.createLocalProfile("Ana", "Pérez", mockUri)
        assertTrue(result.isSuccess)
        val uid = result.getOrNull()!!

        coVerify { imageRepository.uploadProfileImage(uid, mockUri) }
        coVerify { userDao.insertUser(match { it.profilePictureUrl == "file://avatar.jpg" }) }
    }

    @Test
    fun `getUserProfile returns domain user when entity exists`() = runTest(testDispatcher) {
        val entity = UserEntity(
            uid = "u1",
            email = "u1@local.notifast",
            firstName = "Lucía",
            lastName = "Ramos",
            profilePictureUrl = "file://pic.jpg"
        )
        coEvery { userDao.getUserById("u1") } returns entity

        val result = repository.getUserProfile("u1")
        assertTrue(result.isSuccess)
        val user = result.getOrNull()!!
        assertEquals("u1", user.uid)
        assertEquals("Lucía", user.firstName)
        assertEquals("Ramos", user.lastName)
        assertEquals("file://pic.jpg", user.profilePictureUrl)
    }

    @Test
    fun `getUserProfile returns failure when entity not found`() = runTest(testDispatcher) {
        coEvery { userDao.getUserById("missing") } returns null

        val result = repository.getUserProfile("missing")
        assertTrue(result.isFailure)
        assertEquals("User not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateProfile updates user entity in Room and cleans up old image`() = runTest(testDispatcher) {
        val oldEntity = UserEntity(
            uid = "u1",
            email = "u1@local.notifast",
            firstName = "OldFirst",
            lastName = "OldLast",
            profilePictureUrl = "file://old_image.jpg"
        )
        coEvery { userDao.getUserById("u1") } returns oldEntity
        val newUri = mockk<Uri>()
        coEvery { imageRepository.uploadProfileImage("u1", newUri) } returns Result.success("file://new_image.jpg")
        coEvery { imageRepository.deleteImage("file://old_image.jpg") } returns Result.success(Unit)
        coEvery { userDao.updateUser(any()) } returns 1

        val result = repository.updateProfile("u1", "NewFirst", "NewLast", newUri)
        assertTrue(result.isSuccess)

        coVerify { imageRepository.deleteImage("file://old_image.jpg") }
        coVerify {
            userDao.updateUser(
                match {
                    it.uid == "u1" &&
                        it.firstName == "NewFirst" &&
                        it.lastName == "NewLast" &&
                        it.profilePictureUrl == "file://new_image.jpg"
                }
            )
        }
    }

    @Test
    fun `updateProfile returns failure when user not found`() = runTest(testDispatcher) {
        coEvery { userDao.getUserById("missing") } returns null

        val result = repository.updateProfile("missing", "First", "Last", null)
        assertTrue(result.isFailure)
        assertEquals("User not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `logout clears dataStore key and emits false on authState`() = runTest(testDispatcher) {
        repository.logout()

        coVerify { dataStore.updateData(any()) }
        assertEquals(false, repository.authState.value)
        assertNull(repository.getCurrentUserId())
    }

    @Test
    fun `createLocalProfile reuses existing user after logout`() = runTest(testDispatcher) {
        val existing = UserEntity(uid = "user-a", firstName = "Lucía", lastName = "Vieja")
        coEvery { userDao.getAllUsers() } returns listOf(existing)
        coEvery { userDao.updateUser(any()) } returns 1
        val prefs = mockk<Preferences>(relaxed = true)
        every { dataStore.data } returns flowOf(prefs)
        coEvery { dataStore.updateData(any()) } returns prefs

        val result = repository.createLocalProfile("Ana", "Nueva", null)

        assertEquals("user-a", result.getOrNull())
        coVerify(exactly = 0) { userDao.insertUser(any()) }
        coVerify { userDao.updateUser(match { it.uid == "user-a" && it.firstName == "Ana" }) }
    }

    @Test
    fun `createFreshLocalProfile wipes then creates`() = runTest(testDispatcher) {
        val existing = UserEntity(uid = "user-a")
        coEvery { userDao.getAllUsers() } returns listOf(existing) andThen emptyList()
        coEvery { userDao.deleteUser("user-a") } returns 1
        coEvery { userDao.insertUser(any()) } returns 1L
        val prefs = mockk<Preferences>(relaxed = true)
        every { dataStore.data } returns flowOf(prefs)
        coEvery { dataStore.updateData(any()) } returns prefs

        val result = repository.createFreshLocalProfile("Ana", "Nueva", null)

        assertTrue(result.isSuccess)
        coVerify { userDao.deleteUser("user-a") }
        coVerify { userDao.insertUser(any()) }
    }

    @Test
    fun `initializeAuthState emits true when active user exists in Room`() = runTest(testDispatcher) {
        val prefs = mockk<Preferences>()
        every { prefs[keyActiveUserId] } returns "user-active"
        every { dataStore.data } returns flowOf(prefs)
        coEvery { userDao.getUserById("user-active") } returns UserEntity(uid = "user-active")

        repository.initializeAuthState()
        assertEquals(true, repository.authState.value)
    }

    @Test
    fun `initializeAuthState emits false when active user is not found in Room`() = runTest(testDispatcher) {
        val prefs = mockk<Preferences>()
        every { prefs[keyActiveUserId] } returns "user-deleted"
        every { dataStore.data } returns flowOf(prefs)
        coEvery { userDao.getUserById("user-deleted") } returns null

        repository.initializeAuthState()
        assertEquals(false, repository.authState.value)
    }
}
