package com.notifast.app.presentation.feature.noteeditor.math

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SymbolEntry(val display: String, val insert: String)

object MathSymbols {
    val greek = listOf(
        SymbolEntry("α", "alpha"), SymbolEntry("β", "beta"), SymbolEntry("γ", "gamma"),
        SymbolEntry("δ", "delta"), SymbolEntry("θ", "theta"), SymbolEntry("λ", "lambda"),
        SymbolEntry("π", "pi"), SymbolEntry("σ", "sigma"), SymbolEntry("φ", "phi"),
        SymbolEntry("ω", "omega"), SymbolEntry("Γ", "Gamma"), SymbolEntry("Σ", "Sigma")
    )
    val operators = listOf(
        SymbolEntry("+", "+"), SymbolEntry("−", "-"), SymbolEntry("×", "*"),
        SymbolEntry("÷", "/"), SymbolEntry("=", "="), SymbolEntry("≠", "!="),
        SymbolEntry("≤", "<="), SymbolEntry("≥", ">="), SymbolEntry("±", "+/-"),
        SymbolEntry("∞", "inf"), SymbolEntry("≈", "~"), SymbolEntry("∈", "in")
    )
    val structures = listOf(
        SymbolEntry("a/b", "frac{}{}"), SymbolEntry("√", "sqrt{}"),
        SymbolEntry("xⁿ", "x^{}"), SymbolEntry("xₙ", "x_{}"),
        SymbolEntry("∑", "sum_{}^{}"), SymbolEntry("∫", "int_{}^{}"),
        SymbolEntry("( )", "()"), SymbolEntry("[ ]", "[]")
    )
}

@Composable
fun SymbolKeyboard(
    onInsert: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SymbolRow(symbols = MathSymbols.greek, onInsert = onInsert)
        SymbolRow(symbols = MathSymbols.operators, onInsert = onInsert)
        SymbolRow(symbols = MathSymbols.structures, onInsert = onInsert)
    }
}

@Composable
private fun SymbolRow(symbols: List<SymbolEntry>, onInsert: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        symbols.forEach { entry ->
            Surface(
                onClick = { onInsert(entry.insert) },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.padding(1.dp)
            ) {
                Text(
                    text = entry.display,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
