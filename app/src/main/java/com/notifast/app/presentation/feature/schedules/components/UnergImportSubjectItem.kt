package com.notifast.app.presentation.feature.schedules.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.notifast.app.domain.model.schedule.UnergImportPreviewSubject
import com.notifast.app.presentation.feature.notes.components.SubjectIconMapper
import com.notifast.app.presentation.theme.ThemeUtils
import com.notifast.app.presentation.theme.color.toSafeComposeColor
import com.notifast.app.presentation.theme.isDark
import com.notifast.app.presentation.theme.type.JetBrainsMonoFamily

@Composable
fun UnergImportSubjectItem(
    subject: UnergImportPreviewSubject,
    onToggleSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.isDark
    val baseColor = subject.colorHex.toSafeComposeColor(default = MaterialTheme.colorScheme.primary)
    val accessibleColor = ThemeUtils.getAccessibleColor(baseColor, isDark)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (subject.selected) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            1.dp,
            if (subject.selected) accessibleColor.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggleSelected() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = subject.selected,
                    onCheckedChange = { onToggleSelected() },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = CircleShape,
                    color = accessibleColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = SubjectIconMapper.getIcon(subject.icon),
                            contentDescription = subject.subjectName,
                            tint = accessibleColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = subject.subjectName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (subject.section.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = accessibleColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = stringResource(R.string.subject_section_chip, subject.section),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accessibleColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (subject.isExistingSubject) {
                        Text(
                            text = stringResource(R.string.unerg_import_conflict_title),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Schedules details
            Column(
                modifier = Modifier.padding(start = 36.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                subject.schedules.forEach { s ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val dayName = when (s.dayOfWeek) {
                            1 -> "Lunes"
                            2 -> "Martes"
                            3 -> "Miércoles"
                            4 -> "Jueves"
                            5 -> "Viernes"
                            6 -> "Sábado"
                            else -> "Domingo"
                        }
                        Text(
                            text = "$dayName ${s.startTime} - ${s.endTime}",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (s.classroom.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = s.classroom,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnergConflictStrategySelector(
    conflictStrategy: com.notifast.app.domain.model.schedule.UnergImportConflictStrategy,
    onSelectStrategy: (com.notifast.app.domain.model.schedule.UnergImportConflictStrategy) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.unerg_import_conflict_title),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectStrategy(com.notifast.app.domain.model.schedule.UnergImportConflictStrategy.MERGE_SMART) }
        ) {
            androidx.compose.material3.RadioButton(
                selected = conflictStrategy == com.notifast.app.domain.model.schedule.UnergImportConflictStrategy.MERGE_SMART,
                onClick = { onSelectStrategy(com.notifast.app.domain.model.schedule.UnergImportConflictStrategy.MERGE_SMART) }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.unerg_import_strategy_merge),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectStrategy(com.notifast.app.domain.model.schedule.UnergImportConflictStrategy.REPLACE_ALL_SCHEDULES) }
        ) {
            androidx.compose.material3.RadioButton(
                selected = conflictStrategy == com.notifast.app.domain.model.schedule.UnergImportConflictStrategy.REPLACE_ALL_SCHEDULES,
                onClick = { onSelectStrategy(com.notifast.app.domain.model.schedule.UnergImportConflictStrategy.REPLACE_ALL_SCHEDULES) }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.unerg_import_strategy_replace),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
