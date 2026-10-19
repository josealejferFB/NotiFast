package com.notifast.app.presentation.feature.studyhub

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.domain.model.study.CoverTemplateType
import com.notifast.app.presentation.atoms.FabAction
import com.notifast.app.presentation.atoms.NotiFastExpandableFab
import com.notifast.app.presentation.atoms.NotiFastSnackbarHost
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.studyhub.components.CoverTemplateEditorSheet
import com.notifast.app.presentation.feature.studyhub.components.CoverTemplatesSection
import com.notifast.app.presentation.feature.studyhub.components.DocumentPreviewModalSheet
import com.notifast.app.presentation.feature.studyhub.components.StudyDocumentActionSheet
import com.notifast.app.presentation.feature.studyhub.components.StudyDocumentsSection
import com.notifast.app.presentation.feature.studyhub.components.StudyHubSegmentedTabs

private val STUDY_MIME_TYPES = arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")

@Composable
fun StudyHubScreen(
    onSearchClick: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    viewModel: StudyHubViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    val docPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.onEvent(StudyHubEvent.ImportDocument(uri))
    }
    val authResolution = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { res ->
        viewModel.onEvent(StudyHubEvent.CompleteAuthResolution(res.resultCode == Activity.RESULT_OK, res.data))
    }

    LaunchedEffect(uiState.pendingAuthIntent) {
        val pending = uiState.pendingAuthIntent ?: return@LaunchedEffect
        try {
            authResolution.launch(IntentSenderRequest.Builder(pending.intentSender).build())
        } catch (_: Exception) {
            viewModel.onEvent(StudyHubEvent.CompleteAuthResolution(false))
        }
    }

    LaunchedEffect(uiState.pendingOpenUrl) {
        val url = uiState.pendingOpenUrl ?: return@LaunchedEffect
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
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

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                NotiFastExpandableFab(
                    actions = listOf(
                        FabAction(stringResource(R.string.study_btn_new_template), Icons.Outlined.AutoStories, { viewModel.onEvent(StudyHubEvent.OpenNewTemplateEditor(CoverTemplateType.UNERG_DEGREE)) }, stringResource(R.string.study_btn_new_template), MaterialTheme.colorScheme.secondary),
                        FabAction(stringResource(R.string.study_btn_import_doc), Icons.AutoMirrored.Filled.NoteAdd, { docPicker.launch(STUDY_MIME_TYPES) }, stringResource(R.string.study_btn_import_doc), MaterialTheme.colorScheme.primary)
                    ),
                    modifier = Modifier.padding(bottom = 72.dp)
                )
            },
            snackbarHost = { NotiFastSnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            ) {
                StudyHubSegmentedTabs(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = { tab -> viewModel.onEvent(StudyHubEvent.SelectTab(tab)) }
                )

                Box(modifier = Modifier.fillMaxSize().padding(top = 2.dp)) {
                    AnimatedContent(
                        targetState = uiState.selectedTab,
                        transitionSpec = {
                            if (targetState == StudyHubTab.TEMPLATES) {
                                (slideInHorizontally { it / 3 } + fadeIn()).togetherWith(slideOutHorizontally { -it / 3 } + fadeOut())
                            } else {
                                (slideInHorizontally { -it / 3 } + fadeIn()).togetherWith(slideOutHorizontally { it / 3 } + fadeOut())
                            }
                        },
                        label = "StudyHubTabContentTransition"
                    ) { tab ->
                        when (tab) {
                            StudyHubTab.DOCUMENTS -> {
                                StudyDocumentsSection(
                                    documents = uiState.filteredDocuments,
                                    subjects = uiState.subjects,
                                    selectedSubject = uiState.selectedSubjectFilter,
                                    selectedDocType = uiState.selectedDocTypeFilter,
                                    searchQuery = uiState.searchQuery,
                                    onSearchQueryChange = { q -> viewModel.onEvent(StudyHubEvent.UpdateSearchQuery(q)) },
                                    onSelectSubject = { s -> viewModel.onEvent(StudyHubEvent.SelectSubjectFilter(s)) },
                                    onSelectDocType = { t -> viewModel.onEvent(StudyHubEvent.SelectDocTypeFilter(t)) },
                                    onDocumentClick = { doc -> viewModel.onEvent(StudyHubEvent.OpenPreview(doc)) },
                                    onDocumentMoreClick = { doc -> viewModel.onEvent(StudyHubEvent.SetActiveActionDocument(doc)) },
                                    onImportClick = { docPicker.launch(STUDY_MIME_TYPES) }
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
        }
    }

    uiState.activeActionDocument?.let { doc ->
        StudyDocumentActionSheet(
            document = doc,
            subjects = uiState.subjects,
            onDismiss = { viewModel.onEvent(StudyHubEvent.SetActiveActionDocument(null)) },
            onPreview = { viewModel.onEvent(StudyHubEvent.OpenPreview(doc)) },
            onOpenExternal = { StudyHubDocIntents.openExternalDoc(context, doc.fileName) },
            onDuplicate = { viewModel.onEvent(StudyHubEvent.DuplicateDocument(doc.id)) },
            onAssignSubject = { subjectId, subjectName ->
                viewModel.onEvent(StudyHubEvent.AssignSubject(doc.id, subjectId, subjectName))
            },
            onShare = { StudyHubDocIntents.shareDoc(context, doc.fileName, doc.title) },
            onDelete = { viewModel.onEvent(StudyHubEvent.DeleteDocument(doc.id)) }
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
