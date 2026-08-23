package com.notifast.app.presentation.feature.schedules.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.data.model.Schedule
import com.notifast.app.presentation.feature.schedules.ScheduleViewMode
import java.util.Calendar

/**
 * NotiFast 3.0 Tactile Day Selector & Tri-View Mode Switcher with Dual-Dot Indicators.
 */
@Composable
fun ScheduleDaySelector(
    selectedDay: Int,
    classCountsByDay: Map<Int, Int>,
    evaluationCountsByDay: Map<Int, Int> = emptyMap(),
    viewMode: ScheduleViewMode,
    onDaySelected: (Int) -> Unit,
    onViewModeChanged: (ScheduleViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val daysOrder = listOf(1, 2, 3, 4, 5, 6, 0)
    val today = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 0 else it }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ViewModeSegmentedToggle(
                currentMode = viewMode,
                onModeSelected = onViewModeChanged
            )
        }

        if (viewMode == ScheduleViewMode.DAILY_TIMELINE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                daysOrder.forEach { dayIndex ->
                    val isSelected = selectedDay == dayIndex
                    val isToday = dayIndex == today
                    val dayName = Schedule.DAY_NAMES.getOrElse(dayIndex) { "Día $dayIndex" }.take(3)
                    val classCount = classCountsByDay[dayIndex] ?: 0
                    val evalCount = evaluationCountsByDay[dayIndex] ?: 0

                    DayPill(
                        dayName = dayName,
                        classCount = classCount,
                        evalCount = evalCount,
                        isSelected = isSelected,
                        isToday = isToday,
                        onClick = { onDaySelected(dayIndex) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayPill(
    dayName: String,
    classCount: Int,
    evalCount: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        animationSpec = spring(dampingRatio = 0.85f),
        label = "PillContainerColor"
    )

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        shadowElevation = 0.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dayName,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            if (isToday) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                )
            }
            if (evalCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                )
            }
            if (classCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = classCount.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
