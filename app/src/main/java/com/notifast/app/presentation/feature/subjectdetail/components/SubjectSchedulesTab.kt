package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.util.ScheduleCalculations
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import java.util.Calendar

/**
 * NotiFast 3.2 Subject Schedules Tab (UI/UX Pro Max Edition).
 * Displays a weekly academic rhythm summary card, chronological class list,
 * today indicator, modality pills, and direct virtual aula access.
 */
@Composable
fun SubjectSchedulesTab(
    schedules: List<Schedule>,
    onAddScheduleClick: () -> Unit,
    onDeleteScheduleClick: (Schedule) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayDayOfWeek = remember {
        val calDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
        if (calDay == 0) 0 else calDay
    }

    val sortedSchedules = remember(schedules) {
        schedules.sortedWith(
            compareBy(
                { if (it.dayOfWeek == 0) 7 else it.dayOfWeek },
                { ScheduleCalculations.timeToMinutes(it.startTime) }
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (sortedSchedules.isEmpty()) {
            NotiFastEmptyState(
                title = stringResource(R.string.subject_detail_schedules_empty_title),
                message = stringResource(R.string.subject_detail_schedules_empty_desc),
                actionLabel = stringResource(R.string.subject_detail_schedules_add_btn),
                onActionClick = onAddScheduleClick
            )
        } else {
            SubjectSchedulesSummaryCard(
                schedules = sortedSchedules,
                onAddScheduleClick = onAddScheduleClick
            )

            sortedSchedules.forEach { schedule ->
                SubjectScheduleCard(
                    schedule = schedule,
                    isToday = schedule.dayOfWeek == todayDayOfWeek,
                    onDeleteClick = { onDeleteScheduleClick(schedule) }
                )
            }
        }
    }
}
