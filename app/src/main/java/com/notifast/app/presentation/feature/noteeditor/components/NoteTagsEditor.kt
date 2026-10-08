package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.notifast.app.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteTagsEditor(
    selectedTags: List<String>,
    availableTags: List<String>,
    onTagToggled: (String) -> Unit,
    onCustomTagAdded: (String) -> Unit,
    onCustomTagRemoved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customTag by remember { mutableStateOf("") }
    val suggestions = listOf(
        stringResource(R.string.notes_tag_suggestion_summary),
        stringResource(R.string.notes_tag_suggestion_exam),
        stringResource(R.string.notes_tag_suggestion_homework),
        stringResource(R.string.notes_tag_suggestion_formula),
        stringResource(R.string.notes_tag_suggestion_important)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.notes_tags_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (selectedTags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedTags.forEach { tag ->
                    InputChip(
                        selected = true,
                        onClick = { onCustomTagRemoved(tag) },
                        label = { Text("#$tag") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.notes_remove_tag_cd),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }

        val unselected = (suggestions + availableTags)
            .distinct()
            .filter { candidate -> selectedTags.none { it.equals(candidate, ignoreCase = true) } }
        if (unselected.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                unselected.forEach { tag ->
                    FilterChip(
                        selected = false,
                        onClick = { onTagToggled(tag) },
                        label = { Text(if (tag.startsWith("#")) tag else "#$tag") }
                    )
                }
            }
        }

        OutlinedTextField(
            value = customTag,
            onValueChange = { customTag = it },
            label = { Text(stringResource(R.string.notes_add_tag_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (customTag.isNotBlank()) {
                        onCustomTagAdded(customTag.trim().removePrefix("#"))
                        customTag = ""
                    }
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
