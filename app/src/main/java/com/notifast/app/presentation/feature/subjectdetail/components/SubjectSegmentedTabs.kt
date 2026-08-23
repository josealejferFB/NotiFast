package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.feature.subjectdetail.SubjectQuadrant

@Composable
fun SubjectSegmentedTabs(
    selectedQuadrant: SubjectQuadrant,
    onQuadrantSelected: (SubjectQuadrant) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val quadrants = listOf(
        SubjectQuadrant.PANORAMA to stringResource(R.string.subject_detail_tab_panorama),
        SubjectQuadrant.EVALUATIONS to stringResource(R.string.subject_detail_tab_evaluations),
        SubjectQuadrant.NOTES to stringResource(R.string.subject_detail_tab_notes),
        SubjectQuadrant.SCHEDULES to stringResource(R.string.subject_detail_tab_schedules)
    )

    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            quadrants.forEach { (quadrant, label) ->
                val isSelected = selectedQuadrant == quadrant
                val targetBg = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                val targetContent = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                val animatedBg by animateColorAsState(targetValue = targetBg, animationSpec = spring(stiffness = 500f), label = "TabBg")
                val animatedContent by animateColorAsState(targetValue = targetContent, animationSpec = spring(stiffness = 500f), label = "TabContent")

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = animatedBg,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        if (!isSelected) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onQuadrantSelected(quadrant)
                        }
                    }
                ) {
                    Box(modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp), contentAlignment = Alignment.Center) {
                        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = animatedContent, textAlign = TextAlign.Center, maxLines = 1)
                    }
                }
            }
        }
    }
}
