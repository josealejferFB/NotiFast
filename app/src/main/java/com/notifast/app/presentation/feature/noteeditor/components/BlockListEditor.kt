package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                BlockRenderer(
                    block = block,
                    isFocused = block.id == focusedBlockId,
                    onTextChange = { text -> onBlockTextChanged(block.id, text) },
                    onFocus = { onBlockFocused(block.id) },
                    onCheckToggle = { index -> onCheckToggle(block.id, index) },
                    onCheckTextChange = { index, text -> onCheckTextChange(block.id, index, text) },
                    onImageClick = { onImageClick(block.id) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 2.dp)
                )
                IconButton(onClick = { onBlockDeleted(block.id) }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Eliminar bloque",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
