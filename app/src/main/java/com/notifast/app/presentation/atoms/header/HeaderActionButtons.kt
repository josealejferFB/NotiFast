package com.notifast.app.presentation.atoms.header

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.notifast.app.R

import androidx.compose.material.icons.filled.Search

/**
 * NotiFast 3.0 Header Action Buttons.
 * Includes search icon, notification bell with warm badge, cloud sync status, and tactile back button.
 */
@Composable
fun HeaderHomeActions(
    hasUnreadNotifications: Boolean,
    isCloudSynced: Boolean,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSearchClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bell_press_scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Search button if provided
        if (onSearchClick != null) {
            val searchInteractionSource = remember { MutableInteractionSource() }
            val isSearchPressed by searchInteractionSource.collectIsPressedAsState()
            val searchScale by animateFloatAsState(
                targetValue = if (isSearchPressed) 0.97f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "search_press_scale"
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer {
                        scaleX = searchScale
                        scaleY = searchScale
                    }
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = searchInteractionSource,
                        indication = null,
                        role = Role.Button,
                        onClickLabel = "Buscar",
                        onClick = onSearchClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Storage & sync indicator (honest local storage vs cloud sync)
        val syncDescription = if (isCloudSynced) {
            stringResource(R.string.topbar_sync_synced_cd)
        } else {
            stringResource(R.string.topbar_local_storage_cd)
        }

        Icon(
            imageVector = if (isCloudSynced) Icons.Default.CloudDone else Icons.Default.PhoneAndroid,
            contentDescription = syncDescription,
            tint = if (isCloudSynced) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(20.dp)
        )

        // Notification Bell with Badge and Tactile Spring
        Box(
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.topbar_notifications_cd),
                    onClick = onNotificationsClick
                ),
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                badge = {
                    if (hasUnreadNotifications) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(7.dp)
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = stringResource(R.string.topbar_notifications_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun HeaderBackButton(
    onBackClick: () -> Unit,
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
        label = "back_press_scale"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = stringResource(R.string.topbar_back_cd),
                onClick = onBackClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.topbar_back_cd),
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}
