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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton

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
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onToggleSelectAll) {
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
                    style = MaterialTheme.typography.bodySmall,
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
                onClick = onRestore,
                enabled = canRestore && !isOffline && !isLoading,
                isLoading = false,
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )

            NotiFastButton(
                text = stringResource(R.string.cloud_action_upload),
                onClick = onUpload,
                enabled = canUpload && !isOffline && !isLoading,
                isLoading = isLoading,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
