package com.notifast.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeParserTest {

    @Test
    fun `rejects garbage`() {
        assertNull(TimeParser.toMinutes("foo"))
        assertNull(TimeParser.toMinutes(""))
        assertNull(TimeParser.toMinutes("25:00"))
        assertNull(TimeParser.toMinutes("10:60"))
    }

    @Test
    fun `accepts HH mm`() {
        assertEquals(510, TimeParser.toMinutes("08:30"))
        assertEquals(1439, TimeParser.toMinutes("23:59"))
        assertEquals(0, TimeParser.toMinutes("00:00"))
    }

    @Test
    fun `isValidRange requires start before end`() {
        assertTrue(TimeParser.isValidRange("10:00", "11:00"))
        assertFalse(TimeParser.isValidRange("10:00", "09:00"))
        assertFalse(TimeParser.isValidRange("foo", "11:00"))
    }

    @Test
    fun `overlaps half-open`() {
        assertFalse(TimeParser.overlaps("10:00", "11:00", "11:00", "12:00"))
        assertTrue(TimeParser.overlaps("10:00", "11:30", "11:00", "12:00"))
    }
}
