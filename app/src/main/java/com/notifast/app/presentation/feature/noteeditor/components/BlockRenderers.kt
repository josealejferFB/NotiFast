package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.TextSpan

@Composable
fun BlockRenderer(
    block: NoteBlock,
    isFocused: Boolean,
    onTextChange: (String) -> Unit,
    onFocus: () -> Unit,
    onCheckToggle: (Int) -> Unit,
    onCheckTextChange: (Int, String) -> Unit,
    onImageClick: () -> Unit,
    onSpansChanged: (List<TextSpan>) -> Unit = {},
    onSelectionChange: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    when (block) {
        is NoteBlock.Heading -> HeadingBlock(block, isFocused, onTextChange, onFocus, onSpansChanged, onSelectionChange, modifier)
        is NoteBlock.Paragraph -> ParagraphBlock(block, isFocused, onTextChange, onFocus, onSpansChanged, onSelectionChange, modifier)
        is NoteBlock.Quote -> QuoteBlock(block, isFocused, onTextChange, onFocus, onSpansChanged, onSelectionChange, modifier)
        is NoteBlock.BulletList -> BulletListBlock(block, modifier)
        is NoteBlock.Checklist -> ChecklistBlock(block, onCheckToggle, onCheckTextChange, modifier)
        is NoteBlock.Image -> ImageBlock(block, onImageClick, modifier)
        is NoteBlock.MathBlock -> MathBlockView(
            block = block,
            isFocused = isFocused,
            onSourceChange = onTextChange,
            onFocus = onFocus,
            modifier = modifier
        )
        is NoteBlock.Divider -> DividerBlock(modifier)
    }
}

@Composable
private fun HeadingBlock(
    block: NoteBlock.Heading,
    isFocused: Boolean,
    onTextChange: (String) -> Unit,
    onFocus: () -> Unit,
    onSpansChanged: (List<TextSpan>) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val style = when (block.level) {
        1 -> MaterialTheme.typography.headlineSmall
        2 -> MaterialTheme.typography.titleLarge
        else -> MaterialTheme.typography.titleMedium
    }
    RichBlockTextField(
        value = block.text,
        spans = block.spans,
        onValueChange = onTextChange,
        onSpansChange = onSpansChanged,
        onFocus = onFocus,
        onSelectionChange = onSelectionChange,
        textStyle = style.copy(fontWeight = FontWeight.Bold),
        isFocused = isFocused,
        modifier = modifier
    )
}

@Composable
private fun ParagraphBlock(
    block: NoteBlock.Paragraph,
    isFocused: Boolean,
    onTextChange: (String) -> Unit,
    onFocus: () -> Unit,
    onSpansChanged: (List<TextSpan>) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    RichBlockTextField(
        value = block.text,
        spans = block.spans,
        onValueChange = onTextChange,
        onSpansChange = onSpansChanged,
        onFocus = onFocus,
        onSelectionChange = onSelectionChange,
        textStyle = MaterialTheme.typography.bodyLarge,
        isFocused = isFocused,
        modifier = modifier
    )
}

@Composable
private fun QuoteBlock(
    block: NoteBlock.Quote,
    isFocused: Boolean,
    onTextChange: (String) -> Unit,
    onFocus: () -> Unit,
    onSpansChanged: (List<TextSpan>) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(24.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        RichBlockTextField(
            value = block.text,
            spans = block.spans,
            onValueChange = onTextChange,
            onSpansChange = onSpansChanged,
            onFocus = onFocus,
            onSelectionChange = onSelectionChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            isFocused = isFocused,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun BulletListBlock(block: NoteBlock.BulletList, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        block.items.forEach { item ->
            Row(verticalAlignment = Alignment.Top) {
                Text("•", modifier = Modifier.padding(end = 8.dp, top = 2.dp))
                Text(item.text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ChecklistBlock(
    block: NoteBlock.Checklist,
    onCheckToggle: (Int) -> Unit,
    onCheckTextChange: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        block.items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = item.isChecked, onCheckedChange = { onCheckToggle(index) })
                BlockTextField(
                    value = item.text,
                    onValueChange = { onCheckTextChange(index, it) },
                    onFocus = {},
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (item.isChecked) {
                            androidx.compose.ui.text.style.TextDecoration.LineThrough
                        } else null
                    ),
                    isFocused = false,
                    placeholder = "Tarea…"
                )
            }
        }
    }
}

@Composable
private fun ImageBlock(block: NoteBlock.Image, onImageClick: () -> Unit, modifier: Modifier = Modifier) {
    AnnotatedImage(
        path = block.path,
        annotationJson = block.annotationJson,
        contentDescription = block.caption,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onImageClick)
    )
}

@Composable
private fun MathBlockView(
    block: NoteBlock.MathBlock,
    isFocused: Boolean,
    onSourceChange: (String) -> Unit,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Functions, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            if (isFocused) {
                BlockTextField(
                    value = block.source,
                    onValueChange = onSourceChange,
                    onFocus = onFocus,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    isFocused = true,
                    placeholder = "frac{a}{b}"
                )
            } else {
                com.notifast.app.presentation.feature.noteeditor.math.MathRenderer(source = block.source)
            }
        }
        if (isFocused) {
            com.notifast.app.presentation.feature.noteeditor.math.SymbolKeyboard(
                onInsert = { insert -> onSourceChange(block.source + insert) }
            )
        }
    }
}

@Composable
private fun DividerBlock(modifier: Modifier = Modifier) {
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
