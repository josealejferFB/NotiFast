package com.notifast.app.presentation.feature.studyhub.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.study.OpenXmlDocxGenerator
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.presentation.atoms.NotiFastCard

@Composable
fun CoverAuthorsEditorCard(
    draft: CoverDraft,
    onUpdateDraft: ((CoverDraft) -> CoverDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    // Local list of (name, ci) remembered by draft.id so spaces are preserved while typing
    var authorItems by remember(draft.id) {
        mutableStateOf(
            if (draft.authors.isEmpty()) {
                listOf("" to "")
            } else {
                draft.authors.map { OpenXmlDocxGenerator.parseAuthor(it) }
            }
        )
    }

    fun syncAuthors(items: List<Pair<String, String>>) {
        authorItems = items
        val formattedList = items.map { (name, ci) ->
            val cleanCi = ci.trim()
            if (cleanCi.isNotBlank()) "$name (C.I.: $cleanCi)" else name
        }
        onUpdateDraft { it.copy(authors = formattedList) }
    }

    NotiFastCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 14.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.study_authors_section_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            authorItems.forEachIndexed { index, (name, ci) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { newName ->
                            val updated = authorItems.toMutableList()
                            updated[index] = newName to ci
                            syncAuthors(updated)
                        },
                        label = { Text(stringResource(R.string.study_author_name_label)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ci,
                        onValueChange = { newCi ->
                            val updated = authorItems.toMutableList()
                            updated[index] = name to newCi
                            syncAuthors(updated)
                        },
                        label = { Text(stringResource(R.string.study_author_ci_label)) },
                        placeholder = { Text(stringResource(R.string.study_author_ci_hint)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(135.dp),
                        singleLine = true
                    )

                    if (authorItems.size > 1) {
                        IconButton(
                            onClick = {
                                val updated = authorItems.toMutableList()
                                updated.removeAt(index)
                                syncAuthors(updated)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.RemoveCircleOutline,
                                contentDescription = stringResource(R.string.study_author_remove),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    val updated = authorItems + listOf("" to "")
                    syncAuthors(updated)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.study_author_add_btn),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}
