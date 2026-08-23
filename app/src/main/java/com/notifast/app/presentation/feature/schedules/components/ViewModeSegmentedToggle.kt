package com.notifast.app.presentation.feature.schedules.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.presentation.feature.schedules.ScheduleViewMode

@Composable
internal fun ViewModeSegmentedToggle(
    currentMode: ScheduleViewMode,
    onModeSelected: (ScheduleViewMode) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(3.dp)) {
            ToggleOption(
                icon = Icons.Default.ViewTimeline,
                title = stringResource(R.string.agenda_view_timeline),
                isSelected = currentMode == ScheduleViewMode.DAILY_TIMELINE,
                onClick = { onModeSelected(ScheduleViewMode.DAILY_TIMELINE) }
            )
            Spacer(modifier = Modifier.width(2.dp))
            ToggleOption(
                icon = Icons.Default.CalendarMonth,
                title = stringResource(R.string.agenda_view_calendar),
                isSelected = currentMode == ScheduleViewMode.MONTHLY_CALENDAR,
                onClick = { onModeSelected(ScheduleViewMode.MONTHLY_CALENDAR) }
            )
            Spacer(modifier = Modifier.width(2.dp))
            ToggleOption(
                icon = Icons.Default.GridView,
                title = stringResource(R.string.agenda_view_week),
                isSelected = currentMode == ScheduleViewMode.WEEKLY_BENTO,
                onClick = { onModeSelected(ScheduleViewMode.WEEKLY_BENTO) }
            )
        }
    }
}

@Composable
private fun ToggleOption(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
