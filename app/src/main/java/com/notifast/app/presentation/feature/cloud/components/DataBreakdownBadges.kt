package com.notifast.app.presentation.feature.cloud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.domain.model.cloud.CloudSubjectItem
import com.notifast.app.presentation.theme.type.monoAcademicBold
import com.notifast.app.presentation.theme.type.monoAcademicSmall
import java.util.Locale

/**
 * Micro-badges row displaying counts for notes, evaluations, schedules and file size
 * using high-contrast tabular monospace typography.
 */
@Composable
fun DataBreakdownBadges(
    item: CloudSubjectItem,
    modifier: Modifier = Modifier
) {
    val sizeStr = formatSizeBytes(item.sizeBytes)
    val hasAnyBadge = item.noteCount > 0 || item.evaluationCount > 0 || item.scheduleCount > 0 || sizeStr.isNotEmpty()

    if (hasAnyBadge) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.noteCount > 0) {
                MicroBadgePill(
                    icon = Icons.Rounded.Description,
                    text = stringResource(R.string.cloud_badge_notes, item.noteCount)
                )
            }
            if (item.evaluationCount > 0) {
                MicroBadgePill(
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    text = stringResource(R.string.cloud_badge_evaluations, item.evaluationCount)
                )
            }
            if (item.scheduleCount > 0) {
                MicroBadgePill(
                    icon = Icons.Rounded.CalendarMonth,
                    text = stringResource(R.string.cloud_badge_schedules, item.scheduleCount)
                )
            }
            if (sizeStr.isNotEmpty()) {
                Text(
                    text = sizeStr,
                    style = MaterialTheme.typography.monoAcademicBold.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Text(
            text = stringResource(R.string.cloud_subject_empty_local),
            style = MaterialTheme.typography.monoAcademicSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f)
        )
    }
}

@Composable
private fun MicroBadgePill(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 5.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.monoAcademicSmall.copy(fontSize = 10.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatSizeBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024f * 1024f))
        bytes >= 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024f)
        bytes > 0 -> "$bytes B"
        else -> ""
    }
}
