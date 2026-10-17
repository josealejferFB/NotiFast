package com.notifast.app.presentation.feature.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.feature.settings.SettingsDialogType
import com.notifast.app.presentation.feature.settings.SettingsEvent
import com.notifast.app.presentation.feature.settings.SettingsUiState

@Composable
fun SettingsNotificationsSection(
    uiState: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsSectionContainer(
        title = stringResource(R.string.settings_section_notifications),
        icon = Icons.Default.Notifications,
        modifier = modifier
    ) {
        if (!uiState.isSystemNotificationPermissionGranted) {
            PermissionWarningBanner(onRequestPermission = onRequestPermission)
        }

        val classSub = if (uiState.classAlertsEnabled) {
            stringResource(R.string.settings_notifications_classes_subtitle, uiState.classAlertMinutes)
        } else {
            stringResource(R.string.settings_notifications_classes_disabled)
        }

        val evalSub = if (uiState.evalAlertsEnabled) {
            stringResource(R.string.settings_notifications_evaluations_subtitle, uiState.evalAlertDays)
        } else {
            stringResource(R.string.settings_notifications_evaluations_disabled)
        }

        SettingsRowItem(
            title = stringResource(R.string.settings_notifications_classes_title),
            subtitle = classSub,
            icon = Icons.Default.NotificationsActive,
            trailing = SettingsTrailingContent.SwitchToggle(
                checked = uiState.classAlertsEnabled,
                onCheckedChange = { checked ->
                    if (checked && !uiState.isSystemNotificationPermissionGranted) {
                        onRequestPermission()
                    }
                    onEvent(SettingsEvent.SetClassAlertsEnabled(checked))
                },
                badgeText = stringResource(R.string.settings_notifications_badge_min, uiState.classAlertMinutes)
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
                onCheckedChange = { checked ->
                    if (checked && !uiState.isSystemNotificationPermissionGranted) {
                        onRequestPermission()
                    }
                    onEvent(SettingsEvent.SetEvalAlertsEnabled(checked))
                },
                badgeText = stringResource(R.string.settings_notifications_badge_days, uiState.evalAlertDays)
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
                onCheckedChange = { checked ->
                    if (checked && !uiState.isSystemNotificationPermissionGranted) {
                        onRequestPermission()
                    }
                    onEvent(SettingsEvent.SetDailySummaryEnabled(checked))
                }
            )
        )

        SettingsRowItem(
            title = stringResource(R.string.settings_notifications_test_btn),
            subtitle = stringResource(R.string.settings_notifications_test_btn_desc),
            icon = Icons.Default.NotificationsActive,
            iconTint = MaterialTheme.colorScheme.primary,
            titleColor = MaterialTheme.colorScheme.primary,
            trailing = SettingsTrailingContent.Chevron,
            showDivider = false,
            onClick = { onEvent(SettingsEvent.SendTestNotification) }
        )
    }
}

@Composable
private fun PermissionWarningBanner(onRequestPermission: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_notifications_permission_warning_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.settings_notifications_permission_warning_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            NotiFastButton(
                text = stringResource(R.string.settings_notifications_permission_enable_btn),
                onClick = onRequestPermission,
                backgroundColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
