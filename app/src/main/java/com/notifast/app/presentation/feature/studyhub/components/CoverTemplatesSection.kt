package com.notifast.app.presentation.feature.studyhub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import com.notifast.app.presentation.atoms.NotiFastCard

@Composable
fun CoverTemplatesSection(
    drafts: List<CoverDraft>,
    onSelectTemplate: (CoverTemplateType) -> Unit,
    onEditDraft: (CoverDraft) -> Unit,
    onDeleteDraft: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.study_templates_catalog_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            TemplateCatalogCard(
                title = stringResource(R.string.study_template_apa7_title),
                description = stringResource(R.string.study_template_apa7_desc),
                icon = Icons.Default.Description,
                badgeColor = Color(0xFF6750A4),
                onClick = { onSelectTemplate(CoverTemplateType.APA_7) }
            )
        }

        item {
            TemplateCatalogCard(
                title = stringResource(R.string.study_template_ieee_title),
                description = stringResource(R.string.study_template_ieee_desc),
                icon = Icons.Default.AutoAwesome,
                badgeColor = Color(0xFF006C50),
                onClick = { onSelectTemplate(CoverTemplateType.IEEE) }
            )
        }

        item {
            TemplateCatalogCard(
                title = stringResource(R.string.study_template_institutional_title),
                description = stringResource(R.string.study_template_institutional_desc),
                icon = Icons.Default.School,
                badgeColor = Color(0xFF006399),
                onClick = { onSelectTemplate(CoverTemplateType.INSTITUTIONAL) }
            )
        }

        if (drafts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.study_templates_recent_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(drafts, key = { it.id }) { draft ->
                DraftItemCard(
                    draft = draft,
                    onClick = { onEditDraft(draft) },
                    onDelete = { onDeleteDraft(draft.id) }
                )
            }
        }
    }
}

@Composable
private fun TemplateCatalogCard(
    title: String,
    description: String,
    icon: ImageVector,
    badgeColor: Color,
    onClick: () -> Unit
) {
    NotiFastCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 14.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = badgeColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun DraftItemCard(
    draft: CoverDraft,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val templateName = when (draft.templateType) {
        CoverTemplateType.APA_7 -> "APA 7"
        CoverTemplateType.IEEE -> "IEEE"
        CoverTemplateType.INSTITUTIONAL -> "Tesis"
    }

    NotiFastCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 12.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = draft.workTitle.ifBlank { "Sin título" },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = templateName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    if (draft.subjectName.isNotBlank()) {
                        Text(
                            text = "• ${draft.subjectName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (draft.formattedDate.isNotBlank()) {
                        Text(
                            text = "• ${draft.formattedDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.study_doc_action_delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
