package com.notifast.app.presentation.feature.notes.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.notifast.app.R
import com.notifast.app.data.model.Note
import com.notifast.app.presentation.atoms.NotiFastMarkdown
import com.notifast.app.presentation.feature.notes.util.NoteShareHelper

@Composable
fun NoteDetailExpandable(
    note: Note,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onTaskCheckedChange: ((taskIndex: Int, isChecked: Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var boxCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var titleBottomInScrollableContent by remember { mutableIntStateOf(0) }

    val showStickyHeader by remember {
        derivedStateOf {
            titleBottomInScrollableContent > 0 && scrollState.value >= titleBottomInScrollableContent
        }
    }

    val onShareNote = {
        NoteShareHelper.shareNote(context, note)
    }

    val cardShape = RoundedCornerShape(24.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp)
            .clip(cardShape),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { boxCoordinates = it }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                if (note.imageUrl != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        SubcomposeAsyncImage(
                            model = note.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = note.subject.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            onClick = onShareNote
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = stringResource(R.string.notes_share_cd),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.notes_share),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.onGloballyPositioned { titleCoords ->
                            val box = boxCoordinates
                            if (box != null && box.isAttached && titleCoords.isAttached) {
                                val titleBottomInBox = box.localPositionOf(titleCoords, Offset.Zero).y + titleCoords.size.height
                                titleBottomInScrollableContent = (titleBottomInBox + scrollState.value).toInt()
                            }
                        }
                    )

                    if (note.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            note.tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "#$tag",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (note.content.isNotBlank()) {
                        NotiFastMarkdown(content = note.content, onTaskCheckedChange = onTaskCheckedChange)
                    } else if (note.blocks.isNotEmpty()) {
                        note.blocks.forEach { block ->
                            BlockReadOnlyRenderer(block = block)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        NotiFastMarkdown(content = "_Sin contenido escrito aún..._")
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    NoteDetailBottomActions(
                        onClose = onClose,
                        onEdit = onEdit
                    )
                }
            }

            // Dynamic sticky header: appears smoothly when title is scrolled out of view
            androidx.compose.animation.AnimatedVisibility(
                visible = showStickyHeader,
                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(animationSpec = tween(250)) { -it / 2 },
                exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(animationSpec = tween(200)) { -it / 2 },
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                NoteDetailStickyHeader(
                    title = note.title,
                    subject = note.subject,
                    onShare = onShareNote
                )
            }
        }
    }
}
