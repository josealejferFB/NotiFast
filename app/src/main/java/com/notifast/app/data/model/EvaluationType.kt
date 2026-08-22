package com.notifast.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.PresentToAll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby

enum class EvaluationType(
    val label: String,
    val color: Color,
    val icon: ImageVector,
    val showWeight: Boolean,
    val showGrade: Boolean
) {
    EXAM(
        label = "Examen",
        color = Ruby,
        icon = Icons.Default.Quiz,
        showWeight = true,
        showGrade = true
    ),
    PRESENTATION(
        label = "Exposición",
        color = Color(0xFFFF9800),
        icon = Icons.Default.PresentToAll,
        showWeight = true,
        showGrade = true
    ),
    PROJECT(
        label = "Entrega de Trabajo",
        color = Aqua,
        icon = Icons.Default.Assignment,
        showWeight = true,
        showGrade = true
    ),
    HOMEWORK(
        label = "Tarea",
        color = Color(0xFF7E57C2),
        icon = Icons.Default.Task,
        showWeight = false,
        showGrade = false
    );

    companion object {
        fun fromName(name: String): EvaluationType {
            return entries.find { it.name == name } ?: EXAM
        }
    }
}
