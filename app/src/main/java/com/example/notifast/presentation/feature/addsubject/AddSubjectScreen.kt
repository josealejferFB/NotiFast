package com.example.notifast.presentation.feature.addsubject

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import com.example.notifast.presentation.atoms.NotiFastButton
import com.example.notifast.presentation.atoms.NotiFastCard
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.atoms.NotiFastTextField
import com.example.notifast.presentation.feature.notes.components.SubjectIconMapper
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

val subjectColorPalette = listOf(
    "#BF092F", // Ruby
    "#A10827", // Darker Ruby
    "#DF2145", // Lighter Ruby
    "#3B9797", // Aqua
    "#2E7D7D", // Darker Aqua
    "#59BDBD", // Lighter Aqua
    "#4A235A", // Deep Purple
    "#1A5276", // Deep Blue
    "#0E6655", // Forest Green
    "#9A7D0A", // Gold
    "#873600", // Rust
    "#17202A"  // Dark Slate
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectScreen(
    viewModel: AddSubjectViewModel = hiltViewModel(),
    onSubjectCreated: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expandedStep by remember { mutableIntStateOf(1) }

    if (uiState.isSaved) {
        onSubjectCreated(uiState.subjectName)
        return
    }

    val previewColor = try { Color(android.graphics.Color.parseColor(uiState.selectedColor)) } catch(e: Exception) { Aqua }
    val animatedColor by animateColorAsState(
        targetValue = previewColor,
        animationSpec = tween(durationMillis = 350),
        label = "colorPreview"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                com.example.notifast.presentation.atoms.NotiFastTopBar(
                    type = com.example.notifast.presentation.atoms.TopBarType.Detail(
                        title = "Nueva Materia",
                        onBackClick = onBackClick
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                // Dynamic animated preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(animatedColor)
                            .border(BorderStroke(2.dp, Color.White.copy(alpha = 0.5f)), RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = SubjectIconMapper.getIcon(uiState.selectedIcon),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                // --- PASO 1 ---
                NotiFastCard(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { expandedStep = 1 },
                    contentPadding = 0.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Surface(color = Ruby, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("1. Información", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            }
                        }

                        AnimatedVisibility(visible = expandedStep == 1) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                NotiFastTextField(
                                    value = uiState.subjectName,
                                    onValueChange = { viewModel.onEvent(AddSubjectEvent.NameChanged(it)) },
                                    label = "Nombre de la materia",
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { expandedStep = 2 },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    enabled = uiState.subjectName.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Ruby,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        disabledContentColor = Color.LightGray
                                    )
                                ) {
                                    Text("Siguiente", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        AnimatedVisibility(visible = expandedStep != 1) {
                            Text(
                                text = uiState.subjectName.ifBlank { "Sin nombre" },
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- PASO 2 ---
                NotiFastCard(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { expandedStep = 2 },
                    contentPadding = 0.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Surface(color = Ruby, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("2. Identidad Visual", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            }
                        }

                        AnimatedVisibility(visible = expandedStep == 2) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Elige un color",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                                Spacer(Modifier.height(12.dp))
                                
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    subjectColorPalette.chunked(4).forEach { rowColors ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                                        ) {
                                            rowColors.forEach { hexColor ->
                                                val selected = uiState.selectedColor == hexColor
                                                val color = try { Color(android.graphics.Color.parseColor(hexColor)) } catch(e: Exception) { Color.Gray }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                        .clickable { viewModel.onEvent(AddSubjectEvent.ColorSelected(hexColor)) }
                                                        .border(
                                                            width = if (selected) 3.dp else 0.dp,
                                                            color = if (selected) Color.White else Color.Transparent,
                                                            shape = CircleShape
                                                        )
                                                        .padding(if (selected) 3.dp else 0.dp)
                                                ) {
                                                    if (selected) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clip(CircleShape)
                                                                .border(2.dp, color, CircleShape)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(Modifier.height(24.dp))
                                
                                Text(
                                    text = "Elige un icono",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                                Spacer(Modifier.height(12.dp))
                                
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    SubjectIconMapper.iconList.chunked(4).forEach { rowIcons ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                                        ) {
                                            rowIcons.forEach { iconName ->
                                                val selected = uiState.selectedIcon == iconName
                                                val iconVector = SubjectIconMapper.getIcon(iconName)
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(if (selected) previewColor.copy(alpha = 0.15f) else Color(0xFFF5F5F5))
                                                        .border(
                                                            width = if (selected) 2.dp else 1.dp,
                                                            color = if (selected) previewColor else Color.LightGray.copy(alpha = 0.4f),
                                                            shape = RoundedCornerShape(14.dp)
                                                        )
                                                        .clickable { viewModel.onEvent(AddSubjectEvent.IconSelected(iconName)) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = iconVector,
                                                        contentDescription = iconName,
                                                        tint = if (selected) previewColor else Color.Gray,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(Modifier.height(24.dp))
                                
                                uiState.error?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.height(12.dp))
                                }
                                
                                NotiFastButton(
                                    text = "CREAR MATERIA",
                                    onClick = { viewModel.onEvent(AddSubjectEvent.SaveClicked) },
                                    isLoading = uiState.isLoading
                                )
                            }
                        }

                        AnimatedVisibility(visible = expandedStep != 2) {
                            Text(
                                text = "Color e Ícono seleccionados",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
