package com.notifast.app.data.remote.google

import android.accounts.Account
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.gson.JsonParser
import com.notifast.app.BuildConfig
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.repository.CloudAuthException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class GoogleIdResult(
    val account: CloudAccount,
    val idToken: String
)

sealed interface DriveAuthResult {
    data class Success(val accessToken: String) : DriveAuthResult
    data class NeedsResolution(val pendingIntent: PendingIntent) : DriveAuthResult
    data class Failure(val throwable: Throwable) : DriveAuthResult
}

interface GoogleAuthDataSource {
    suspend fun signInWithGoogle(activityContext: Context): Result<GoogleIdResult>
    suspend fun authorizeDrive(accountEmail: String): DriveAuthResult
    fun getAuthorizationResultFromIntent(intent: Intent): DriveAuthResult
    suspend fun clearCredentialState()
}

@Singleton
class DefaultGoogleAuthDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) : GoogleAuthDataSource {

    companion object {
        const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
    }

    private val credentialManager: CredentialManager by lazy {
        CredentialManager.create(context)
    }

    override suspend fun signInWithGoogle(activityContext: Context): Result<GoogleIdResult> {
        return try {
            val serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
            val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(activityContext, request)
            val credential = response.credential

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            val stableSub = extractSubFromIdToken(idToken) ?: googleIdTokenCredential.id
            val email = googleIdTokenCredential.id

            val account = CloudAccount(
                accountId = stableSub,
                email = email,
                displayName = googleIdTokenCredential.displayName,
                photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
            )

            Result.success(GoogleIdResult(account, idToken))
        } catch (e: GetCredentialCancellationException) {
            Result.failure(CloudAuthException(CloudAuthException.Reason.CANCELLED, e))
        } catch (e: NoCredentialException) {
            Result.failure(CloudAuthException(CloudAuthException.Reason.CANCELLED, e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun authorizeDrive(accountEmail: String): DriveAuthResult {
        return try {
            val authClient = Identity.getAuthorizationClient(context)
            val request = AuthorizationRequest.builder()
                .setRequestedScopes(listOf(Scope(DRIVE_APPDATA_SCOPE)))
                .setAccount(Account(accountEmail, "com.google"))
                .build()

            val result = authClient.authorize(request).await()
            if (result.hasResolution()) {
                val pendingIntent = result.pendingIntent
                if (pendingIntent != null) {
                    DriveAuthResult.NeedsResolution(pendingIntent)
                } else {
                    DriveAuthResult.Failure(CloudAuthException(CloudAuthException.Reason.REAUTH_REQUIRED))
                }
            } else {
                val token = result.accessToken
                if (!token.isNullOrBlank()) {
                    DriveAuthResult.Success(token)
                } else {
                    DriveAuthResult.Failure(CloudAuthException(CloudAuthException.Reason.REAUTH_REQUIRED))
                }
            }
        } catch (e: ApiException) {
            when (e.statusCode) {
                CommonStatusCodes.CANCELED -> DriveAuthResult.Failure(CloudAuthException(CloudAuthException.Reason.CANCELLED, e))
                CommonStatusCodes.SIGN_IN_REQUIRED -> DriveAuthResult.Failure(CloudAuthException(CloudAuthException.Reason.REAUTH_REQUIRED, e))
                CommonStatusCodes.SERVICE_DISABLED, CommonStatusCodes.SERVICE_VERSION_UPDATE_REQUIRED ->
                    DriveAuthResult.Failure(CloudAuthException(CloudAuthException.Reason.NO_PLAY_SERVICES, e))
                else -> DriveAuthResult.Failure(e)
            }
        } catch (e: Exception) {
            DriveAuthResult.Failure(e)
        }
    }

    override fun getAuthorizationResultFromIntent(intent: Intent): DriveAuthResult {
        return try {
            val authClient = Identity.getAuthorizationClient(context)
            val result = authClient.getAuthorizationResultFromIntent(intent)
            val token = result.accessToken
            if (!token.isNullOrBlank()) {
                DriveAuthResult.Success(token)
            } else {
                DriveAuthResult.Failure(CloudAuthException(CloudAuthException.Reason.PERMISSION_DENIED))
            }
        } catch (e: ApiException) {
            if (e.statusCode == CommonStatusCodes.CANCELED) {
                DriveAuthResult.Failure(CloudAuthException(CloudAuthException.Reason.CANCELLED, e))
            } else {
                DriveAuthResult.Failure(CloudAuthException(CloudAuthException.Reason.PERMISSION_DENIED, e))
            }
        } catch (e: Exception) {
            DriveAuthResult.Failure(e)
        }
    }

    override suspend fun clearCredentialState() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {}
    }

    private fun extractSubFromIdToken(idToken: String): String? {
        return try {
            val parts = idToken.split(".")
            if (parts.size >= 2) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
                val json = JsonParser.parseString(payload).asJsonObject
                json.get("sub")?.asString
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
