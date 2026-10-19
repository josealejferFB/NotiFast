package com.notifast.app.data.repository

import android.content.Context
import com.notifast.app.data.local.dao.CoverDraftDao
import com.notifast.app.data.local.dao.StudyDocumentDao
import com.notifast.app.data.local.entity.CoverDraftEntity
import com.notifast.app.data.local.entity.StudyDocumentEntity
import com.notifast.app.data.study.OpenXmlDocxGenerator
import com.notifast.app.data.study.OpenXmlDocxParser
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import com.notifast.app.domain.model.study.StudyDocSource
import com.notifast.app.domain.model.study.StudyDocType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class LocalStudyDocumentRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var studyDocDao: StudyDocumentDao
    private lateinit var coverDraftDao: CoverDraftDao
    private lateinit var docxParser: OpenXmlDocxParser
    private lateinit var docxGenerator: OpenXmlDocxGenerator
    private lateinit var repository: LocalStudyDocumentRepositoryImpl
    private lateinit var tempStudyDocsDir: File

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        studyDocDao = mockk(relaxed = true)
        coverDraftDao = mockk(relaxed = true)
        docxParser = OpenXmlDocxParser()
        docxGenerator = OpenXmlDocxGenerator()

        tempStudyDocsDir = File.createTempFile("study_docs_test", "")
        tempStudyDocsDir.delete()
        tempStudyDocsDir.mkdirs()

        every { context.filesDir } returns tempStudyDocsDir.parentFile

        repository = LocalStudyDocumentRepositoryImpl(
            context = context,
            studyDocDao = studyDocDao,
            coverDraftDao = coverDraftDao,
            docxParser = docxParser,
            docxGenerator = docxGenerator,
            docsDirOverride = tempStudyDocsDir
        )
    }

    @After
    fun tearDown() {
        tempStudyDocsDir.deleteRecursively()
    }

    @Test
    fun `duplicateDocument copies physical file in study_docs and appends Copia to title`() = runTest {
        // 1. Create original physical file
        val originalFile = File(tempStudyDocsDir, "orig-uuid.pdf")
        originalFile.writeText("sample pdf binary data")

        val origEntity = StudyDocumentEntity(
            id = "doc-1",
            userId = "u1",
            subjectId = "s1",
            subjectName = "Física",
            title = "Guía Lab 1",
            fileName = "orig-uuid.pdf",
            docType = "PDF",
            sizeBytes = originalFile.length(),
            pageCount = 3,
            sourceType = "IMPORTED",
            googleDocId = null,
            googleWebViewLink = null,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        coEvery { studyDocDao.getDocumentById("u1", "doc-1") } returns origEntity

        val result = repository.duplicateDocument("u1", "doc-1")
        assertTrue(result.isSuccess)
        val duplicated = result.getOrThrow()

        assertEquals("Guía Lab 1 (Copia)", duplicated.title)
        assertEquals(StudyDocSource.DUPLICATED, duplicated.sourceType)

        // Verify cloned file exists physically
        val clonedFile = File(tempStudyDocsDir, duplicated.fileName)
        assertTrue(clonedFile.exists())
        assertEquals("sample pdf binary data", clonedFile.readText())

        coVerify { studyDocDao.upsertDocument(match { it.title == "Guía Lab 1 (Copia)" && it.sourceType == "DUPLICATED" }) }
    }

    @Test
    fun `deleteDocument removes physical file from study_docs and deletes row in DAO`() = runTest {
        val fileToDelete = File(tempStudyDocsDir, "to-delete.pdf")
        fileToDelete.writeText("delete me")
        assertTrue(fileToDelete.exists())

        val entity = StudyDocumentEntity(
            id = "doc-del",
            userId = "u1",
            subjectId = "s1",
            subjectName = "Química",
            title = "Tabla Periódica",
            fileName = "to-delete.pdf",
            docType = "PDF",
            sizeBytes = fileToDelete.length(),
            pageCount = 1,
            sourceType = "IMPORTED",
            createdAt = 1000L,
            updatedAt = 1000L
        )

        coEvery { studyDocDao.getDocumentById("u1", "doc-del") } returns entity

        val result = repository.deleteDocument("u1", "doc-del")
        assertTrue(result.isSuccess)

        // File should be physically gone
        assertFalse(fileToDelete.exists())
        coVerify { studyDocDao.deleteDocument("u1", "doc-del") }
    }

    @Test
    fun `generateLocalDocxFromDraft sanitizes illegal title characters while keeping full title in entity`() = runTest {
        val draft = CoverDraft(
            id = "d1",
            userId = "u1",
            templateType = CoverTemplateType.APA_7,
            subjectId = "s1",
            subjectName = "Física",
            workTitle = "Informe: Lab 1 / Péndulo *Especial* <Final>?",
            subtitle = "Práctica",
            authors = listOf("Juan Pérez"),
            teacherName = "Prof. Newton",
            institutionName = "UCV",
            facultyOrDepartment = "Ciencias",
            city = "Caracas",
            formattedDate = "2026"
        )

        val result = repository.generateLocalDocxFromDraft(draft)
        assertTrue(result.isSuccess)
        val generatedDoc = result.getOrThrow()

        assertEquals("Informe: Lab 1 / Péndulo *Especial* <Final>?", generatedDoc.title)
        assertEquals(StudyDocType.DOCX, generatedDoc.docType)
        assertEquals(StudyDocSource.GENERATED_TEMPLATE, generatedDoc.sourceType)

        // Verify physical file was written and can be parsed
        val docxFile = repository.getDocumentFile(generatedDoc.fileName)
        assertTrue(docxFile.exists())
        assertTrue(docxFile.length() > 0)

        val parseResult = repository.parseDocxPreview(generatedDoc.fileName)
        assertTrue(parseResult.isSuccess)
    }

    @Test
    fun `observeDocuments maps entities to domain models`() = runTest {
        val entity = StudyDocumentEntity(
            id = "doc-1",
            userId = "u1",
            subjectId = "s1",
            subjectName = "Matemáticas",
            title = "Cálculo I",
            fileName = "calc.pdf",
            docType = "PDF",
            sizeBytes = 1024L,
            pageCount = 10,
            sourceType = "IMPORTED",
            createdAt = 1000L,
            updatedAt = 1000L
        )

        every { studyDocDao.observeDocuments("u1") } returns flowOf(listOf(entity))

        val docs = repository.observeDocuments("u1").first()
        assertEquals(1, docs.size)
        assertEquals("Cálculo I", docs.first().title)
    }
}
