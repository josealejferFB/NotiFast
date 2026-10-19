package com.notifast.app.presentation.atoms.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.TopBarType

/**
 * TopBar content for the Academic Schedule / Agenda screen.
 * Focuses strictly on temporal orientation and schedule actions, eliminating personal greetings.
 */
@Composable
fun AgendaTopBarContent(
    type: TopBarType.Agenda,
    modifier: Modifier = Modifier
) {
    val displayTitle = type.title ?: stringResource(R.string.topbar_agenda_title)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderContextTitle(
                title = displayTitle,
                modifier = Modifier.weight(1f, fill = false)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeaderContextPill(text = type.dateText)

                if (type.onSearchClick != null) {
                    HeaderSearchButton(onClick = type.onSearchClick)
                }

                type.actions?.invoke()
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    }
}
