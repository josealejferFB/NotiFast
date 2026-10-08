package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.feature.noteeditor.BlockType

@Composable
fun BlockInsertMenu(
    onInsert: (BlockType) -> Unit,
    onAddImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val actions = listOf(
        Triple(BlockType.PARAGRAPH, Icons.Default.FormatBold, stringResource(R.string.editor_block_text)),
        Triple(BlockType.HEADING, Icons.Default.Title, stringResource(R.string.editor_block_heading)),
        Triple(BlockType.BULLET_LIST, Icons.Default.FormatListBulleted, stringResource(R.string.editor_block_list)),
        Triple(BlockType.ORDERED_LIST, Icons.Default.FormatListNumbered, stringResource(R.string.editor_block_ordered_list)),
        Triple(BlockType.CHECKLIST, Icons.Default.CheckBox, stringResource(R.string.editor_block_checklist)),
        Triple(BlockType.QUOTE, Icons.Default.FormatQuote, stringResource(R.string.editor_block_quote)),
        Triple(BlockType.MATH, Icons.Default.Functions, stringResource(R.string.editor_block_math)),
        Triple(BlockType.DIVIDER, Icons.Default.HorizontalRule, stringResource(R.string.editor_block_divider))
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
            actions.forEach { (type, icon, label) ->
                InsertChip(icon = icon, label = label) {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInsert(type)
                }
            }
            InsertChip(icon = Icons.Default.Image, label = stringResource(R.string.editor_block_photo)) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onAddImage()
            }
        }
    }
}

@Composable
private fun InsertChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.5.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.padding(end = 2.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            androidx.compose.material3.Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
