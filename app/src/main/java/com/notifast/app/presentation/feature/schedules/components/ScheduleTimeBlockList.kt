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
import com.notifast.app.data.model.Schedule
import com.notifast.app.presentation.atoms.NotiFastEmptyState

@Composable
fun ScheduleTimeBlockList(
    schedules: List<Schedule>,
    onDeleteSchedule: (Schedule) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 100.dp, top = 8.dp)
) {
    if (schedules.isEmpty()) {
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = contentPadding
    ) {
        val sortedSchedules = schedules.sortedBy { it.startTime }
        items(sortedSchedules, key = { it.id }) { schedule ->
            ScheduleCard(
                schedule = schedule,
                onDelete = { onDeleteSchedule(schedule) }
            )
        }
    }
}
