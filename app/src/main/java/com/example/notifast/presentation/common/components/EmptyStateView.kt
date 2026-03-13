package com.example.notifast.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

@Composable
fun EmptyStateView(searchQuery: String, onAddSubject: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = Aqua.copy(alpha = 0.15f)) { 
                Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, modifier = Modifier.padding(24.dp).fillMaxSize(), tint = Ruby) 
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (searchQuery.isEmpty()) "Comienza a organizar tus apuntes" else "No hay coincidencias", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold, 
                textAlign = TextAlign.Center
            )
            if (searchQuery.isEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onAddSubject, colors = ButtonDefaults.buttonColors(containerColor = Aqua)) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear mi primera Materia")
                }
            }
        }
    }
}
