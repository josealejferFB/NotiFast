package com.notifast.app.presentation.feature.schedules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.Color
import com.notifast.app.presentation.atoms.FabAction

fun buildSchedulesFabActions(
    scheduleLabel: String,
    evaluationLabel: String,
    scheduleColor: Color,
    evaluationColor: Color,
    onAddScheduleClick: () -> Unit = {},
    onAddEvaluationClick: () -> Unit = {},
    unergImportLabel: String = "",
    unergImportColor: Color = scheduleColor,
    onUnergImportClick: () -> Unit = {}
): List<FabAction> {
    val actions = mutableListOf(
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

    if (unergImportLabel.isNotBlank()) {
        actions.add(
            FabAction(
                label = unergImportLabel,
                icon = Icons.Default.PictureAsPdf,
                onClick = onUnergImportClick,
                contentDescription = unergImportLabel,
                color = unergImportColor
            )
        )
    }

    return actions
}
