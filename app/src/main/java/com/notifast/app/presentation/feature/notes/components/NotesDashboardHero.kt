package com.notifast.app.presentation.feature.notes.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.feature.notes.NextActivity
import com.notifast.app.presentation.theme.ThemeUtils
import com.notifast.app.presentation.theme.color.ForestSage
import com.notifast.app.presentation.theme.color.MutedCrimson
import com.notifast.app.presentation.theme.color.AquaGreen
import com.notifast.app.presentation.theme.color.toSafeComposeColor
import com.notifast.app.presentation.theme.type.monoAcademic
import com.notifast.app.presentation.util.GradeFormatters
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NotesDashboardHero(
    nextActivity: NextActivity,
    totalNotes: Int,
    totalSubjects: Int,
    globalAverage: Double?,
    onAddNoteClick: (String?) -> Unit,
    displayScale: GradeScale = GradeScale.SCALE_0_10,
    minPassingGrade: Double = displayScale.minPassingGrade,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NextActivityCard(nextActivity = nextActivity, onAddNoteClick = onAddNoteClick)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricStatCard(Modifier.weight(1f), stringResource(R.string.notes_hub_stat_notes_label), "$totalNotes", Icons.AutoMirrored.Filled.MenuBook, MaterialTheme.colorScheme.primary)
            MetricStatCard(Modifier.weight(1f), stringResource(R.string.notes_hub_stat_subjects_label), "$totalSubjects", Icons.Default.School, MaterialTheme.colorScheme.secondary)
            MetricStatCard(
                Modifier.weight(1f),
                stringResource(R.string.notes_hub_stat_average_label),
                globalAverage?.let { GradeFormatters.average(it, displayScale) } ?: stringResource(R.string.notes_hub_stat_average_na),
                Icons.AutoMirrored.Filled.Assignment,
                if ((globalAverage ?: 0.0) >= minPassingGrade) ForestSage else AquaGreen
            )
        }
    }
}

@Composable
private fun NextActivityCard(nextActivity: NextActivity, onAddNoteClick: (String?) -> Unit) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "hero_press"
    )

    Card(
        modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }.clickable(interactionSource = interactionSource, indication = null) {
            when (nextActivity) {
                is NextActivity.ClassActivity -> onAddNoteClick(nextActivity.schedule.subjectName)
                is NextActivity.EvaluationActivity -> onAddNoteClick(nextActivity.evaluation.subjectName)
                NextActivity.FreeDay -> onAddNoteClick(null)
            }
        },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            when (nextActivity) {
                is NextActivity.ClassActivity -> {
                    val rawColor = nextActivity.colorHex.toSafeComposeColor(default = MutedCrimson)
                    val accentColor = remember(rawColor, isDark) { ThemeUtils.getAccessibleColor(rawColor, isDark) }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = accentColor.copy(alpha = 0.14f), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))) {
                            Text(stringResource(R.string.notes_hub_next_class_badge), Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = accentColor, fontWeight = FontWeight.SemiBold)
                        }
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.clickable { onAddNoteClick(nextActivity.schedule.subjectName) }) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, null, Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.notes_hub_take_note_btn), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(nextActivity.schedule.subjectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text("${nextActivity.schedule.startTime} - ${nextActivity.schedule.endTime}", style = MaterialTheme.typography.monoAcademic, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        if (nextActivity.schedule.classroom.isNotBlank()) Text(" • ${nextActivity.schedule.classroom}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
                is NextActivity.EvaluationActivity -> {
                    val rawColor = nextActivity.colorHex.toSafeComposeColor(default = AquaGreen)
                    val accentColor = remember(rawColor, isDark) { ThemeUtils.getAccessibleColor(rawColor, isDark) }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = accentColor.copy(alpha = 0.14f), border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))) {
                            Text(stringResource(R.string.notes_hub_next_eval_badge), Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = accentColor, fontWeight = FontWeight.SemiBold)
                        }
                        nextActivity.evaluation.weight?.let { Text("${it.toInt()}%", style = MaterialTheme.typography.monoAcademic, color = accentColor, fontWeight = FontWeight.Bold) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(nextActivity.evaluation.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    val dateFormatted = nextActivity.evaluation.date?.let { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(it) }.orEmpty()
                    Text("${nextActivity.evaluation.subjectName} • $dateFormatted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                NextActivity.FreeDay -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = ForestSage.copy(alpha = 0.15f), modifier = Modifier.size(38.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.CheckCircle, null, tint = ForestSage, modifier = Modifier.size(20.dp)) }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(stringResource(R.string.notes_hub_free_day_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.notes_hub_free_day_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricStatCard(modifier: Modifier, label: String, value: String, icon: ImageVector, accentColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, Modifier.size(13.dp), tint = accentColor)
                Spacer(Modifier.width(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.monoAcademic, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
