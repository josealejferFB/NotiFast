package com.example.notifast.presentation.feature.subjectdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notifast.data.model.Evaluation
import com.example.notifast.presentation.theme.color.Aqua
import java.util.Calendar
import java.util.Date

private fun parseSubjectColor(hex: String): Color {
    return try {
        if (hex.isNotBlank()) Color(android.graphics.Color.parseColor(hex))
        else Aqua
    } catch (e: Exception) { Aqua }
}

private fun daysUntil(date: Date): Long {
    val diff = date.time - System.currentTimeMillis()
    return diff / (1000 * 60 * 60 * 24)
}

private fun urgencyColor(days: Long): Color = when {
    days < 0 -> Color.Gray
    days <= 7 -> Color(0xFFE53935)
    days <= 14 -> Color(0xFFFB8C00)
    else -> Color(0xFF43A047)
}

private val months = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")

private fun dayOfMonth(date: Date): Int = Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_MONTH)
private fun monthOf(date: Date): String = months[Calendar.getInstance().apply { time = date }.get(Calendar.MONTH)]

@Composable
fun SubjectDetailCalendarRow(
    evaluations: List<Evaluation>,
    subjectColorHex: String
) {
    if (evaluations.isEmpty()) return

    val color = parseSubjectColor(subjectColorHex)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text("Calendario", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(evaluations.take(6)) { eval ->
                val date = eval.date
                if (date != null) {
                    CalendarSquare(
                        day = dayOfMonth(date),
                        month = monthOf(date),
                        title = eval.title,
                        color = color,
                        urgencyColor = urgencyColor(daysUntil(date)),
                        weight = eval.weight
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarSquare(
    day: Int,
    month: String,
    title: String,
    color: Color,
    urgencyColor: Color,
    weight: Double?
) {
    Card(
        modifier = Modifier.size(width = 120.dp, height = 175.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(month, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(vertical = 6.dp))
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("$day", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(urgencyColor))
            Spacer(Modifier.height(6.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            if (weight != null && weight > 0.0) {
                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                ) {
                    Text("${(weight * 100).toInt()}%", fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
    }
}
