package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R

@Composable
fun DockedAccessoryToolbar(
    onInsertFormatting: (prefix: String, suffix: String) -> Unit,
    onAttachPhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Categoría: Texto
            ToolbarItemButton(
                icon = Icons.Default.FormatBold,
                contentDescription = stringResource(R.string.editor_format_bold),
                onClick = { onInsertFormatting("**", "**") }
            )
            ToolbarItemButton(
                icon = Icons.Default.FormatItalic,
                contentDescription = stringResource(R.string.editor_format_italic),
                onClick = { onInsertFormatting("*", "*") }
            )
            ToolbarItemButton(
                icon = Icons.Default.FormatQuote,
                contentDescription = stringResource(R.string.editor_format_quote),
                onClick = { onInsertFormatting("> ", "") }
            )

            ToolbarMicroDivider()

            // Categoría: Estructura
            ToolbarItemButton(
                textBadge = "H1",
                contentDescription = stringResource(R.string.editor_format_h1),
                onClick = { onInsertFormatting("# ", "") }
            )
            ToolbarItemButton(
                textBadge = "H2",
                contentDescription = stringResource(R.string.editor_format_h2),
                onClick = { onInsertFormatting("## ", "") }
            )

            ToolbarMicroDivider()

            // Categoría: Listas
            ToolbarItemButton(
                icon = Icons.Default.CheckBox,
                contentDescription = stringResource(R.string.editor_format_checkbox),
                onClick = { onInsertFormatting("- [ ] ", "") }
            )
            ToolbarItemButton(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                contentDescription = stringResource(R.string.editor_format_bullet),
                onClick = { onInsertFormatting("- ", "") }
            )

            ToolbarMicroDivider()

            // Categoría: Insertar
            ToolbarItemButton(
                icon = Icons.Default.Functions,
                contentDescription = stringResource(R.string.editor_format_latex),
                onClick = { onInsertFormatting("$$ ", " $$") }
            )
            ToolbarItemButton(
                icon = Icons.Default.HorizontalRule,
                contentDescription = stringResource(R.string.editor_format_divider),
                onClick = { onInsertFormatting("\n---\n", "") }
            )
            ToolbarItemButton(
                icon = Icons.Default.CameraAlt,
                contentDescription = stringResource(R.string.editor_format_photo),
                onClick = onAttachPhotoClick
            )
        }
    }
}

@Composable
private fun ToolbarMicroDivider(modifier: Modifier = Modifier) {
    VerticalDivider(
        modifier = modifier
            .height(20.dp)
            .padding(horizontal = 2.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun ToolbarItemButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    textBadge: String? = null
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                onClickLabel = contentDescription,
                role = Role.Button
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        } else if (textBadge != null) {
            Text(
                text = textBadge,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
