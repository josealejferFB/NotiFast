package com.notifast.app.data.backup

import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CoverBackupArchiveTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var codec: BackupArchiveCodec
    private lateinit var hasher: ContentHasher

    @Before
    fun setUp() {
        codec = BackupArchiveCodec(ImageOptimizer())
        hasher = ContentHasher()
    }

    @Test
    fun covers_round_trip_preserves_templates_and_metadata() {
        val workDir = tempFolder.newFolder("work")
        val extractDir = tempFolder.newFolder("extract")
        val outFile = File(tempFolder.newFolder("out"), "covers.nfbak")

        val draft1 = CoverDraft(
            id = "draft-1",
            userId = "user-1",
            templateType = CoverTemplateType.APA_7,
            workTitle = "Impacto de la IA en la Educación Superior",
            subtitle = "Estudio empírico 2026",
            authors = listOf("Ada Lovelace", "Alan Turing"),
            institutionName = "Universidad Nacional",
            teacherName = "Prof. Gauss",
            tutorName = "Dr. Euler"
        )

        val draft2 = CoverDraft(
            id = "draft-2",
            userId = "user-1",
            templateType = CoverTemplateType.UNERG_DEGREE,
            workTitle = "Sistema de Gestión Académica Offline-First",
            countryLine = "REPÚBLICA BOLIVARIANA DE VENEZUELA",
            programLine = "INGENIERÍA EN INFORMÁTICA",
            authors = listOf("Grace Hopper")
        )

        val originalDrafts = listOf(draft1, draft2)

        val writeResult = codec.writeCovers(originalDrafts, outFile, workDir)
        assertTrue("writeCovers should succeed: ${writeResult.exceptionOrNull()}", writeResult.isSuccess)

        val readResult = codec.readCovers(outFile, extractDir)
        assertTrue("readCovers should succeed: ${readResult.exceptionOrNull()}", readResult.isSuccess)

        val restoredDrafts = readResult.getOrThrow()
        assertEquals(2, restoredDrafts.size)
        assertEquals("draft-1", restoredDrafts[0].id)
        assertEquals(CoverTemplateType.APA_7, restoredDrafts[0].templateType)
        assertEquals("Impacto de la IA en la Educación Superior", restoredDrafts[0].workTitle)
        assertEquals(listOf("Ada Lovelace", "Alan Turing"), restoredDrafts[0].authors)

        assertEquals("draft-2", restoredDrafts[1].id)
        assertEquals(CoverTemplateType.UNERG_DEGREE, restoredDrafts[1].templateType)
    }

    @Test
    fun hashing_covers_is_deterministic_and_order_independent() {
        val d1 = CoverDraft(id = "a", userId = "u1", templateType = CoverTemplateType.APA_7, workTitle = "Tesis 1")
        val d2 = CoverDraft(id = "b", userId = "u1", templateType = CoverTemplateType.IEEE, workTitle = "Paper 2")

        val hash1 = hasher.hashCovers(listOf(d1, d2))
        val hashReordered = hasher.hashCovers(listOf(d2, d1))
        val hashModified = hasher.hashCovers(listOf(d1.copy(workTitle = "Tesis Modificada"), d2))

        assertEquals("Hash must be order-independent based on draft id sorting", hash1, hashReordered)
        assertNotEquals("Hash must change when title changes", hash1, hashModified)
    }
}
