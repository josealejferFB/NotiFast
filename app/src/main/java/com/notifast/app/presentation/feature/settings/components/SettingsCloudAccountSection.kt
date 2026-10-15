package com.notifast.app.presentation.feature.settings.components

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.notifast.app.R
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.presentation.feature.settings.SettingsDialogType
import com.notifast.app.presentation.feature.settings.SettingsEvent

@Composable
fun SettingsCloudAccountSection(
    account: CloudAccount?,
    isLoading: Boolean,
    context: Context,
    onEvent: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsSectionContainer(
        title = stringResource(R.string.settings_section_cloud_account),
        icon = Icons.Default.Cloud,
        modifier = modifier
    ) {
        if (account == null) {
            SettingsRowItem(
                title = stringResource(R.string.settings_cloud_btn_link),
                subtitle = stringResource(R.string.settings_cloud_account_unlinked_subtitle),
                icon = Icons.Default.CloudSync,
                iconTint = MaterialTheme.colorScheme.primary,
                trailing = SettingsTrailingContent.Chevron,
                onClick = if (!isLoading) {
                    { onEvent(SettingsEvent.LinkAccount(context)) }
                } else null
            )
        } else {
            SettingsRowItem(
                title = account.displayName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.settings_cloud_account_linked_title),
                subtitle = stringResource(R.string.settings_cloud_account_linked_subtitle, account.email),
                icon = Icons.Default.CloudDone,
                iconTint = MaterialTheme.colorScheme.tertiary,
                trailing = SettingsTrailingContent.None,
                onClick = null
            )
            SettingsRowItem(
                title = stringResource(R.string.settings_cloud_btn_switch),
                subtitle = null,
                icon = Icons.Default.Sync,
                trailing = SettingsTrailingContent.Chevron,
                onClick = if (!isLoading) {
                    { onEvent(SettingsEvent.SwitchAccount(context)) }
                } else null
            )
            SettingsRowItem(
                title = stringResource(R.string.settings_cloud_btn_unlink),
                subtitle = null,
                icon = Icons.Default.LinkOff,
                iconTint = MaterialTheme.colorScheme.error,
                titleColor = MaterialTheme.colorScheme.error,
                trailing = SettingsTrailingContent.Chevron,
                onClick = if (!isLoading) {
                    { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.UNLINK_GOOGLE_ACCOUNT)) }
                } else null
            )
        }
    }
}
