package com.notifast.app.presentation.feature.schedules

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SchedulesFabNormalizationTest {

    @Test
    fun `fab actions are configured in correct order with schedule top and evaluation bottom`() {
        var scheduleClicked = false
        var evaluationClicked = false

        val scheduleColor = Color.Blue
        val evaluationColor = Color.Red

        val actions = buildSchedulesFabActions(
            scheduleLabel = "Horario de Clase",
            evaluationLabel = "Evaluación o Entrega",
            scheduleColor = scheduleColor,
            evaluationColor = evaluationColor,
            onAddScheduleClick = { scheduleClicked = true },
            onAddEvaluationClick = { evaluationClicked = true }
        )

        assertEquals("Debe contener exactamente 2 acciones", 2, actions.size)

        // Acción 1 (Superior / Arriba): Nuevo Horario
        val scheduleAction = actions[0]
        assertEquals("Horario de Clase", scheduleAction.label)
        assertEquals("Horario de Clase", scheduleAction.contentDescription)
        assertEquals(Icons.Default.Schedule, scheduleAction.icon)
        assertEquals(scheduleColor, scheduleAction.color)
        scheduleAction.onClick()
        assertTrue("Callback de horario debe ejecutarse", scheduleClicked)

        // Acción 2 (Inferior / Abajo): Nueva Evaluación
        val evaluationAction = actions[1]
        assertEquals("Evaluación o Entrega", evaluationAction.label)
        assertEquals("Evaluación o Entrega", evaluationAction.contentDescription)
        assertEquals(Icons.AutoMirrored.Filled.Assignment, evaluationAction.icon)
        assertEquals(evaluationColor, evaluationAction.color)
        evaluationAction.onClick()
        assertTrue("Callback de evaluación debe ejecutarse", evaluationClicked)
    }
}
