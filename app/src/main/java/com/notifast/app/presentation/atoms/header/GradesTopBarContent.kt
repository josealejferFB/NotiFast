package com.notifast.app.presentation.atoms.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.TopBarType
import java.util.Locale

/**
 * Analytical TopBar content for the Academic Grades & Performance screen.
 * Embeds global GPA micro-badge in tabular JetBrains Mono font.
 */
@Composable
fun GradesTopBarContent(
    type: TopBarType.Grades,
    modifier: Modifier = Modifier
) {
    val displayTitle = type.title ?: stringResource(R.string.topbar_grades_title)
    val noAverageText = stringResource(R.string.topbar_grades_no_average)
    val badgeText = remember(type.averageGrade, type.gradeScale, noAverageText) {
        val avg = type.averageGrade
        if (avg != null && avg > 0.0) {
            String.format(Locale.US, "%.2f", avg)
        } else {
            noAverageText
        }
    }

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
                badgeText = badgeText,
                isBadgeMono = true,
                badgeLeadingIcon = Icons.Default.Calculate,
                badgeContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                badgeContentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f, fill = false)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
