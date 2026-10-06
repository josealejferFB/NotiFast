package com.notifast.app.presentation.feature.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.util.GradeFormatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsGradeScaleDialog(
    currentScale: String,
    currentPassingGrade: Float,
    evaluationsWithGradeCount: Int,
    onConfirm: (scale: String, passingGrade: Float) -> Unit,
    onDismiss: () -> Unit
) {
    val currentScaleObj = GradeScale.fromId(currentScale)
    val currentMax = currentScaleObj.maxGrade.toFloat()
    // 0f means "use scale default" (D16/D17); out-of-range stored values also fall back.
    val initialPassing = currentPassingGrade.takeIf { it != 0f && it in 0f..currentMax }
        ?: currentScaleObj.minPassingGrade.toFloat()

    var selectedScale by remember { mutableStateOf(currentScale) }
    var passingGrade by remember { mutableFloatStateOf(initialPassing) }

    val (maxRange, _, stepSize) = when (selectedScale) {
        "0-10" -> Triple(10f, 6.0f, 0.5f)
        "0-100" -> Triple(100f, 60.0f, 5.0f)
        "A-F" -> Triple(4f, 2.0f, 0.1f)
        else -> Triple(20f, 11.0f, 0.5f)
    }

    fun selectScale(newScaleId: String) {
        if (newScaleId == selectedScale) return
        val from = GradeScale.fromId(selectedScale)
        val to = GradeScale.fromId(newScaleId)
        val converted = GradePolicy.convertPassingGrade(passingGrade.toDouble(), from, to)
        selectedScale = newScaleId
        passingGrade = converted.toFloat().coerceIn(0f, maxRangeFor(newScaleId))
    }

    val selectedScaleObj = GradeScale.fromId(selectedScale)
    val currentMinFormatted = GradeFormatters.passing(initialPassing.toDouble(), currentScaleObj)
    val newMinFormatted = GradeFormatters.passing(passingGrade.toDouble(), selectedScaleObj)
    val previewText = if (evaluationsWithGradeCount > 0) {
        stringResource(
            R.string.settings_grade_scale_convert_preview,
            evaluationsWithGradeCount,
            currentMinFormatted,
            newMinFormatted
        )
    } else {
        stringResource(R.string.settings_grade_scale_convert_none, currentMinFormatted, newMinFormatted)
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_academic_scale_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                GradeScaleOptionItem(
                    title = stringResource(R.string.settings_academic_scale_vigesimal),
                    subtitle = stringResource(R.string.settings_academic_scale_vigesimal_desc),
                    isSelected = selectedScale == "0-20",
                    onClick = { selectScale("0-20") }
                )

                GradeScaleOptionItem(
                    title = stringResource(R.string.settings_academic_scale_decimal),
                    subtitle = stringResource(R.string.settings_academic_scale_decimal_desc),
                    isSelected = selectedScale == "0-10",
                    onClick = { selectScale("0-10") }
                )

                GradeScaleOptionItem(
                    title = stringResource(R.string.settings_academic_scale_percent),
                    subtitle = stringResource(R.string.settings_academic_scale_percent_desc),
                    isSelected = selectedScale == "0-100",
                    onClick = { selectScale("0-100") }
                )

                GradeScaleOptionItem(
                    title = stringResource(R.string.settings_academic_scale_letters),
                    subtitle = stringResource(R.string.settings_academic_scale_letters_desc),
                    isSelected = selectedScale == "A-F",
                    onClick = { selectScale("A-F") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_academic_passing_grade_title),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = GradeFormatters.passing(passingGrade.toDouble(), selectedScaleObj),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = passingGrade,
                    onValueChange = { passingGrade = it },
                    valueRange = 0f..maxRange,
                    steps = if (stepSize > 0) ((maxRange / stepSize).toInt() - 1).coerceAtLeast(0) else 0,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotiFastButton(
                    text = stringResource(R.string.btn_save),
                    onClick = {
                        onConfirm(selectedScale, passingGrade)
                        onDismiss()
                    }
                )
            }
        }
    }
}

private fun maxRangeFor(scaleId: String): Float = when (scaleId) {
    "0-10" -> 10f
    "0-100" -> 100f
    "A-F" -> 4f
    else -> 20f
}

@Composable
private fun GradeScaleOptionItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
