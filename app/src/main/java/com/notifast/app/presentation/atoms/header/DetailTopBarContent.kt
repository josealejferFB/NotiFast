package com.notifast.app.presentation.atoms.header

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notifast.app.presentation.atoms.TopBarType

/**
 * TopBar content for secondary detail, creation, and editor screens.
 * Features 48x48dp tactile back button, smart title truncation, and action slot.
 */
@Composable
fun DetailTopBarContent(
    type: TopBarType.Detail,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 6.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderBackButton(onBackClick = type.onBackClick)

            HeaderContextTitle(
                title = type.title,
                subtitle = type.subtitle,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            type.actions()
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        )
    }
}
