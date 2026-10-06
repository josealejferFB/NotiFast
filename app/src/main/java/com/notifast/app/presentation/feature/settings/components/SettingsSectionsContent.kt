package com.notifast.app.presentation.feature.settings.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Download
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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))

        // Section 1: Hero Card 3.0
        SettingsProfileHeroCard(
            user = uiState.userProfile,
            academicCycle = uiState.activeAcademicCycle,
            syncStatusText = uiState.lastSyncTimestampText,
            onEditProfile = onEditProfile
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Section 2: Appearance & Design
        SettingsSectionContainer(
            title = stringResource(R.string.settings_section_appearance),
            icon = Icons.Default.Palette
        ) {
            val themeSubtitle = when (uiState.isDarkMode) {
                null -> stringResource(R.string.settings_appearance_theme_system)
                true -> stringResource(R.string.settings_appearance_theme_dark)
                false -> stringResource(R.string.settings_appearance_theme_light)
            }
            SettingsRowItem(
                title = stringResource(R.string.settings_appearance_theme_title),
                subtitle = themeSubtitle,
                icon = Icons.Default.Palette,
                onClick = { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.THEME)) }
            )
            SettingsRowItem(
                title = stringResource(R.string.settings_appearance_haptics_title),
                subtitle = stringResource(R.string.settings_appearance_haptics_subtitle),
                icon = Icons.Default.Vibration,
                trailing = SettingsTrailingContent.SwitchToggle(
                    checked = uiState.hapticsEnabled,
                    onCheckedChange = { onEvent(SettingsEvent.SetHapticsEnabled(it)) }
                )
            )
        }

        // Section 3: Proactive Notifications
        SettingsSectionContainer(
            title = stringResource(R.string.settings_section_notifications),
            icon = Icons.Default.Notifications
        ) {
            val classSub = if (uiState.classAlertsEnabled) {
                stringResource(R.string.settings_notifications_classes_subtitle, uiState.classAlertMinutes)
            } else stringResource(R.string.settings_notifications_classes_disabled)

            val evalSub = if (uiState.evalAlertsEnabled) {
                stringResource(R.string.settings_notifications_evaluations_subtitle, uiState.evalAlertDays)
            } else stringResource(R.string.settings_notifications_evaluations_disabled)

            SettingsRowItem(
                title = stringResource(R.string.settings_notifications_classes_title),
                subtitle = classSub,
                icon = Icons.Default.NotificationsActive,
                trailing = SettingsTrailingContent.SwitchToggle(
                    checked = uiState.classAlertsEnabled,
                    onCheckedChange = { onEvent(SettingsEvent.SetClassAlertsEnabled(it)) }
                ),
                onClick = if (uiState.classAlertsEnabled) {
                    { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.CLASS_ALERT_TIMING)) }
                } else null
            )
            SettingsRowItem(
                title = stringResource(R.string.settings_notifications_evaluations_title),
                subtitle = evalSub,
                icon = Icons.Default.CalendarMonth,
                trailing = SettingsTrailingContent.SwitchToggle(
                    checked = uiState.evalAlertsEnabled,
                    onCheckedChange = { onEvent(SettingsEvent.SetEvalAlertsEnabled(it)) }
                ),
                onClick = if (uiState.evalAlertsEnabled) {
                    { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.EVAL_ALERT_TIMING)) }
                } else null
            )
            SettingsRowItem(
                title = stringResource(R.string.settings_notifications_daily_summary_title),
                subtitle = stringResource(R.string.settings_notifications_daily_summary_subtitle),
                icon = Icons.Default.WbSunny,
                trailing = SettingsTrailingContent.SwitchToggle(
                    checked = uiState.dailySummaryEnabled,
                    onCheckedChange = { onEvent(SettingsEvent.SetDailySummaryEnabled(it)) }
                )
            )
        }

        // Section 4: Academic Settings
        SettingsSectionContainer(
            title = stringResource(R.string.settings_section_academic),
            icon = Icons.Default.School
        ) {
            val scale = GradeScale.fromId(uiState.defaultGradeScale)
            val effectivePassing = if (uiState.minPassingGrade == 0f) {
                scale.minPassingGrade.toFloat()
            } else {
                uiState.minPassingGrade
            }
            val scaleDesc = when (uiState.defaultGradeScale) {
                "0-10" -> stringResource(R.string.settings_academic_scale_decimal)
                "0-100" -> stringResource(R.string.settings_academic_scale_percent)
                "A-F" -> stringResource(R.string.settings_academic_scale_letters)
                else -> stringResource(R.string.settings_academic_scale_vigesimal)
            }
            val passingText = GradeFormatters.passing(effectivePassing.toDouble(), scale)

            SettingsRowItem(
                title = stringResource(R.string.settings_academic_scale_title),
                subtitle = "$scaleDesc • Mín: $passingText",
                icon = Icons.Default.Grade,
                onClick = { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.GRADE_SCALE)) }
            )
            SettingsRowItem(
                title = stringResource(R.string.settings_academic_cycle_title),
                subtitle = "${uiState.activeAcademicCycle} • ${stringResource(R.string.settings_academic_cycle_desc)}",
                icon = Icons.Default.Today,
                onClick = { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.ACADEMIC_CYCLE)) }
            )
            val firstDayLabel = if (uiState.firstDayOfWeek == "SUNDAY") {
                stringResource(R.string.settings_first_day_sunday)
            } else stringResource(R.string.settings_first_day_monday)

            SettingsRowItem(
                title = stringResource(R.string.settings_academic_first_day_title),
                subtitle = firstDayLabel,
                icon = Icons.Default.CalendarMonth,
                onClick = { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.FIRST_DAY_OF_WEEK)) }
            )
        }

        // Section 5: Data & Storage
        SettingsSectionContainer(
            title = stringResource(R.string.settings_section_storage),
            icon = Icons.Default.Storage
        ) {
            SettingsRowItem(
                title = stringResource(R.string.settings_storage_cache_title),
                subtitle = stringResource(R.string.settings_storage_cache_clear_btn),
                icon = Icons.Default.CleaningServices,
                trailing = SettingsTrailingContent.ValueBadge(uiState.cacheSizeFormatted),
                onClick = { onEvent(SettingsEvent.ClearCache) }
            )
            SettingsRowItem(
                title = stringResource(R.string.settings_storage_privacy_title),
                subtitle = stringResource(R.string.settings_storage_privacy_subtitle),
                icon = Icons.Default.Shield,
                onClick = { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.PRIVACY_POLICY)) }
            )
        }

        // Section 6: About & Support
        SettingsSectionContainer(
            title = stringResource(R.string.settings_section_about),
            icon = Icons.Default.Info
        ) {
            SettingsRowItem(
                title = stringResource(R.string.settings_about_thesis_title),
                subtitle = stringResource(R.string.settings_about_version_value),
                icon = Icons.Default.Info,
                onClick = { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.ABOUT_BOTTOM_SHEET)) }
            )
            SettingsRowItem(
                title = stringResource(R.string.settings_about_feedback_title),
                subtitle = stringResource(R.string.settings_about_feedback_subtitle),
                icon = Icons.Default.Feedback,
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:contacto@notifast.app")
                        putExtra(Intent.EXTRA_SUBJECT, "NotiFast 3.0 Feedback")
                    }
                    try {
                        context.startActivity(Intent.createChooser(intent, "Feedback"))
                    } catch (_: Exception) {}
                }
            )
            SettingsRowItem(
                title = stringResource(R.string.settings_about_logout_title),
                subtitle = stringResource(R.string.settings_about_logout_subtitle),
                icon = Icons.AutoMirrored.Filled.Logout,
                iconTint = MaterialTheme.colorScheme.error,
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.LOGOUT)) }
            )
        }

        Spacer(modifier = Modifier.height(110.dp))
    }
}
