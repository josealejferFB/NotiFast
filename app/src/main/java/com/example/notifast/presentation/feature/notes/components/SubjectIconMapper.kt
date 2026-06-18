package com.example.notifast.presentation.feature.notes.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector

object SubjectIconMapper {
    val iconList = listOf(
        "School", "Book", "Science", "Calculate", "Computer",
        "Palette", "Sports", "Music", "Translate", "History",
        "Brush", "Public"
    )

    fun getIcon(name: String): ImageVector {
        return when (name) {
            "School" -> Icons.Filled.School
            "Book" -> Icons.Filled.Book
            "Science" -> Icons.Filled.Science
            "Calculate" -> Icons.Filled.Calculate
            "Computer" -> Icons.Filled.Computer
            "Palette" -> Icons.Filled.Palette
            "Sports" -> Icons.Filled.SportsBasketball
            "Music" -> Icons.Filled.MusicNote
            "Translate" -> Icons.Filled.Translate
            "History" -> Icons.Filled.History
            "Brush" -> Icons.Filled.Brush
            "Public" -> Icons.Filled.Public
            // Map old emoji icons for backward compatibility
            "📚" -> Icons.Filled.School
            "📖" -> Icons.Filled.Book
            "🔬" -> Icons.Filled.Science
            "🧮" -> Icons.Filled.Calculate
            "💻" -> Icons.Filled.Computer
            "🎨" -> Icons.Filled.Palette
            "⚽" -> Icons.Filled.SportsBasketball
            "🎵" -> Icons.Filled.MusicNote
            "📝" -> Icons.Filled.Book
            else -> Icons.Filled.School
        }
    }
}
