package com.notifast.app.presentation.feature.schedules.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import com.notifast.app.presentation.theme.isDark
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.notifast.app.data.model.Schedule
import com.notifast.app.presentation.feature.notes.components.SubjectIconMapper
import com.notifast.app.presentation.theme.ThemeUtils
import com.notifast.app.presentation.theme.color.toSafeComposeColor
import com.notifast.app.presentation.theme.type.JetBrainsMonoFamily

@Composable
fun ScheduleCard(
    schedule: Schedule,
    isCurrent: Boolean,
    isConflicted: Boolean,
    onTakeNote: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onJoinMeeting: (String) -> Unit,
    modifier: Modifier = Modifier,
    subjectIcon: String = "📚",
    subjectColorHex: String = "",
    subjectSection: String = ""
) {
    val isDark = MaterialTheme.colorScheme.isDark
    val rawColor = if (schedule.color != 0L) {
        Color(schedule.color)
    } else if (subjectColorHex.isNotBlank()) {
        subjectColorHex.toSafeComposeColor(default = MaterialTheme.colorScheme.primary)
    } else {
        MaterialTheme.colorScheme.primary
    }
    val accessibleColor = ThemeUtils.getAccessibleColor(rawColor, isDark)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isConflicted) MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
            else if (isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(accessibleColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SubjectIconMapper.getIcon(subjectIcon),
                        contentDescription = schedule.subjectName,
                        tint = accessibleColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = schedule.subjectName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (subjectSection.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            androidx.compose.material3.Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = accessibleColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = stringResource(R.string.subject_section_chip, subjectSection),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accessibleColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${schedule.startTime} - ${schedule.endTime}",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.schedules_action_edit),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = stringResource(R.string.schedules_action_delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (schedule.classroom.isNotBlank() || schedule.teacher.isNotBlank() || schedule.modality == Schedule.MODALITY_VIRTUAL) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (schedule.classroom.isNotBlank()) DetailChip(Icons.Default.MeetingRoom, stringResource(R.string.schedules_classroom_label, schedule.classroom))
                    if (schedule.teacher.isNotBlank()) DetailChip(Icons.Default.Person, stringResource(R.string.schedules_teacher_label, schedule.teacher))
                    if (schedule.modality == Schedule.MODALITY_VIRTUAL) DetailChip(Icons.Default.Videocam, stringResource(R.string.schedules_modality_virtual))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (!schedule.meetingUrl.isNullOrBlank()) {
                    IconButton(onClick = { onJoinMeeting(schedule.meetingUrl) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Videocam, contentDescription = stringResource(R.string.schedules_beacon_join_class), tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                IconButton(onClick = onTakeNote, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = stringResource(R.string.schedules_beacon_take_note), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
