package com.notifast.app.presentation.feature.addschedule.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.data.model.Schedule
import com.notifast.app.presentation.theme.color.Aqua

private data class DayOption(
    val dayOfWeek: Int,
    val abbreviation: String,
    val fullName: String
)

private val DAYS_ORDER = listOf(
    DayOption(1, "L", "Lunes"),
    DayOption(2, "M", "Martes"),
    DayOption(3, "M", "Miércoles"),
    DayOption(4, "J", "Jueves"),
    DayOption(5, "V", "Viernes"),
    DayOption(6, "S", "Sábado"),
    DayOption(0, "D", "Domingo")
)

@Composable
fun ScheduleDaySegmentedBar(
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.schedules_field_day),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DAYS_ORDER.forEach { dayOption ->
                val isSelected = selectedDayIndex == dayOption.dayOfWeek
                val animBgColor by animateColorAsState(
                    targetValue = if (isSelected) Aqua else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "DayBgAnimation"
                )
                val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(CircleShape)
                        .background(animBgColor)
                        .clickable { onDaySelected(dayOption.dayOfWeek) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayOption.abbreviation,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
