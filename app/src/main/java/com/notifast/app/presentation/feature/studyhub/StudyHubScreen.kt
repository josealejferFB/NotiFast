package com.notifast.app.presentation.feature.studyhub

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastSnackbarHost
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.studyhub.components.AssignSubjectDialog
import com.notifast.app.presentation.feature.studyhub.components.CoverTemplateEditorSheet
import com.notifast.app.presentation.feature.studyhub.components.CoverTemplatesSection
import com.notifast.app.presentation.feature.studyhub.components.DocumentPreviewModalSheet
import com.notifast.app.presentation.feature.studyhub.components.StudyDocumentActionSheet
import com.notifast.app.presentation.feature.studyhub.components.StudyDocumentsSection

@Composable
fun StudyHubScreen(
    onSearchClick: () -> Unit = {},
    viewModel: StudyHubViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onEvent(StudyHubEvent.ImportDocument(uri))
        }
    }

    val authResolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val isOk = result.resultCode == Activity.RESULT_OK
        viewModel.onEvent(StudyHubEvent.CompleteAuthResolution(isOk, result.data))
    }

    LaunchedEffect(uiState.pendingAuthIntent) {
        val pending = uiState.pendingAuthIntent ?: return@LaunchedEffect
        try {
            val req = IntentSenderRequest.Builder(pending.intentSender).build()
            authResolutionLauncher.launch(req)
        } catch (_: Exception) {
            viewModel.onEvent(StudyHubEvent.CompleteAuthResolution(false))
        }
    }

    LaunchedEffect(uiState.pendingOpenUrl) {
        val url = uiState.pendingOpenUrl ?: return@LaunchedEffect
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        } catch (_: Exception) {
        } finally {
            viewModel.onEvent(StudyHubEvent.ClearPendingOpenUrl)
        }
    }

    LaunchedEffect(uiState.snackbarMessage, uiState.errorMessage) {
        val msg = uiState.snackbarMessage ?: uiState.errorMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.onEvent(StudyHubEvent.ClearSnackbar)
            viewModel.onEvent(StudyHubEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            NotiFastTopBar(
                type = TopBarType.Simple(
                    title = stringResource(R.string.study_hub_title),
                    actions = {
                        IconButton(onClick = {
                            docPickerLauncher.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                        }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.study_btn_import_doc))
                        }
                        IconButton(onClick = onSearchClick) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(R.string.notes_search_placeholder))
                        }
                    }
                )
            )
        },
        snackbarHost = { NotiFastSnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = uiState.selectedTab == StudyHubTab.DOCUMENTS,
                    onClick = { viewModel.onEvent(StudyHubEvent.SelectTab(StudyHubTab.DOCUMENTS)) },
                    text = { Text(stringResource(R.string.study_tab_documents)) }
                )
                Tab(
                    selected = uiState.selectedTab == StudyHubTab.TEMPLATES,
                    onClick = { viewModel.onEvent(StudyHubEvent.SelectTab(StudyHubTab.TEMPLATES)) },
                    text = { Text(stringResource(R.string.study_tab_templates)) }
                )
            }

            Box(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                when (uiState.selectedTab) {
                    StudyHubTab.DOCUMENTS -> {
                        StudyDocumentsSection(
                            documents = uiState.filteredDocuments,
                            subjects = uiState.subjects,
                            selectedSubject = uiState.selectedSubjectFilter,
                            selectedDocType = uiState.selectedDocTypeFilter,
                            searchQuery = uiState.searchQuery,
                            onSelectSubject = { s -> viewModel.onEvent(StudyHubEvent.SelectSubjectFilter(s)) },
                            onSelectDocType = { t -> viewModel.onEvent(StudyHubEvent.SelectDocTypeFilter(t)) },
                            onDocumentClick = { doc -> viewModel.onEvent(StudyHubEvent.OpenPreview(doc)) },
                            onDocumentMoreClick = { doc -> viewModel.onEvent(StudyHubEvent.SetActiveActionDocument(doc)) },
                            onImportClick = {
                                docPickerLauncher.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                            }
                        )
                    }
                    StudyHubTab.TEMPLATES -> {
                        CoverTemplatesSection(
                            drafts = uiState.coverDrafts,
                            onSelectTemplate = { type -> viewModel.onEvent(StudyHubEvent.OpenNewTemplateEditor(type)) },
                            onEditDraft = { draft -> viewModel.onEvent(StudyHubEvent.EditExistingDraft(draft)) },
                            onDeleteDraft = { id -> viewModel.onEvent(StudyHubEvent.DeleteDraft(id)) }
                        )
                    }
                }
            }
        }
    }

    uiState.activeActionDocument?.let { doc ->
        StudyDocumentActionSheet(
            document = doc,
            onDismiss = { viewModel.onEvent(StudyHubEvent.SetActiveActionDocument(null)) },
            onPreview = { viewModel.onEvent(StudyHubEvent.OpenPreview(doc)) },
            onOpenExternal = { StudyHubDocIntents.openExternalDoc(context, doc.fileName) },
            onDuplicate = { viewModel.onEvent(StudyHubEvent.DuplicateDocument(doc.id)) },
            onRequestAssignSubject = { viewModel.onEvent(StudyHubEvent.SetAssignSubjectDialogOpen(true)) },
            onShare = { StudyHubDocIntents.shareDoc(context, doc.fileName, doc.title) },
            onRequestDelete = { viewModel.onEvent(StudyHubEvent.SetDeleteDocumentDialogOpen(true)) }
        )
    }

    if (uiState.isAssignSubjectDialogOpen && uiState.activeActionDocument != null) {
        val currentDoc = uiState.activeActionDocument!!
        AssignSubjectDialog(
            subjects = uiState.subjects,
            currentSubjectName = currentDoc.subjectName,
            onDismiss = { viewModel.onEvent(StudyHubEvent.SetAssignSubjectDialogOpen(false)) },
            onSelectSubject = { subj ->
                viewModel.onEvent(StudyHubEvent.AssignSubject(currentDoc.id, subj?.id.orEmpty(), subj?.name.orEmpty()))
            }
        )
    }

    if (uiState.isDeleteDocumentDialogOpen && uiState.activeActionDocument != null) {
        NotiFastDialog(
            title = stringResource(R.string.study_doc_delete_title),
            message = stringResource(R.string.study_doc_delete_msg),
            confirmText = stringResource(R.string.study_doc_action_delete),
            onConfirm = { viewModel.onEvent(StudyHubEvent.DeleteDocument(uiState.activeActionDocument!!.id)) },
            onDismiss = { viewModel.onEvent(StudyHubEvent.SetDeleteDocumentDialogOpen(false)) }
        )
    }

    uiState.activePreviewDocument?.let { doc ->
        DocumentPreviewModalSheet(
            document = doc,
            file = uiState.activePreviewFile,
            docxBlocks = uiState.docxPreviewBlocks,
            isLoading = uiState.isPreviewLoading,
            onOpenExternal = { StudyHubDocIntents.openExternalDoc(context, doc.fileName) },
            onShare = { StudyHubDocIntents.shareDoc(context, doc.fileName, doc.title) },
            onDismiss = { viewModel.onEvent(StudyHubEvent.DismissPreview) }
        )
    }

    uiState.activeEditorDraft?.let { draft ->
        CoverTemplateEditorSheet(
            draft = draft,
            subjects = uiState.subjects,
            isExporting = uiState.isExporting,
            onUpdateDraft = { transform -> viewModel.onEvent(StudyHubEvent.UpdateDraftField(transform)) },
            onSaveLocalDocx = { d -> viewModel.onEvent(StudyHubEvent.GenerateLocalDocx(d)) },
            onExportToGoogleDocs = { d -> viewModel.onEvent(StudyHubEvent.ExportDraftToGoogleDocs(d, context)) },
            onDismiss = { viewModel.onEvent(StudyHubEvent.DismissEditor) }
        )
    }
}
