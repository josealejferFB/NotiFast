package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.StrikethroughS
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.TextStyle

@Composable
fun TextFormatToolbar(
    visible: Boolean,
    activeStyles: Set<TextStyle>,
    onToggleStyle: (TextStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = visible, modifier = modifier) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                FormatButton(Icons.Default.FormatBold, stringResource(R.string.editor_bold), TextStyle.BOLD in activeStyles) {
                    onToggleStyle(TextStyle.BOLD)
                }
                FormatButton(Icons.Default.FormatItalic, stringResource(R.string.editor_italic), TextStyle.ITALIC in activeStyles) {
                    onToggleStyle(TextStyle.ITALIC)
                }
                FormatButton(Icons.Default.FormatUnderlined, stringResource(R.string.editor_underline), TextStyle.UNDERLINE in activeStyles) {
                    onToggleStyle(TextStyle.UNDERLINE)
                }
                FormatButton(Icons.Default.StrikethroughS, stringResource(R.string.editor_strikethrough), TextStyle.STRIKETHROUGH in activeStyles) {
                    onToggleStyle(TextStyle.STRIKETHROUGH)
                }
                FormatButton(Icons.Default.Highlight, stringResource(R.string.editor_highlight), TextStyle.HIGHLIGHT in activeStyles) {
                    onToggleStyle(TextStyle.HIGHLIGHT)
                }
            }
        }
    }
}

@Composable
private fun FormatButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isActive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.padding(6.dp),
                tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
