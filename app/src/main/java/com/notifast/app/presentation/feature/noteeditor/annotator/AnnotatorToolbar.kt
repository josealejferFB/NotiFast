package com.notifast.app.presentation.feature.noteeditor.annotator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.DrawTool

@Composable
fun AnnotatorToolbar(
    currentTool: DrawTool,
    currentColor: Int,
    currentWidth: Float,
    canUndo: Boolean,
    canRedo: Boolean,
    onToolSelected: (DrawTool) -> Unit,
    onColorSelected: (Int) -> Unit,
    onWidthSelected: (Float) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        -0x1000000, // black
        -0x10000,   // red
        -0xff0001,  // blue
        -0xff0100,  // green
        -0x100,     // yellow
        -0xffff01   // magenta
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.note_annotate_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Row {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.btn_cancel))
                    }
                    IconButton(onClick = onSave) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.btn_save))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolChip(
                    icon = Icons.Default.Edit,
                    label = stringResource(R.string.note_annotate_pen),
                    selected = currentTool == DrawTool.PEN,
                    onClick = { onToolSelected(DrawTool.PEN) }
                )
                ToolChip(
                    icon = Icons.Default.Highlight,
                    label = stringResource(R.string.note_annotate_highlighter),
                    selected = currentTool == DrawTool.HIGHLIGHTER,
                    onClick = { onToolSelected(DrawTool.HIGHLIGHTER) }
                )
                IconButton(onClick = onUndo, enabled = canUndo) {
                    Icon(Icons.Default.Undo, contentDescription = stringResource(R.string.note_annotate_undo))
                }
                IconButton(onClick = onRedo, enabled = canRedo) {
                    Icon(Icons.Default.Redo, contentDescription = stringResource(R.string.note_annotate_redo))
                }
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.note_annotate_clear))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                colors.forEach { colorInt ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(colorInt), CircleShape)
                            .clickable { onColorSelected(colorInt) }
                            .then(
                                if (colorInt == currentColor) {
                                    Modifier.padding(2.dp)
                                } else Modifier
                            )
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    listOf(3f, 5f, 10f).forEach { w ->
                        Box(
                            modifier = Modifier
                                .size(w.dp * 3)
                                .background(
                                    if (w == currentWidth) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    CircleShape
                                )
                                .clickable { onWidthSelected(w) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
