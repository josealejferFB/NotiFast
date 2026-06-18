package com.example.notifast.presentation.feature.subjectdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import coil.compose.SubcomposeAsyncImage
import com.example.notifast.data.model.Note
import com.example.notifast.presentation.theme.color.Aqua
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
    return "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}"
}

@Composable
fun SubjectDetailNotesMosaic(
    notes: List<Note>,
    subjectColorHex: String,
    onNoteClick: (Note) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text("Apuntes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        if (notes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Sin apuntes a\u00FAn", color = Color.Gray)
                }
            }
        } else {
            val color = parseSubjectColor(subjectColorHex)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 220.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 800.dp)
            ) {
                items(notes) { note ->
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(color))
            Box(modifier = Modifier.padding(16.dp)) {
                if (note.imageUrl != null) {
                    Row {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(note.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(6.dp))
                            note.content.take(100).let { preview ->
                                Text(preview, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray.copy(alpha = 0.2f))) {
                            SubcomposeAsyncImage(model = note.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.Crop)
                        }
                    }
                } else {
                    Text(note.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(6.dp))
                    Text(note.content, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                note.timestamp?.let {
                    Text(formatShortDate(it), style = MaterialTheme.typography.labelSmall, color = Color.LightGray, modifier = Modifier.align(Alignment.BottomEnd).padding(top = 6.dp))
                }
            }
        }
    }
}
