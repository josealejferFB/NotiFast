package com.notifast.app.presentation.feature.schedules.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.notifast.app.R
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.EvaluationType
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.theme.type.JetBrainsMonoFamily
import com.notifast.app.presentation.util.GradeFormatters

/**
 * NotiFast 3.0 Tactical Agenda Evaluation Detail BottomSheet.
 */
@Composable
fun AgendaEvaluationBottomSheet(
    eval: Evaluation,
    displayScale: GradeScale = GradeScale.SCALE_0_10,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val evaluationType = EvaluationType.fromName(eval.type)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(evaluationType.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = evaluationType.icon,
                    contentDescription = null,
                    tint = evaluationType.color,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = eval.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = eval.subjectName,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(16.dp))

        if (!eval.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = eval.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        DetailRow(
            icon = Icons.AutoMirrored.Filled.Subject,
            label = "Descripción",
            content = eval.description,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(14.dp))
        DetailRow(
            icon = Icons.Default.Lightbulb,
            label = "Consejos Rápidos",
            content = eval.quickTips,
            color = MaterialTheme.colorScheme.secondary
        )

        if (evaluationType.showGrade && eval.grade != null) {
            Spacer(modifier = Modifier.height(14.dp))
            DetailRow(
                icon = Icons.Default.Event,
                label = stringResource(R.string.eval_grade_obtained_label),
                content = GradeFormatters.gradeWithScale(eval.grade, eval.gradeScaleId, displayScale),
                color = MaterialTheme.colorScheme.primary,
                isMono = true
            )
        }

        if (evaluationType.showWeight && eval.weight != null) {
            Spacer(modifier = Modifier.height(14.dp))
            DetailRow(
                icon = Icons.Default.Event,
                label = "Ponderación",
                content = "${eval.weight}%",
                color = MaterialTheme.colorScheme.primary,
                isMono = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FilledTonalIconButton(
                onClick = onEdit,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.btn_edit),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            FilledTonalIconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = stringResource(R.string.btn_delete),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    content: String,
    color: Color,
    isMono: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(top = 2.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = content.ifBlank { "Sin detalles registrados" },
                fontSize = 13.sp,
                fontFamily = if (isMono) JetBrainsMonoFamily else null,
                fontWeight = if (isMono) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
