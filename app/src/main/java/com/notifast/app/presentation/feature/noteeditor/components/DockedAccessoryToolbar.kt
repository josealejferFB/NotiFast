package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.notifast.app.presentation.feature.noteeditor.util.FormatStyle

data class MarkdownToolbarItem(
    val style: FormatStyle? = null,
    val icon: ImageVector? = null,
    val textBadge: String? = null,
    val contentDescriptionRes: Int,
    val prefix: String = "",
    val suffix: String = "",
    val customAction: (() -> Unit)? = null
)

@Composable
fun DockedAccessoryToolbar(
    onInsertFormatting: (prefix: String, suffix: String) -> Unit,
    onAttachPhotoClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeStyles: Set<FormatStyle> = emptySet(),
    onToggleFormat: ((FormatStyle) -> Unit)? = null
) {
    @Composable
    fun ItemBtn(item: MarkdownToolbarItem) {
        ToolbarItemButton(
            item = item,
            activeStyles = activeStyles,
            onToggleFormat = onToggleFormat,
            onInsertFormatting = onInsertFormatting
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth().height(52.dp),
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
            ItemBtn(MarkdownToolbarItem(FormatStyle.BOLD, Icons.Default.FormatBold, null, R.string.editor_format_bold, "**", "**"))
            ItemBtn(MarkdownToolbarItem(FormatStyle.ITALIC, Icons.Default.FormatItalic, null, R.string.editor_format_italic, "*", "*"))
            ItemBtn(MarkdownToolbarItem(FormatStyle.QUOTE, Icons.Default.FormatQuote, null, R.string.editor_format_quote, "> "))

            ToolbarMicroDivider()

            ItemBtn(MarkdownToolbarItem(FormatStyle.H1, null, "H1", R.string.editor_format_h1, "# "))
            ItemBtn(MarkdownToolbarItem(FormatStyle.H2, null, "H2", R.string.editor_format_h2, "## "))

            ToolbarMicroDivider()

            ItemBtn(MarkdownToolbarItem(FormatStyle.CHECKBOX, Icons.Default.CheckBox, null, R.string.editor_format_checkbox, "- [ ] "))
            ItemBtn(MarkdownToolbarItem(FormatStyle.BULLET, Icons.AutoMirrored.Filled.FormatListBulleted, null, R.string.editor_format_bullet, "- "))

            ToolbarMicroDivider()

            ItemBtn(MarkdownToolbarItem(FormatStyle.LATEX, Icons.Default.Functions, null, R.string.editor_format_latex, "$$ ", " $$"))
            ItemBtn(MarkdownToolbarItem(FormatStyle.DIVIDER, Icons.Default.HorizontalRule, null, R.string.editor_format_divider, "\n---\n"))
            ItemBtn(MarkdownToolbarItem(null, Icons.Default.CameraAlt, null, R.string.editor_format_photo, customAction = onAttachPhotoClick))
        }
    }
}

@Composable
private fun ToolbarMicroDivider(modifier: Modifier = Modifier) {
    VerticalDivider(
        modifier = modifier.height(20.dp).padding(horizontal = 2.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun ToolbarItemButton(
    item: MarkdownToolbarItem,
    activeStyles: Set<FormatStyle>,
    onToggleFormat: ((FormatStyle) -> Unit)?,
    onInsertFormatting: (prefix: String, suffix: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isActive = item.style != null && item.style in activeStyles
    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
    }
    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .clickable(
                onClickLabel = stringResource(item.contentDescriptionRes),
                role = Role.Button
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (item.customAction != null) {
                    item.customAction.invoke()
                } else if (onToggleFormat != null && item.style != null) {
                    onToggleFormat(item.style)
                } else {
                    onInsertFormatting(item.prefix, item.suffix)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (item.icon != null) {
            Icon(
                imageVector = item.icon,
                contentDescription = stringResource(item.contentDescriptionRes),
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        } else if (item.textBadge != null) {
            Text(
                text = item.textBadge,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
