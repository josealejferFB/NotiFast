package com.example.notifast.presentation.feature.editevaluation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
fun EditEvaluationScreen(
    viewModel: EditEvaluationViewModel = hiltViewModel(),
    onEvaluationUpdated: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onEvaluationUpdated() }

    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedDate by remember { mutableStateOf(uiState.date ?: Date()) }

    val evaluationType = EvaluationType.fromName(uiState.type)

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { NotiFastCenterAlignedTopBar(title = "Editar ${evaluationType.label}") }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = uiState.subjectName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Materia") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Aqua) },
                    shape = RoundedCornerShape(20.dp),
                    enabled = false,
                    colors = TextFieldDefaults.colors(
                        disabledContainerColor = Color.White.copy(alpha = 0.7f),
                        disabledIndicatorColor = Color.Transparent,
                        disabledLabelColor = Aqua,
                        disabledTextColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotiFastFormField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(EditEvaluationEvent.UpdateTitle(it)) },
                    label = "Título de la Evaluación",
                    leadingIcon = Icons.Default.Title
                )

                if (evaluationType.showGrade) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NotiFastFormField(
                            value = uiState.grade,
                            onValueChange = { viewModel.onEvent(EditEvaluationEvent.UpdateGrade(it)) },
                            label = "Nota (1-10)",
                            leadingIcon = Icons.Default.Grade,
                            cursorColor = Color(0xFFFFB300),
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.onEvent(EditEvaluationEvent.TogglePresented) },
                            color = if (uiState.isPresented) Aqua.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.7f),
                            border = if (uiState.isPresented) BorderStroke(1.dp, Aqua) else null
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isPresented) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (uiState.isPresented) Aqua else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (uiState.isPresented) "Presentado" else "Pendiente",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isPresented) Aqua else Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                NotiFastDatePickerField(
                    date = selectedDate,
                    onDateSelected = { selectedDate = it },
                    label = "Fecha de Evaluación"
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotiFastImagePickerField(
                    existingImageUrl = uiState.existingImageUrl,
                    placeholderText = "Cambiar imagen de referencia",
                    onImageSelected = { selectedImageUri = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotiFastFormField(
                    value = uiState.description,
                    onValueChange = { viewModel.onEvent(EditEvaluationEvent.UpdateDescription(it)) },
                    label = "Descripción",
                    height = 100.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                NotiFastFormField(
                    value = uiState.quickTips,
                    onValueChange = { viewModel.onEvent(EditEvaluationEvent.UpdateQuickTips(it)) },
                    label = "Tips / Apuntes Rápidos",
                    leadingIcon = Icons.Default.Lightbulb,
                    cursorColor = Color(0xFFFBC02D)
                )

                if (evaluationType.showWeight) {
                    Spacer(modifier = Modifier.height(16.dp))

                    NotiFastFormField(
                        value = uiState.weight,
                        onValueChange = { viewModel.onEvent(EditEvaluationEvent.UpdateWeight(it)) },
                        label = "Peso / Porcentaje (opcional)",
                        leadingIcon = Icons.Default.Scale,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        cursorColor = Aqua
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (uiState.title.isNotBlank()) {
                            viewModel.onEvent(EditEvaluationEvent.Save(selectedDate, selectedImageUri))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ruby),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    enabled = uiState.title.isNotBlank() && !uiState.isSaving
                ) {
                    Text("ACTUALIZAR ${evaluationType.label.uppercase()}", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
