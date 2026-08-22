package com.notifast.app.presentation.feature.addevaluation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby
import kotlin.math.roundToInt

@Composable
fun EvaluationWeightSlider(
    weight: String,
    onWeightChanged: (String) -> Unit,
    availableSubjectWeight: Int = 100,
    modifier: Modifier = Modifier
) {
    val currentWeight = (weight.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0).toFloat()
    val isExceeded = currentWeight > availableSubjectWeight

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.evaluation_field_weight),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Percentage Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isExceeded) Ruby.copy(alpha = 0.15f) else Aqua.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${currentWeight.roundToInt()}%",
                    color = if (isExceeded) Ruby else Aqua,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Discrete 5% step slider (0 to 100 -> 19 intermediate steps)
        Slider(
            value = currentWeight,
            onValueChange = { newValue ->
                val stepped = ((newValue / 5.0f).roundToInt() * 5).coerceIn(0, 100)
                onWeightChanged(stepped.toString())
            },
            valueRange = 0f..100f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = if (isExceeded) Ruby else Aqua,
                activeTrackColor = if (isExceeded) Ruby else Aqua,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Helper / Available Weight Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isExceeded) {
                    stringResource(R.string.evaluation_weight_exceeded)
                } else {
                    stringResource(R.string.evaluation_available_weight, availableSubjectWeight)
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isExceeded) Ruby else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isExceeded) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = "Pasos de 5%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
