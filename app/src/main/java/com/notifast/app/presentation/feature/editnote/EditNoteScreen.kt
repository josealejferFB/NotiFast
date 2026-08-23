package com.notifast.app.presentation.feature.editnote

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastFormField
import com.notifast.app.presentation.atoms.NotiFastImagePickerField
import com.notifast.app.presentation.atoms.NotiFastMarkdown
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.addnote.components.NoteMarkdownToolbar
import com.notifast.app.presentation.feature.addnote.components.NoteTagsFlowRow
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    viewModel: EditNoteViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNoteSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.isSaved) { onNoteSaved(); return }

    var showDiscardDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = uiState.note != null && (
        uiState.title != (uiState.note?.title ?: "") ||
        uiState.content != (uiState.note?.content ?: "") ||
        uiState.imageUri != null
    )

    val handleBack = {
        if (hasUnsavedChanges) showDiscardDialog = true else onBackClick()
    }

    BackHandler(enabled = hasUnsavedChanges) { showDiscardDialog = true }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    NotiFastTopBar(
                        type = TopBarType.Detail(
                            title = "Editar Apunte",
                            onBackClick = handleBack
                        )
                    )
                    TabRow(
                        selectedTabIndex = if (uiState.isLivePreview) 1 else 0,
                        containerColor = Color.Transparent,
                        contentColor = Ruby,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[if (uiState.isLivePreview) 1 else 0]),
                                color = Ruby
                            )
                        }
                    ) {
                        Tab(
                            selected = !uiState.isLivePreview,
                            onClick = { viewModel.onEvent(EditNoteEvent.ToggleLivePreview(false)) },
                            text = { Text(stringResource(R.string.notes_tab_edit), fontWeight = if (!uiState.isLivePreview) FontWeight.Bold else FontWeight.Medium) }
                        )
                        Tab(
                            selected = uiState.isLivePreview,
                            onClick = { viewModel.onEvent(EditNoteEvent.ToggleLivePreview(true)) },
                            text = { Text(stringResource(R.string.notes_tab_preview), fontWeight = if (uiState.isLivePreview) FontWeight.Bold else FontWeight.Medium) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            if (uiState.note == null) {
                NotiFastCircularLoading()
                return@Scaffold
            }

            AnimatedContent(
                targetState = uiState.isLivePreview,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "EditPreviewTransition",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) { isPreview ->
                if (isPreview) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        NotiFastCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = (uiState.note?.subject ?: "").uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Aqua
                                )
                                Text(
                                    text = uiState.title.ifBlank { "Sin Título" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                NotiFastMarkdown(content = uiState.content.ifBlank { "_Sin contenido escrito aún..._" })
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        uiState.syncStatus?.let { status ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text(text = status, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }

                        NotiFastFormField(
                            value = uiState.title,
                            onValueChange = { viewModel.onEvent(EditNoteEvent.TitleChanged(it)) },
                            label = "Título del apunte"
                        )

                        NoteMarkdownToolbar(
                            onInsertFormatting = { prefix, suffix ->
                                viewModel.onEvent(EditNoteEvent.ContentChanged(uiState.content + "$prefix$suffix"))
                            }
                        )

                        NotiFastFormField(
                            value = uiState.content,
                            onValueChange = { viewModel.onEvent(EditNoteEvent.ContentChanged(it)) },
                            label = "Contenido en Markdown...",
                            height = 200.dp,
                            singleLine = false
                        )

                        NoteTagsFlowRow(
                            selectedTags = uiState.tags,
                            onTagToggled = { viewModel.onEvent(EditNoteEvent.ToggleTag(it)) },
                            onAddCustomTag = { viewModel.onEvent(EditNoteEvent.AddTag(it)) },
                            onRemoveTag = { viewModel.onEvent(EditNoteEvent.RemoveTag(it)) }
                        )

                        NotiFastImagePickerField(
                            existingImageUrl = uiState.note?.imageUrl,
                            placeholderText = if (uiState.imageUri == null && uiState.note?.imageUrl == null) "Añadir foto de referencia" else "Cambiar foto adjuntada",
                            onImageSelected = { viewModel.onEvent(EditNoteEvent.ImageSelected(it)) }
                        )

                        Spacer(Modifier.height(8.dp))

                        NotiFastButton(
                            text = stringResource(R.string.btn_save),
                            onClick = { viewModel.onEvent(EditNoteEvent.SaveClicked) },
                            isLoading = uiState.isLoading,
                            backgroundColor = Aqua,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                        )
                    }
                }
            }
        }

        if (showDiscardDialog) {
            NotiFastDialog(
                title = stringResource(R.string.notes_discard_title),
                message = stringResource(R.string.notes_discard_message),
                confirmText = stringResource(R.string.schedules_discard_confirm),
                dismissText = stringResource(R.string.schedules_discard_cancel),
                confirmColor = Ruby,
                confirmWeight = FontWeight.Bold,
                onConfirm = { showDiscardDialog = false; onBackClick() },
                onDismiss = { showDiscardDialog = false }
            )
        }
    }
}
