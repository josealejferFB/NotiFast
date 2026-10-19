package com.notifast.app.domain.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class Ascii85DecoderTest {

    @Test
    fun `decode standard ascii85 string matches original text`() {
        val input = "87cURD]i,\"Ebo80~>"
        val expected = "Hello World!"
        val decoded = Ascii85Decoder.decode(input.toByteArray(Charsets.US_ASCII))
        assertEquals(expected, String(decoded, Charsets.US_ASCII))
    }

    @Test
    fun `decode with z zero shortcut produces four zero bytes`() {
        val input = "z~>"
        val expected = byteArrayOf(0, 0, 0, 0)
        val decoded = Ascii85Decoder.decode(input.toByteArray(Charsets.US_ASCII))
        assertArrayEquals(expected, decoded)
    }

    @Test
    fun `decode ignores whitespace and newlines within stream`() {
        val input = " 87cUR\n D]i,\"  \r\n Ebo80 ~>"
        val expected = "Hello World!"
        val decoded = Ascii85Decoder.decode(input.toByteArray(Charsets.US_ASCII))
        assertEquals(expected, String(decoded, Charsets.US_ASCII))
    }
}
