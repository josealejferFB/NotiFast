package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.notifast.app.data.model.Note
import com.notifast.app.presentation.theme.color.Aqua
import java.util.Calendar
import java.util.Date

private fun parseSubjectColor(hex: String): Color {
    return try {
        if (hex.isNotBlank()) Color(android.graphics.Color.parseColor(hex))
        else Aqua
    } catch (e: Exception) { Aqua }
}

private fun formatShortDate(date: Date?): String {
    if (date == null) return ""
    val cal = Calendar.getInstance().apply { time = date }
    return "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
}

@Composable
fun SubjectDetailNotesMosaic(
    notes: List<Note>,
    subjectColorHex: String,
    onNoteClick: (Note) -> Unit = {}
) {
    if (notes.isEmpty()) return

    val color = parseSubjectColor(subjectColorHex)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        notes.forEachIndexed { index, note ->
            val isLast = index == notes.lastIndex
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                // Timeline Graphic
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
                    Box(modifier = Modifier.padding(top = 28.dp).size(12.dp).clip(CircleShape).background(color))
                    if (!isLast) {
                        Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(color.copy(alpha = 0.3f)))
                    } else {
                        Spacer(Modifier.fillMaxHeight())
                    }
                }
                
                Spacer(Modifier.width(16.dp))
                
                Box(modifier = Modifier.weight(1f).padding(bottom = 16.dp)) {
                    NoteTile(note = note, color = color, onClick = { onNoteClick(note) })
                }
            }
        }
    }
}

@Composable
private fun NoteTile(note: Note, color: Color, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            note.timestamp?.let {
                Text(
                    text = formatShortDate(it),
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            note.content.take(150).let { preview ->
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }
            
            if (note.imageUrl != null) {
                Spacer(Modifier.height(12.dp))
                SubcomposeAsyncImage(
                    model = note.imageUrl, 
                    contentDescription = null, 
                    modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(8.dp)), 
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
