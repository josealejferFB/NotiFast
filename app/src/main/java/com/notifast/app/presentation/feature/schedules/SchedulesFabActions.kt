package com.notifast.app.presentation.feature.schedules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.Color
import com.notifast.app.presentation.atoms.FabAction

fun buildSchedulesFabActions(
    scheduleLabel: String,
    evaluationLabel: String,
    scheduleColor: Color,
    evaluationColor: Color,
    onAddScheduleClick: () -> Unit = {},
    onAddEvaluationClick: () -> Unit = {}
): List<FabAction> = listOf(
    FabAction(
        label = scheduleLabel,
        icon = Icons.Default.Schedule,
        onClick = onAddScheduleClick,
        contentDescription = scheduleLabel,
        color = scheduleColor
    ),
    FabAction(
        label = evaluationLabel,
        icon = Icons.AutoMirrored.Filled.Assignment,
        onClick = onAddEvaluationClick,
        contentDescription = evaluationLabel,
        color = evaluationColor
    )
)
