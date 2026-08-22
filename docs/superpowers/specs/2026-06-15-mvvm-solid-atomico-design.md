# NotiFast — Migración a MVVM + SOLID + Arquitectura Atómica

**Fecha:** 2026-06-15
**Estado:** Aprobado
**Version del proyecto:** 0.6.0

---

## 1. Objetivo

Migrar la aplicación NotiFast de su arquitectura actual (capa única sin DI, sin interfaces, repositorios monolíticos) a una arquitectura **MVVM + SOLID atómica con componentes locales**, resolviendo los bugs críticos existentes en el proceso.

## 2. Principios rectores

| Principio | Aplicación |
|---|---|
| **SRP** | Cada archivo = una clase. Cada ViewModel = una feature. Cada repositorio = una entidad. |
| **DIP** | ViewModels dependen de interfaces, no de implementaciones concretas. Hilt inyecta. |
| **OCP** | Nueva feature = nuevo paquete. No se modifica código existente de otras features. |
| **ISP** | Interfaces pequeñas y específicas por entidad. |
| **Atomicidad** | Átomos compartidos en `presentation/atoms/`. Moléculas y organismos locales por feature. |
| **Sin shared ViewModels** | Cada feature tiene su propio ViewModel, UiState y eventos. |

## 3. Stack tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| Kotlin | 2.0.21 | Lenguaje |
| Jetpack Compose | BOM 2024.09.00 | UI declarativa |
| Material 3 | via BOM | Sistema de diseño |
| Navigation Compose | 2.8.5 | Navegación |
| Hilt | 2.51.1 (latest compatible) | Inyección de dependencias |
| Firebase Auth | via BOM 33.9.0 | Autenticación |
| Firebase Firestore | via BOM | Base de datos |
| Firebase Storage | via BOM | Almacenamiento de imágenes |
| Coil | 2.7.0 | Carga de imágenes |

## 4. Estructura de paquetes

```
com.notifast.app/
├── NotiFastApplication.kt              (@HiltAndroidApp)
├── MainActivity.kt                      (@AndroidEntryPoint -> setContent)
│
├── di/
│   ├── AppModule.kt                     (@Module @InstallIn(SingletonComponent))
│   │   → provee: FirebaseAuth, FirebaseFirestore, FirebaseStorage, Application
│   └── RepositoryModule.kt             (@Module @InstallIn(SingletonComponent))
│       → bind: AuthRepository, NoteRepository, EvaluationRepository, ImageRepository
│
├── data/
│   ├── model/                           # DTOs con anotaciones Firestore
│   │   ├── UserDto.kt
│   │   ├── NoteDto.kt
│   │   └── EvaluationDto.kt
│   └── repository/
│       ├── AuthRepositoryImpl.kt        # Autenticación + perfil
│       ├── NoteRepositoryImpl.kt        # CRUD notas (+ subject CRUD incluido)
│       ├── EvaluationRepositoryImpl.kt  # CRUD evaluaciones
│       └── ImageRepositoryImpl.kt       # Upload Firebase Storage + compress
│
├── domain/repository/                   # Interfaces puras (sin dependencias Android)
│   ├── AuthRepository.kt
│   ├── NoteRepository.kt
│   ├── EvaluationRepository.kt
│   └── ImageRepository.kt
│
├── presentation/
│   ├── atoms/                           # Componentes atómicos compartidos
│   │   ├── NotiFastButton.kt
│   │   ├── NotiFastTextField.kt
│   │   ├── NotiFastCard.kt
│   │   ├── NotiFastChip.kt
│   │   ├── NotiFastTopBar.kt
│   │   ├── NotiFastCircularLoading.kt
│   │   ├── NotiFastEmptyState.kt
│   │   ├── NotiFastDialog.kt
│   │   └── NotiFastBackground.kt
│   ├── theme/                           # (sin cambios, mantener)
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── navigation/
│   │   ├── Screen.kt                    # Sealed class + rutas con args tipados
│   │   └── NavGraph.kt                  # NavHost + composables()
│   └── feature/
│       ├── auth/
│       │   ├── LoginContract.kt         # UiState + Event sealed interface
│       │   ├── LoginViewModel.kt
│       │   ├── LoginScreen.kt
│       │   └── components/
│       │       └── LoginForm.kt
│       ├── notes/
│       │   ├── NotesContract.kt
│       │   ├── NotesViewModel.kt
│       │   ├── NotesScreen.kt
│       │   └── components/
│       │       ├── NoteCard.kt
│       │       ├── NoteSearchBar.kt
│       │       └── NoteFilterBar.kt
│       ├── addnote/
│       │   ├── AddNoteContract.kt
│       │   ├── AddNoteViewModel.kt
│       │   ├── AddNoteScreen.kt
│       │   └── components/
│       ├── editnote/
│       │   ├── EditNoteContract.kt
│       │   ├── EditNoteViewModel.kt
│       │   ├── EditNoteScreen.kt
│       │   └── components/
│       ├── subjectdetail/
│       │   ├── SubjectDetailContract.kt
│       │   ├── SubjectDetailViewModel.kt
│       │   ├── SubjectDetailScreen.kt
│       │   └── components/
│       ├── addevaluation/
│       │   ├── AddEvaluationContract.kt
│       │   ├── AddEvaluationViewModel.kt
│       │   ├── AddEvaluationScreen.kt
│       │   └── components/
│       ├── editevaluation/
│       │   ├── EditEvaluationContract.kt
│       │   ├── EditEvaluationViewModel.kt
│       │   ├── EditEvaluationScreen.kt
│       │   └── components/
│       ├── calendar/
│       │   ├── CalendarContract.kt
│       │   ├── CalendarViewModel.kt
│       │   ├── CalendarScreen.kt
│       │   └── components/
│       │       ├── CalendarGrid.kt
│       │       ├── EvaluationItem.kt
│       │       └── NoteItem.kt
│       ├── grades/
│       │   ├── GradesContract.kt
│       │   ├── GradesViewModel.kt
│       │   ├── GradesScreen.kt
│       │   └── components/
│       │       ├── GradeCard.kt
│       │       └── GradeRevampDialog.kt
│       ├── settings/
│       │   ├── SettingsContract.kt
│       │   ├── SettingsViewModel.kt
│       │   ├── SettingsScreen.kt
│       │   └── components/
│       ├── editprofile/
│       │   ├── EditProfileContract.kt
│       │   ├── EditProfileViewModel.kt
│       │   ├── EditProfileScreen.kt
│       │   └── components/
│       └── schedules/
│           ├── SchedulesContract.kt
│           ├── SchedulesViewModel.kt
│           ├── SchedulesScreen.kt       # Implementación real, no placeholder
│           └── components/
```

## 5. Diseño de Contratos (Contract Pattern)

Cada feature sigue este patrón exacto:

```kotlin
// ***Contract.kt — Estado + Eventos
data class XxxUiState(
    val isLoading: Boolean = false,
    val error: String? = null
    // + campos específicos de la feature
)

sealed interface XxxEvent {
    // eventos de UI que el ViewModel procesa
}
```

```kotlin
// ***ViewModel.kt
@HiltViewModel
class XxxViewModel @Inject constructor(
    private val repository: XxxRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(XxxUiState())
    val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()

    fun onEvent(event: XxxEvent) {
        // when(event) { ... }
    }
}
```

```kotlin
// ***Screen.kt
@Composable
fun XxxScreen(
    viewModel: XxxViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // UI usando átomos compartidos + moléculas locales
}
```

## 6. Flujo de datos

```
Firestore/FirebaseAuth/FirebaseStorage
        ↕ (SDK calls)
RepositoryImpl (data/repository/)
        ↕ (interface)
ViewModel (presentation/feature/xxx/)
        ↕ (StateFlow)
Screen composable (collectAsStateWithLifecycle)
```

- **Sin UseCases**: La lógica de coordinación (ej: subir imagen → crear nota) vive en el ViewModel.
- **Sin mappers complejos**: Los DTOs de Firestore sirven directamente como modelos de UI (son planos, sin lógica).

## 7. Navegación — Fix

**Problema actual:** `selectedNote`/`selectedEvaluation` como `mutableStateOf` en `NavGraph` se pierde en rotación.

**Solución:** Pasar `noteId`/`evaluationId` como argumentos de ruta tipados. El ViewModel recibe el ID via `SavedStateHandle` y carga los datos desde el repositorio.

```kotlin
sealed class Screen(val route: String) {
    data object Notes : Screen("notes")
    data object AddNote : Screen("add_note")
    data class EditNote(val noteId: String) : Screen("edit_note/{noteId}") {
        companion object { const val ROUTE = "edit_note/{noteId}" }
    }
    // ...
}
```

```kotlin
@HiltViewModel
class EditNoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val noteId: String = checkNotNull(savedStateHandle["noteId"])
    // carga la nota por ID
}
```

## 8. Manejo de imágenes — Fix

**Problema actual:** Las imágenes se guardan en `context.filesDir` local y no se sincronizan entre dispositivos.

**Solución:** Nuevo `ImageRepository` que:
1. Comprime el bitmap (calidad 80, JPEG)
2. Sube a Firebase Storage en `users/{uid}/images/{uuid}.jpg`
3. Retorna la URL de descarga (`downloadUrl`)
4. Guarda la URL en Firestore

```kotlin
interface ImageRepository {
    suspend fun uploadProfileImage(uid: String, uri: Uri): Result<String>
    suspend fun uploadNoteImage(uid: String, uri: Uri): Result<String>
    suspend fun deleteImage(url: String): Result<Unit>
}
```

## 9. Null safety — Fix

**Problema actual:** `authResult.user!!` en 3 lugares lanza NPE si es null.

**Solución:** Reemplazar con `?: return Result.failure(...)`.

```kotlin
val user = authResult.user ?: return Result.failure(AuthException("User is null"))
```

## 10. Eliminación de duplicación — Fix

**Problema actual:** 6 composables están definidos tanto en `NotesScreen.kt` como en archivos separados.

**Solución:** 
- `NotesScreen.kt` se reduce a ~50 líneas (solo el layout principal con Scaffold).
- Los componentes extraídos en `presentation/feature/notes/components/` se convierten en moléculas locales.
- Si son atómicos y reusables (ej: `EmptyStateView`), se mueven a `presentation/atoms/`.

## 11. Schedules — Fix

**Problema actual:** Placeholder "Próximamente".

**Solución:** Implementar pantalla básica con RecyclerView de horarios (data class Schedule con día, hora, materia, aula). Firestore collection `users/{uid}/schedules/`.

## 12. Hilt Modules

### AppModule.kt

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides @Singleton
    fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}
```

### RepositoryModule.kt

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds @Singleton
    abstract fun bindEvaluationRepository(impl: EvaluationRepositoryImpl): EvaluationRepository

    @Binds @Singleton
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository
}
```

## 13. Features — Mapeo completo

| Feature | ViewModel | Dependencias | Screen | Ruta |
|---|---|---|---|---|
| auth | LoginViewModel | AuthRepository | LoginScreen | `login` |
| notes | NotesViewModel | NoteRepository, ImageRepository | NotesScreen | `notes` |
| addnote | AddNoteViewModel | NoteRepository, ImageRepository | AddNoteScreen | `add_note` |
| editnote | EditNoteViewModel | NoteRepository, ImageRepository | EditNoteScreen | `edit_note/{noteId}` |
| subjectdetail | SubjectDetailViewModel | NoteRepository | SubjectDetailScreen | `subject_detail/{subjectName}` |
| calendar | CalendarViewModel | NoteRepository, EvaluationRepository | CalendarScreen | `calendar` |
| grades | GradesViewModel | EvaluationRepository | GradesScreen | `grades` |
| settings | SettingsViewModel | AuthRepository | SettingsScreen | `settings` |
| editprofile | EditProfileViewModel | AuthRepository, ImageRepository | EditProfileScreen | `edit_profile` |
| addevaluation | AddEvaluationViewModel | EvaluationRepository, NoteRepository | AddEvaluationScreen | `add_evaluation` |
| editevaluation | EditEvaluationViewModel | EvaluationRepository, NoteRepository | EditEvaluationScreen | `edit_evaluation/{evalId}` |
| schedules | SchedulesViewModel | SchedulesRepository (nuevo) | SchedulesScreen | `schedules` |

## 14. Cronograma de migración

### Fase 1 — Fundación
1. Agregar Hilt + dependencias Gradle
2. Crear `NotiFastApplication.kt` con `@HiltAndroidApp`
3. Crear interfaces en `domain/repository/`
4. Crear Hilt modules (`AppModule`, `RepositoryModule`)
5. Refactorizar `NotesRepository` → `NoteRepositoryImpl` + `EvaluationRepositoryImpl`
6. Crear `ImageRepositoryImpl` con Firebase Storage
7. Crear `presentation/atoms/` con componentes compartidos
8. Fix null safety en AuthRepository

### Fase 2 — Features
9. Migrar `auth/` feature
10. Migrar `notes/` feature (eliminar duplicación)
11. Migrar `addnote/` + `editnote/`
12. Migrar `calendar/`
13. Migrar `grades/`
14. Migrar `settings/` + `editprofile/`
15. Migrar `addevaluation/` + `editevaluation/`
16. Migrar `subjectdetail/`
17. Implementar `schedules/`

### Fase 3 — Limpieza
18. Eliminar archivos antiguos (ViewModels viejos, repositorios viejos, screens viejos)
19. Verificar build y navegación completa
20. Ejecutar lint

## 15. Riesgos y mitigaciones

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Rotura de datos en Firestore (cambio de estructura) | ALTO | No cambiamos estructura de Firestore, solo el código cliente |
| Hilt no compila con AGP 9.0.1 | MEDIO | Verificar compatibilidad Hilt + AGP 9 + Kotlin 2.0 en fase 1 |
| Pérdida de imágenes locales al migrar a Storage | MEDIO | Migrar imágenes gradualmente: primero nuevas, luego migrar existentes |
| Regresión en UI | MEDIO | Mantener mismo diseño visual exacto. Refactor es solo estructural |
| Build lento con Hilt | BAJO | Hilt KSP apt, usar `@Singleton` solo donde necesario |

## 16. Criterios de éxito

- ✅ Build compila sin errores
- ✅ Todas las rutas de navegación funcionan
- ✅ Login/registro funcional
- ✅ CRUD notas funcional
- ✅ CRUD evaluaciones funcional
- ✅ Calendario funcional
- ✅ Calificaciones funcional
- ✅ Subida de imágenes a Firebase Storage funcional
- ✅ Sin `!!` inseguros en repositorios
- ✅ Sin componentes duplicados
- ✅ Schedules implementado
- ✅ Sin warnings de lint graves
