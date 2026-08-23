package com.notifast.app.presentation.feature.schedules.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notifast.app.presentation.feature.schedules.LiveClassStatus

/**
 * NotiFast 3.0 Live Class Beacon Card.
 * Displays active class in real-time or upcoming countdown with tactile quick actions.
 */
@Composable
fun ScheduleLiveStatusCard(
    status: LiveClassStatus,
    onTakeNote: (String) -> Unit,
    onJoinMeeting: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                is LiveClassStatus.Active -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                is LiveClassStatus.Upcoming -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            1.dp,
            when (status) {
                is LiveClassStatus.Active -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                is LiveClassStatus.Upcoming -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.outlineVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (status) {
                is LiveClassStatus.Active -> ActiveClassContent(status, isDark, onTakeNote, onJoinMeeting)
                is LiveClassStatus.Upcoming -> UpcomingClassContent(status)
                is LiveClassStatus.DayCompleted -> DayCompletedContent(status.totalClassesToday)
                is LiveClassStatus.NoClassesToday -> NoClassesContent()
            }
        }
    }
}
