package com.example.notifast.presentation.ui.notes.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.notifast.data.model.Note
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

@Composable
fun NoteDetailExpandable(
    note: Note, 
    onSave: (String, String) -> Unit, 
    onClose: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }

    Card(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            if (note.imageUrl != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    SubcomposeAsyncImage(model = note.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().wrapContentHeight(), contentScale = ContentScale.FillWidth)
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                        onClick = { if (isEditing) onSave(title, content); isEditing = !isEditing },
                        shape = CircleShape, color = if (isEditing) Aqua else Ruby
                    ) {
                        Icon(imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp).size(20.dp))
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(Aqua.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                        onClick = { if (isEditing) onSave(title, content); isEditing = !isEditing },
                        shape = CircleShape, color = if (isEditing) Aqua else Ruby
                    ) {
                        Icon(imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp).size(20.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                if (isEditing) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ruby))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Contenido") }, modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Ruby))
                } else {
                    Text(text = note.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = note.content, style = MaterialTheme.typography.bodyLarge, lineHeight = 26.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(32.dp))
                FilledTonalButton(
                    onClick = onClose, modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.filledTonalButtonColors(containerColor = Ruby.copy(alpha = 0.08f), contentColor = Ruby)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finalizar lectura", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
        }
    }
}
