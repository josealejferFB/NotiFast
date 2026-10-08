package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.NoteBlock

@Composable
fun EditableListBlock(
    block: NoteBlock.BulletList,
    onItemTextChange: (Int, String) -> Unit,
    onItemAdded: () -> Unit,
    onItemRemoved: (Int) -> Unit,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        block.items.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                val prefix = if (block.isOrdered) "${index + 1}." else "•"
                Text(
                    text = prefix,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .width(24.dp)
                        .padding(top = 2.dp)
                )
                BlockTextField(
                    value = item.text,
                    onValueChange = { onItemTextChange(index, it) },
                    onFocus = onFocus,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    isFocused = false,
                    placeholder = "",
                    modifier = Modifier.weight(1f)
                )
                if (block.items.size > 1) {
                    IconButton(
                        onClick = { onItemRemoved(index) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.editor_list_item_remove_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        TextButton(onClick = onItemAdded) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.editor_add_item),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
