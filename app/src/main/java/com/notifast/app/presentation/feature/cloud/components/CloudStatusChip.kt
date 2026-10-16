package com.notifast.app.presentation.feature.cloud.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.domain.model.cloud.CloudSubjectStatus

@Composable
fun CloudStatusChip(
    status: CloudSubjectStatus,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val (labelRes, icon, contentColor, containerColor) = if (isError) {
        Quad(
            R.string.cloud_status_error,
            Icons.Default.SyncProblem,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        )
    } else {
        when (status) {
            CloudSubjectStatus.SYNCED -> Quad(
                R.string.cloud_status_synced,
                Icons.Default.CloudDone,
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
            )
        CloudSubjectStatus.LOCAL_CHANGES -> Quad(
            R.string.cloud_status_local_changes,
            Icons.Default.Edit,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        )
        CloudSubjectStatus.CLOUD_NEWER -> Quad(
            R.string.cloud_status_cloud_newer,
            Icons.Default.CloudDownload,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        )
        CloudSubjectStatus.BOTH_UNLINKED -> Quad(
            R.string.cloud_status_both_unlinked,
            Icons.Default.SyncProblem,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        )
        CloudSubjectStatus.LOCAL_ONLY -> Quad(
            R.string.cloud_status_local_only,
            Icons.Default.PhoneAndroid,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
            CloudSubjectStatus.CLOUD_ONLY -> Quad(
                R.string.cloud_status_cloud_only,
                Icons.Default.CloudQueue,
                MaterialTheme.colorScheme.onSurfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        }
    }

    val labelText = stringResource(labelRes)

    Row(
        modifier = modifier
            .semantics { contentDescription = labelText }
            .background(color = containerColor, shape = RoundedCornerShape(999.dp))
            .border(
                BorderStroke(0.75.dp, contentColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = labelText,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
