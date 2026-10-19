package com.notifast.app.presentation.feature.studyhub.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.theme.isDark

@Composable
fun CoverTemplatesSection(
    drafts: List<CoverDraft>,
    onSelectTemplate: (CoverTemplateType) -> Unit,
    onEditDraft: (CoverDraft) -> Unit,
    onDeleteDraft: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.isDark

    val unergColor = if (isDark) Color(0xFF4DB6AC) else Color(0xFF006C50)
    val apaColor = if (isDark) Color(0xFFB39DDB) else Color(0xFF6750A4)
    val ieeeColor = if (isDark) Color(0xFF4DD0E1) else Color(0xFF00838F)
    val instColor = if (isDark) Color(0xFF64B5F6) else Color(0xFF0277BD)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                title = stringResource(R.string.study_template_unerg_title),
                description = stringResource(R.string.study_template_unerg_desc),
                icon = Icons.Outlined.School,
                badgeColor = unergColor,
                academicBadge = stringResource(R.string.study_template_badge_unerg),
                onClick = { onSelectTemplate(CoverTemplateType.UNERG_DEGREE) }
            )
        }

        item {
            TemplateCatalogCard(
                title = stringResource(R.string.study_template_apa7_title),
                description = stringResource(R.string.study_template_apa7_desc),
                icon = Icons.Outlined.Description,
                badgeColor = apaColor,
                academicBadge = stringResource(R.string.study_template_badge_apa7),
                onClick = { onSelectTemplate(CoverTemplateType.APA_7) }
            )
        }

        item {
            TemplateCatalogCard(
                title = stringResource(R.string.study_template_ieee_title),
                description = stringResource(R.string.study_template_ieee_desc),
                icon = Icons.Outlined.AutoAwesome,
                badgeColor = ieeeColor,
                academicBadge = stringResource(R.string.study_template_badge_ieee),
                onClick = { onSelectTemplate(CoverTemplateType.IEEE) }
            )
        }

        item {
            TemplateCatalogCard(
                title = stringResource(R.string.study_template_institutional_title),
                description = stringResource(R.string.study_template_institutional_desc),
                icon = Icons.Outlined.AccountBalance,
                badgeColor = instColor,
                academicBadge = stringResource(R.string.study_template_badge_institutional),
                onClick = { onSelectTemplate(CoverTemplateType.INSTITUTIONAL) }
            )
        }

        if (drafts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
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
private fun DraftItemCard(
    draft: CoverDraft,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val templateName = when (draft.templateType) {
        CoverTemplateType.APA_7 -> "APA 7"
        CoverTemplateType.IEEE -> "IEEE"
        CoverTemplateType.INSTITUTIONAL -> "Tesis"
        CoverTemplateType.UNERG_DEGREE -> stringResource(R.string.study_template_unerg_tag)
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
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                    if (draft.subjectName.isNotBlank()) {
                        Text(
                            text = "• ${draft.subjectName}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (draft.formattedDate.isNotBlank()) {
                        Text(
                            text = "• ${draft.formattedDate}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.study_doc_action_delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}
