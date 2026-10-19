package com.notifast.app.presentation.feature.studyhub.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.domain.model.study.ApaFont
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType

@Composable
fun GenericMiniPreviewContent(
    draft: CoverDraft,
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val inkColor = Color(0xFF0F172A)
    val subInkColor = Color(0xFF475569)

    val apaFont = ApaFont.fromFontName(draft.fontName)
    val previewFont = if (apaFont.isSerif) FontFamily.Serif else FontFamily.SansSerif

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (draft.templateType == CoverTemplateType.INSTITUTIONAL && draft.institutionName.isNotBlank()) {
            Text(
                text = draft.institutionName.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    fontSize = 8.sp,
                    fontFamily = previewFont
                ),
                color = inkColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        Text(
            text = draft.workTitle.ifBlank { "Título del Trabajo" },
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = previewFont
            ),
            color = inkColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        if (draft.subtitle.isNotBlank()) {
            Text(
                text = draft.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontFamily = previewFont),
                color = subInkColor,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        if (draft.authors.isNotEmpty()) {
            Text(
                text = draft.authors.joinToString(", "),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    fontFamily = previewFont
                ),
                color = inkColor,
                textAlign = TextAlign.Center
            )
        }

        if (draft.teacherName.isNotBlank()) {
            Text(
                text = draft.teacherName,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp, fontFamily = previewFont),
                color = subInkColor,
                textAlign = TextAlign.Center
            )
        }

        if (draft.subjectName.isNotBlank()) {
            Text(
                text = draft.subjectName,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp, fontFamily = previewFont),
                color = subInkColor,
                textAlign = TextAlign.Center
            )
        }

        if (draft.formattedDate.isNotBlank()) {
            Text(
                text = draft.formattedDate,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontFamily = previewFont),
                color = subInkColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
