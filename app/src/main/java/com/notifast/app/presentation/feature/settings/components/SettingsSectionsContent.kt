package com.notifast.app.presentation.feature.settings.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.notifast.app.presentation.atoms.AlertSeverity
import com.notifast.app.presentation.atoms.rememberNotiFastAlert
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.feature.settings.SettingsDialogType
import com.notifast.app.presentation.feature.settings.SettingsEvent
import com.notifast.app.presentation.feature.settings.SettingsUiState
import com.notifast.app.presentation.util.GradeFormatters

@Composable
fun SettingsSectionsContent(
    uiState: SettingsUiState,
    context: Context,
    onEditProfile: () -> Unit,
    onEvent: (SettingsEvent) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))
        SettingsProfileHeroCard(
            firstName = uiState.displayFirstName,
            lastName = uiState.displayLastName,
            email = uiState.displayEmail,
            photoUrl = uiState.displayPhotoUrl,
            academicCycle = uiState.activeAcademicCycle,
            syncSummary = uiState.syncSummary,
            onEditProfile = onEditProfile
        )
        Spacer(modifier = Modifier.height(14.dp))
        SettingsCloudAccountSection(
            account = uiState.cloudAccount,
            syncSummary = uiState.syncSummary,
            isLoading = uiState.isCloudLoading,
            context = context,
            onEvent = onEvent
        )
        Spacer(modifier = Modifier.height(14.dp))
        SettingsAcademicSection(uiState = uiState, onEvent = onEvent)
        SettingsNotificationsSection(
            uiState = uiState,
            onEvent = onEvent,
            onRequestPermission = onRequestPermission
        )
        SettingsAppearanceSection(uiState = uiState, onEvent = onEvent)
        SettingsStorageAboutSection(uiState = uiState, context = context, onEvent = onEvent)
        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
private fun SettingsAcademicSection(uiState: SettingsUiState, onEvent: (SettingsEvent) -> Unit) {
    SettingsSectionContainer(title = stringResource(R.string.settings_section_academic), icon = Icons.Default.School) {
        val scale = GradeScale.fromId(uiState.defaultGradeScale)
        val effectivePassing = if (uiState.minPassingGrade == 0f) scale.minPassingGrade.toFloat() else uiState.minPassingGrade
        val scaleDesc = when (uiState.defaultGradeScale) {
            "0-10" -> stringResource(R.string.settings_academic_scale_decimal)
            "0-100" -> stringResource(R.string.settings_academic_scale_percent)
            "A-F" -> stringResource(R.string.settings_academic_scale_letters)
            else -> stringResource(R.string.settings_academic_scale_vigesimal)
        }
        val passingText = GradeFormatters.passing(effectivePassing.toDouble(), scale)
        SettingsRowItem(
            title = stringResource(R.string.settings_academic_scale_title),
            subtitle = stringResource(R.string.settings_academic_scale_subtitle_format, scaleDesc, passingText),
            icon = Icons.Default.Grade,
            trailing = SettingsTrailingContent.ValueBadge(text = "${uiState.defaultGradeScale} • $passingText", isMono = true),
            onClick = { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.GRADE_SCALE)) }
        )
        val firstDayLabel = if (uiState.firstDayOfWeek == "SUNDAY") stringResource(R.string.settings_first_day_sunday) else stringResource(R.string.settings_first_day_monday)
        SettingsRowItem(
            title = stringResource(R.string.settings_academic_first_day_title),
            subtitle = stringResource(R.string.settings_academic_first_day_subtitle, firstDayLabel),
            icon = Icons.Default.CalendarMonth,
            onClick = { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.FIRST_DAY_OF_WEEK)) }
        )
        SettingsRowItem(
            title = stringResource(R.string.settings_academic_cycle_title),
            subtitle = stringResource(R.string.settings_academic_cycle_desc),
            icon = Icons.Default.Today,
            trailing = SettingsTrailingContent.ValueBadge(text = uiState.activeAcademicCycle, isMono = true),
            showDivider = false,
            onClick = { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.ACADEMIC_CYCLE)) }
        )
    }
}

@Composable
private fun SettingsAppearanceSection(uiState: SettingsUiState, onEvent: (SettingsEvent) -> Unit) {
    SettingsSectionContainer(title = stringResource(R.string.settings_section_appearance), icon = Icons.Default.Palette) {
        ThemeSelectorCard(
            currentPresetId = uiState.themePresetId,
            currentBackgroundPresetId = uiState.backgroundPresetId,
            customPrimaryHex = uiState.customPrimaryHex,
            customBackgroundHex = uiState.customBackgroundHex,
            isDarkMode = uiState.isDarkMode,
            onSelectPreset = { onEvent(SettingsEvent.SelectThemePreset(it)) },
            onSelectBackgroundPreset = { onEvent(SettingsEvent.SelectBackgroundPreset(it)) },
            onSetDarkMode = { onEvent(SettingsEvent.SetDarkMode(it)) },
            onOpenCustomStudio = { tab -> onEvent(SettingsEvent.OpenCustomThemeSheet(tab)) }
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
        SettingsRowItem(
            title = stringResource(R.string.settings_appearance_haptics_title),
            subtitle = stringResource(R.string.settings_appearance_haptics_subtitle),
            icon = Icons.Default.Vibration,
            trailing = SettingsTrailingContent.SwitchToggle(checked = uiState.hapticsEnabled, onCheckedChange = { onEvent(SettingsEvent.SetHapticsEnabled(it)) }),
            showDivider = false
        )
    }
}

@Composable
private fun SettingsStorageAboutSection(uiState: SettingsUiState, context: Context, onEvent: (SettingsEvent) -> Unit) {
    SettingsSectionContainer(title = stringResource(R.string.settings_section_storage), icon = Icons.Default.Storage) {
        SettingsRowItem(
            title = stringResource(R.string.settings_storage_cache_title),
            subtitle = stringResource(R.string.settings_storage_cache_subtitle),
            icon = Icons.Default.CleaningServices,
            trailing = SettingsTrailingContent.ValueBadge(text = uiState.cacheSizeFormatted, isMono = true),
            onClick = { onEvent(SettingsEvent.ClearCache) }
        )
        SettingsRowItem(
            title = stringResource(R.string.settings_storage_privacy_title),
            subtitle = stringResource(R.string.settings_storage_privacy_subtitle),
            icon = Icons.Default.Shield,
            onClick = { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.PRIVACY_POLICY)) }
        )
        SettingsRowItem(
            title = stringResource(R.string.settings_about_thesis_title),
            subtitle = stringResource(R.string.settings_about_version_value),
            icon = Icons.Default.Info,
            onClick = { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.ABOUT_BOTTOM_SHEET)) }
        )
        val showAlert = rememberNotiFastAlert()
        SettingsRowItem(
            title = stringResource(R.string.settings_about_feedback_title),
            subtitle = stringResource(R.string.settings_about_feedback_subtitle),
            icon = Icons.Default.Feedback,
            onClick = { sendFeedback(context, showAlert) }
        )
        SettingsRowItem(
            title = stringResource(R.string.settings_about_logout_title),
            subtitle = stringResource(R.string.settings_about_logout_subtitle),
            icon = Icons.AutoMirrored.Filled.Logout,
            iconTint = MaterialTheme.colorScheme.error,
            titleColor = MaterialTheme.colorScheme.error,
            showDivider = false,
            onClick = { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.LOGOUT)) }
        )
    }
}

private fun sendFeedback(context: Context, showAlert: (String, AlertSeverity) -> Unit) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:contacto@notifast.app")).apply {
        putExtra(Intent.EXTRA_SUBJECT, "NotiFast 3.0 Feedback")
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Feedback"))
    } catch (e: Exception) {
        Log.w("SettingsSections", "Failed to launch feedback email", e)
        showAlert(context.getString(R.string.settings_about_feedback_error), AlertSeverity.ERROR)
    }
}


