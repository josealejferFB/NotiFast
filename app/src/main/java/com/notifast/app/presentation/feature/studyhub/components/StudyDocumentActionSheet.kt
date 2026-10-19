package com.notifast.app.presentation.feature.studyhub.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.study.StudyDocument

private enum class ActionSheetView {
    MAIN,
    ASSIGN_SUBJECT,
    CONFIRM_DELETE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyDocumentActionSheet(
    document: StudyDocument,
    subjects: List<SubjectData>,
    onDismiss: () -> Unit,
    onPreview: () -> Unit,
    onOpenExternal: () -> Unit,
    onDuplicate: () -> Unit,
    onAssignSubject: (subjectId: String, subjectName: String) -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentView by remember { mutableStateOf(ActionSheetView.MAIN) }

    BackHandler(enabled = currentView != ActionSheetView.MAIN) {
        currentView = ActionSheetView.MAIN
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedContent(
                targetState = currentView,
                transitionSpec = {
                    if (targetState == ActionSheetView.MAIN) {
                        (slideInHorizontally { -it / 3 } + fadeIn()).togetherWith(slideOutHorizontally { it / 3 } + fadeOut())
                    } else {
                        (slideInHorizontally { it / 3 } + fadeIn()).togetherWith(slideOutHorizontally { -it / 3 } + fadeOut())
                    }
                },
                label = "ActionSheetTransition"
            ) { view ->
                when (view) {
                    ActionSheetView.MAIN -> {
                        ActionSheetMainContent(
                            document = document,
                            onPreview = { onDismiss(); onPreview() },
                            onOpenExternal = { onDismiss(); onOpenExternal() },
                            onDuplicate = { onDismiss(); onDuplicate() },
                            onRequestAssignSubject = { currentView = ActionSheetView.ASSIGN_SUBJECT },
                            onShare = { onDismiss(); onShare() },
                            onRequestDelete = { currentView = ActionSheetView.CONFIRM_DELETE }
                        )
                    }
                    ActionSheetView.ASSIGN_SUBJECT -> {
                        AssignSubjectSheetContent(
                            document = document,
                            subjects = subjects,
                            onBack = { currentView = ActionSheetView.MAIN },
                            onSelectSubject = { sId, sName ->
                                onDismiss()
                                onAssignSubject(sId, sName)
                            }
                        )
                    }
                    ActionSheetView.CONFIRM_DELETE -> {
                        ConfirmDeleteSheetContent(
                            document = document,
                            onBack = { currentView = ActionSheetView.MAIN },
                            onConfirmDelete = {
                                onDismiss()
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionSheetMainContent(
    document: StudyDocument,
    onPreview: () -> Unit,
    onOpenExternal: () -> Unit,
    onDuplicate: () -> Unit,
    onRequestAssignSubject: () -> Unit,
    onShare: () -> Unit,
    onRequestDelete: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = document.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = document.fileName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(8.dp))

        ActionItem(Icons.Outlined.Visibility, stringResource(R.string.study_doc_action_preview), onClick = onPreview)
        ActionItem(Icons.AutoMirrored.Outlined.OpenInNew, stringResource(R.string.study_doc_action_open_external), onClick = onOpenExternal)
        ActionItem(Icons.Outlined.ContentCopy, stringResource(R.string.study_doc_action_duplicate), onClick = onDuplicate)
        ActionItem(Icons.Outlined.BookmarkBorder, stringResource(R.string.study_doc_action_assign_subject), onClick = onRequestAssignSubject)
        ActionItem(Icons.Outlined.Share, stringResource(R.string.study_doc_action_share), onClick = onShare)

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            onClick = onRequestDelete,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.study_doc_action_delete),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.study_doc_action_delete),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(color = tint, fontWeight = FontWeight.Medium)
            )
        }
    }
}
