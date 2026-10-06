package com.notifast.app.presentation.feature.editevaluation.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.atoms.NotiFastFormField
import com.notifast.app.presentation.atoms.NotiFastImagePickerField

@Composable
fun EditEvaluationGradeFields(
    isPresented: Boolean,
    onTogglePresented: () -> Unit,
    grade: String,
    onGradeChange: (String) -> Unit,
    isGradeInvalid: Boolean,
    weight: String,
    onWeightChange: (String) -> Unit,
    existingImageUrl: String?,
    onImageSelected: (Uri?) -> Unit,
    gradeScale: GradeScale = GradeScale.SCALE_0_10
) {
    val gradeRangeLabel = stringResource(
        R.string.edit_eval_grade_range_label,
        gradeScale.minGrade.toString(),
        gradeScale.maxGrade.toString()
    )
    val gradeRangeError = stringResource(
        R.string.grades_dialog_grade_range,
        gradeScale.minGrade.toString(),
        gradeScale.maxGrade.toString()
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.eval_presented_label),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.eval_presented_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isPresented,
                onCheckedChange = { onTogglePresented() }
            )
        }

        if (isPresented) {
            NotiFastFormField(
                value = grade,
                onValueChange = onGradeChange,
                label = gradeRangeLabel,
                leadingIcon = Icons.Default.Star,
                isError = isGradeInvalid,
                supportingText = if (isGradeInvalid) {
                    { Text(gradeRangeError, color = MaterialTheme.colorScheme.error) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Text(
                text = stringResource(R.string.eval_grade_scale_hint, gradeScale.label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        NotiFastFormField(
            value = weight,
            onValueChange = onWeightChange,
            label = stringResource(R.string.eval_weight_label),
            leadingIcon = Icons.Default.Scale,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        NotiFastImagePickerField(
            existingImageUrl = existingImageUrl,
            placeholderText = "Foto o guía adjunta (Opcional)",
            onImageSelected = onImageSelected
        )
    }
}
