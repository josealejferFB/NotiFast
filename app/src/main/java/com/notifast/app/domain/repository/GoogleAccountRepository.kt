package com.notifast.app.domain.repository

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.notifast.app.domain.model.cloud.CloudAccount
import kotlinx.coroutines.flow.Flow

sealed interface AuthStep {
    data class NeedsResolution(val pendingIntent: PendingIntent) : AuthStep
    data class Linked(val account: CloudAccount) : AuthStep
}

class CloudAuthException(val reason: Reason, cause: Throwable? = null) : Exception(cause) {
    enum class Reason {
        CANCELLED,
        NO_PLAY_SERVICES,
        PERMISSION_DENIED,
        REAUTH_REQUIRED
    }
}

interface GoogleAccountRepository {
    val account: Flow<CloudAccount?>
    suspend fun signIn(activityContext: Context): Result<AuthStep>
    suspend fun switchAccount(activityContext: Context): Result<AuthStep> = signIn(activityContext)
    suspend fun completeAuthorization(data: Intent?): Result<CloudAccount>
    suspend fun accessToken(forceRefresh: Boolean = false): Result<String>
    suspend fun unlink(): Result<Unit>
}
