package com.notifast.app.presentation.feature.cloud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.domain.model.cloud.CloudSubjectItem
import com.notifast.app.domain.model.cloud.CloudSubjectStatus
import com.notifast.app.presentation.feature.notes.components.SubjectIconMapper
import com.notifast.app.presentation.theme.ThemeUtils

@Composable
fun CloudSubjectRow(
    item: CloudSubjectItem,
    isSelected: Boolean,
    onToggle: (String) -> Unit,
    onDeleteCloud: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val subjectColor = ThemeUtils.getAccessibleColor(item.colorHex, isDark)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle(item.subjectId) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle(item.subjectId) },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        Spacer(modifier = Modifier.width(4.dp))

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(subjectColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = SubjectIconMapper.getIcon(item.icon),
                contentDescription = null,
                tint = subjectColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            CloudStatusChip(status = item.status)
        }

        if (item.status == CloudSubjectStatus.CLOUD_ONLY && onDeleteCloud != null) {
            IconButton(
                onClick = { onDeleteCloud(item.subjectId) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = stringResource(R.string.cloud_action_delete_cloud),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
