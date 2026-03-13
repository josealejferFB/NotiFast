package com.example.notifast.presentation.ui.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.notifast.data.model.Note
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

@Composable
fun NoteEstheticCard(note: Note, isEven: Boolean, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val baseColor = if (isEven) Ruby else Aqua
    val subjectColor = if (isEven) Aqua else Ruby
    Box(modifier = Modifier.width(373.dp).height(208.dp).padding(vertical = 8.dp).clickable(interactionSource = interactionSource, indication = null) { onClick() }) {
        Card(modifier = Modifier.width(363.dp).height(171.dp).align(if (isEven) Alignment.BottomEnd else Alignment.BottomStart), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = baseColor)) {
            Column(modifier = Modifier.fillMaxSize().padding(start = if (isEven) 185.dp else 24.dp, end = if (isEven) 24.dp else 185.dp, top = 20.dp, bottom = 12.dp)) {
                Text(text = note.title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(text = note.content, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f), maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp)) }
                }
            }
        }
        Card(modifier = Modifier.width(174.dp).height(202.dp).align(if (isEven) Alignment.TopStart else Alignment.TopEnd), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = subjectColor), elevation = CardDefaults.cardElevation(10.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp).clip(RoundedCornerShape(18.dp)).background(Color.Black.copy(alpha = 0.15f))) {
                    if (note.imageUrl != null) SubcomposeAsyncImage(model = note.imageUrl, contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(44.dp), tint = Color.White.copy(alpha = 0.3f))
                }
                Text(text = note.subject, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
