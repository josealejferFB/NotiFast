# Estrategia de Pruebas y Aseguramiento de Calidad: NotiFast

| Metadato | Valor |
|---|---|
| **Suite Automatizada Actual** | **805 pruebas unitarias automatizadas** |
| **Tasa de Aprobación** | **100% de éxito (0 fallos, 0 errores, 0 ignorados)** |
| **Herramientas de Testing** | JUnit 4, MockK `1.13.8`, Coroutines Test `1.9.0`, Turbine `1.0.0`, Robolectric `4.13` |
| **Bases de Datos de Prueba** | Room In-Memory Database (`Room.inMemoryDatabaseBuilder`) |
| **Pruebas de Red y Nube** | OkHttp `MockWebServer` |
| **Documentos Relacionados** | [Métricas de Éxito](../01-product/success-metrics.md) · [Arquitectura](../03-architecture/system-overview.md) · [Privacidad](./privacy-offline-first.md) |

---

## 1. La Pirámide de Pruebas en NotiFast

NotiFast fundamenta su robustez en una pirámide de pruebas diseñada para otorgar máxima velocidad de retroalimentación en el ciclo de desarrollo y verificación rigurosa para la tesis de grado:

```
          / \
         /   \       [Pruebas de Integración y End-to-End (E2E)]
        /     \      - Flujos completos Room + Workers + UI
       /───────\     - MockWebServer para Google Drive REST API
      /         \    
     /  Robolectric  [Pruebas de Componentes Android & ViewModels]
    /   + Turbine     - Turbinas de StateFlow y eventos MVI
   /─────────────────\  - Pruebas de migración SQLite (v1 ➔ v9)
  /                   \
 /    Pruebas Unitarias [Pruebas de Lógica Pura y Dominio]
/   Puras (MockK / JVM) - GradePolicy, ScheduleCalculations, ContentHasher
─────────────────────── - Decodificador ASCII85, Parsers de Texto
```

---

## 2. Distribución y Cobertura de la Suite (805 Tests)

La suite de 805 tests se distribuye sistemáticamente a lo largo de las tres capas de la Clean Architecture:

### 2.1 Capa de Dominio (Lógica Pura y Políticas)
- **`GradePolicyTest`:** Validación matemática exhaustiva del motor multiescala (Base 20, Base 10, Porcentual, GPA 4.0), promedios ponderados, redondeos y simulador de meta deseada.
- **`ScheduleCalculationsTest`:** Verificación de ventanas deslizantes semanales (`startOfWeek`), filtrado de evaluaciones por día y cálculo de minutos transcurridos en clases activas.
- **`ValidateScheduleOverlapUseCaseTest`:** Cobertura de solapamientos horarios en el mismo día, días distintos y clases consecutivas inmediatas.
- **`RescheduleAllAlertsUseCaseTest`:** Comprobación del re-armado masivo de alarmas en `ExactAlarmScheduler`.
- **`ResolveSubjectNameUseCaseTest`:** Verificación de resolución de nombres correlativos (`Cálculo (1)`, `Cálculo (2)`) ante colisiones en importación.
- **`TeacherFormattersTest` / `Ascii85DecoderTest`:** Validación léxica y decodificación de flujos comprimidos ReportLab.

### 2.2 Capa de Datos (Persistencia, Migraciones y Red)
- **`RoomDaoInMemoryTest`:** Pruebas directas sobre base de datos SQLite en memoria verificando operaciones CRUD, claves compuestas y borrado atómico en cascada (`UserDao`, `SubjectDao`, `NoteDao`, `EvaluationDao`, `ScheduleDao`, `TeacherDao`).
- **`NotiFastMigrationsTest`:** Validación de la no destrucción de datos a lo largo de las migraciones `1➔2➔3➔4➔5➔6➔7➔8➔9` utilizando el `MigrationTestHelper` de Room.
- **`ContentHasherTest`:** Verificación de la invariabilidad y determinismo del hash SHA-256 ante reordenamiento de listas o alteraciones mínimas de contenido.
- **`DriveRestClientTest`:** Simulación de peticiones HTTP en streaming multipart mediante **OkHttp MockWebServer**, validando códigos de respuesta 200, 204, 401 (reintento de token) y 404.
- **`LocalNoteRepositoryImplTest` / `LocalEvaluationRepositoryImplTest`:** Pruebas de integración de repositorios locales asegurando que los Flows emitan los datos mapeados hacia los modelos de dominio.

### 2.3 Capa de Presentación (ViewModels MVI y Sistema de Temas)
- **Pruebas de ViewModels con Turbine:** Cada ViewModel (`NotesViewModel`, `SchedulesViewModel`, `GradesViewModel`, `CloudViewModel`, `NoteEditorViewModel`, etc.) es sometido a flujos de eventos `onEvent` verificando con **Turbine** que las transiciones de `UiState` sean unívocas y libres de condiciones de carrera.
- **`ThemePaletteGeneratorTest`:** Verificación algorítmica de contraste WCAG AAA ($\ge 4.5:1$) sobre más de 50 muestras de colores primarios y de fondo.
- **Protección Anti-Doble Submit:** Pruebas unitarias que certifican que múltiples llamadas concurrentes a `saveNote()` o `saveSubject()` sean ignoradas síncronamente mientras `isSaving == true`.

---

## 3. Entorno y Herramientas de Ejecución

| Herramienta | Versión | Rol en la Estrategia |
|---|---|---|
| **JUnit 4** | `4.13.2` | Marco base de ejecución de pruebas unitarias. |
| **MockK** | `1.13.8` | Creación de mocks, spies y captura de argumentos en Kotlin idiomático. |
| **Kotlinx Coroutines Test** | `1.9.0` | Control temporal de coroutines (`runTest`, `StandardTestDispatcher`). |
| **Turbine** | `1.0.0` | Testeo asertivo de flujos reactivos `StateFlow` y `SharedFlow`. |
| **Robolectric** | `4.13` | Simulación de componentes del framework Android (Intents, Context, NotificationManager) en la JVM local sin emulador. |
| **OkHttp MockWebServer** | `4.12.0` | Servidor HTTP simulado para probar la integración con Google Drive REST API. |

---

## 4. Pipeline de Integración Continua (CI/CD)

En el repositorio del proyecto, el pipeline de GitHub Actions (`.github/workflows/ci.yml`) aplica la política de tolerancia cero:

```yaml
- name: Run Unit Tests Suite
  run: ./gradlew testDebugUnitTest --no-daemon --stacktrace

- name: Assemble Debug APK
  run: ./gradlew assembleDebug --no-daemon
```

**Regla de Oro:** Si un solo test de los 805 falla, el build se marca en rojo de inmediato y se prohíbe cualquier despliegue o fusión de código.
