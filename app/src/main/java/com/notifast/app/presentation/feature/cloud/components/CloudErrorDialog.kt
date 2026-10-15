package com.notifast.app.presentation.feature.cloud.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.feature.cloud.CloudError

@Composable
fun CloudErrorDialog(
    error: CloudError,
    onDismiss: () -> Unit
) {
    val errorMsg = when (error) {
        CloudError.CANCELLED -> stringResource(R.string.cloud_error_cancelled)
        CloudError.NO_PLAY_SERVICES -> stringResource(R.string.cloud_error_no_play_services)
        CloudError.PERMISSION_DENIED -> stringResource(R.string.cloud_error_permission_denied)
        CloudError.REAUTH_REQUIRED -> stringResource(R.string.cloud_error_reauth_required)
        CloudError.QUOTA_FULL -> stringResource(R.string.cloud_error_quota_full)
        CloudError.OFFLINE -> stringResource(R.string.cloud_error_offline)
        CloudError.GENERIC -> stringResource(R.string.cloud_error_generic)
    }

    NotiFastDialog(
        title = stringResource(R.string.cloud_hub_title),
        message = errorMsg,
        onConfirm = onDismiss,
        onDismiss = onDismiss,
        confirmText = stringResource(R.string.btn_accept),
        dismissText = ""
    )
}
