package com.notifast.app.presentation.feature.schedules

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.data.model.Schedule
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.schedules.components.ScheduleDayTabs
import com.notifast.app.presentation.feature.schedules.components.ScheduleTimeBlockList
import com.notifast.app.presentation.theme.color.Ruby

@Composable
fun SchedulesScreen(
    viewModel: SchedulesViewModel = hiltViewModel(),
    onAddScheduleClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var scheduleToDelete by remember { mutableStateOf<Schedule?>(null) }
    val context = LocalContext.current

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
            viewModel.onEvent(SchedulesEvent.ClearError)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NotiFastTopBar(
                    type = TopBarType.Simple(
                        title = stringResource(R.string.schedules_title)
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddScheduleClick,
                    containerColor = Ruby,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.schedules_add_title)
                    )
                }
            }
        ) { innerPadding ->
            if (uiState.isLoading) {
                NotiFastCircularLoading()
                return@Scaffold
            }

            val classCountsByDay = remember(uiState.groupedSchedules) {
                uiState.groupedSchedules.mapValues { it.value.size }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ScheduleDayTabs(
                    selectedDay = uiState.selectedDay,
                    classCountsByDay = classCountsByDay,
                    onDaySelected = { viewModel.onEvent(SchedulesEvent.SelectDay(it)) }
                )

                val daySchedules = uiState.groupedSchedules[uiState.selectedDay] ?: emptyList()

                AnimatedContent(
                    targetState = uiState.selectedDay,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "DaySchedulesTransition",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) { targetDay ->
                    val schedulesForTarget = uiState.groupedSchedules[targetDay] ?: emptyList()
                    ScheduleTimeBlockList(
                        schedules = schedulesForTarget,
                        onDeleteSchedule = { scheduleToDelete = it }
                    )
                }
            }
        }

        scheduleToDelete?.let { schedule ->
            NotiFastDialog(
                title = stringResource(R.string.schedules_delete_title),
                message = stringResource(
                    R.string.schedules_delete_message,
                    schedule.subjectName,
                    schedule.startTime,
                    schedule.endTime
                ),
                confirmText = stringResource(R.string.btn_delete),
                dismissText = stringResource(R.string.btn_cancel),
                confirmColor = Ruby,
                confirmWeight = FontWeight.Bold,
                onConfirm = {
                    viewModel.onEvent(SchedulesEvent.DeleteSchedule(schedule))
                    scheduleToDelete = null
                },
                onDismiss = { scheduleToDelete = null }
            )
        }
    }
}
