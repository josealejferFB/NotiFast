package com.notifast.app.domain.util

import java.io.ByteArrayOutputStream

/**
 * Standard Adobe ASCII85 (btoa) stream decoder.
 * Supports ~> delimiter, whitespace ignoring, and 'z' abbreviation for 4 zero bytes.
 */
object Ascii85Decoder {

    private val POW85 = longArrayOf(
        85L * 85L * 85L * 85L, // 85^4 = 52200625
        85L * 85L * 85L,       // 85^3 = 614125
        85L * 85L,             // 85^2 = 7225
        85L,                   // 85^1 = 85
        1L                     // 85^0 = 1
    )

    fun decode(input: ByteArray): ByteArray {
        val out = ByteArrayOutputStream(input.size)
        var count = 0
        val block = IntArray(5)
        var i = 0

        while (i < input.size) {
            val b = input[i].toInt() and 0xFF
            i++

            // Stop at standard ASCII85 terminator ~>
            if (b == '~'.code) {
                if (i < input.size && (input[i].toInt() and 0xFF) == '>'.code) {
                    break
                }
            }

            // Ignore whitespace and control characters
            if (b <= ' '.code) continue

            // 'z' represents four 0x00 bytes
            if (b == 'z'.code) {
                if (count == 0) {
                    out.write(0)
                    out.write(0)
                    out.write(0)
                    out.write(0)
                    continue
                } else {
                    // Invalid 'z' in the middle of a tuple
                    continue
                }
            }

            // Normal ASCII85 character between '!' (33) and 'u' (117)
            if (b in '!'.code..'u'.code) {
                block[count] = b - '!'.code
                count++
                if (count == 5) {
                    var value = 0L
                    for (k in 0..4) {
                        value += block[k] * POW85[k]
                    }
                    out.write(((value shr 24) and 0xFF).toInt())
                    out.write(((value shr 16) and 0xFF).toInt())
                    out.write(((value shr 8) and 0xFF).toInt())
                    out.write((value and 0xFF).toInt())
                    count = 0
                }
            }
        }

        // Handle partial tuple at EOF
        if (count > 1) {
            for (k in count until 5) {
                block[k] = 84 // Pad with 'u' (index 84)
            }
            var value = 0L
            for (k in 0..4) {
                value += block[k] * POW85[k]
            }
            // A tuple of count characters yields count - 1 bytes
            for (k in 0 until (count - 1)) {
                val shift = (3 - k) * 8
                out.write(((value shr shift) and 0xFF).toInt())
            }
        }

        return out.toByteArray()
    }
}
