package com.notifast.app.presentation.feature.settings

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.notifast.app.data.model.User
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.model.cloud.LocalSyncSummary

enum class SettingsDialogType {
    THEME,
    GRADE_SCALE,
    ACADEMIC_CYCLE,
    FIRST_DAY_OF_WEEK,
    CLASS_ALERT_TIMING,
    EVAL_ALERT_TIMING,
    ABOUT_BOTTOM_SHEET,
    PRIVACY_POLICY,
    LOGOUT,
    UNLINK_GOOGLE_ACCOUNT
}

data class SettingsUiState(
    val isLoading: Boolean = false,
    val userProfile: User? = null,
    val displayFirstName: String = "",
    val displayLastName: String = "",
    val displayEmail: String = "",
    val displayPhotoUrl: String? = null,
    val cloudAccount: CloudAccount? = null,
    val syncSummary: LocalSyncSummary = LocalSyncSummary.NO_ACCOUNT,
    val isCloudHubOpen: Boolean = false,
    val pendingAuthIntent: PendingIntent? = null,
    val isCloudLoading: Boolean = false,
    val cloudError: String? = null,
    val isDarkMode: Boolean? = null,
    val hapticsEnabled: Boolean = true,
    val classAlertsEnabled: Boolean = true,
    val classAlertMinutes: Int = 15,
    val evalAlertsEnabled: Boolean = true,
    val evalAlertDays: Int = 1,
    val dailySummaryEnabled: Boolean = false,
    val defaultGradeScale: String = "0-10",
    val minPassingGrade: Float = 0f,
    val evaluationsWithGradeCount: Int = 0,
    val lastConvertedCount: Int = 0,
    val lastScaleLabel: String? = null,
    val error: String? = null,
    val activeAcademicCycle: String = "2026-I",
    val firstDayOfWeek: String = "MONDAY",
    val cacheSizeBytes: Long = 0L,
    val cacheSizeFormatted: String = "0.0 MB",
    val activeDialog: SettingsDialogType? = null,
    val snackbarMessage: String? = null,
    val isLoggedOut: Boolean = false
)

sealed interface SettingsEvent {
    data object LoadProfile : SettingsEvent
    data object RefreshCacheSize : SettingsEvent
    data class SetDarkMode(val enabled: Boolean?) : SettingsEvent
    data class SetHapticsEnabled(val enabled: Boolean) : SettingsEvent
    data class SetClassAlertsEnabled(val enabled: Boolean) : SettingsEvent
    data class SetClassAlertMinutes(val minutes: Int) : SettingsEvent
    data class SetEvalAlertsEnabled(val enabled: Boolean) : SettingsEvent
    data class SetEvalAlertDays(val days: Int) : SettingsEvent
    data class SetDailySummaryEnabled(val enabled: Boolean) : SettingsEvent
    data class ChangeGradeScale(val targetScaleId: String, val minPassingGrade: Float) : SettingsEvent
    data class SetActiveAcademicCycle(val cycle: String) : SettingsEvent
    data class SetFirstDayOfWeek(val day: String) : SettingsEvent
    data object ClearCache : SettingsEvent
    data class OpenDialog(val dialog: SettingsDialogType) : SettingsEvent
    data object DismissDialog : SettingsEvent
    data object OpenCloudHub : SettingsEvent
    data object CloseCloudHub : SettingsEvent
    data object ClearSnackbar : SettingsEvent
    data object Logout : SettingsEvent
    data class LinkAccount(val activityContext: Context) : SettingsEvent
    data class SwitchAccount(val activityContext: Context) : SettingsEvent
    data object UnlinkAccount : SettingsEvent
    data class AuthResult(val data: Intent?) : SettingsEvent
    data object ClearCloudError : SettingsEvent
}

