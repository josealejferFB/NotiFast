package com.notifast.app.presentation.feature.addsubject.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R

val subjectColorPalette = listOf(
    "#BF092F", // Ruby
    "#A10827", // Darker Ruby
    "#DF2145", // Lighter Ruby
    "#3B9797", // Aqua
    "#2E7D7D", // Darker Aqua
    "#59BDBD", // Lighter Aqua
    "#4A235A", // Deep Purple
    "#1A5276", // Deep Blue
    "#0E6655", // Forest Green
    "#9A7D0A", // Gold
    "#873600", // Rust
    "#17202A"  // Dark Slate
)

@Composable
fun SubjectColorGrid(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.add_subject_color_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            subjectColorPalette.chunked(4).forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    rowColors.forEach { hexColor ->
                        val selected = selectedColor.equals(hexColor, ignoreCase = true)
                        val color = try {
                            Color(android.graphics.Color.parseColor(hexColor))
                        } catch (e: Exception) {
                            Color.Gray
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onColorSelected(hexColor) }
                                .border(
                                    width = if (selected) 3.dp else 0.dp,
                                    color = if (selected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .padding(if (selected) 3.dp else 0.dp)
                        ) {
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .border(2.dp, color, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
