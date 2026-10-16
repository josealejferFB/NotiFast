package com.notifast.app.data.repository

import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.data.remote.google.DriveAuthResult
import com.notifast.app.data.remote.google.GoogleAuthDataSource
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.repository.AuthStep
import com.notifast.app.domain.repository.CloudAuthException
import com.notifast.app.domain.repository.GoogleAccountRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.googleAccountDataStore: DataStore<Preferences> by preferencesDataStore(name = "linked_google_account")

@Singleton
class GoogleAccountRepositoryImpl(
    private val context: Context,
    private val dataSource: GoogleAuthDataSource,
    private val subjectCloudStateDao: SubjectCloudStateDao,
    private val dataStoreOverride: DataStore<Preferences>? = null
) : GoogleAccountRepository {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        dataSource: GoogleAuthDataSource,
        subjectCloudStateDao: SubjectCloudStateDao
    ) : this(context, dataSource, subjectCloudStateDao, null)

    private object AccountKeys {
        val ACCOUNT_ID = stringPreferencesKey("account_id")
        val EMAIL = stringPreferencesKey("email")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val PHOTO_URL = stringPreferencesKey("photo_url")
    }

    private val dataStore: DataStore<Preferences>
        get() = dataStoreOverride ?: context.googleAccountDataStore

    private var pendingAccount: CloudAccount? = null

    override val account: Flow<CloudAccount?> = dataStore.data.map { prefs ->
        val id = prefs[AccountKeys.ACCOUNT_ID]
        val email = prefs[AccountKeys.EMAIL]
        if (!id.isNullOrBlank() && !email.isNullOrBlank()) {
            CloudAccount(
                accountId = id,
                email = email,
                displayName = prefs[AccountKeys.DISPLAY_NAME],
                photoUrl = prefs[AccountKeys.PHOTO_URL]
            )
        } else {
            null
        }
    }

    override suspend fun signIn(activityContext: Context): Result<AuthStep> {
        return try {
            val idResult = dataSource.signInWithGoogle(activityContext).getOrElse {
                return Result.failure(it)
            }

            when (val driveResult = dataSource.authorizeDrive(idResult.account.email)) {
                is DriveAuthResult.NeedsResolution -> {
                    pendingAccount = idResult.account
                    Result.success(AuthStep.NeedsResolution(driveResult.pendingIntent))
                }
                is DriveAuthResult.Success -> {
                    pendingAccount = null
                    replaceAndPersistAccount(idResult.account)
                    Result.success(AuthStep.Linked(idResult.account))
                }
                is DriveAuthResult.Failure -> {
                    pendingAccount = null
                    Result.failure(driveResult.throwable)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun switchAccount(activityContext: Context): Result<AuthStep> {
        return try {
            // Clear Credential Manager session state so the picker lets the user choose another account,
            // WITHOUT wiping the currently linked account or DataStore until the new sign-in succeeds.
            dataSource.clearCredentialState()
            signIn(activityContext)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeAuthorization(data: Intent?): Result<CloudAccount> {
        if (data == null) {
            return Result.failure(CloudAuthException(CloudAuthException.Reason.CANCELLED))
        }

        return when (val driveResult = dataSource.getAuthorizationResultFromIntent(data)) {
            is DriveAuthResult.Success -> {
                val acct = pendingAccount
                    ?: return Result.failure(IllegalStateException("No pending Google account found for resolution"))
                replaceAndPersistAccount(acct)
                pendingAccount = null
                Result.success(acct)
            }
            is DriveAuthResult.Failure -> {
                Result.failure(driveResult.throwable)
            }
            is DriveAuthResult.NeedsResolution -> {
                Result.failure(IllegalStateException("Unexpected resolution requested in completion"))
            }
        }
    }

    override suspend fun accessToken(forceRefresh: Boolean): Result<String> {
        return try {
            val currentAccount = account.first()
                ?: return Result.failure(CloudAuthException(CloudAuthException.Reason.REAUTH_REQUIRED))

            when (val driveResult = dataSource.authorizeDrive(currentAccount.email)) {
                is DriveAuthResult.Success -> Result.success(driveResult.accessToken)
                is DriveAuthResult.NeedsResolution -> Result.failure(CloudAuthException(CloudAuthException.Reason.REAUTH_REQUIRED))
                is DriveAuthResult.Failure -> Result.failure(driveResult.throwable)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlink(): Result<Unit> {
        return try {
            val current = account.first()
            dataSource.clearCredentialState()
            dataStore.edit { prefs ->
                prefs.remove(AccountKeys.ACCOUNT_ID)
                prefs.remove(AccountKeys.EMAIL)
                prefs.remove(AccountKeys.DISPLAY_NAME)
                prefs.remove(AccountKeys.PHOTO_URL)
            }
            if (current != null) {
                subjectCloudStateDao.deleteByAccount(current.accountId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun replaceAndPersistAccount(acct: CloudAccount) {
        val previous = account.first()
        if (previous != null && previous.accountId != acct.accountId) {
            subjectCloudStateDao.deleteByAccount(previous.accountId)
        }
        persistAccount(acct)
    }

    private suspend fun persistAccount(acct: CloudAccount) {
        dataStore.edit { prefs ->
            prefs[AccountKeys.ACCOUNT_ID] = acct.accountId
            prefs[AccountKeys.EMAIL] = acct.email
            if (acct.displayName != null) {
                prefs[AccountKeys.DISPLAY_NAME] = acct.displayName
            } else {
                prefs.remove(AccountKeys.DISPLAY_NAME)
            }
            if (acct.photoUrl != null) {
                prefs[AccountKeys.PHOTO_URL] = acct.photoUrl
            } else {
                prefs.remove(AccountKeys.PHOTO_URL)
            }
        }
    }
}
