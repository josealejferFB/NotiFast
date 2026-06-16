package com.example.notifast.presentation.atoms

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun NotiFastImagePickerField(
    existingImageUrl: String? = null,
    placeholderText: String = "Añadir imagen",
    modifier: Modifier = Modifier,
    onImageSelected: (Uri?) -> Unit
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        onImageSelected(uri)
    }

    val displayUri = selectedImageUri ?: existingImageUrl

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { galleryLauncher.launch("image/*") },
        contentAlignment = Alignment.Center
    ) {
        if (displayUri != null) {
            AsyncImage(
                model = displayUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = Color(0xFF3B9797).copy(alpha = 0.1f)) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF3B9797), modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(placeholderText, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        }
    }
}
