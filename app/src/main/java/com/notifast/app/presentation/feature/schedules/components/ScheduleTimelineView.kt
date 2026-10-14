package com.notifast.app.presentation.feature.schedules.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import com.notifast.app.presentation.feature.schedules.ScheduleTimelineItem

/**
 * NotiFast 3.0 Timeline View.
 * Displays vertical timeline with daily evaluations header, classes, conflict warnings, and study gaps.
 */
@Composable
fun ScheduleTimelineView(
    items: List<ScheduleTimelineItem>,
    conflicts: List<Schedule>,
    dailyEvaluations: List<Evaluation> = emptyList(),
    onEvaluationClick: (Evaluation) -> Unit = {},
    onTakeNote: (String) -> Unit = {},
    onEditSchedule: (String) -> Unit = {},
    onDeleteSchedule: (Schedule) -> Unit = {},
    onJoinMeeting: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    displayScale: GradeScale = GradeScale.SCALE_0_10,
    contentPadding: PaddingValues = PaddingValues(bottom = 120.dp, top = 4.dp)
) {
    if (items.isEmpty() && dailyEvaluations.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            NotiFastEmptyState(
                icon = Icons.Default.Schedule,
                title = stringResource(R.string.schedules_empty_title),
                message = stringResource(R.string.schedules_empty_message)
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = contentPadding
    ) {
        if (dailyEvaluations.isNotEmpty()) {
            item(key = "daily_evaluations_header") {
                AgendaDayEvaluationsSection(
                    evaluations = dailyEvaluations,
                    onEvaluationClick = onEvaluationClick,
                    displayScale = displayScale
                )
            }
        }

        if (conflicts.isNotEmpty()) {
            item(key = "conflict_banner") {
                ScheduleConflictBanner(
                    conflicts = conflicts,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        items(
            items = items,
            key = { item ->
                when (item) {
                    is ScheduleTimelineItem.ClassItem -> "class_${item.schedule.id}"
                    is ScheduleTimelineItem.GapItem -> "gap_${item.startTime}_${item.endTime}"
                }
            }
        ) { timelineItem ->
            when (timelineItem) {
                is ScheduleTimelineItem.ClassItem -> {
                    ScheduleCard(
                        schedule = timelineItem.schedule,
                        isCurrent = timelineItem.isCurrent,
                        isConflicted = timelineItem.isConflicted,
                        subjectIcon = timelineItem.subjectIcon,
                        subjectColorHex = timelineItem.subjectColorHex,
                        onTakeNote = { onTakeNote(timelineItem.schedule.subjectName) },
                        onEdit = { onEditSchedule(timelineItem.schedule.id) },
                        onDelete = { onDeleteSchedule(timelineItem.schedule) },
                        onJoinMeeting = onJoinMeeting
                    )
                }
                is ScheduleTimelineItem.GapItem -> {
                    ScheduleGapCard(
                        startTime = timelineItem.startTime,
                        endTime = timelineItem.endTime,
                        durationMinutes = timelineItem.durationMinutes
                    )
                }
            }
        }
    }
}
