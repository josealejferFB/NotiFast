package com.notifast.app.presentation.feature.calendar.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.EvaluationType
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby

@Composable
fun EvaluationDetailBottomSheet(
    eval: Evaluation,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val evaluationType = EvaluationType.fromName(eval.type)

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(evaluationType.color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = evaluationType.icon,
                    contentDescription = null,
                    tint = evaluationType.color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = eval.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = eval.subjectName,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(24.dp))

        if (!eval.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = eval.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        DetailRow(icon = Icons.AutoMirrored.Filled.Subject, label = "Descripción", content = eval.description)
        Spacer(modifier = Modifier.height(16.dp))
        DetailRow(icon = Icons.Default.Lightbulb, label = "Tips Rápidos", content = eval.quickTips, color = Color(0xFFFBC02D))

        if (evaluationType.showGrade && eval.grade != null) {
            Spacer(modifier = Modifier.height(16.dp))
            DetailRow(icon = Icons.Default.Event, label = "Nota Obtenida", content = "${eval.grade}/10", color = Aqua)
        }

        if (evaluationType.showWeight && eval.weight != null) {
            Spacer(modifier = Modifier.height(16.dp))
            DetailRow(icon = Icons.Default.Event, label = "Peso / Porcentaje", content = "${eval.weight}%", color = Aqua)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            FilledTonalIconButton(
                onClick = onEdit,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Aqua.copy(alpha = 0.12f),
                    contentColor = Aqua
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            FilledTonalIconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Ruby.copy(alpha = 0.12f),
                    contentColor = Ruby
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Borrar", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, content: String, color: Color = Ruby) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(top = 2.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content.ifBlank { "Sin detalles" },
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
