package com.notifast.app.presentation.feature.settings.components

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.notifast.app.R
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.model.cloud.LocalSyncSummary
import com.notifast.app.presentation.feature.settings.SettingsDialogType
import com.notifast.app.presentation.feature.settings.SettingsEvent

@Composable
fun SettingsCloudAccountSection(
    account: CloudAccount?,
    syncSummary: LocalSyncSummary,
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
                subtitle = stringResource(R.string.settings_cloud_unlinked_banner_desc),
                icon = Icons.Default.CloudSync,
                iconTint = MaterialTheme.colorScheme.primary,
                trailing = SettingsTrailingContent.Chevron,
                onClick = if (!isLoading) {
                    { onEvent(SettingsEvent.LinkAccount(context)) }
                } else null
            )
            SettingsRowItem(
                title = stringResource(R.string.settings_cloud_open_hub_title),
                subtitle = stringResource(R.string.settings_cloud_open_hub_subtitle_unlinked),
                icon = Icons.Default.FolderShared,
                iconTint = MaterialTheme.colorScheme.secondary,
                trailing = SettingsTrailingContent.Chevron,
                showDivider = false,
                onClick = { onEvent(SettingsEvent.OpenCloudHub) }
            )
        } else {
            val isPending = syncSummary == LocalSyncSummary.PENDING
            val hubSubtitle = if (isPending) {
                stringResource(R.string.settings_cloud_open_hub_subtitle_pending)
            } else {
                stringResource(R.string.settings_cloud_open_hub_subtitle_synced)
            }
            val hubBadgeText = if (isPending) {
                stringResource(R.string.cloud_status_local_changes)
            } else {
                stringResource(R.string.cloud_status_synced)
            }

            SettingsRowItem(
                title = stringResource(R.string.settings_cloud_open_hub_title),
                subtitle = hubSubtitle,
                icon = if (isPending) Icons.Default.CloudSync else Icons.Default.FolderShared,
                iconTint = if (isPending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                trailing = SettingsTrailingContent.ValueBadge(
                    text = hubBadgeText,
                    containerColor = if (isPending) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    } else {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                    },
                    contentColor = if (isPending) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                ),
                onClick = { onEvent(SettingsEvent.OpenCloudHub) }
            )
            SettingsRowItem(
                title = account.displayName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.settings_cloud_account_linked_title),
                subtitle = stringResource(R.string.settings_cloud_account_linked_subtitle, account.email),
                icon = Icons.Default.CloudDone,
                iconTint = MaterialTheme.colorScheme.secondary,
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
                showDivider = false,
                onClick = if (!isLoading) {
                    { onEvent(SettingsEvent.OpenDialog(SettingsDialogType.UNLINK_GOOGLE_ACCOUNT)) }
                } else null
            )
        }
    }
}

