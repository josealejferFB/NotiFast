package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.feature.notes.components.SubjectIconMapper
import com.notifast.app.presentation.theme.ThemeUtils
import com.notifast.app.presentation.theme.type.monoAcademicLarge
import com.notifast.app.presentation.util.GradeFormatters

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share

@Composable
fun SubjectDetailHeroBento(
    subjectName: String,
    subjectIcon: String,
    subjectColorHex: String,
    averageGrade: Double?,
    displayScale: GradeScale = GradeScale.SCALE_0_10,
    evaluatedPercent: Float,
    onBackClick: () -> Unit,
    onAddNoteClick: () -> Unit,
    onAddEvaluationClick: () -> Unit,
    onAddScheduleClick: () -> Unit,
    onShareClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val baseColor = ThemeUtils.getAccessibleColor(subjectColorHex, isDark)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = baseColor,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(38.dp)) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.topbar_back_cd), tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.22f), modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = SubjectIconMapper.getIcon(subjectIcon), contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = subjectName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEditClick, modifier = Modifier.size(38.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.btn_edit), tint = Color.White)
                }
                IconButton(onClick = onShareClick, modifier = Modifier.size(38.dp)) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = stringResource(R.string.share_subject_title), tint = Color.White)
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroMetricPill(
                    label = stringResource(R.string.subject_detail_stat_gpa),
                    value = averageGrade?.let { GradeFormatters.average(it, displayScale) } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                HeroMetricPill(
                    label = stringResource(R.string.subject_detail_stat_evaluated),
                    value = "${(evaluatedPercent * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroQuickActionButton(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    label = stringResource(R.string.subject_detail_action_note),
                    onClick = onAddNoteClick,
                    modifier = Modifier.weight(1f)
                )
                HeroQuickActionButton(
                    icon = Icons.Default.CalendarMonth,
                    label = stringResource(R.string.subject_detail_action_evaluation),
                    onClick = onAddEvaluationClick,
                    modifier = Modifier.weight(1f)
                )
                HeroQuickActionButton(
                    icon = Icons.Default.Schedule,
                    label = stringResource(R.string.subject_detail_action_schedule),
                    onClick = onAddScheduleClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HeroMetricPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.Black.copy(alpha = 0.22f)) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            Text(text = value, style = MaterialTheme.typography.monoAcademicLarge, color = Color.White, fontSize = 20.sp)
        }
    }
}

@Composable
private fun HeroQuickActionButton(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.18f)) {
        Row(modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}
