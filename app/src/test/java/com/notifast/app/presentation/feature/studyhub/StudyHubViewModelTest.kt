package com.notifast.app.presentation.feature.studyhub

import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import com.notifast.app.domain.model.study.DocxPreviewBlock
import com.notifast.app.domain.model.study.StudyDocSource
import com.notifast.app.domain.model.study.StudyDocType
import com.notifast.app.domain.model.study.StudyDocument
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.GoogleDocsExportRepository
import com.notifast.app.domain.repository.GoogleDocsExportStep
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.usecase.study.AssignDocumentSubjectUseCase
import com.notifast.app.domain.usecase.study.DeleteCoverDraftUseCase
import com.notifast.app.domain.usecase.study.DeleteStudyDocumentUseCase
import com.notifast.app.domain.usecase.study.DuplicateStudyDocumentUseCase
import com.notifast.app.domain.usecase.study.GenerateLocalCoverDocxUseCase
import com.notifast.app.domain.usecase.study.GetStudyDocumentFileUseCase
import com.notifast.app.domain.usecase.study.ImportStudyDocumentUseCase
import com.notifast.app.domain.usecase.study.ObserveCoverDraftsUseCase
import com.notifast.app.domain.usecase.study.ObserveStudyDocumentsUseCase
import com.notifast.app.domain.usecase.study.ParseDocxPreviewUseCase
import com.notifast.app.domain.usecase.study.PrefillCoverDraftForSubjectUseCase
import com.notifast.app.domain.usecase.study.SaveCoverDraftUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class StudyHubViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var authRepository: AuthRepository
    private lateinit var noteRepository: NoteRepository
    private lateinit var observeStudyDocumentsUseCase: ObserveStudyDocumentsUseCase
    private lateinit var importStudyDocumentUseCase: ImportStudyDocumentUseCase
    private lateinit var duplicateStudyDocumentUseCase: DuplicateStudyDocumentUseCase
    private lateinit var assignDocumentSubjectUseCase: AssignDocumentSubjectUseCase
    private lateinit var deleteStudyDocumentUseCase: DeleteStudyDocumentUseCase
    private lateinit var getStudyDocumentFileUseCase: GetStudyDocumentFileUseCase
    private lateinit var parseDocxPreviewUseCase: ParseDocxPreviewUseCase
    private lateinit var observeCoverDraftsUseCase: ObserveCoverDraftsUseCase
    private lateinit var prefillCoverDraftForSubjectUseCase: PrefillCoverDraftForSubjectUseCase
    private lateinit var saveCoverDraftUseCase: SaveCoverDraftUseCase
    private lateinit var deleteCoverDraftUseCase: DeleteCoverDraftUseCase
    private lateinit var generateLocalCoverDocxUseCase: GenerateLocalCoverDocxUseCase
    private lateinit var googleDocsExportRepository: GoogleDocsExportRepository

    private lateinit var viewModel: StudyHubViewModel

    private val sampleDocPdf = StudyDocument(
        id = "d1", userId = "u1", subjectId = "s1", subjectName = "Física",
        title = "Guía Péndulo", fileName = "d1.pdf", docType = StudyDocType.PDF,
        sizeBytes = 2048L, pageCount = 4, sourceType = StudyDocSource.IMPORTED
    )

    private val sampleDocDocx = StudyDocument(
        id = "d2", userId = "u1", subjectId = "s2", subjectName = "Química",
        title = "Informe Estequiometría", fileName = "d2.docx", docType = StudyDocType.DOCX,
        sizeBytes = 4096L, pageCount = 2, sourceType = StudyDocSource.IMPORTED
    )

    private val sampleDraft = CoverDraft(
        id = "draft-1", userId = "u1", templateType = CoverTemplateType.APA_7,
        subjectId = "s1", subjectName = "Física", workTitle = "Informe Final"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        authRepository = mockk(relaxed = true)
        noteRepository = mockk(relaxed = true)
        observeStudyDocumentsUseCase = mockk(relaxed = true)
        importStudyDocumentUseCase = mockk(relaxed = true)
        duplicateStudyDocumentUseCase = mockk(relaxed = true)
        assignDocumentSubjectUseCase = mockk(relaxed = true)
        deleteStudyDocumentUseCase = mockk(relaxed = true)
        getStudyDocumentFileUseCase = mockk(relaxed = true)
        parseDocxPreviewUseCase = mockk(relaxed = true)
        observeCoverDraftsUseCase = mockk(relaxed = true)
        prefillCoverDraftForSubjectUseCase = mockk(relaxed = true)
        saveCoverDraftUseCase = mockk(relaxed = true)
        deleteCoverDraftUseCase = mockk(relaxed = true)
        generateLocalCoverDocxUseCase = mockk(relaxed = true)
        googleDocsExportRepository = mockk(relaxed = true)

        coEvery { authRepository.getCurrentUserId() } returns "u1"
        coEvery { noteRepository.getSubjects("u1") } returns flowOf(
            listOf(
                SubjectData("s1", "Física", "🔬", "#FF0000", "Ciclo 1"),
                SubjectData("s2", "Química", "🧪", "#00FF00", "Ciclo 1")
            )
        )
        coEvery { observeStudyDocumentsUseCase("u1") } returns flowOf(listOf(sampleDocPdf, sampleDocDocx))
        coEvery { observeCoverDraftsUseCase("u1") } returns flowOf(listOf(sampleDraft))

        viewModel = StudyHubViewModel(
            authRepository = authRepository,
            noteRepository = noteRepository,
            observeStudyDocumentsUseCase = observeStudyDocumentsUseCase,
            importStudyDocumentUseCase = importStudyDocumentUseCase,
            duplicateStudyDocumentUseCase = duplicateStudyDocumentUseCase,
            assignDocumentSubjectUseCase = assignDocumentSubjectUseCase,
            deleteStudyDocumentUseCase = deleteStudyDocumentUseCase,
            getStudyDocumentFileUseCase = getStudyDocumentFileUseCase,
            parseDocxPreviewUseCase = parseDocxPreviewUseCase,
            observeCoverDraftsUseCase = observeCoverDraftsUseCase,
            prefillCoverDraftForSubjectUseCase = prefillCoverDraftForSubjectUseCase,
            saveCoverDraftUseCase = saveCoverDraftUseCase,
            deleteCoverDraftUseCase = deleteCoverDraftUseCase,
            generateLocalCoverDocxUseCase = generateLocalCoverDocxUseCase,
            googleDocsExportRepository = googleDocsExportRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads documents and subjects and drafts`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("u1", state.userId)
        assertEquals(2, state.allDocuments.size)
        assertEquals(2, state.filteredDocuments.size)
        assertEquals(2, state.subjects.size)
        assertEquals(1, state.coverDrafts.size)
    }

    @Test
    fun `filtering by DocTypeFilter and Subject updates filteredDocuments`() = runTest {
        advanceUntilIdle()

        // Filter only DOCX
        viewModel.onEvent(StudyHubEvent.SelectDocTypeFilter(DocTypeFilter.DOCX))
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredDocuments.size)
        assertEquals("Informe Estequiometría", viewModel.uiState.value.filteredDocuments.first().title)

        // Filter by subject Física (has only PDF) -> result empty
        viewModel.onEvent(StudyHubEvent.SelectSubjectFilter("Física"))
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.filteredDocuments.size)

        // Reset DocType to ALL -> shows Física PDF
        viewModel.onEvent(StudyHubEvent.SelectDocTypeFilter(DocTypeFilter.ALL))
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.filteredDocuments.size)
        assertEquals("Guía Péndulo", viewModel.uiState.value.filteredDocuments.first().title)
    }

    @Test
    fun `OpenTemplateEditor triggers prefill and sets activeEditorDraft`() = runTest {
        advanceUntilIdle()
        val prefilled = CoverDraft(
            id = "d-pref", userId = "u1", templateType = CoverTemplateType.APA_7,
            subjectId = "s1", subjectName = "Física", authors = listOf("Juan Pérez"),
            teacherName = "Prof. Newton", institutionName = "UCV"
        )
        coEvery { prefillCoverDraftForSubjectUseCase("u1", CoverTemplateType.APA_7, "s1", "Física") } returns prefilled

        viewModel.onEvent(StudyHubEvent.OpenNewTemplateEditor(CoverTemplateType.APA_7, "Física"))
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.activeEditorDraft)
        assertEquals("Prof. Newton", viewModel.uiState.value.activeEditorDraft?.teacherName)
    }

    @Test
    fun `DuplicateDocument calls usecase and sets snackbarMessage`() = runTest {
        advanceUntilIdle()
        val duplicated = sampleDocPdf.copy(id = "d-copy", title = "Guía Péndulo (Copia)")
        coEvery { duplicateStudyDocumentUseCase("u1", "d1") } returns Result.success(duplicated)

        viewModel.onEvent(StudyHubEvent.DuplicateDocument("d1"))
        advanceUntilIdle()

        coVerify { duplicateStudyDocumentUseCase("u1", "d1") }
        assertNotNull(viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `ExportDraftToGoogleDocs sets pendingOpenUrl on success`() = runTest {
        advanceUntilIdle()
        val draft = sampleDraft
        val step = GoogleDocsExportStep.Exported("gdoc-1", "https://docs.google.com/document/d/gdoc-1/edit", draft)
        coEvery { googleDocsExportRepository.exportDraftToGoogleDocs(draft, any()) } returns Result.success(step)

        viewModel.onEvent(StudyHubEvent.ExportDraftToGoogleDocs(draft, null))
        advanceUntilIdle()

        assertEquals("https://docs.google.com/document/d/gdoc-1/edit", viewModel.uiState.value.pendingOpenUrl)
    }
}
