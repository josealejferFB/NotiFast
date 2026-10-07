package com.notifast.app.presentation.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notifast.app.domain.model.GradeScale

data class GradePreset(val label: String, val value: Double)

fun gradePresetsFor(scale: GradeScale): List<GradePreset> = when (scale) {
    GradeScale.SCALE_0_20 -> listOf(20.0, 18.0, 16.0, 14.0, 11.0, 8.0).map { it.toPreset() }
    GradeScale.SCALE_0_100 -> listOf(100.0, 90.0, 80.0, 70.0, 60.0, 50.0).map { it.toPreset() }
    GradeScale.SCALE_LETTER -> listOf(
        GradePreset("A", 4.0),
        GradePreset("B", 3.0),
        GradePreset("C", 2.0),
        GradePreset("D", 1.0),
        GradePreset("F", 0.0)
    )
    else -> listOf(10.0, 9.0, 8.0, 7.0, 6.0, 5.0).map { it.toPreset() }
}

private fun Double.toPreset(): GradePreset =
    GradePreset(
        label = if (this % 1.0 == 0.0) toInt().toString() else toString(),
        value = this
    )

fun presetText(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

/**
 * Compact preset grade chips for the active [scale] (A-F maps to GPA values).
 */
@Composable
fun NotiFastGradePresetChips(
    scale: GradeScale,
    selectedText: String,
    onPresetClick: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val presetChips = remember(scale) { gradePresetsFor(scale) }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        presetChips.forEach { preset ->
            val text = presetText(preset.value)
            FilterChip(
                selected = selectedText == text,
                onClick = { onPresetClick(preset.value) },
                label = { Text(preset.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
