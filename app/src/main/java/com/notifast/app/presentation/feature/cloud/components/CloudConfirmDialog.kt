package com.notifast.app.presentation.feature.cloud.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.feature.cloud.CloudConfirm

@Composable
fun CloudConfirmDialog(
    confirm: CloudConfirm,
    onConfirm: (Boolean) -> Unit
) {
    val (titleRes, message) = when (confirm) {
        is CloudConfirm.CloudNewer -> Pair(
            R.string.cloud_confirm_newer_title,
            stringResource(R.string.cloud_confirm_newer_msg)
        )
        is CloudConfirm.MobileDataLarge -> Pair(
            R.string.cloud_confirm_metered_title,
            stringResource(R.string.cloud_confirm_metered_msg)
        )
        is CloudConfirm.RestoreGlobal -> Pair(
            R.string.cloud_confirm_restore_global_title,
            stringResource(R.string.cloud_confirm_restore_global_msg)
        )
        is CloudConfirm.RestoreCovers -> Pair(
            R.string.cloud_confirm_restore_covers_title,
            stringResource(R.string.cloud_confirm_restore_covers_msg)
        )
        is CloudConfirm.DeleteCloud -> Pair(
            R.string.cloud_confirm_delete_cloud_title,
            stringResource(R.string.cloud_confirm_delete_cloud_msg, confirm.item.name)
        )
    }

    val isDestructive = confirm is CloudConfirm.DeleteCloud

    NotiFastDialog(
        title = stringResource(titleRes),
        message = message,
        onConfirm = { onConfirm(true) },
        onDismiss = { onConfirm(false) },
        confirmText = stringResource(R.string.btn_accept),
        dismissText = stringResource(R.string.btn_cancel),
        confirmColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        confirmWeight = FontWeight.SemiBold
    )
}
