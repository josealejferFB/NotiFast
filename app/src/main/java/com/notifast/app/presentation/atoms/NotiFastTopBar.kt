package com.notifast.app.presentation.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.header.HeaderAvatar
import com.notifast.app.presentation.atoms.header.HeaderBackButton
import com.notifast.app.presentation.atoms.header.HeaderContextPill
import com.notifast.app.presentation.atoms.header.HeaderHomeActions
import java.util.Locale

sealed class TopBarType {
    data class Home(
        val firstName: String,
        val email: String,
        val profilePictureUrl: String?,
        val hasUnreadNotifications: Boolean = false,
        val isCloudSynced: Boolean = true,
        val contextText: String? = null,
        val onProfileClick: () -> Unit,
        val onNotificationsClick: () -> Unit = {},
        val onSearchClick: (() -> Unit)? = null
    ) : TopBarType()

    data class Detail(
        val title: String,
        val subtitle: String? = null,
        val onBackClick: () -> Unit,
        val actions: @Composable () -> Unit = {}
    ) : TopBarType()

    data class Simple(
        val title: String,
        val subtitle: String? = null,
        val actions: @Composable () -> Unit = {}
    ) : TopBarType()
}

/**
 * NotiFast 3.0 AAA Editorial TopBar.
 * Implements the Tactile Studio Notebook aesthetic: high-contrast typography,
 * spring-physics avatar, real-time sync ring, contextual date pill, and modular architecture.
 */
@Composable
fun NotiFastTopBar(
    type: TopBarType,
    modifier: Modifier = Modifier
) {
    val defaultStudentName = stringResource(R.string.topbar_default_student)
    val homeDisplayName = remember(type, defaultStudentName) {
        if (type is TopBarType.Home) {
            if (type.firstName.isNotBlank()) {
                type.firstName.trim()
            } else {
                type.email.split("@").getOrElse(0) { defaultStudentName }
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
        } else ""
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        when (type) {
            is TopBarType.Home -> {
                HomeTopBarContent(
                    displayName = homeDisplayName,
                    type = type
                )
            }
            is TopBarType.Detail -> {
                DetailTopBarContent(type = type)
            }
            is TopBarType.Simple -> {
                SimpleTopBarContent(type = type)
            }
        }
    }
}

@Composable
private fun HomeTopBarContent(
    displayName: String,
    type: TopBarType.Home
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Group: Tactile Avatar + Editorial Greeting & Date Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                HeaderAvatar(
                    displayName = displayName,
                    profilePictureUrl = type.profilePictureUrl,
                    isCloudSynced = type.isCloudSynced,
                    onClick = type.onProfileClick
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.topbar_greeting, displayName),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    HeaderContextPill(text = type.contextText)
                }
            }

            // Right Group: Cloud Sync + Notifications Bell + Search
            HeaderHomeActions(
                hasUnreadNotifications = type.hasUnreadNotifications,
                isCloudSynced = type.isCloudSynced,
                onNotificationsClick = type.onNotificationsClick,
                onSearchClick = type.onSearchClick
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    }
}

@Composable
private fun DetailTopBarContent(type: TopBarType.Detail) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 6.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderBackButton(onBackClick = type.onBackClick)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = type.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!type.subtitle.isNullOrBlank()) {
                    Text(
                        text = type.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            type.actions()
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    }
}

@Composable
private fun SimpleTopBarContent(type: TopBarType.Simple) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.2).sp
                )
                if (!type.subtitle.isNullOrBlank()) {
                    Text(
                        text = type.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            type.actions()
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    }
}
