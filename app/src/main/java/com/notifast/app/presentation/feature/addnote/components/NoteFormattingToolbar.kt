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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby

data class FormatAction(
    val label: String,
    val prefix: String,
    val suffix: String = "",
    val isMonospace: Boolean = false
)

@Composable
fun NoteFormattingToolbar(
    onInsertFormatting: (prefix: String, suffix: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        FormatAction("H1", "# "),
        FormatAction("H2", "## "),
        FormatAction("H3", "### "),
        FormatAction("B", "**", "**"),
        FormatAction("I", "*", "*"),
        FormatAction("• Lista", "* "),
        FormatAction("☑ Tarea", "- [ ] "),
        FormatAction("“ Cita", "> "),
        FormatAction("`Code`", "`", "`", isMonospace = true)
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
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
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    onClick = { onInsertFormatting(action.prefix, action.suffix) }
                ) {
                    Text(
                        text = action.label,
                        fontWeight = FontWeight.Bold,
                        fontFamily = if (action.isMonospace) FontFamily.Monospace else FontFamily.Default,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
