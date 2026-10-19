package com.notifast.app.presentation.feature.cloud

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.notifast.app.domain.model.cloud.BatchResult
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.model.cloud.CloudSubjectItem
import com.notifast.app.domain.model.cloud.CloudSubjectStatus
import com.notifast.app.domain.model.cloud.RestoreConflict
import com.notifast.app.domain.model.cloud.RestoreDecision
import com.notifast.app.domain.model.cloud.UploadProgress

data class CloudUiState(
    val account: CloudAccount? = null,
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val global: CloudFile? = null,
    val globalSelected: Boolean = false,
    val covers: CloudFile? = null,
    val coversSelected: Boolean = false,
    val coversStatus: CloudSubjectStatus = CloudSubjectStatus.LOCAL_ONLY,
    val localCoverCount: Int = 0,
    val items: List<CloudSubjectItem> = emptyList(),
    val selected: Set<String> = emptySet(),
    val canUpload: Boolean = false,
    val canRestore: Boolean = false,
    val cloudOnlyBanner: Int = 0,
    val progress: UploadProgress.Progress? = null,
    val summary: BatchResult? = null,
    val pendingConflicts: List<RestoreConflict> = emptyList(),
    val pendingConfirm: CloudConfirm? = null,
    val pendingAuthIntent: PendingIntent? = null,
    val error: CloudError? = null,
    val conflictNameError: String? = null,
    val failedSubjectIds: Set<String> = emptySet()
)

sealed interface CloudConfirm {
    data object CloudNewer : CloudConfirm
    data class MobileDataLarge(val bytes: Long) : CloudConfirm
    data object RestoreGlobal : CloudConfirm
    data object RestoreCovers : CloudConfirm
    data class DeleteCloud(val item: CloudSubjectItem) : CloudConfirm
}

sealed interface CloudEvent {
    data class SignIn(val activity: Context) : CloudEvent
    data class AuthResult(val data: Intent?) : CloudEvent
    data class Toggle(val id: String) : CloudEvent
    data object ToggleGlobal : CloudEvent
    data object ToggleCovers : CloudEvent
    data object SelectAll : CloudEvent
    data object Upload : CloudEvent
    data object Restore : CloudEvent
    data class Confirm(val accepted: Boolean) : CloudEvent
    data class ResolveConflict(val decision: RestoreDecision, val applyToRest: Boolean) : CloudEvent
    data class DeleteFromCloud(val id: String) : CloudEvent
    data object RetryFailed : CloudEvent
    data object Refresh : CloudEvent
    data object DismissSummary : CloudEvent
    data object ClearError : CloudEvent
    data class ValidateConflictName(val candidate: String) : CloudEvent
}

enum class CloudError {
    CANCELLED,
    NO_PLAY_SERVICES,
    PERMISSION_DENIED,
    REAUTH_REQUIRED,
    QUOTA_FULL,
    OFFLINE,
    GENERIC
}
