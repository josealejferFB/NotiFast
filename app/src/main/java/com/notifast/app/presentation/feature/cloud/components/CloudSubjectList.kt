package com.notifast.app.presentation.feature.cloud.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.cloud.CloudSubjectItem
import com.notifast.app.presentation.atoms.NotiFastEmptyState

/**
 * 120 FPS LazyColumn list for subjects and global settings with spring item placement animations.
 */
@Composable
fun CloudSubjectList(
    filteredItems: List<CloudSubjectItem>,
    hasAnyItems: Boolean,
    showGlobalRow: Boolean,
    isGlobalSelected: Boolean,
    hasGlobalCloudCopy: Boolean,
    selectedIds: Set<String>,
    failedIds: Set<String>,
    onToggleGlobal: () -> Unit,
    onToggleSubject: (String) -> Unit,
    onDeleteCloudSubject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showGlobalRow) {
            item(key = "cloud_global_settings") {
                GlobalSettingsRow(
                    isSelected = isGlobalSelected,
                    onToggle = onToggleGlobal,
                    hasCloudCopy = hasGlobalCloudCopy
                )
            }
        }

        if (filteredItems.isEmpty() && hasAnyItems) {
            item(key = "cloud_empty_filter") {
                NotiFastEmptyState(
                    title = stringResource(R.string.cloud_empty_filter_title),
                    message = stringResource(R.string.cloud_empty_filter_msg),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

        items(
            items = filteredItems,
            key = { it.subjectId }
        ) { item ->
            CloudSubjectRow(
                item = item,
                isSelected = item.subjectId in selectedIds,
                hasError = item.subjectId in failedIds,
                onToggle = onToggleSubject,
                onDeleteCloud = onDeleteCloudSubject,
                modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    fadeOutSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    placementSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            )
        }
    }
}
