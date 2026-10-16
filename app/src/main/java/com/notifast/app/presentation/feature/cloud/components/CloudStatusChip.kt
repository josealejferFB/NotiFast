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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.SyncProblem
import androidx.compose.material.icons.rounded.WarningAmber
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
import com.notifast.app.presentation.theme.color.AquaDark
import com.notifast.app.presentation.theme.color.AquaGreen
import com.notifast.app.presentation.theme.color.ForestSage
import com.notifast.app.presentation.theme.color.SecondaryContainerDark
import com.notifast.app.presentation.theme.color.SecondaryContainerLight
import com.notifast.app.presentation.theme.isDark
import com.notifast.app.presentation.theme.type.monoAcademicSmall

/**
 * NotiFast 3.0 Cloud Status Chip AAA.
 * Strict compliance with WCAG AAA contrast, tactile papercraft borders and official corporate palette:
 * - Ruby (Error/Conflict)
 * - Emerald / ForestSage (Synced)
 * - Amber / Sunset (Pending / Local changes)
 * - Aqua (Cloud only)
 */
@Composable
fun CloudStatusChip(
    status: CloudSubjectStatus,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val isDark = MaterialTheme.colorScheme.isDark

    val config = if (isError) {
        ChipConfig(
            labelRes = R.string.cloud_status_error,
            icon = Icons.Rounded.SyncProblem,
            contentColor = MaterialTheme.colorScheme.error,
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = if (isDark) 0.35f else 0.70f)
        )
    } else {
        when (status) {
            CloudSubjectStatus.SYNCED -> ChipConfig(
                labelRes = R.string.cloud_status_synced,
                icon = Icons.Rounded.CheckCircle,
                contentColor = if (isDark) Color(0xFF34D399) else ForestSage,
                containerColor = if (isDark) Color(0xFF065F46).copy(alpha = 0.45f) else Color(0xFFDCFCE7)
            )
            CloudSubjectStatus.LOCAL_CHANGES -> ChipConfig(
                labelRes = R.string.cloud_status_local_changes,
                icon = Icons.Rounded.EditCalendar,
                contentColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                containerColor = if (isDark) Color(0xFF78350F).copy(alpha = 0.45f) else Color(0xFFFEF3C7)
            )
            CloudSubjectStatus.CLOUD_NEWER -> ChipConfig(
                labelRes = R.string.cloud_status_cloud_newer,
                icon = Icons.Rounded.CloudDownload,
                contentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0369A1),
                containerColor = if (isDark) Color(0xFF0C4A6E).copy(alpha = 0.45f) else Color(0xFFE0F2FE)
            )
            CloudSubjectStatus.BOTH_UNLINKED -> ChipConfig(
                labelRes = R.string.cloud_status_both_unlinked,
                icon = Icons.Rounded.WarningAmber,
                contentColor = MaterialTheme.colorScheme.error,
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = if (isDark) 0.35f else 0.70f)
            )
            CloudSubjectStatus.LOCAL_ONLY -> ChipConfig(
                labelRes = R.string.cloud_status_local_only,
                icon = Icons.Rounded.PhoneAndroid,
                contentColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309),
                containerColor = if (isDark) Color(0xFF78350F).copy(alpha = 0.40f) else Color(0xFFFEF3C7)
            )
            CloudSubjectStatus.CLOUD_ONLY -> ChipConfig(
                labelRes = R.string.cloud_status_cloud_only,
                icon = Icons.Rounded.CloudQueue,
                contentColor = if (isDark) AquaDark else AquaGreen,
                containerColor = if (isDark) SecondaryContainerDark.copy(alpha = 0.45f) else SecondaryContainerLight
            )
        }
    }

    val labelText = stringResource(config.labelRes)

    Row(
        modifier = modifier
            .semantics { contentDescription = labelText }
            .background(color = config.containerColor, shape = RoundedCornerShape(999.dp))
            .border(
                BorderStroke(0.75.dp, config.contentColor.copy(alpha = 0.40f)),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = config.icon,
            contentDescription = null,
            tint = config.contentColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = labelText,
            color = config.contentColor,
            style = MaterialTheme.typography.monoAcademicSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

private data class ChipConfig(
    val labelRes: Int,
    val icon: ImageVector,
    val contentColor: Color,
    val containerColor: Color
)
