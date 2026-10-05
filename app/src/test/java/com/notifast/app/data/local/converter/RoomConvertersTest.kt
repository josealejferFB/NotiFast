package com.notifast.app.data.local.converter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class RoomConvertersTest {

    private val converters = RoomConverters()

    @Test
    fun `date to timestamp and timestamp to date roundtrip`() {
        val now = Date()
        val timestamp = converters.dateToTimestamp(now)
        val reconstructed = converters.fromTimestamp(timestamp)

        assertEquals(now.time, timestamp)
        assertEquals(now, reconstructed)
    }

    @Test
    fun `null date handles gracefully`() {
        assertNull(converters.dateToTimestamp(null))
        assertNull(converters.fromTimestamp(null))
    }

    @Test
    fun `string list to json and json to string list roundtrip`() {
        val tags = listOf("Parcial", "Fórmula", "Urgente")
        val json = converters.fromStringList(tags)
        val reconstructed = converters.toStringList(json)

        assertEquals(tags, reconstructed)
    }

    @Test
    fun `empty and null string list roundtrip`() {
        val emptyJson = converters.fromStringList(emptyList())
        assertEquals(emptyList<String>(), converters.toStringList(emptyJson))
        assertEquals(emptyList<String>(), converters.toStringList(null))
    }
}
