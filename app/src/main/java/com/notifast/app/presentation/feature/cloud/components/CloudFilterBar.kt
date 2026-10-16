package com.notifast.app.presentation.feature.cloud.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastChip

enum class CloudFilter {
    ALL,
    PENDING,
    SYNCED,
    CONFLICTS
}

/**
 * NotiFast 3.0 Cloud Quick Filter Bar AAA.
 * Provides tactile filtering with spring physics and count badges for rapid scanning.
 */
@Composable
fun CloudFilterBar(
    currentFilter: CloudFilter,
    onFilterSelected: (CloudFilter) -> Unit,
    allCount: Int,
    pendingCount: Int,
    syncedCount: Int,
    conflictsCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotiFastChip(
            label = stringResource(R.string.cloud_filter_all, allCount),
            selected = currentFilter == CloudFilter.ALL,
            onClick = { onFilterSelected(CloudFilter.ALL) }
        )
        NotiFastChip(
            label = stringResource(R.string.cloud_filter_pending, pendingCount),
            selected = currentFilter == CloudFilter.PENDING,
            onClick = { onFilterSelected(CloudFilter.PENDING) }
        )
        NotiFastChip(
            label = stringResource(R.string.cloud_filter_synced, syncedCount),
            selected = currentFilter == CloudFilter.SYNCED,
            onClick = { onFilterSelected(CloudFilter.SYNCED) }
        )
        if (conflictsCount > 0) {
            NotiFastChip(
                label = stringResource(R.string.cloud_filter_conflicts, conflictsCount),
                selected = currentFilter == CloudFilter.CONFLICTS,
                onClick = { onFilterSelected(CloudFilter.CONFLICTS) }
            )
        }
    }
}
