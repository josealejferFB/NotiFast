package com.notifast.app.presentation.feature.studyhub

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudyHubViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository,
    private val observeStudyDocumentsUseCase: ObserveStudyDocumentsUseCase,
    private val importStudyDocumentUseCase: ImportStudyDocumentUseCase,
    private val duplicateStudyDocumentUseCase: DuplicateStudyDocumentUseCase,
    private val assignDocumentSubjectUseCase: AssignDocumentSubjectUseCase,
    private val deleteStudyDocumentUseCase: DeleteStudyDocumentUseCase,
    private val getStudyDocumentFileUseCase: GetStudyDocumentFileUseCase,
    private val parseDocxPreviewUseCase: ParseDocxPreviewUseCase,
    private val observeCoverDraftsUseCase: ObserveCoverDraftsUseCase,
    private val prefillCoverDraftForSubjectUseCase: PrefillCoverDraftForSubjectUseCase,
    private val saveCoverDraftUseCase: SaveCoverDraftUseCase,
    private val deleteCoverDraftUseCase: DeleteCoverDraftUseCase,
    private val generateLocalCoverDocxUseCase: GenerateLocalCoverDocxUseCase,
    private val googleDocsExportRepository: GoogleDocsExportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyHubUiState())
    val uiState: StateFlow<StudyHubUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: ""
            _uiState.update { it.copy(userId = userId) }
            if (userId.isBlank()) return@launch

            launch {
                authRepository.observeUserProfile(userId)
                    .catch { emit(null) }
                    .collect { user ->
                        _uiState.update {
                            it.copy(
                                userFirstName = user?.firstName.orEmpty(),
                                userEmail = user?.email.orEmpty(),
                                userProfilePictureUrl = user?.profilePictureUrl
                            )
                        }
                    }
            }

            launch {
                noteRepository.getSubjects(userId)
                    .catch { emit(emptyList()) }
                    .collect { subjects ->
                        _uiState.update { it.copy(subjects = subjects) }
                    }
            }

            launch {
                observeCoverDraftsUseCase(userId)
                    .catch { emit(emptyList()) }
                    .collect { drafts ->
                        _uiState.update { it.copy(coverDrafts = drafts) }
                    }
            }

            launch {
                observeStudyDocumentsUseCase(userId)
                    .catch { emit(emptyList()) }
                    .collect { docs ->
                        _uiState.update { state ->
                            state.copy(
                                allDocuments = docs,
                                filteredDocuments = filterDocuments(
                                    docs,
                                    state.selectedSubjectFilter,
                                    state.selectedDocTypeFilter,
                                    state.searchQuery
                                )
                            )
                        }
                    }
            }
        }
    }

    fun onEvent(event: StudyHubEvent) {
        when (event) {
            is StudyHubEvent.LoadData -> loadData()
            is StudyHubEvent.SelectTab -> _uiState.update { it.copy(selectedTab = event.tab) }
            is StudyHubEvent.SelectSubjectFilter -> updateSubjectFilter(event.subjectName)
            is StudyHubEvent.SelectDocTypeFilter -> updateDocTypeFilter(event.filter)
            is StudyHubEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is StudyHubEvent.ImportDocument -> importDocument(event)
            is StudyHubEvent.DuplicateDocument -> duplicateDocument(event.documentId)
            is StudyHubEvent.AssignSubject -> assignSubject(event)
            is StudyHubEvent.DeleteDocument -> deleteDocument(event.documentId)
            is StudyHubEvent.SetActiveActionDocument -> _uiState.update { it.copy(activeActionDocument = event.document) }
            is StudyHubEvent.SetAssignSubjectDialogOpen -> _uiState.update { it.copy(isAssignSubjectDialogOpen = event.open) }
            is StudyHubEvent.SetDeleteDocumentDialogOpen -> _uiState.update { it.copy(isDeleteDocumentDialogOpen = event.open) }
            is StudyHubEvent.OpenPreview -> openPreview(event.document)
            is StudyHubEvent.DismissPreview -> _uiState.update {
                it.copy(activePreviewDocument = null, activePreviewFile = null, docxPreviewBlocks = emptyList())
            }
            is StudyHubEvent.OpenNewTemplateEditor -> openNewTemplateEditor(event.templateType, event.subjectName)
            is StudyHubEvent.EditExistingDraft -> _uiState.update { it.copy(activeEditorDraft = event.draft) }
            is StudyHubEvent.UpdateDraftField -> _uiState.update { state ->
                state.copy(activeEditorDraft = state.activeEditorDraft?.let(event.transform))
            }
            is StudyHubEvent.SaveDraft -> saveDraft(event.draft)
            is StudyHubEvent.GenerateLocalDocx -> generateLocalDocx(event.draft)
            is StudyHubEvent.ExportDraftToGoogleDocs -> exportToGoogleDocs(event.draft, event.context)
            is StudyHubEvent.CompleteAuthResolution -> handleAuthResolution(event.isSuccess, event.resultIntent)
            is StudyHubEvent.DeleteDraft -> deleteDraft(event.draftId)
            is StudyHubEvent.DismissEditor -> _uiState.update { it.copy(activeEditorDraft = null) }
            is StudyHubEvent.ClearSnackbar -> _uiState.update { it.copy(snackbarMessage = null) }
            is StudyHubEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
            is StudyHubEvent.ClearPendingOpenUrl -> _uiState.update { it.copy(pendingOpenUrl = null) }
        }
    }

    private fun updateSubjectFilter(subjectName: String?) {
        _uiState.update { state ->
            state.copy(
                selectedSubjectFilter = subjectName,
                filteredDocuments = filterDocuments(state.allDocuments, subjectName, state.selectedDocTypeFilter, state.searchQuery)
            )
        }
    }

    private fun updateDocTypeFilter(filter: DocTypeFilter) {
        _uiState.update { state ->
            state.copy(
                selectedDocTypeFilter = filter,
                filteredDocuments = filterDocuments(state.allDocuments, state.selectedSubjectFilter, filter, state.searchQuery)
            )
        }
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredDocuments = filterDocuments(state.allDocuments, state.selectedSubjectFilter, state.selectedDocTypeFilter, query)
            )
        }
    }

    private fun filterDocuments(
        docs: List<StudyDocument>,
        subject: String?,
        filter: DocTypeFilter,
        query: String
    ): List<StudyDocument> = docs.filter { doc ->
        val matchesSubject = subject.isNullOrBlank() || doc.subjectName.equals(subject, ignoreCase = true)
        val matchesType = when (filter) {
            DocTypeFilter.ALL -> true
            DocTypeFilter.PDF -> doc.docType == StudyDocType.PDF
            DocTypeFilter.DOCX -> doc.docType == StudyDocType.DOCX
        }
        val matchesQuery = query.isBlank() ||
            doc.title.contains(query, ignoreCase = true) ||
            doc.fileName.contains(query, ignoreCase = true) ||
            doc.subjectName.contains(query, ignoreCase = true)
        matchesSubject && matchesType && matchesQuery
    }

    private fun importDocument(event: StudyHubEvent.ImportDocument) {
        viewModelScope.launch {
            val userId = _uiState.value.userId
            if (userId.isBlank()) return@launch
            importStudyDocumentUseCase(
                userId = userId,
                uri = event.uri,
                subjectId = event.preferredSubjectId,
                subjectName = event.preferredSubjectName
            ).onSuccess { doc ->
                _uiState.update { it.copy(snackbarMessage = "Documento importado: ${doc.title}") }
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Error al importar documento") }
            }
        }
    }

    private fun duplicateDocument(documentId: String) {
        viewModelScope.launch {
            val userId = _uiState.value.userId
            duplicateStudyDocumentUseCase(userId, documentId)
                .onSuccess { doc ->
                    _uiState.update { it.copy(snackbarMessage = "Copia creada: ${doc.title}") }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(errorMessage = err.message ?: "Error al duplicar") }
                }
        }
    }

    private fun assignSubject(event: StudyHubEvent.AssignSubject) {
        viewModelScope.launch {
            val userId = _uiState.value.userId
            assignDocumentSubjectUseCase(userId, event.documentId, event.subjectId, event.subjectName)
                .onSuccess {
                    _uiState.update { it.copy(snackbarMessage = "Materia asignada correctamente", isAssignSubjectDialogOpen = false, activeActionDocument = null) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(errorMessage = err.message ?: "Error al asignar materia", isAssignSubjectDialogOpen = false, activeActionDocument = null) }
                }
        }
    }

    private fun deleteDocument(documentId: String) {
        viewModelScope.launch {
            val userId = _uiState.value.userId
            deleteStudyDocumentUseCase(userId, documentId)
                .onSuccess {
                    _uiState.update { it.copy(snackbarMessage = "Documento eliminado", isDeleteDocumentDialogOpen = false, activeActionDocument = null) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(errorMessage = err.message ?: "Error al eliminar") }
                }
        }
    }

    private fun openPreview(document: StudyDocument) {
        viewModelScope.launch {
            _uiState.update { it.copy(activePreviewDocument = document, isPreviewLoading = true) }
            val file = getStudyDocumentFileUseCase(document.fileName)
            val blocks = if (document.docType == StudyDocType.DOCX) {
                parseDocxPreviewUseCase(document.fileName).getOrDefault(emptyList())
            } else emptyList()

            _uiState.update {
                it.copy(
                    activePreviewFile = file,
                    docxPreviewBlocks = blocks,
                    isPreviewLoading = false
                )
            }
        }
    }

    private fun openNewTemplateEditor(templateType: CoverTemplateType, subjectName: String?) {
        viewModelScope.launch {
            val userId = _uiState.value.userId
            val subjectId = _uiState.value.subjects.find { it.name.equals(subjectName, ignoreCase = true) }?.id.orEmpty()
            val draft = prefillCoverDraftForSubjectUseCase(userId, templateType, subjectId, subjectName.orEmpty())
            _uiState.update { it.copy(activeEditorDraft = draft) }
        }
    }

    private fun saveDraft(draft: CoverDraft) {
        viewModelScope.launch {
            saveCoverDraftUseCase(draft)
            _uiState.update { it.copy(snackbarMessage = "Borrador guardado", activeEditorDraft = null) }
        }
    }

    private fun generateLocalDocx(draft: CoverDraft) {
        viewModelScope.launch {
            generateLocalCoverDocxUseCase(draft)
                .onSuccess {
                    _uiState.update { it.copy(snackbarMessage = "Borrador y .DOCX guardados", activeEditorDraft = null) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(errorMessage = err.message ?: "Error al generar archivo .DOCX") }
                }
        }
    }

    private fun exportToGoogleDocs(draft: CoverDraft, context: Context?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            googleDocsExportRepository.exportDraftToGoogleDocs(draft, context)
                .onSuccess { step ->
                    when (step) {
                        is GoogleDocsExportStep.NeedsResolution -> {
                            _uiState.update { it.copy(pendingAuthIntent = step.pendingIntent, isExporting = false) }
                        }
                        is GoogleDocsExportStep.Exported -> {
                            _uiState.update {
                                it.copy(
                                    isExporting = false,
                                    pendingOpenUrl = step.webViewUrl,
                                    activeEditorDraft = step.updatedDraft,
                                    snackbarMessage = "¡Documento creado en Google Docs!"
                                )
                            }
                        }
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isExporting = false, errorMessage = err.message ?: "Error al exportar a Google Docs") }
                }
        }
    }

    private fun handleAuthResolution(isSuccess: Boolean, resultIntent: Intent?) {
        _uiState.update { it.copy(pendingAuthIntent = null) }
        if (isSuccess) {
            val draft = _uiState.value.activeEditorDraft ?: return
            viewModelScope.launch {
                _uiState.update { it.copy(isExporting = true) }
                googleDocsExportRepository.completeExportAfterResolution(resultIntent, draft)
                    .onSuccess { step ->
                        when (step) {
                            is GoogleDocsExportStep.NeedsResolution -> {
                                _uiState.update { it.copy(pendingAuthIntent = step.pendingIntent, isExporting = false) }
                            }
                            is GoogleDocsExportStep.Exported -> {
                                _uiState.update {
                                    it.copy(
                                        isExporting = false,
                                        pendingOpenUrl = step.webViewUrl,
                                        activeEditorDraft = step.updatedDraft,
                                        snackbarMessage = "¡Documento creado en Google Docs!"
                                    )
                                }
                            }
                        }
                    }
                    .onFailure { err ->
                        _uiState.update { it.copy(isExporting = false, errorMessage = err.message ?: "Error al exportar a Google Docs") }
                    }
            }
        }
    }

    private fun deleteDraft(draftId: String) {
        viewModelScope.launch {
            val userId = _uiState.value.userId
            deleteCoverDraftUseCase(userId, draftId)
            _uiState.update { it.copy(snackbarMessage = "Borrador eliminado") }
        }
    }
}
