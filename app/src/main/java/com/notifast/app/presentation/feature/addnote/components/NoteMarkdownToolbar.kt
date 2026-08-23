package com.notifast.app.presentation.feature.addnote.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MarkdownFormatAction(
    val label: String,
    val prefix: String,
    val suffix: String = "",
    val isMonospace: Boolean = false
)

@Composable
fun NoteMarkdownToolbar(
    onInsertFormatting: (prefix: String, suffix: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val actions = listOf(
        MarkdownFormatAction("H1", "# "),
        MarkdownFormatAction("H2", "## "),
        MarkdownFormatAction("H3", "### "),
        MarkdownFormatAction("B", "**", "**"),
        MarkdownFormatAction("I", "*", "*"),
        MarkdownFormatAction("• Lista", "- "),
        MarkdownFormatAction("☑ Tarea", "- [ ] "),
        MarkdownFormatAction("∑ LaTeX", "$", "$"),
        MarkdownFormatAction("“ Cita", "> "),
        MarkdownFormatAction("`Code`", "`", "`", isMonospace = true)
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions.forEach { action ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 0.5.dp,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onInsertFormatting(action.prefix, action.suffix)
                    }
                ) {
                    Text(
                        text = action.label,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = if (action.isMonospace) FontFamily.Monospace else FontFamily.Default,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
