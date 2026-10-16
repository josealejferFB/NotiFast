package com.notifast.app.presentation.feature.grades.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.feature.grades.GradesTab

/**
 * Tactile pill segmented tab selector for Grades Hub ("Materias" / "Analíticas & Proyecciones").
 * Follows Design System 3.0 pill aesthetics with spring transitions and tactile haptics.
 */
@Composable
fun GradesTabSelector(
    selectedTab: GradesTab,
    onTabSelected: (GradesTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val tabs = listOf(
        GradesTab.SUBJECTS to stringResource(R.string.grades_tab_subjects),
        GradesTab.ANALYTICS to stringResource(R.string.grades_tab_analytics)
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { (tab, label) ->
                val isSelected = selectedTab == tab
                val targetBg = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                val targetContent = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                val animatedBg by animateColorAsState(
                    targetValue = targetBg,
                    animationSpec = spring(stiffness = 500f),
                    label = "GradesTabBg"
                )
                val animatedContent by animateColorAsState(
                    targetValue = targetContent,
                    animationSpec = spring(stiffness = 500f),
                    label = "GradesTabContent"
                )

                Surface(
                    onClick = {
                        if (!isSelected) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTabSelected(tab)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = animatedBg,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier.weight(1f),
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = animatedContent,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
