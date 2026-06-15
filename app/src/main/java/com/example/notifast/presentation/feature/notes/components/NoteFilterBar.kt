package com.example.notifast.presentation.feature.notes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notifast.presentation.atoms.NotiFastChip
import com.example.notifast.presentation.feature.notes.NoteFilter

@Composable
fun NoteFilterBar(currentFilter: NoteFilter, onFilterChange: (NoteFilter) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NoteFilter.entries.forEach { filter ->
            val label = when (filter) { NoteFilter.RECIENTES -> "Recientes"; NoteFilter.ANTIGUAS -> "Antiguas"; NoteFilter.POR_MATERIA -> "Por materia" }
            NotiFastChip(label = label, selected = currentFilter == filter, onClick = { onFilterChange(filter) })
        }
    }
}
