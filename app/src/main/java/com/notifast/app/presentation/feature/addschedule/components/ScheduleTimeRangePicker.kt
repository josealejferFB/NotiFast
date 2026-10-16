package com.notifast.app.presentation.feature.addschedule.components

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.presentation.theme.color.Aqua

private val TIME_PRESETS = listOf(
    "08:00" to "10:00",
    "10:00" to "12:00",
    "14:00" to "16:00",
    "16:00" to "18:00"
)

@Composable
fun ScheduleTimeRangePicker(
    startTime: String,
    endTime: String,
    onTimeRangeChanged: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)

    val showTimePicker = { isStart: Boolean ->
        val currentTime = if (isStart) startTime else endTime
        val parts = currentTime.split(":").mapNotNull { it.toIntOrNull() }
        val initialHour = if (parts.size == 2) parts[0] else if (isStart) 8 else 10
        val initialMinute = if (parts.size == 2) parts[1] else 0

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val formatted = String.format(java.util.Locale.US, "%02d:%02d", hourOfDay, minute)
                if (isStart) {
                    onTimeRangeChanged(formatted, endTime)
                } else {
                    onTimeRangeChanged(startTime, formatted)
                }
            },
            initialHour,
            initialMinute,
            is24Hour
        ).show()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.agenda_create_schedule_option),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))

        // Fast Preset Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TIME_PRESETS.forEach { (presetStart, presetEnd) ->
                val isSelected = startTime == presetStart && endTime == presetEnd
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Aqua else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Aqua else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onTimeRangeChanged(presetStart, presetEnd) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$presetStart - $presetEnd",
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Start and End Time Trigger Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Start Time Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable { showTimePicker(true) }
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.schedule_time_select_start),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Aqua,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = startTime.ifBlank { "--:--" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // End Time Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable { showTimePicker(false) }
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.schedule_time_select_end),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Aqua,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = endTime.ifBlank { "--:--" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
