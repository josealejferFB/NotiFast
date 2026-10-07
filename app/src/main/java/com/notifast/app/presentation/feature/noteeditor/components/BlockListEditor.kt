package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notifast.app.data.model.NoteBlock

@Composable
fun BlockListEditor(
    blocks: List<NoteBlock>,
    focusedBlockId: String?,
    onBlockTextChanged: (String, String) -> Unit,
    onBlockFocused: (String?) -> Unit,
    onCheckToggle: (String, Int) -> Unit,
    onCheckTextChange: (String, Int, String) -> Unit,
    onImageClick: (String) -> Unit,
    onBlockDeleted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        blocks.forEach { block ->
            BlockItemContainer(
                block = block,
                isFocused = block.id == focusedBlockId,
                onTextChange = { text -> onBlockTextChanged(block.id, text) },
                onFocus = { onBlockFocused(block.id) },
                onCheckToggle = { index -> onCheckToggle(block.id, index) },
                onCheckTextChange = { index, text -> onCheckTextChange(block.id, index, text) },
                onImageClick = { onImageClick(block.id) },
                onDelete = { onBlockDeleted(block.id) }
            )
        }
    }
}

@Composable
private fun BlockItemContainer(
    block: NoteBlock,
    isFocused: Boolean,
    onTextChange: (String) -> Unit,
    onFocus: () -> Unit,
    onCheckToggle: (Int) -> Unit,
    onCheckTextChange: (Int, String) -> Unit,
    onImageClick: () -> Unit,
    onDelete: () -> Unit
) {
    BlockRenderer(
        block = block,
        isFocused = isFocused,
        onTextChange = onTextChange,
        onFocus = onFocus,
        onCheckToggle = onCheckToggle,
        onCheckTextChange = onCheckTextChange,
        onImageClick = onImageClick,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
