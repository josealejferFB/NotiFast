package com.notifast.app.domain.util

sealed class MathNode {
    data class Symbol(val text: String) : MathNode()
    data class Fraction(val numerator: String, val denominator: String) : MathNode()
    data class Sqrt(val radicand: String) : MathNode()
    data class Superscript(val base: String, val exponent: String) : MathNode()
    data class Subscript(val base: String, val index: String) : MathNode()
    data class Sum(val lower: String, val upper: String) : MathNode()
}

object MathParser {

    private val greek = mapOf(
        "alpha" to "α", "beta" to "β", "gamma" to "γ", "delta" to "δ",
        "epsilon" to "ε", "theta" to "θ", "lambda" to "λ", "mu" to "μ",
        "pi" to "π", "sigma" to "σ", "phi" to "φ", "omega" to "ω",
        "Gamma" to "Γ", "Delta" to "Δ", "Theta" to "Θ", "Lambda" to "Λ",
        "Pi" to "Π", "Sigma" to "Σ", "Phi" to "Φ", "Omega" to "Ω"
    )

    fun parse(source: String): MathNode {
        if (source.isBlank()) return MathNode.Symbol("")

        // frac{a}{b}
        if (source.startsWith("frac{")) {
            val (num, rest) = extractBrace(source, 4)
            if (rest.startsWith("{")) {
                val (den, _) = extractBrace(rest, 0)
                return MathNode.Fraction(num, den)
            }
        }

        // sqrt{x}
        if (source.startsWith("sqrt{")) {
            val (radicand, _) = extractBrace(source, 4)
            return MathNode.Sqrt(radicand)
        }

        // sum_{lower}^{upper}
        if (source.startsWith("sum_{")) {
            val (lower, afterLower) = extractBrace(source, 4)
            if (afterLower.startsWith("^")) {
                val (upper, _) = extractBrace(afterLower, 1)
                return MathNode.Sum(lower, upper)
            }
        }

        // base^{exp}
        val supMatch = Regex("^(.+)\\^\\{(.+)\\}$").find(source)
        if (supMatch != null) {
            return MathNode.Superscript(supMatch.groupValues[1], supMatch.groupValues[2])
        }

        // base_{idx}
        val subMatch = Regex("^(.+)_\\{(.+)\\}$").find(source)
        if (subMatch != null) {
            return MathNode.Subscript(subMatch.groupValues[1], subMatch.groupValues[2])
        }

        // greek / plain symbol
        return MathNode.Symbol(greek[source] ?: source)
    }

    private fun extractBrace(s: String, startIndex: Int): Pair<String, String> {
        // startIndex points at '{'
        if (startIndex >= s.length || s[startIndex] != '{') return "" to s
        var depth = 0
        for (i in startIndex until s.length) {
            when (s[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) {
                        return s.substring(startIndex + 1, i) to s.substring(i + 1)
                    }
                }
            }
        }
        return s.substring(startIndex + 1) to ""
    }
}
