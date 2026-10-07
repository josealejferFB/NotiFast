package com.notifast.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MathParserTest {

    @Test
    fun `plain text becomes single symbol node`() {
        val node = MathParser.parse("x")
        assertTrue(node is MathNode.Symbol)
        assertEquals("x", (node as MathNode.Symbol).text)
    }

    @Test
    fun `frac produces fraction node`() {
        val node = MathParser.parse("frac{a}{b}")
        assertTrue(node is MathNode.Fraction)
        val frac = node as MathNode.Fraction
        assertEquals("a", frac.numerator)
        assertEquals("b", frac.denominator)
    }

    @Test
    fun `sqrt produces sqrt node`() {
        val node = MathParser.parse("sqrt{x}")
        assertTrue(node is MathNode.Sqrt)
        assertEquals("x", (node as MathNode.Sqrt).radicand)
    }

    @Test
    fun `x^{2} produces superscript`() {
        val node = MathParser.parse("x^{2}")
        assertTrue(node is MathNode.Superscript)
        val sup = node as MathNode.Superscript
        assertEquals("x", sup.base)
        assertEquals("2", sup.exponent)
    }

    @Test
    fun `x_{i} produces subscript`() {
        val node = MathParser.parse("x_{i}")
        assertTrue(node is MathNode.Subscript)
        val sub = node as MathNode.Subscript
        assertEquals("x", sub.base)
        assertEquals("i", sub.index)
    }

    @Test
    fun `sum produces sum node`() {
        val node = MathParser.parse("sum_{i=1}^{n}")
        assertTrue(node is MathNode.Sum)
        val sum = node as MathNode.Sum
        assertEquals("i=1", sum.lower)
        assertEquals("n", sum.upper)
    }

    @Test
    fun `greek letters map to unicode`() {
        val node = MathParser.parse("alpha")
        assertTrue(node is MathNode.Symbol)
        assertEquals("α", (node as MathNode.Symbol).text)
    }

    @Test
    fun `empty string produces empty symbol`() {
        val node = MathParser.parse("")
        assertTrue(node is MathNode.Symbol)
        assertEquals("", (node as MathNode.Symbol).text)
    }
}
