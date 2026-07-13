package com.example.notifast.presentation.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.notifast.presentation.theme.color.Aqua
import java.util.Locale

sealed class TopBarType {
    data class Home(
        val firstName: String,
        val email: String,
        val profilePictureUrl: String?,
        val onProfileClick: () -> Unit
    ) : TopBarType()

    data class Detail(
        val title: String,
        val onBackClick: () -> Unit
    ) : TopBarType()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotiFastTopBar(type: TopBarType) {
    val homeDisplayName = remember(type) {
        if (type is TopBarType.Home) {
            if (type.firstName.isNotBlank()) type.firstName 
            else type.email.split("@").getOrElse(0) { "Estudiante" }.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        } else ""
    }

    CenterAlignedTopAppBar(
        title = {
            when (type) {
                is TopBarType.Home -> {
                    Text(
                        text = "Hola, $homeDisplayName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                is TopBarType.Detail -> {
                    Text(
                        text = type.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        navigationIcon = {
            when (type) {
                is TopBarType.Home -> {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Aqua)
                            .clickable { type.onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (type.profilePictureUrl != null) {
                            SubcomposeAsyncImage(model = type.profilePictureUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Text(homeDisplayName.take(1).uppercase(Locale.getDefault()), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
                is TopBarType.Detail -> {
                    IconButton(onClick = type.onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
