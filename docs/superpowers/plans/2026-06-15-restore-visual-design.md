# Restore Original Visual Design Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore the original NotiFast visual design (logo, StudyBackground, transparent CenterAlignedTopAppBar, HeaderProfile on Notes, inline login/signup toggle) while keeping the new MVVM + SOLID atómico architecture and Google Sign-In.

**Architecture:** All visual restoration happens in `presentation/` layer. Atoms are updated/created, feature screens are modified to use original styling. No changes to data layer, DI, or navigation.

**Tech Stack:** Jetpack Compose, Material3, Coil, Firestore

---

### Task 1: Create NotiFastStudyBackground atom

**Files:**
- Create: `app/src/main/java/com/example/notifast/presentation/atoms/NotiFastStudyBackground.kt`
- Modify: None

- [ ] **Step 1: Write NotiFastStudyBackground.kt**

```kotlin
package com.example.notifast.presentation.atoms

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

@Composable
fun NotiFastStudyBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")

    val studyIcons = listOf(
        Icons.AutoMirrored.Filled.MenuBook,
        Icons.Default.Edit,
        Icons.Default.School,
        Icons.Default.HistoryEdu,
        Icons.Default.Science,
        Icons.Default.AutoStories,
        Icons.Default.Lightbulb,
        Icons.Default.Calculate,
        Icons.Default.Functions,
        Icons.Default.Psychology,
        Icons.Default.Architecture,
        Icons.Default.Biotech,
        Icons.Default.Draw,
        Icons.Default.EmojiEvents,
        Icons.Default.Quiz,
        Icons.Default.Translate,
        Icons.Default.Abc,
        Icons.Default.AutoAwesome,
        Icons.Default.AutoGraph
    )

    Box(modifier = modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color.White, Color(0xFFF8F9FA)))
    )) {
        studyIcons.forEachIndexed { index, icon ->
            val duration = 4000 + (index * 600)

            val yOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 40f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "yOffset_$index"
            )

            val rotation by infiniteTransition.animateFloat(
                initialValue = -10f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration + 1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rotation_$index"
            )

            val xPos = (index * 17) % 95
            val yPos = (index * 31) % 100

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (xPos.dp * 4), y = (yPos.dp * 10) + yOffset.dp)
                    .graphicsLayer(rotationZ = rotation)
                    .size(44.dp)
                    .alpha(0.04f),
                tint = if (index % 2 == 0) Ruby else Aqua
            )
        }
    }
}
```

- [ ] **Step 2: Build verification**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

### Task 2: Create NotiFastCenterAlignedTopBar atom

**Files:**
- Create: `app/src/main/java/com/example/notifast/presentation/atoms/NotiFastCenterAlignedTopBar.kt`
- Modify: None

- [ ] **Step 1: Write NotiFastCenterAlignedTopBar.kt**

```kotlin
package com.example.notifast.presentation.atoms

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotiFastCenterAlignedTopBar(title: String) {
    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold) },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
    )
}
```

- [ ] **Step 2: Build verification**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

### Task 3: Restore LoginScreen with original design + Google Sign-In

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/auth/LoginScreen.kt` (full rewrite)
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/auth/LoginContract.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/auth/LoginViewModel.kt`

- [ ] **Step 1: Rewrite LoginScreen with original design + Google button**

Replace entire LoginScreen.kt with the restored design featuring:
- `NotiFastStudyBackground` instead of `NotiFastBackground`
- Logo (`R.drawable.logo`) + "NotiFast" title in Ruby (42sp, Black weight, -1sp letterSpacing)
- Subtitle "Tu aprendizaje, a la velocidad de tus ideas."
- Inline toggle between Login and SignUp modes
- `NotiFastFormField` (equivalent to old `AttractiveTextField`)
- `statusBarsPadding()` + `navigationBarsPadding()`
- SignUp mode shows name fields + profile photo picker
- Login/SignUp button colored Ruby/Aqua depending on mode
- Error display in a Card
- Google Sign-In button always visible below the auth toggle
- On Google Sign-In success, navigate to Notes

```kotlin
package com.example.notifast.presentation.feature.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.notifast.R
import com.example.notifast.presentation.atoms.NotiFastFormField
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

private const val WEB_CLIENT_ID = "972069019951-kpd69bj1oik7kq9r4sg47rggcvohcdi8.apps.googleusercontent.com"

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoggedIn) {
        onLoginSuccess()
        return
    }

    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { viewModel.onEvent(LoginEvent.GoogleSignIn(it)) }
        } catch (e: ApiException) {
            viewModel.onEvent(LoginEvent.GoogleError(e.localizedMessage ?: "Error al iniciar con Google"))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "NotiFast",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Ruby,
                letterSpacing = (-1).sp
            )

            Text(
                text = "Tu aprendizaje, a la velocidad de tus ideas.",
                fontSize = 14.sp,
                color = Color.Gray.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (isSignUp) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Aqua.copy(alpha = 0.3f), CircleShape)
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(32.dp), tint = Aqua)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    NotiFastFormField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = "Nombre",
                        leadingIcon = Icons.Default.Person,
                        cursorColor = Aqua,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    NotiFastFormField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Apellido",
                        cursorColor = Aqua,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            NotiFastFormField(
                value = email,
                onValueChange = { email = it },
                label = "Correo electrónico",
                leadingIcon = Icons.Default.AlternateEmail,
                cursorColor = if (isSignUp) Aqua else Ruby
            )

            Spacer(modifier = Modifier.height(16.dp))

            NotiFastFormField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                leadingIcon = Icons.Default.LockOpen,
                cursorColor = if (isSignUp) Aqua else Ruby
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(color = if (isSignUp) Aqua else Ruby)
            } else {
                Button(
                    onClick = {
                        if (isSignUp) {
                            viewModel.onEvent(LoginEvent.SignUpWithDetails(email, password, firstName, lastName, selectedImageUri))
                        } else {
                            viewModel.onEvent(LoginEvent.LoginWithEmail(email, password))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSignUp) Aqua else Ruby),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
                ) {
                    Text(
                        text = if (isSignUp) "CREAR CUENTA" else "ENTRAR",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Continuar con Google")
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { isSignUp = !isSignUp }) {
                    Text(
                        text = if (isSignUp) "¿Ya eres parte de NotiFast? Inicia sesión"
                               else "¿Eres nuevo? Regístrate y empieza a aprender",
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            uiState.error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Update LoginContract.kt**

Replace existing events with:

```kotlin
package com.example.notifast.presentation.feature.auth

import android.net.Uri

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

sealed interface LoginEvent {
    data class LoginWithEmail(val email: String, val password: String) : LoginEvent
    data class SignUpWithDetails(
        val email: String,
        val password: String,
        val firstName: String,
        val lastName: String,
        val profileImageUri: Uri?
    ) : LoginEvent
    data class GoogleSignIn(val idToken: String) : LoginEvent
    data class GoogleError(val message: String) : LoginEvent
}
```

- [ ] **Step 3: Update LoginViewModel.kt**

Replace with:

```kotlin
package com.example.notifast.presentation.feature.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifast.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.LoginWithEmail -> login(event.email, event.password)
            is LoginEvent.SignUpWithDetails -> signUp(event.email, event.password, event.firstName, event.lastName, event.profileImageUri)
            is LoginEvent.GoogleSignIn -> signInWithGoogle(event.idToken)
            is LoginEvent.GoogleError -> _uiState.update { it.copy(error = event.message) }
        }
    }

    private fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email, password).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    private fun signUp(email: String, password: String, firstName: String, lastName: String, profileImageUri: Uri?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signUp(email, password, firstName, lastName, profileImageUri).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.signInWithGoogle(idToken).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isLoggedIn = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }
}
```

- [ ] **Step 4: Remove unused LoginForm.kt** (no longer needed, login UI is inline in LoginScreen)

Delete: `app/src/main/java/com/example/notifast/presentation/feature/auth/components/LoginForm.kt`

- [ ] **Step 5: Build verification**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

### Task 4: Restore NotesScreen with HeaderProfile

**Files:**
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/notes/NotesScreen.kt`
- Modify: `app/src/main/java/com/example/notifast/presentation/feature/notes/NotesContract.kt`

- [ ] **Step 1: Rewrite NotesScreen**

Changes:
- Remove `Scaffold(topBar = { NotiFastTopBar(title = "NotiFast") })`
- Use `Scaffold(containerColor = Color.Transparent)` with no topBar
- Add `safeDrawingPadding()` to root Box
- Add `HeaderProfile` composable at the top (profile pic + name + email + logout + add-subject/add-note buttons)
- Add "Mis Notas" title
- Restore original search bar with BasicTextField
- Restore filter chips
- Keep existing note list and components

- [ ] **Step 2: Create HeaderProfile composable (inline in NotesScreen)**

The HeaderProfile shows:
- Profile picture (circle, clickable → EditProfile)
- "Hola, {name}" + email
- Logout icon (error color)
- Add Subject button (Aqua, creates new folder)
- Add Note button (Ruby, + icon)

Note: Since we removed `AuthViewModel`, the NotesScreen needs to get user profile data. Add `AuthRepository` injection to NotesViewModel and expose user profile in `NotesUiState`.

- [ ] **Step 3: Build verification**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

### Task 5: Update all remaining screens to use transparent CenterAlignedTopAppBar

**Files to modify:**
- `app/src/main/java/com/example/notifast/presentation/feature/settings/SettingsScreen.kt`
- `app/src/main/java/com/example/notifast/presentation/feature/editprofile/EditProfileScreen.kt`
- `app/src/main/java/com/example/notifast/presentation/feature/addnote/AddNoteScreen.kt`
- `app/src/main/java/com/example/notifast/presentation/feature/editnote/EditNoteScreen.kt`
- `app/src/main/java/com/example/notifast/presentation/feature/addevaluation/AddEvaluationScreen.kt`
- `app/src/main/java/com/example/notifast/presentation/feature/editevaluation/EditEvaluationScreen.kt`
- `app/src/main/java/com/example/notifast/presentation/feature/calendar/CalendarScreen.kt`
- `app/src/main/java/com/example/notifast/presentation/feature/grades/GradesScreen.kt`
- `app/src/main/java/com/example/notifast/presentation/feature/schedules/SchedulesScreen.kt`
- `app/src/main/java/com/example/notifast/presentation/feature/subjectdetail/SubjectDetailScreen.kt`

- [ ] **Step 1: Replace NotiFastTopBar with NotiFastCenterAlignedTopBar in all screens**

For each screen:
- Replace `topBar = { NotiFastTopBar(title = "...", onBackClick = ...) }` with `topBar = { NotiFastCenterAlignedTopBar(title = "...") }`
- Add `containerColor = Color.Transparent` to Scaffold
- Remove `safeDrawingPadding()` if added (not needed with transparent top bar)

- [ ] **Step 2: Update imports** — replace `NotiFastTopBar` import with `NotiFastCenterAlignedTopBar` where needed

- [ ] **Step 3: Build verification**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

### Task 6: Final verification

- [ ] **Step 1: Full build and review**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Check all screens visually** (user needs to run on device)

Verify:
- LoginScreen has logo, icons background, inline signup toggle
- NotesScreen has profile header, search, filter chips
- All screens have transparent top bars
- Google Sign-In still works
