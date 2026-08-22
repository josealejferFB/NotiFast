package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.notifast.app.data.model.Note
import com.notifast.app.presentation.atoms.NotiFastMarkdown
import com.notifast.app.presentation.theme.color.Aqua

@Composable
fun SubjectDetailNoteDetail(
    note: Note,
    subjectColorHex: String,
    onClose: () -> Unit
) {
    val accentColor = try {
        if (subjectColorHex.isNotBlank()) Color(android.graphics.Color.parseColor(subjectColorHex))
        else Aqua
    } catch (e: Exception) { Aqua }

    Card(
        modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            if (note.imageUrl != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    SubcomposeAsyncImage(
                        model = note.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp).background(Brush.horizontalGradient(listOf(accentColor, accentColor.copy(alpha = 0.5f)))),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(onClick = onClose, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(note.subject.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accentColor)
                Spacer(Modifier.height(4.dp))
                Text(note.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                NotiFastMarkdown(content = note.content)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
