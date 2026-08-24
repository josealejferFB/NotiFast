package com.notifast.app.data.model.package_data

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.EvaluationType
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SubjectPackageCodecTest {

    private val sampleSubject = SubjectData(
        name = "Estructuras de Datos",
        colorHex = "#4338CA",
        icon = "💻"
    )

    private val sampleSchedules = listOf(
        Schedule(
            id = "s1",
            subjectName = "Estructuras de Datos",
            dayOfWeek = 2,
            startTime = "10:00",
            endTime = "12:00",
            classroom = "Lab 4",
            teacher = "Prof. Turing",
            modality = Schedule.MODALITY_PRESENCIAL
        )
    )

    private val sampleEvaluations = listOf(
        Evaluation(
            id = "e1",
            subjectName = "Estructuras de Datos",
            title = "Parcial 1 - Grafos y Árboles",
            description = "Árboles AVL y algoritmos Dijkstra/BFS",
            type = EvaluationType.EXAM.name,
            weight = 35.0
        )
    )

    private val sampleNotes = listOf(
        Note(
            id = "n1",
            subject = "Estructuras de Datos",
            title = "Complejidad Asintótica Big-O",
            content = "O(1) < O(log n) < O(n) < O(n log n) < O(n^2)",
            tags = listOf("teoria", "algoritmos")
        )
    )

    private val samplePackage = SubjectPackage(
        schemaVersion = 1,
        exportTimestamp = 1724450000000L,
        subject = sampleSubject,
        schedules = sampleSchedules,
        evaluations = sampleEvaluations,
        notes = sampleNotes
    )

    @Test
    fun `encode and decode JSON preserves all entities and metadata`() {
        val json = SubjectPackageCodec.toJson(samplePackage)
        assertNotNull(json)
        assertTrue(json.contains("Estructuras de Datos"))

        val decoded = SubjectPackageCodec.fromJson(json)
        assertEquals(samplePackage.schemaVersion, decoded.schemaVersion)
        assertEquals(samplePackage.subject.name, decoded.subject.name)
        assertEquals(samplePackage.subject.colorHex, decoded.subject.colorHex)
        assertEquals(samplePackage.subject.icon, decoded.subject.icon)
        
        assertEquals(1, decoded.schedules.size)
        assertEquals("Lab 4", decoded.schedules.first().classroom)
        assertEquals("Prof. Turing", decoded.schedules.first().teacher)

        assertEquals(1, decoded.evaluations.size)
        assertEquals("Parcial 1 - Grafos y Árboles", decoded.evaluations.first().title)
        assertEquals(35.0, decoded.evaluations.first().weight ?: 0.0, 0.01)

        assertEquals(1, decoded.notes.size)
        assertEquals("Complejidad Asintótica Big-O", decoded.notes.first().title)
        assertEquals(listOf("teoria", "algoritmos"), decoded.notes.first().tags)
    }

    @Test
    fun `encode to compressed Base64 produces compact string and decodes correctly`() {
        val compressed = SubjectPackageCodec.encodeToCompressedString(samplePackage)
        assertNotNull(compressed)
        assertTrue(compressed.startsWith("NOTIFAST1:"))

        val decoded = SubjectPackageCodec.decodeFromCompressedString(compressed)
        assertEquals(samplePackage.subject.name, decoded.subject.name)
        assertEquals(samplePackage.schedules.size, decoded.schedules.size)
        assertEquals(samplePackage.evaluations.size, decoded.evaluations.size)
        assertEquals(samplePackage.notes.size, decoded.notes.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `decoding invalid string throws IllegalArgumentException`() {
        SubjectPackageCodec.decodeFromCompressedString("INVALID_CORRUPTED_STRING")
    }
}
