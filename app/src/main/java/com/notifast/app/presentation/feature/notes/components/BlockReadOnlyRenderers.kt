package com.notifast.app.presentation.feature.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.notifast.app.data.model.NoteBlock

@Composable
fun BlockReadOnlyRenderer(block: NoteBlock, modifier: Modifier = Modifier) {
    when (block) {
        is NoteBlock.Heading -> {
            val style = when (block.level) {
                1 -> MaterialTheme.typography.headlineSmall
                2 -> MaterialTheme.typography.titleLarge
                else -> MaterialTheme.typography.titleMedium
            }
            Text(
                text = block.text,
                style = style.copy(fontWeight = FontWeight.Bold),
                modifier = modifier.fillMaxWidth()
            )
        }
        is NoteBlock.Paragraph -> Text(
            text = block.text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = modifier.fillMaxWidth()
        )
        is NoteBlock.Quote -> Row(modifier = modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
            )
            Text(
                text = block.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        is NoteBlock.BulletList -> Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            block.items.forEach { item ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("•", modifier = Modifier.padding(end = 8.dp, top = 2.dp))
                    Text(item.text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        is NoteBlock.Checklist -> Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            block.items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = item.isChecked, onCheckedChange = null)
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
                        ),
                        color = if (item.isChecked) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
        is NoteBlock.Image -> com.notifast.app.presentation.feature.noteeditor.components.AnnotatedImage(
            path = block.path,
            annotationJson = block.annotationJson,
            contentDescription = block.caption,
            modifier = modifier.fillMaxWidth()
        )
        is NoteBlock.MathBlock -> Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            com.notifast.app.presentation.feature.noteeditor.math.MathRenderer(
                source = block.source
            )
        }
        is NoteBlock.Divider -> HorizontalDivider(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
