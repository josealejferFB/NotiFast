package com.notifast.app.presentation.atoms.header

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.notifast.app.R
import java.util.Locale

/**
 * NotiFast 3.0 Tactile Header Avatar with Cloud Sync Ring.
 * Features 0.97f spring tactile response, dynamic sync ring, and high-contrast monogram fallback.
 */
@Composable
fun HeaderAvatar(
    displayName: String,
    profilePictureUrl: String?,
    isCloudSynced: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "avatar_press_scale"
    )

    val syncRingColor = if (isCloudSynced) {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
    }

    val initial = remember(displayName) {
        displayName.trim().take(1).uppercase(Locale.getDefault()).ifEmpty { "E" }
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = stringResource(R.string.topbar_avatar_cd, displayName),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .border(
                    width = 1.5.dp,
                    color = syncRingColor,
                    shape = CircleShape
                )
                .padding(2.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (!profilePictureUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = profilePictureUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = {
                        MonogramFallback(initial)
                    },
                    loading = {
                        MonogramFallback(initial)
                    }
                )
            } else {
                MonogramFallback(initial)
            }
        }
    }
}

@Composable
private fun MonogramFallback(initial: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
    }
}
