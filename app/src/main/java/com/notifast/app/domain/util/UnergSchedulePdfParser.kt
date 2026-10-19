package com.notifast.app.domain.util

import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.model.schedule.UnergImportPreviewData
import com.notifast.app.domain.model.schedule.UnergImportPreviewSubject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.regex.Pattern
import java.util.zip.InflaterInputStream
import javax.inject.Inject

/**
 * Native, offline-first PDF parser specifically optimized for UNERG DACE schedule vouchers.
 * Decompresses ReportLab Flate/ASCII85 streams without external heavyweight dependencies.
 */
class UnergSchedulePdfParser @Inject constructor() {

    companion object {
        private val SCHEDULE_TIME_PATTERN = Pattern.compile(
            """(LUNES|MARTES|MI[EÉ]RCOLES|JUEVES|VIERNES|S[AÁ]BADO|DOMINGO|NO PRESENCIAL):\s*(\d{2}:\d{2})(?::\d{2})?\s*-\s*(\d{2}:\d{2})(?::\d{2})?""",
            Pattern.CASE_INSENSITIVE
        )

        private val CI_PATTERN = Pattern.compile(
            """C\.I\.?:\s*(\d+)""",
            Pattern.CASE_INSENSITIVE
        )

        private val PERIOD_PATTERN = Pattern.compile(
            """PER[IÍ]ODO:\s*([^\n-]+)""",
            Pattern.CASE_INSENSITIVE
        )

        private val CAREER_PATTERN = Pattern.compile(
            """CARRERA:\s*(.+?)(?=\s*PER[IÍ]ODO:|\z)""",
            Pattern.CASE_INSENSITIVE
        )
    }

    fun parse(inputStream: InputStream): Result<UnergImportPreviewData> {
        return try {
            val bytes = inputStream.readBytes()
            if (bytes.size < 5 || !bytes.take(5).toByteArray().toString(Charsets.US_ASCII).startsWith("%PDF-")) {
                return Result.failure(IllegalArgumentException("El archivo proporcionado no es un documento PDF válido."))
            }

            val decompressedText = extractAllTextStreams(bytes)
            if (decompressedText.isBlank()) {
                return Result.failure(IllegalStateException("No se pudo extraer contenido de texto del documento PDF."))
            }

            val tokens = extractTextTokens(decompressedText)
            val previewData = parseScheduleData(tokens)
            Result.success(previewData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractAllTextStreams(pdfBytes: ByteArrayListOrArray): String {
        val out = StringBuilder()
        var pos = 0
        val data = pdfBytes

        while (true) {
            val sIdx = indexOf(data, "stream".toByteArray(Charsets.US_ASCII), pos)
            if (sIdx == -1) break
            val eIdx = indexOf(data, "endstream".toByteArray(Charsets.US_ASCII), sIdx)
            if (eIdx == -1) break

            val dictStart = lastIndexOf(data, "<<".toByteArray(Charsets.US_ASCII), sIdx)
            val dictStr = if (dictStart != -1 && dictStart < sIdx) {
                String(data.copyOfRange(dictStart, sIdx), Charsets.ISO_8859_1)
            } else ""

            if (dictStr.contains("FlateDecode")) {
                var streamBytes = data.copyOfRange(sIdx + 6, eIdx)
                streamBytes = trimWhitespace(streamBytes)

                if (dictStr.contains("ASCII85Decode")) {
                    streamBytes = Ascii85Decoder.decode(streamBytes)
                }

                try {
                    val decompressed = decompressFlate(streamBytes)
                    out.append(String(decompressed, Charsets.ISO_8859_1)).append("\n")
                } catch (_: Exception) {
                    // Ignore non-standard or image streams
                }
            }

            pos = eIdx + 9
        }

        return out.toString()
    }

    private fun decompressFlate(input: ByteArray): ByteArray {
        val bis = ByteArrayInputStream(input)
        val iis = InflaterInputStream(bis)
        val bos = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        var read: Int
        while (iis.read(buffer).also { read = it } != -1) {
            bos.write(buffer, 0, read)
        }
        return bos.toByteArray()
    }

    private fun extractTextTokens(decompressedPdfText: String): List<String> {
        val tokens = mutableListOf<String>()
        val tjPattern = Pattern.compile("""\(((?:\\\(|\\\)|\\\\|[^\)])*)\)\s*Tj""")
        val matcher = tjPattern.matcher(decompressedPdfText)

        while (matcher.find()) {
            val raw = matcher.group(1) ?: continue
            val unescaped = unescapePdfString(raw)
            if (unescaped.isNotBlank()) {
                tokens.add(unescaped.trim())
            }
        }
        return tokens
    }

    private fun unescapePdfString(raw: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < raw.length) {
            val c = raw[i]
            if (c == '\\' && i + 1 < raw.length) {
                val next = raw[i + 1]
                if (next in '0'..'7') {
                    var octal = "" + next
                    i += 2
                    if (i < raw.length && raw[i] in '0'..'7') {
                        octal += raw[i]
                        i++
                        if (i < raw.length && raw[i] in '0'..'7') {
                            octal += raw[i]
                            i++
                        }
                    }
                    val code = octal.toIntOrNull(8) ?: 32
                    sb.append(code.toChar())
                    continue
                } else {
                    when (next) {
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000c')
                        '(' -> sb.append('(')
                        ')' -> sb.append(')')
                        '\\' -> sb.append('\\')
                        else -> sb.append(next)
                    }
                    i += 2
                    continue
                }
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    private fun parseScheduleData(tokens: List<String>): UnergImportPreviewData {
        var studentCi = ""
        var career = ""
        var academicPeriod = ""

        // Extract metadata from header tokens
        for (i in tokens.indices) {
            val t = tokens[i]
            if (studentCi.isEmpty()) {
                val ciMatcher = CI_PATTERN.matcher(t)
                if (ciMatcher.find()) {
                    studentCi = ciMatcher.group(1).orEmpty()
                }
            }
            if (academicPeriod.isEmpty()) {
                val perMatcher = PERIOD_PATTERN.matcher(t)
                if (perMatcher.find()) {
                    academicPeriod = perMatcher.group(1).orEmpty().trim()
                }
            }
            if (career.isEmpty()) {
                val carMatcher = CAREER_PATTERN.matcher(t)
                if (carMatcher.find()) {
                    career = carMatcher.group(1).orEmpty().trim()
                }
            }
        }

        // Find table start after table header tokens
        var startIndex = 0
        for (i in tokens.indices) {
            if (tokens[i].equals("AULA", ignoreCase = true) || tokens[i].equals("CLASE", ignoreCase = true)) {
                startIndex = i + 1
            }
        }

        // Group rows into subjects
        val previewSubjects = mutableListOf<UnergImportPreviewSubject>()
        var ignoredNonPresencialCount = 0
        var totalPresencialHours = 0

        var currentIndex = startIndex
        while (currentIndex < tokens.size) {
            val token = tokens[currentIndex]

            // Stop when reaching footer signatures or notes
            if (token.startsWith("SCIH", ignoreCase = true) ||
                token.startsWith("Este documento", ignoreCase = true) ||
                token.startsWith("D.A.C.E", ignoreCase = true)
            ) {
                break
            }

            // A subject block starts with a subject code (e.g. "EL", "PG0083", "IS0222")
            // followed by Subject Name, Section, and schedule pairs.
            if (isSubjectCode(token) && currentIndex + 2 < tokens.size) {
                val subjectNameRaw = tokens[currentIndex + 1]
                val sectionCandidate = tokens[currentIndex + 2]

                // Validate that sectionCandidate is indeed a section (e.g. "1", "2", "19", "01")
                if (sectionCandidate.matches(Regex("""\d{1,4}"""))) {
                    val section = sectionCandidate
                    val subjectName = cleanSubjectName(subjectNameRaw)
                    currentIndex += 3 // Skip code, name, section

                    // Skip any dash '-' tokens before schedules
                    while (currentIndex < tokens.size && tokens[currentIndex] == "-") {
                        currentIndex++
                    }

                    // Extract schedule pairs (TimePattern, Classroom)
                    val schedules = mutableListOf<Schedule>()
                    while (currentIndex < tokens.size) {
                        val scheduleToken = tokens[currentIndex]
                        val matcher = SCHEDULE_TIME_PATTERN.matcher(scheduleToken)
                        if (matcher.matches()) {
                            val dayOrStatus = matcher.group(1).orEmpty().uppercase()
                            val startTime = matcher.group(2).orEmpty()
                            val endTime = matcher.group(3).orEmpty()
                            currentIndex++

                            val classroomToken = if (currentIndex < tokens.size) tokens[currentIndex] else ""
                            currentIndex++

                            if (dayOrStatus == "NO PRESENCIAL" ||
                                classroomToken.contains("distancia", ignoreCase = true) ||
                                classroomToken.contains("NO PRESENCIAL", ignoreCase = true)
                            ) {
                                ignoredNonPresencialCount++
                            } else {
                                val dayOfWeek = mapDayOfWeek(dayOrStatus)
                                val durationHours = calculateDurationHours(startTime, endTime)
                                totalPresencialHours += durationHours

                                schedules.add(
                                    Schedule(
                                        id = UUID.randomUUID().toString(),
                                        subjectName = subjectName,
                                        dayOfWeek = dayOfWeek,
                                        startTime = startTime,
                                        endTime = endTime,
                                        classroom = classroomToken.trim(),
                                        modality = "Presencial"
                                    )
                                )
                            }
                        } else {
                            // Reached next subject or end of table
                            break
                        }
                    }

                    if (schedules.isNotEmpty()) {
                        val style = SubjectStyleAssigner.assignStyle(subjectName)
                        val classrooms = schedules.map { it.classroom }.distinct().joinToString(", ")
                        val hoursPerWeek = schedules.sumOf { calculateDurationHours(it.startTime, it.endTime) }

                        previewSubjects.add(
                            UnergImportPreviewSubject(
                                subjectName = subjectName,
                                section = section,
                                hoursPerWeek = hoursPerWeek,
                                classroomSummary = classrooms,
                                schedules = schedules,
                                isExistingSubject = false,
                                colorHex = style.colorHex,
                                icon = style.icon
                            )
                        )
                    }
                    continue
                }
            }

            currentIndex++
        }

        return UnergImportPreviewData(
            studentCi = studentCi,
            career = career,
            academicPeriod = academicPeriod,
            subjects = previewSubjects,
            totalPresencialHours = totalPresencialHours,
            ignoredNonPresencialCount = ignoredNonPresencialCount
        )
    }

    private fun isSubjectCode(token: String): Boolean {
        // e.g. "EL", "PG0083", "IS0222", "ID0221", "EA0244", "IT9241"
        return token.matches(Regex("""^[A-Z]{2,4}\d{0,4}$"""))
    }

    private fun cleanSubjectName(raw: String): String {
        val trimmed = raw.trim().replace(Regex("""\s+"""), " ")
        return if (trimmed.length > 50) trimmed.take(50).trim() else trimmed
    }

    private fun mapDayOfWeek(dayName: String): Int {
        return when {
            dayName.contains("LUNES") -> 1
            dayName.contains("MARTES") -> 2
            dayName.contains("MI") -> 3
            dayName.contains("JUEVES") -> 4
            dayName.contains("VIERNES") -> 5
            dayName.contains("S") -> 6
            dayName.contains("DOMINGO") -> 7
            else -> 1
        }
    }

    private fun calculateDurationHours(startTime: String, endTime: String): Int {
        return try {
            val (sh, sm) = startTime.split(":").map { it.toInt() }
            val (eh, em) = endTime.split(":").map { it.toInt() }
            val diffMinutes = (eh * 60 + em) - (sh * 60 + sm)
            maxOf(1, (diffMinutes + 30) / 60)
        } catch (_: Exception) {
            2
        }
    }

    private fun indexOf(data: ByteArray, target: ByteArray, start: Int): Int {
        if (target.isEmpty() || start >= data.size) return -1
        outer@ for (i in start..(data.size - target.size)) {
            for (j in target.indices) {
                if (data[i + j] != target[j]) continue@outer
            }
            return i
        }
        return -1
    }

    private fun lastIndexOf(data: ByteArray, target: ByteArray, before: Int): Int {
        if (target.isEmpty()) return -1
        val maxStart = minOf(before - target.size, data.size - target.size)
        outer@ for (i in maxStart downTo 0) {
            for (j in target.indices) {
                if (data[i + j] != target[j]) continue@outer
            }
            return i
        }
        return -1
    }

    private fun trimWhitespace(data: ByteArray): ByteArray {
        var start = 0
        while (start < data.size && data[start] <= ' '.code.toByte()) {
            start++
        }
        var end = data.size - 1
        while (end >= start && data[end] <= ' '.code.toByte()) {
            end--
        }
        return if (start == 0 && end == data.size - 1) data else data.copyOfRange(start, end + 1)
    }
}

private typealias ByteArrayListOrArray = ByteArray
