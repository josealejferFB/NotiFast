package com.notifast.app.presentation.feature.cloud.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.theme.type.monoAcademicBold

/**
 * NotiFast 3.0 Cloud Action Bar AAA.
 * Features corporate Aqua primary action for upload/sync, tactile haptic feedback,
 * and tabular monospace numbers for selection count.
 */
@Composable
fun CloudActionBar(
    allSelected: Boolean,
    selectedCount: Int,
    canRestore: Boolean,
    canUpload: Boolean,
    isOffline: Boolean,
    isLoading: Boolean,
    onToggleSelectAll: () -> Unit,
    onRestore: () -> Unit,
    onUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleSelectAll()
                }
            ) {
                Text(
                    text = if (allSelected) {
                        stringResource(R.string.cloud_action_deselect_all)
                    } else {
                        stringResource(R.string.cloud_action_select_all)
                    }
                )
            }

            if (selectedCount > 0) {
                Text(
                    text = stringResource(R.string.cloud_selected_count, selectedCount),
                    style = MaterialTheme.typography.monoAcademicBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NotiFastButton(
                text = stringResource(R.string.cloud_action_restore),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onRestore()
                },
                enabled = canRestore && !isOffline && !isLoading,
                isLoading = false,
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Primary action: Corporate Aqua
            NotiFastButton(
                text = stringResource(R.string.cloud_action_upload),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUpload()
                },
                enabled = canUpload && !isOffline && !isLoading,
                isLoading = isLoading,
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}
