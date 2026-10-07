package com.notifast.app.presentation.feature.noteeditor.math

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.domain.util.MathNode
import com.notifast.app.domain.util.MathParser

@Composable
fun MathRenderer(source: String, modifier: Modifier = Modifier) {
    val node = MathParser.parse(source)
    MathNodeView(node = node, modifier = modifier)
}

@Composable
private fun MathNodeView(node: MathNode, modifier: Modifier = Modifier) {
    when (node) {
        is MathNode.Symbol -> Text(
            text = node.text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = modifier
        )
        is MathNode.Fraction -> Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(node.numerator, style = MaterialTheme.typography.bodyMedium)
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(horizontal = 4.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(node.denominator, style = MaterialTheme.typography.bodyMedium)
        }
        is MathNode.Sqrt -> Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("√", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp))
            Text(
                node.radicand,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 2.dp)
            )
            Text("¯", style = MaterialTheme.typography.bodySmall)
        }
        is MathNode.Superscript -> Row(
            modifier = modifier,
            verticalAlignment = Alignment.Top
        ) {
            Text(node.base, style = MaterialTheme.typography.bodyLarge)
            Text(
                node.exponent,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        is MathNode.Subscript -> Row(
            modifier = modifier,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(node.base, style = MaterialTheme.typography.bodyLarge)
            Text(
                node.index,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
            )
        }
        is MathNode.Sum -> Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("∑", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp))
            Row {
                Text(
                    node.lower,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                )
            }
            if (node.upper.isNotBlank()) {
                Text(
                    node.upper,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                )
            }
        }
    }
}
