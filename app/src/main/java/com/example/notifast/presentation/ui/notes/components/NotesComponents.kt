package com.example.notifast.presentation.ui.notes.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.notifast.data.model.Note
import com.example.notifast.data.model.User
import com.example.notifast.presentation.common.viewmodel.AuthViewModel
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby
import com.google.firebase.auth.FirebaseUser

@Composable
fun SubjectHeader(header: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onClick() }, 
        shape = RoundedCornerShape(12.dp), 
        color = Aqua.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = Aqua, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = header.uppercase(), 
                style = MaterialTheme.typography.labelLarge, 
                fontWeight = FontWeight.ExtraBold, 
                color = Aqua, 
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun SubjectFeaturedCard(
    subjectName: String, 
    notes: List<Note>, 
    onNoteClick: (Note) -> Unit,
    onSubjectClick: () -> Unit
) {
    Card(
        modifier = Modifier.width(280.dp).height(220.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().height(120.dp).padding(8.dp), 
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(3) { index ->
                        val note = notes.getOrNull(index)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.LightGray.copy(alpha = 0.2f))
                                .clickable { note?.let { onNoteClick(it) } }
                        ) {
                            if (note?.imageUrl != null) {
                                SubcomposeAsyncImage(model = note.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(20.dp), tint = Color.Gray)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = subjectName, 
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubjectClick() }, 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = Ruby
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), 
                    horizontalArrangement = Arrangement.SpaceBetween, 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "${notes.size} notas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(text = "ver todas", modifier = Modifier.clickable { onSubjectClick() }, style = MaterialTheme.typography.bodySmall, color = Ruby, fontWeight = FontWeight.Bold)
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-10).dp)
                    .size(56.dp)
                    .clickable { onSubjectClick() }, 
                shape = CircleShape, 
                color = Ruby, 
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) { 
                    Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp)) 
                }
            }
        }
    }
}

@Composable
fun HeaderProfile(
    userProfile: User?, 
    userAuth: FirebaseUser?, 
    onEditProfile: () -> Unit, 
    onAddSubject: () -> Unit, 
    onAddNote: () -> Unit, 
    onLogout: () -> Unit, 
    viewModel: AuthViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp), 
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onEditProfile() }, 
                contentAlignment = Alignment.Center
            ) {
                if (userProfile?.profilePictureUrl != null) {
                    SubcomposeAsyncImage(model = userProfile.profilePictureUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                val displayName = if (userProfile?.firstName != null && userProfile.firstName.isNotBlank()) {
                    "${userProfile.firstName} ${userProfile.lastName}"
                } else {
                    userAuth?.email?.split("@")?.get(0) ?: "Usuario"
                }
                Text(text = "Hola, $displayName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = userAuth?.email ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp).clickable { viewModel.logout(); onLogout() }, 
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onAddSubject, shape = RoundedCornerShape(14.dp), color = Aqua, modifier = Modifier.size(width = 46.dp, height = 46.dp)) { 
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)) } 
            }
            Spacer(modifier = Modifier.width(10.dp))
            Surface(onClick = onAddNote, shape = RoundedCornerShape(14.dp), color = Ruby, modifier = Modifier.size(width = 46.dp, height = 46.dp)) { 
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp)) } 
            }
        }
    }
}

@Composable
fun FilterChip(selected: Boolean, onClick: () -> Unit, label: String) {
    Surface(
        modifier = Modifier.clickable { onClick() }, 
        shape = RoundedCornerShape(20.dp), 
        color = if (selected) Ruby else Color.Transparent, 
        border = if (selected) null else BorderStroke(1.dp, Color.LightGray)
    ) {
        Text(
            text = label, 
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, 
            color = if (selected) Color.White else Color.Gray
        )
    }
}
