package com.notifast.app.presentation.atoms.header

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
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
import com.notifast.app.domain.model.cloud.LocalSyncSummary

/**
 * Tactile Search Icon Button with 48x48dp touch target and spring animation.
 */
@Composable
fun HeaderSearchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "search_press_scale"
    )
    val searchLabel = stringResource(R.string.topbar_search_cd)

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
                onClickLabel = searchLabel,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = searchLabel,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Tactile Cloud Sync Status Button with honest local storage vs cloud indicators.
 */
@Composable
fun HeaderCloudButton(
    syncSummary: LocalSyncSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "cloud_press_scale"
    )

    val syncDescription = when (syncSummary) {
        LocalSyncSummary.NO_ACCOUNT -> stringResource(R.string.topbar_sync_no_account_cd)
        LocalSyncSummary.ALL_SYNCED -> stringResource(R.string.topbar_sync_synced_cd)
        LocalSyncSummary.PENDING -> stringResource(R.string.topbar_sync_pending_cd)
    }

    val syncIcon = when (syncSummary) {
        LocalSyncSummary.NO_ACCOUNT -> Icons.Default.PhoneAndroid
        LocalSyncSummary.ALL_SYNCED -> Icons.Default.CloudDone
        LocalSyncSummary.PENDING -> Icons.Default.CloudSync
    }

    val syncTint = when (syncSummary) {
        LocalSyncSummary.NO_ACCOUNT -> MaterialTheme.colorScheme.onSurfaceVariant
        LocalSyncSummary.ALL_SYNCED -> MaterialTheme.colorScheme.tertiary
        LocalSyncSummary.PENDING -> MaterialTheme.colorScheme.primary
    }

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
                onClickLabel = syncDescription,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        BadgedBox(
            badge = {
                if (syncSummary == LocalSyncSummary.PENDING) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(6.dp)
                    )
                }
            }
        ) {
            Icon(
                imageVector = syncIcon,
                contentDescription = syncDescription,
                tint = syncTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Tactile Notification Bell Button with unread indicator badge.
 */
@Composable
fun HeaderNotificationButton(
    hasUnreadNotifications: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bell_press_scale"
    )
    val notifLabel = stringResource(R.string.topbar_notifications_cd)

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
                onClickLabel = notifLabel,
                onClick = onClick
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
                contentDescription = notifLabel,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}


/**
 * Composed Action Group for Home / Notes Hub.
 */
@Composable
fun HeaderHomeActions(
    hasUnreadNotifications: Boolean,
    syncSummary: LocalSyncSummary = LocalSyncSummary.NO_ACCOUNT,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSearchClick: (() -> Unit)? = null,
    onCloudClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (onSearchClick != null) {
            HeaderSearchButton(onClick = onSearchClick)
        }
        HeaderCloudButton(
            syncSummary = syncSummary,
            onClick = onCloudClick
        )
        HeaderNotificationButton(
            hasUnreadNotifications = hasUnreadNotifications,
            onClick = onNotificationsClick
        )
    }
}
