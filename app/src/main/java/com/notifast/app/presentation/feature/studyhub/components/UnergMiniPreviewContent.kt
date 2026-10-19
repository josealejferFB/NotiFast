package com.notifast.app.presentation.feature.studyhub.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.data.study.OpenXmlDocxGenerator
import com.notifast.app.domain.model.study.ApaFont
import com.notifast.app.domain.model.study.CoverDraft

@Composable
fun UnergMiniPreviewContent(
    draft: CoverDraft,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val inkColor = Color(0xFF0F172A)
    val subInkColor = Color(0xFF475569)

    val apaFont = ApaFont.fromFontName(draft.fontName)
    val previewFont = if (apaFont.isSerif) FontFamily.Serif else FontFamily.SansSerif

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header with Logos and Membrete
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(R.drawable.logo_unerg_banner),
                contentDescription = "UNERG Banner",
                modifier = Modifier
                    .height(28.dp)
                    .weight(0.26f),
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier
                    .weight(0.48f)
                    .padding(horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = draft.countryLine.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp, lineHeight = 7.sp, fontWeight = FontWeight.Bold, fontFamily = previewFont),
                    color = inkColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = draft.ministryLine.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp, lineHeight = 7.sp, fontWeight = FontWeight.Bold, fontFamily = previewFont),
                    color = inkColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = draft.institutionName.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp, lineHeight = 7.sp, fontWeight = FontWeight.Bold, fontFamily = previewFont),
                    color = inkColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = draft.facultyOrDepartment.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp, lineHeight = 7.sp, fontWeight = FontWeight.Bold, fontFamily = previewFont),
                    color = inkColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = draft.programLine.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp, lineHeight = 7.sp, fontWeight = FontWeight.Bold, fontFamily = previewFont),
                    color = inkColor,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = draft.curricularUnit.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp, lineHeight = 7.sp, fontWeight = FontWeight.Bold, fontFamily = previewFont),
                    color = inkColor,
                    textAlign = TextAlign.Center
                )
            }
            Image(
                painter = painterResource(R.drawable.logo_unerg_ais),
                contentDescription = "AIS Logo",
                modifier = Modifier
                    .size(26.dp)
                    .weight(0.26f),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Title and Subtitle Centered
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = draft.workTitle.ifBlank { "TÍTULO DEL PROYECTO DE GRADO" }.uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 13.sp, fontFamily = previewFont),
                color = inkColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            if (draft.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = draft.subtitle.uppercase(),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp, lineHeight = 10.sp, fontFamily = previewFont),
                    color = subInkColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Millimeter Side-by-side columns: Docente vs Autores & Tutor
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Docente:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp, fontFamily = previewFont),
                    color = inkColor
                )
                Text(
                    text = draft.teacherName.ifBlank { "Prof. Asignatura" },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp, fontFamily = previewFont),
                    color = subInkColor
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                val authorHeader = if (draft.authors.size > 1) "Autores:" else "Autor:"
                Text(
                    text = authorHeader,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp, fontFamily = previewFont),
                    color = inkColor
                )
                draft.authors.forEach { rawAuthor ->
                    val (name, ci) = OpenXmlDocxGenerator.parseAuthor(rawAuthor)
                    Text(
                        text = name.ifBlank { "Integrante" },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp, fontFamily = previewFont),
                        color = subInkColor
                    )
                    if (ci.isNotBlank()) {
                        Text(
                            text = "C.I.: $ci",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 7.sp, fontFamily = previewFont),
                            color = subInkColor
                        )
                    }
                }

                if (draft.tutorName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tutor Académico:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp, fontFamily = previewFont),
                        color = inkColor
                    )
                    Text(
                        text = draft.tutorName,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp, fontFamily = previewFont),
                        color = subInkColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Footer City and Date
        Text(
            text = "${draft.city} - ${draft.formattedDate}".trim(' ', '-'),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 8.sp, fontFamily = previewFont),
            color = subInkColor,
            textAlign = TextAlign.Center
        )
    }
}
