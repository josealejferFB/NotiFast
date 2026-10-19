package com.notifast.app.presentation.feature.studyhub

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import com.notifast.app.domain.model.study.DocxPreviewBlock
import com.notifast.app.domain.model.study.StudyDocument
import java.io.File

enum class StudyHubTab {
    DOCUMENTS,
    TEMPLATES
}

enum class DocTypeFilter {
    ALL,
    PDF,
    DOCX
}

data class StudyHubUiState(
    val userId: String = "",
    val userFirstName: String = "",
    val userEmail: String = "",
    val userProfilePictureUrl: String? = null,
    val allDocuments: List<StudyDocument> = emptyList(),
    val filteredDocuments: List<StudyDocument> = emptyList(),
    val subjects: List<SubjectData> = emptyList(),
    val coverDrafts: List<CoverDraft> = emptyList(),
    val selectedTab: StudyHubTab = StudyHubTab.DOCUMENTS,
    val selectedSubjectFilter: String? = null,
    val selectedDocTypeFilter: DocTypeFilter = DocTypeFilter.ALL,
    val searchQuery: String = "",
    val activePreviewDocument: StudyDocument? = null,
    val activePreviewFile: File? = null,
    val docxPreviewBlocks: List<DocxPreviewBlock> = emptyList(),
    val isPreviewLoading: Boolean = false,
    val activeActionDocument: StudyDocument? = null,
    val isAssignSubjectDialogOpen: Boolean = false,
    val isDeleteDocumentDialogOpen: Boolean = false,
    val activeEditorDraft: CoverDraft? = null,
    val isExporting: Boolean = false,
    val pendingAuthIntent: PendingIntent? = null,
    val pendingOpenUrl: String? = null,
    val snackbarMessage: String? = null,
    val errorMessage: String? = null
)

sealed interface StudyHubEvent {
    data object LoadData : StudyHubEvent
    data class SelectTab(val tab: StudyHubTab) : StudyHubEvent
    data class SelectSubjectFilter(val subjectName: String?) : StudyHubEvent
    data class SelectDocTypeFilter(val filter: DocTypeFilter) : StudyHubEvent
    data class UpdateSearchQuery(val query: String) : StudyHubEvent
    data class ImportDocument(
        val uri: Uri,
        val preferredSubjectId: String = "",
        val preferredSubjectName: String = ""
    ) : StudyHubEvent
    data class DuplicateDocument(val documentId: String) : StudyHubEvent
    data class AssignSubject(
        val documentId: String,
        val subjectId: String = "",
        val subjectName: String = ""
    ) : StudyHubEvent
    data class DeleteDocument(val documentId: String) : StudyHubEvent
    data class SetActiveActionDocument(val document: StudyDocument?) : StudyHubEvent
    data class SetAssignSubjectDialogOpen(val open: Boolean) : StudyHubEvent
    data class SetDeleteDocumentDialogOpen(val open: Boolean) : StudyHubEvent
    data class OpenPreview(val document: StudyDocument) : StudyHubEvent
    data object DismissPreview : StudyHubEvent
    data class OpenNewTemplateEditor(
        val templateType: CoverTemplateType,
        val subjectName: String? = null
    ) : StudyHubEvent
    data class EditExistingDraft(val draft: CoverDraft) : StudyHubEvent
    data class UpdateDraftField(val transform: (CoverDraft) -> CoverDraft) : StudyHubEvent
    data class SaveDraft(val draft: CoverDraft) : StudyHubEvent
    data class GenerateLocalDocx(val draft: CoverDraft) : StudyHubEvent
    data class ExportDraftToGoogleDocs(
        val draft: CoverDraft,
        val context: Context? = null
    ) : StudyHubEvent
    data class CompleteAuthResolution(val isSuccess: Boolean, val resultIntent: Intent? = null) : StudyHubEvent
    data class DeleteDraft(val draftId: String) : StudyHubEvent
    data object DismissEditor : StudyHubEvent
    data object ClearSnackbar : StudyHubEvent
    data object ClearError : StudyHubEvent
    data object ClearPendingOpenUrl : StudyHubEvent
}
