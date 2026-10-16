package com.notifast.app.presentation.feature.noteeditor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import com.notifast.app.presentation.atoms.NotiFastChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R

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
    var showNewTagInput by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val suggestions = listOf(
        stringResource(R.string.notes_tag_suggestion_summary),
        stringResource(R.string.notes_tag_suggestion_exam),
        stringResource(R.string.notes_tag_suggestion_homework),
        stringResource(R.string.notes_tag_suggestion_formula),
        stringResource(R.string.notes_tag_suggestion_important)
    )

    val commitCustomTag = {
        val cleaned = customTag.trim().removePrefix("#")
        if (cleaned.isNotBlank()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onCustomTagAdded(cleaned)
            customTag = ""
            showNewTagInput = false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.notes_tags_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Selected tags chips
        if (selectedTags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedTags.forEach { tag ->
                    NotiFastChip(
                        label = "#$tag",
                        selected = true,
                        onClick = { onCustomTagRemoved(tag) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.notes_remove_tag_cd),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        // Suggestions + Add Tag Button
        val unselected = (suggestions + availableTags)
            .distinct()
            .filter { candidate -> selectedTags.none { it.equals(candidate, ignoreCase = true) } }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!showNewTagInput) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showNewTagInput = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.notes_btn_new_tag),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            unselected.forEach { tag ->
                NotiFastChip(
                    label = if (tag.startsWith("#")) tag else "#$tag",
                    selected = false,
                    onClick = { onTagToggled(tag) }
                )
            }
        }

        // Modern animated tag input capsule
        TagInputCapsule(
            visible = showNewTagInput,
            value = customTag,
            onValueChange = { customTag = it },
            onConfirm = { commitCustomTag() },
            onCancel = {
                customTag = ""
                showNewTagInput = false
            }
        )
    }
}
