package com.notifast.app.presentation.feature.schedules.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.data.model.Evaluation
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import com.notifast.app.presentation.theme.type.JetBrainsMonoFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * NotiFast 3.0 Month Matrix Calendar & Date Evaluation Explorer.
 */
@Composable
fun AgendaMonthCalendarView(
    currentMonth: Calendar,
    selectedDate: Calendar,
    evaluationsByDate: Map<String, List<Evaluation>>,
    dailyEvaluations: List<Evaluation>,
    onDateSelected: (Calendar) -> Unit,
    onChangeMonth: (Int) -> Unit,
    onGoToToday: () -> Unit,
    onEvaluationClick: (Evaluation) -> Unit,
    modifier: Modifier = Modifier,
    firstDayOfWeek: String = "SUNDAY"
) {
    val monthTitle = remember(currentMonth) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            .format(currentMonth.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onChangeMonth(-1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null)
            }

            Text(
                text = monthTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onGoToToday) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = "Ir a hoy",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onChangeMonth(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        CalendarMatrix(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            evaluationsByDate = evaluationsByDate,
            onDateSelected = onDateSelected,
            firstDayOfWeek = firstDayOfWeek
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (dailyEvaluations.isNotEmpty()) stringResource(R.string.agenda_evaluations_date_title)
                   else stringResource(R.string.calendar_empty_title),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (dailyEvaluations.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        if (dailyEvaluations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                NotiFastEmptyState(
                    icon = Icons.Default.Event,
                    title = stringResource(R.string.calendar_empty_title),
                    message = stringResource(R.string.calendar_empty_message)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(dailyEvaluations, key = { it.id }) { eval ->
                    AgendaEvaluationCard(
                        evaluation = eval,
                        onClick = { onEvaluationClick(eval) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarMatrix(
    currentMonth: Calendar,
    selectedDate: Calendar,
    evaluationsByDate: Map<String, List<Evaluation>>,
    onDateSelected: (Calendar) -> Unit,
    firstDayOfWeek: String = "SUNDAY"
) {
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    // 0 = Sunday … 6 = Saturday (matches Schedule.DAY_NAMES)
    val monthStartsOn = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK) - 1
    val startOffset = if (firstDayOfWeek.equals("MONDAY", ignoreCase = true)) 1 else 0
    // Padding so the first day of the month lands under the correct column
    val paddingCount = (monthStartsOn - startOffset + 7) % 7
    val days = (1..daysInMonth).toList()
    val paddingDays = (0 until paddingCount).map { "" }
    val format = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val selectedDateStr = format.format(selectedDate.time)
    val todayStr = format.format(Date())

    val dayLabels = if (startOffset == 1) {
        listOf("L", "M", "X", "J", "V", "S", "D")
    } else {
        listOf("D", "L", "M", "X", "J", "V", "S")
    }

    Column {
        Row(Modifier.fillMaxWidth()) {
            dayLabels.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        val totalCells = paddingDays + days
        totalCells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                week.forEach { day ->
                    if (day is Int) {
                        val date = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                        val dateStr = format.format(date.time)
                        val isSelected = dateStr == selectedDateStr
                        val dayEvals = evaluationsByDate[dateStr] ?: emptyList()
                        val hasEvals = dayEvals.isNotEmpty()
                        val allPresented = hasEvals && dayEvals.all { it.isPresented }
                        val dotColor = if (allPresented) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        val isToday = dateStr == todayStr

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontSize = 12.sp,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (hasEvals) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else dotColor)
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (week.size < 7) {
                    repeat(7 - week.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}
