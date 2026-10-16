package com.notifast.app.presentation.feature.cloud.components

import androidx.compose.runtime.Composable
import com.notifast.app.presentation.feature.cloud.CloudEvent
import com.notifast.app.presentation.feature.cloud.CloudUiState

@Composable
fun CloudDialogsHost(
    uiState: CloudUiState,
    onEvent: (CloudEvent) -> Unit
) {
    if (uiState.pendingConflicts.isNotEmpty()) {
        RestoreConflictDialog(
            conflict = uiState.pendingConflicts.first(),
            errorText = uiState.conflictNameError,
            onValidateName = { onEvent(CloudEvent.ValidateConflictName(it)) },
            onDecision = { decision, applyToRest ->
                onEvent(CloudEvent.ResolveConflict(decision, applyToRest))
            },
            onDismiss = { onEvent(CloudEvent.Confirm(false)) }
        )
    }

    if (uiState.pendingConfirm != null) {
        CloudConfirmDialog(
            confirm = uiState.pendingConfirm,
            onConfirm = { accepted -> onEvent(CloudEvent.Confirm(accepted)) }
        )
    }

    if (uiState.error != null) {
        CloudErrorDialog(
            error = uiState.error,
            onDismiss = { onEvent(CloudEvent.ClearError) }
        )
    }
}
