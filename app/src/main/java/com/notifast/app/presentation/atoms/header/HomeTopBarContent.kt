package com.notifast.app.presentation.atoms.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.TopBarType

/**
 * TopBar content for the Primary Student Hub (Notes).
 * Features student avatar with reactive sync ring, warm greeting, and quick action buttons.
 */
@Composable
fun HomeTopBarContent(
    displayName: String,
    type: TopBarType.Home,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 8.dp),
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
                    syncSummary = type.syncSummary,
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
                    if (type.contextIcon != null) {
                        HeaderContextPill(text = type.contextText, icon = type.contextIcon)
                    } else {
                        HeaderContextPill(text = type.contextText)
                    }
                }
            }

            // Right Group: Cloud Sync + Notifications Bell + Search
            HeaderHomeActions(
                hasUnreadNotifications = type.hasUnreadNotifications,
                syncSummary = type.syncSummary,
                onNotificationsClick = type.onNotificationsClick,
                onSearchClick = type.onSearchClick,
                onCloudClick = type.onCloudClick
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    }
}
