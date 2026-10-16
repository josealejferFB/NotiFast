package com.notifast.app.data.repository

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.data.remote.google.DriveAuthResult
import com.notifast.app.data.remote.google.GoogleAuthDataSource
import com.notifast.app.data.remote.google.GoogleIdResult
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.repository.AuthStep
import com.notifast.app.domain.repository.CloudAuthException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GoogleAccountRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var dataSource: GoogleAuthDataSource
    private lateinit var subjectCloudStateDao: SubjectCloudStateDao
    private lateinit var repository: GoogleAccountRepositoryImpl

    private val testAccount = CloudAccount(
        accountId = "sub-123",
        email = "student@gmail.com",
        displayName = "Student Name",
        photoUrl = "https://photo.url"
    )

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        dataSource = mockk(relaxed = true)
        subjectCloudStateDao = mockk(relaxed = true)

        val testDataStoreFile = tempFolder.newFile("test_account_prefs.preferences_pb")
        val testDataStore = PreferenceDataStoreFactory.create { testDataStoreFile }

        repository = GoogleAccountRepositoryImpl(
            context = context,
            dataSource = dataSource,
            subjectCloudStateDao = subjectCloudStateDao,
            dataStoreOverride = testDataStore
        )
    }

    @Test
    fun sign_in_with_authorization_granted_returns_Linked_and_persists_account() = runTest {
        coEvery { dataSource.signInWithGoogle(any()) } returns Result.success(
            GoogleIdResult(testAccount, "fake-id-token")
        )
        coEvery { dataSource.authorizeDrive(testAccount.email) } returns DriveAuthResult.Success("access-token-1")

        val result = repository.signIn(context)
        assertTrue(result.isSuccess)
        val step = result.getOrThrow()
        assertTrue(step is AuthStep.Linked)
        assertEquals(testAccount, (step as AuthStep.Linked).account)

        // Verify persisted in DataStore
        val stored = repository.account.first()
        assertEquals(testAccount, stored)
    }

    @Test
    fun authorization_needs_resolution_returns_NeedsResolution_and_nothing_persisted_until_completed() = runTest {
        val mockPendingIntent = mockk<PendingIntent>()
        coEvery { dataSource.signInWithGoogle(any()) } returns Result.success(
            GoogleIdResult(testAccount, "fake-id-token")
        )
        coEvery { dataSource.authorizeDrive(testAccount.email) } returns DriveAuthResult.NeedsResolution(mockPendingIntent)

        val result = repository.signIn(context)
        assertTrue(result.isSuccess)
        val step = result.getOrThrow()
        assertTrue(step is AuthStep.NeedsResolution)
        assertEquals(mockPendingIntent, (step as AuthStep.NeedsResolution).pendingIntent)

        // Verify NOT yet persisted
        assertNull(repository.account.first())

        // Now complete authorization with result intent
        val mockIntent = mockk<Intent>()
        every { dataSource.getAuthorizationResultFromIntent(mockIntent) } returns DriveAuthResult.Success("access-token-2")

        val completeResult = repository.completeAuthorization(mockIntent)
        assertTrue(completeResult.isSuccess)
        assertEquals(testAccount, completeResult.getOrThrow())

        // Now verify it IS persisted
        val stored = repository.account.first()
        assertEquals(testAccount, stored)
    }

    @Test
    fun sign_in_cancellation_returns_CloudAuthException_CANCELLED() = runTest {
        coEvery { dataSource.signInWithGoogle(any()) } returns Result.failure(
            CloudAuthException(CloudAuthException.Reason.CANCELLED)
        )

        val result = repository.signIn(context)
        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(ex is CloudAuthException)
        assertEquals(CloudAuthException.Reason.CANCELLED, (ex as CloudAuthException).reason)
    }

    @Test
    fun unlink_clears_dataStore_and_deletes_account_cloud_states() = runTest {
        // Link account first
        coEvery { dataSource.signInWithGoogle(any()) } returns Result.success(
            GoogleIdResult(testAccount, "fake-id-token")
        )
        coEvery { dataSource.authorizeDrive(testAccount.email) } returns DriveAuthResult.Success("access-token-1")
        repository.signIn(context)
        assertEquals(testAccount, repository.account.first())

        // Unlink
        val unlinkResult = repository.unlink()
        assertTrue(unlinkResult.isSuccess)

        coVerify { dataSource.clearCredentialState() }
        coVerify { subjectCloudStateDao.deleteByAccount("sub-123") }
        assertNull(repository.account.first())
    }

    @Test
    fun switchAccount_when_user_cancels_picker_preserves_existing_linked_account() = runTest {
        coEvery { dataSource.signInWithGoogle(any()) } returns Result.success(
            GoogleIdResult(testAccount, "fake-id-token")
        )
        coEvery { dataSource.authorizeDrive(testAccount.email) } returns DriveAuthResult.Success("access-token-1")
        repository.signIn(context)
        assertEquals(testAccount, repository.account.first())

        // Now user taps Switch Account and cancels the picker
        coEvery { dataSource.signInWithGoogle(any()) } returns Result.failure(
            CloudAuthException(CloudAuthException.Reason.CANCELLED)
        )

        val switchResult = repository.switchAccount(context)
        assertTrue(switchResult.isFailure)

        // Existing account MUST still be linked and its cloud state must NOT be deleted
        assertEquals(testAccount, repository.account.first())
        coVerify(exactly = 0) { subjectCloudStateDao.deleteByAccount("sub-123") }
    }

    @Test
    fun switchAccount_when_new_account_linked_replaces_account_and_clears_old_cloud_states() = runTest {
        coEvery { dataSource.signInWithGoogle(any()) } returns Result.success(
            GoogleIdResult(testAccount, "fake-id-token")
        )
        coEvery { dataSource.authorizeDrive(testAccount.email) } returns DriveAuthResult.Success("access-token-1")
        repository.signIn(context)
        assertEquals(testAccount, repository.account.first())

        val secondAccount = CloudAccount(
            accountId = "sub-456",
            email = "other@gmail.com",
            displayName = "Other Student",
            photoUrl = null
        )
        coEvery { dataSource.signInWithGoogle(any()) } returns Result.success(
            GoogleIdResult(secondAccount, "fake-id-token-2")
        )
        coEvery { dataSource.authorizeDrive(secondAccount.email) } returns DriveAuthResult.Success("access-token-2")

        val switchResult = repository.switchAccount(context)
        assertTrue(switchResult.isSuccess)
        assertEquals(secondAccount, repository.account.first())
        coVerify(exactly = 1) { subjectCloudStateDao.deleteByAccount("sub-123") }
    }
}
