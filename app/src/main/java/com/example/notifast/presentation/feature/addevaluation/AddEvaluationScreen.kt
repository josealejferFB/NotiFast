package com.example.notifast.presentation.feature.addevaluation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifast.data.model.EvaluationType
import com.example.notifast.presentation.atoms.NotiFastDatePickerField
import com.example.notifast.presentation.atoms.NotiFastFormField
import com.example.notifast.presentation.atoms.NotiFastImagePickerField
import com.example.notifast.presentation.atoms.NotiFastCenterAlignedTopBar
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEvaluationScreen(
    viewModel: AddEvaluationViewModel = hiltViewModel(),
    subject: String = "",
    onEvaluationAdded: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onEvaluationAdded() }

    LaunchedEffect(subject) { viewModel.setInitialSubject(subject) }

    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { NotiFastCenterAlignedTopBar(title = "Añadir ${uiState.selectedType.label}") }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = uiState.selectedSubject,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Seleccionar Materia") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Aqua) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        shape = RoundedCornerShape(20.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Aqua,
                            focusedLabelColor = Aqua
                        ),
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White, RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp))
                    ) {
                        uiState.subjects.forEach { subjectData ->
                            DropdownMenuItem(
                                text = { Text(subjectData.name, fontWeight = FontWeight.SemiBold) },
                                onClick = {
                                    viewModel.onEvent(AddEvaluationEvent.SelectSubject(subjectData.name))
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EvaluationType.entries.forEach { type ->
                        FilterChip(
                            selected = uiState.selectedType == type,
                            onClick = { viewModel.onEvent(AddEvaluationEvent.SelectType(type)) },
                            label = { Text(type.label, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(type.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = type.color.copy(alpha = 0.15f),
                                selectedLabelColor = type.color,
                                selectedLeadingIconColor = type.color
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = uiState.selectedType == type,
                                selectedBorderColor = type.color
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                NotiFastFormField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(AddEvaluationEvent.UpdateTitle(it)) },
                    label = "Título de la Evaluación",
                    leadingIcon = Icons.Default.Title
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotiFastDatePickerField(
                    date = selectedDate,
                    onDateSelected = { selectedDate = it },
                    label = "Fecha de Evaluación"
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotiFastImagePickerField(
                    placeholderText = "Añadir foto de referencia",
                    onImageSelected = { selectedImageUri = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotiFastFormField(
                    value = uiState.description,
                    onValueChange = { viewModel.onEvent(AddEvaluationEvent.UpdateDescription(it)) },
                    label = "Descripción",
                    height = 100.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotiFastFormField(
                    value = uiState.quickTips,
                    onValueChange = { viewModel.onEvent(AddEvaluationEvent.UpdateQuickTips(it)) },
                    label = "Tips / Apuntes Rápidos",
                    leadingIcon = Icons.Default.Lightbulb,
                    cursorColor = Color(0xFFFBC02D)
                )

                if (uiState.selectedType.showWeight) {
                    Spacer(modifier = Modifier.height(16.dp))

                    NotiFastFormField(
                        value = uiState.weight,
                        onValueChange = { viewModel.onEvent(AddEvaluationEvent.UpdateWeight(it)) },
                        label = "Peso / Porcentaje (opcional)",
                        leadingIcon = Icons.Default.Scale,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        cursorColor = Aqua
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (uiState.selectedSubject.isNotBlank() && uiState.title.isNotBlank()) {
                            viewModel.onEvent(AddEvaluationEvent.Save(selectedDate, selectedImageUri))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ruby),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    enabled = uiState.selectedSubject.isNotBlank() && uiState.title.isNotBlank() && !uiState.isSaving
                ) {
                    Text("GUARDAR ${uiState.selectedType.label.uppercase()}", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
