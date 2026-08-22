package com.notifast.app.presentation.feature.notes.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastChip

@Composable
fun NoteTagFilterRow(
    availableTags: List<String>,
    selectedTag: String?,
    onSelectTag: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (availableTags.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NotiFastChip(
            label = stringResource(R.string.notes_filter_all_tags),
            selected = selectedTag == null,
            onClick = { onSelectTag(null) }
        )
        availableTags.forEach { tag ->
            NotiFastChip(
                label = "#$tag",
                selected = selectedTag == tag,
                onClick = { onSelectTag(if (selectedTag == tag) null else tag) }
            )
        }
    }
}
