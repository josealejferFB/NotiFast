package com.notifast.app.presentation.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Notifications
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
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby
import java.util.Locale

sealed class TopBarType {
    data class Home(
        val firstName: String,
        val email: String,
        val profilePictureUrl: String?,
        val hasUnreadNotifications: Boolean = false,
        val isCloudSynced: Boolean = true,
        val onProfileClick: () -> Unit,
        val onNotificationsClick: () -> Unit = {}
    ) : TopBarType()

    data class Detail(
        val title: String,
        val onBackClick: () -> Unit
    ) : TopBarType()

    data class Simple(
        val title: String
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ruby)
    ) {
        CenterAlignedTopAppBar(
            title = {
                when (type) {
                    is TopBarType.Home -> {
                        Text(
                            text = "Hola, $homeDisplayName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    is TopBarType.Detail -> {
                        Text(
                            text = type.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    is TopBarType.Simple -> {
                        Text(
                            text = type.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                        }
                    }
                    is TopBarType.Simple -> {
                        // Sin ícono de navegación para pestañas raíz
                    }
                }
            },
            actions = {
                if (type is TopBarType.Home) {
                    // Indicador de Nube / Sincronización
                    Icon(
                        imageVector = if (type.isCloudSynced) androidx.compose.material.icons.Icons.Default.CloudDone else androidx.compose.material.icons.Icons.Default.CloudOff,
                        contentDescription = "Estado de Sincronización",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(end = 8.dp).size(22.dp)
                    )
                    
                    // Campana de Notificaciones con Badge
                    androidx.compose.material3.BadgedBox(
                        badge = {
                            if (type.hasUnreadNotifications) {
                                androidx.compose.material3.Badge(containerColor = Aqua)
                            }
                        },
                        modifier = Modifier.padding(end = 16.dp).clickable { type.onNotificationsClick() }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = Color.White
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )
    }
}
