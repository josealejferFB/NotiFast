package com.example.notifast.presentation.ui.edit_evaluation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.notifast.data.model.Evaluation
import com.example.notifast.presentation.common.viewmodel.NotesViewModel
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEvaluationScreen(
    evaluation: Evaluation,
    viewModel: NotesViewModel,
    onEvaluationUpdated: () -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(evaluation.title) }
    var description by remember { mutableStateOf(evaluation.description) }
    var quickTips by remember { mutableStateOf(evaluation.quickTips) }
    var selectedDate by remember { mutableStateOf(evaluation.date ?: Date()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf(evaluation.imageUrl) }
    var grade by remember { mutableStateOf(evaluation.grade?.toInt()?.toString() ?: "") }

    var isPresented by remember { mutableStateOf(evaluation.isPresented) }
    
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> 
        selectedImageUri = uri 
        existingImageUrl = null
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = utcMillis
                        val localCalendar = Calendar.getInstance()
                        localCalendar.set(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH),
                            12, 0, 0
                        )
                        selectedDate = localCalendar.time
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Editar Evaluación", fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Ruby)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Ruby
                    )
                )
            }
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
                    value = evaluation.subjectName,
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

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la Evaluación", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, tint = Ruby) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Ruby,
                        focusedLabelColor = Ruby
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextField(
                        value = grade,
                        onValueChange = { input ->
                            val parsed = input.toDoubleOrNull()
                            if (input.isEmpty() || (parsed != null && parsed in 0.0..10.0)) {
                                grade = input
                            }
                        },
                        label = { Text("Nota (1-10)", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Grade, contentDescription = null, tint = Color(0xFFFFB300)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(20.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Ruby,
                            focusedLabelColor = Ruby
                        )
                    )

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { isPresented = !isPresented },
                        color = if (isPresented) Aqua.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.7f),
                        border = if (isPresented) BorderStroke(1.dp, Aqua) else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isPresented) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isPresented) Aqua else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isPresented) "Presentado" else "Pendiente",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPresented) Aqua else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha de Evaluación", fontSize = 13.sp) },
                    leadingIcon = { 
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Aqua)
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    enabled = false,
                    colors = TextFieldDefaults.colors(
                        disabledContainerColor = Color.White.copy(alpha = 0.7f),
                        disabledIndicatorColor = Color.Transparent,
                        disabledLabelColor = Aqua,
                        disabledTextColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                        .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null || existingImageUrl != null) {
                        AsyncImage(
                            model = selectedImageUri ?: existingImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = Aqua.copy(alpha = 0.1f)) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = Aqua, modifier = Modifier.padding(12.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Cambiar imagen de referencia", color = Color.Gray, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Ruby,
                        focusedLabelColor = Ruby
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = quickTips,
                    onValueChange = { quickTips = it },
                    label = { Text("Tips / Apuntes Rápidos", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFBC02D)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFFFBC02D),
                        focusedLabelColor = Color(0xFFFBC02D)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.updateEvaluation(
                                evaluationId = evaluation.id,
                                title = title,
                                description = description,
                                date = selectedDate,
                                quickTips = quickTips,
                                imageUri = selectedImageUri,
                                existingImageUrl = existingImageUrl,
                                grade = grade.toDoubleOrNull(),
                                weight = null,
                                isPresented = isPresented
                            )
                            onEvaluationUpdated()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ruby),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    enabled = title.isNotBlank()
                ) {
                    Text("ACTUALIZAR EVALUACIÓN", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
