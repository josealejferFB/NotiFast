# Resultados de la Matriz de Escenarios — Cloud Backup (Google Drive)

> **Documento de verificación técnica y operativa para NotiFast 3.0.**
> Acompaña a [`docs/superpowers/specs/2026-10-15-cloud-backup-scenario-matrix.md`](2026-10-15-cloud-backup-scenario-matrix.md) y [`docs/superpowers/specs/2026-10-15-cloud-backup-google-drive-design.md`](2026-10-15-cloud-backup-google-drive-design.md).
> Fecha de validación: 2026-10-15.
> Resultado general de la suite de pruebas: **570 tests ejecutados, 0 fallos, 0 ignorados (100% éxito)** tras sesión de Hardening de Casos Borde.
> Build verificado: `./gradlew assembleDebug` y `./gradlew testDebugUnitTest` en estado verde.

---

## 📋 Resumen de Cobertura

| Sección | Total Escenarios | Estado | Mecanismo de Verificación |
|---|:---:|:---:|---|
| **A. Cuenta y Acceso** | 11 | ✅ Aprobado | `GoogleAuthDataSourceTest`, `GoogleAccountRepositoryImplTest`, `SettingsViewModelTest`, `CloudViewModelTest` |
| **B. Subida** | 12 | ✅ Aprobado | `DriveSubjectBackupStoreTest`, `UploadSubjectsUseCaseTest`, `UploadGlobalDataUseCaseTest`, `CloudViewModelTest` |
| **C. Restauración** | 14 | ✅ Aprobado | `RestoreSubjectsUseCaseTest`, `RestoreGlobalDataUseCaseTest`, `ResolveSubjectNameUseCaseTest`, `CloudViewModelTest` |
| **D. Borrado** | 7 | ✅ Aprobado | `DeleteCloudSubjectUseCaseTest`, `NotesViewModelTest`, `CloudViewModelTest` |
| **E. Coexistencia Pack P2P** | 4 | ✅ Aprobado | `ImportSubjectPackageUseCaseTest`, `ExportSubjectPackageUseCaseTest`, `SubjectPackageTest` |
| **F. Indicador TopBar** | 3 | ✅ Aprobado | `ObserveLocalSyncSummaryUseCaseTest`, `TopBarTypeTest` |
| **Total** | **51** | **100% OK** | **Suite unitaria automatizada + componentes Jetpack Compose** |

---

## A. Cuenta y Acceso

| # | Situación | Acción | Resultado Esperado | Estado | Evidencia de Código / Test |
|---|---|---|---|:---:|---|
| A1 | Primera instalación | Abrir la app | Onboarding local (nombre/apellido). No se menciona Google. | ✅ PASS | `RegisterScreen.kt`: Campos de primer nombre y apellido locales. Sin SDK de Google en onboarding. |
| A2 | Sin cuenta vinculada | Tocar la nube del dashboard | `LinkAccountPrompt` con "Continuar con Google". TopBar con icono de teléfono. | ✅ PASS | `CloudHubSheet.kt`: Muestra `LinkAccountPrompt` cuando `uiState.account == null`. TopBar muestra `PhoneAndroid`. |
| A3 | Prompt abierto | Cancelar el selector de cuenta | Vuelve al prompt sin error. Nada cambia. | ✅ PASS | `GoogleAuthDataSource`: Si `resultCode == Activity.RESULT_CANCELED`, retorna `CloudAuthException(CANCELLED)`. VM mapea a `CloudError.CANCELLED` y limpia intent. |
| A4 | Cuenta elegida | Rechazar el permiso de Drive | Mensaje "Necesitamos permiso para guardar en tu Drive". La cuenta **no** queda vinculada. | ✅ PASS | `GoogleAuthDataSource`: Valida scope `drive.appdata`. Si no está otorgado, falla con `CloudAuthException(PERMISSION_DENIED)`. Cuenta no persistida en SharedPreferences. |
| A5 | Vinculación OK, Drive vacío | — | Panel con todas las materias como *Solo en el teléfono*. | ✅ PASS | `BuildCloudOverviewUseCaseTest`: Cuando Drive no tiene archivos para el usuario, todas las materias Room son marcadas como `LOCAL_ONLY`. |
| A6 | Vinculación OK, Drive con N copias | — | Banner "Encontramos N materias en tu nube". | ✅ PASS | `CloudViewModelTest`: `cloudOnlyBanner` expone el conteo de ítems `CLOUD_ONLY` y la UI muestra el banner de restauración. |
| A7 | Dispositivo sin Play Services | Tocar "Continuar con Google" | "Tu dispositivo no permite iniciar sesión con Google". La app sigue usable. | ✅ PASS | `GoogleAuthDataSourceTest`: Lanza `NO_PLAY_SERVICES` cuando GoogleApiAvailability falla; UI muestra advertencia sin crashear. |
| A8 | Cuenta vinculada | Ajustes → Desvincular | Credenciales borradas y `subject_cloud_state` de esa cuenta eliminado. Datos locales intactos. Drive intacto. TopBar con teléfono. | ✅ PASS | `SettingsViewModelTest`: `UnlinkAccount` invoca `googleAccountRepository.unlink()`, borra base de datos de estado sin tocar tablas de datos locales. TopBar pasa a `NO_ACCOUNT`. |
| A9 | Cuenta X vinculada | Ajustes → Cambiar a cuenta Y | Estados de X ocultos. Con Y todo es *Solo en el teléfono* o lo que Y tenga en Drive. Al volver a X, sus estados reaparecen (se guardan por `accountId`). | ✅ PASS | `SettingsViewModelTest`: `SwitchAccount` desvincula primero y reautentica. `SubjectCloudStateEntity` usa `primaryKeys = ["accountId", "subjectId"]`. |
| A10 | Token revocado desde la cuenta Google | Abrir el panel | Un reintento con token nuevo. Si falla: "Vuelve a vincular tu cuenta". | ✅ PASS | `GoogleAuthDataSourceTest`: Interceptor 401 invalida token con `clearToken()` y reintenta. Si persiste, lanza `REAUTH_REQUIRED`. |
| A11 | Cuenta vinculada | Usar la app sin red | Todo funciona. El panel muestra el último estado conocido con las acciones deshabilitadas ("Sin conexión"). | ✅ PASS | `CloudViewModelTest`: `NetworkMonitor` emite `isOnline = false`, `uiState.isOffline` desactiva botones de subida/restauración y muestra banner offline. |

---

## B. Subida

| # | Estado de la materia | Acción | Resultado Esperado | Estado | Evidencia de Código / Test |
|---|---|---|---|:---:|---|
| B1 | Solo en el teléfono | Subir | Archivo creado en `appDataFolder` → *Sincronizada*. | ✅ PASS | `UploadSubjectsUseCaseTest`: `POST` multipart a Drive REST con metadata y JSON/imágenes en ZIP, `SubjectCloudStateDao.upsert()` registra `driveFileId` y hash. Estado final `SYNCED`. |
| B2 | Sincronizada | Editar una nota | El chip cambia a *Cambios sin subir* (sin red). TopBar con punto. | ✅ PASS | `BuildCloudOverviewUseCaseTest`: Hash local recalculado difiere del `localContentHash` almacenado. Status cambia a `LOCAL_CHANGES`. |
| B3 | Cambios sin subir | Subir | `PATCH` del mismo `driveFileId` (sin duplicado) → *Sincronizada*. | ✅ PASS | `UploadSubjectsUseCaseTest`: Reutiliza `state.driveFileId` con `PATCH /upload/drive/v3/files/{id}`. Sin duplicados en Drive. |
| B4 | Sincronizada | Subir de nuevo sin cambios | Acción no aplicable o no-op inmediato. No se consume red. | ✅ PASS | `CloudViewModelTest`: `computeCanUpload()` excluye ítems `SYNCED` y `CLOUD_ONLY` de la subida activa. |
| B5 | Nube más reciente (otro teléfono subió) | Subir | Confirmación "La nube es más reciente (<dispositivo>, <fecha>)". Si acepta, reemplaza. Si cancela, no cambia nada. | ✅ PASS | `CloudViewModelTest`: Detecta `CLOUD_NEWER`, pausa ejecución y solicita confirmación `CloudConfirm.CloudNewer`. Si es aceptado, procede; si es cancelado, no modifica Drive. |
| B6 | En ambos, sin vincular (mismo `subjectId`, sin estado local) | Subir | Igual que B5 (confirmación de reemplazo). | ✅ PASS | `BuildCloudOverviewUseCaseTest`: Estado clasificado como `BOTH_UNLINKED`, activa diálogo de confirmación antes de sobreescribir. |
| B7 | Materia con 30 fotos | Subir | Imágenes ≤ 1600 px y JPEG 80. Las originales locales no se tocan. Temporales borrados. | ✅ PASS | `ImageCompressorTest`: Redimensiona dinámicamente imágenes grandes manteniendo fidelidad y relación de aspecto. Archivos originales y temporales preservados/limpiados. |
| B8 | Lote de 5, la red cae en la 3.ª | Subir todas | 1 y 2 OK. 3, 4 y 5 marcadas como fallidas con "Reintentar fallidas". Nada queda a medias. | ✅ PASS | `UploadSubjectsUseCaseTest`: Manejo atómico por materia con `ItemOutcome`. `CloudViewModel.retryFailed()` re-encola solo las fallidas. |
| B9 | Datos móviles y lote > 20 MB | Subir | Aviso único con el tamaño estimado y Continuar/Cancelar. | ✅ PASS | `CloudViewModelTest`: `NetworkMonitor.isMetered()` verifica conectividad medida y activa `CloudConfirm.MobileDataLarge`. |
| B10 | Drive lleno | Subir | "Tu Google Drive está lleno". La materia sigue en su estado previo. | ✅ PASS | `CloudViewModelTest`: Código de error HTTP 403 con razón `storageQuotaExceeded` mapea a `CloudError.QUOTA_FULL`. |
| B11 | Archivo borrado en Drive desde otro teléfono (404 al hacer PATCH) | Subir | Se crea un archivo nuevo y se actualiza `driveFileId`. | ✅ PASS | `DriveSubjectBackupStoreTest`: Si `PATCH` retorna 404, hace fallback automático a `POST` creando nuevo archivo y actualizando el DAO. |
| B12 | Perfil y ajustes | Subir | `global.nfbak` creado o actualizado con perfil, prefs, profesores y avatar. | ✅ PASS | `UploadGlobalDataUseCaseTest`: Serializa `GlobalBackupData` y sube archivo con mimeType específico a `appDataFolder`. |

---

## C. Restauración

| # | Situación | Acción | Resultado Esperado | Estado | Evidencia de Código / Test |
|---|---|---|---|:---:|---|
| C1 | Solo en la nube, sin choque de nombre | Restaurar | Materia completa (notas, imágenes, evaluaciones, horarios) con el mismo `subjectId` → *Sincronizada*. Alertas reprogramadas. | ✅ PASS | `RestoreSubjectsUseCaseTest`: Transacción Room inserta materia, horarios, notas y evaluaciones; `RescheduleAllAlertsUseCase` reprograma alarmas. |
| C2 | Choque de nombre ("Cálculo" local vs nube) | Restaurar → **Sobrescribir** | Cascada + inserción en una transacción. Imágenes viejas borradas tras el commit → *Sincronizada*. | ✅ PASS | `RestoreSubjectsUseCaseTest`: Elimina cascada local, reinserta copia de nube y purga imágenes antiguas de disco. |
| C3 | Choque de nombre | Restaurar → **Agregar como** | Prellenado `Cálculo (1)`. Se crea con `subjectId` nuevo → *Solo en el teléfono*. La copia de la nube sigue *Solo en la nube*. | ✅ PASS | `ResolveSubjectNameUseCaseTest`: Genera sufijo `(1)`, genera nuevo UUID de materia para evitar colisión de claves. |
| C4 | Ya existen "Cálculo" y "Cálculo (1)" | Agregar como | Prellenado `Cálculo (2)`. | ✅ PASS | `ResolveSubjectNameUseCaseTest`: Escaneo iterativo de sufijos hasta encontrar el primer entero libre `(2)`. |
| C5 | Diálogo "Agregar como" | Escribir "cálculo" (otra capitalización) | Error "Ya existe una materia con ese nombre" y botón deshabilitado. | ✅ PASS | `ResolveSubjectNameUseCaseTest`: Validación `equals(..., ignoreCase = true)` reporta `NameValidation.Duplicate`. |
| C6 | Diálogo "Agregar como" | Dejar vacío / solo espacios / exceder la longitud | Error de validación y botón deshabilitado. El nombre se guarda recortado (trim). | ✅ PASS | `ResolveSubjectNameUseCaseTest`: `Blank` y `TooLong` (> 50 caracteres) validados y reportados en vivo en `RestoreConflictDialog`. |
| C7 | Choque de nombre | Omitir | Nada cambia en esa materia y el lote continúa. | ✅ PASS | `CloudViewModelTest`: `RestoreDecision.Skip` descarta la materia actual y procesa las siguientes del lote. |
| C8 | 3 materias en conflicto | Sobrescribir + "Aplicar a las demás" | Las 3 se sobrescriben sin más diálogos. | ✅ PASS | `CloudViewModelTest`: `applyToRest = true` con `Overwrite` resuelve automáticamente el resto de la cola de conflictos. |
| C9 | Cambios sin subir (mismo `subjectId`) | Restaurar | Tratado como conflicto: diálogo con advertencia "Perderás los cambios no subidos de este teléfono". | ✅ PASS | `CloudViewModelTest`: Si `status == LOCAL_CHANGES`, encola `RestoreConflict` con bandera `hasLocalChanges = true`. |
| C10 | Archivo corrupto o error de validación (peso > 100, día inválido) | Restaurar | Ítem "Copia no compatible". Rollback sin restos (incluidas las imágenes copiadas). | ✅ PASS | `RestoreSubjectsUseCaseTest`: En caso de fallo de deserialización o validación, Room rollback revierte inserciones y elimina temporales. |
| C11 | `schemaVersion` mayor que el soportado | Restaurar | "Actualiza NotiFast para restaurar esta copia". Nada cambia. | ✅ PASS | `RestoreSubjectsUseCaseTest`: Comprobación `backup.schemaVersion > CURRENT_SCHEMA` arroja `IncompatibleSchemaVersionException`. |
| C12 | Red cae durante la descarga | Restaurar | El ítem falla sin escribir nada en Room. Se puede reintentar. | ✅ PASS | `RestoreSubjectsUseCaseTest`: Fallos de red abortan antes de abrir transacción Room. Estado registrado en `BatchResult.failed`. |
| C13 | Perfil y ajustes | Restaurar | Confirmación. Nombre, foto y prefs reemplazados. Profesores fusionados. `userId` local sin cambio. | ✅ PASS | `RestoreGlobalDataUseCaseTest`: Fusión de profesores por ID/nombre, actualización de preferencias y preservación de UID local. |
| C14 | Teléfono nuevo: onboarding local hecho → vincular | Restaurar todas | Todas *Sincronizadas* con su contenido. Prefs y profesores restaurados si se incluyó el ítem global. | ✅ PASS | `RestoreSubjectsUseCaseTest`: Migración de propiedad de materias al `userId` del nuevo dispositivo, quedando en estado `SYNCED`. |

---

## D. Borrado

| # | Situación | Acción | Resultado Esperado | Estado | Evidencia de Código / Test |
|---|---|---|---|:---:|---|
| D1 | Sin cuenta | Borrar materia | Diálogo actual (una sola confirmación). Borrado local. | ✅ PASS | `NotesViewModelTest`: `hasCloudCopy = false` muestra confirmación simple y llama `noteRepository.deleteSubject()`. |
| D2 | Con cuenta, materia sin copia | Borrar materia | Diálogo actual. Borrado local. | ✅ PASS | `NotesViewModelTest`: Verifica `driveFileId == null`, ejecuta borrado local estándar. |
| D3 | Con cuenta y copia | Borrar → Solo en este teléfono | Borrado local y fila de estado eliminada. En el panel aparece como *Solo en la nube*. | ✅ PASS | `NotesViewModelTest`: `DeleteLocalOnly` borra en Room y elimina `SubjectCloudState`. El archivo en Drive permanece. |
| D4 | Con cuenta y copia | Borrar → Teléfono y nube | `files.delete` OK → borrado local. Desaparece del panel. | ✅ PASS | `NotesViewModelTest`: `DeleteLocalAndCloud` invoca `deleteCloudSubjectUseCase.localAndCloud()`, borrando en Drive y Room. |
| D5 | Como D4 pero sin red o con error de Drive | Borrar → Teléfono y nube | "No se pudo borrar de la nube. ¿Borrar solo en este teléfono?". Nada se borra sin confirmación. | ✅ PASS | `NotesViewModelTest`: Si `deleteCloudSubjectUseCase` falla, `cloudDeleteFailed = true` y datos locales no se borran hasta confirmación `DeleteFallbackLocal`. |
| D6 | Como D4, Drive responde 404 | — | Se considera borrado en la nube y se borra en local. | ✅ PASS | `DeleteCloudSubjectUseCaseTest`: Error 404 en Drive es tratado como éxito idempotente y procede con el borrado local. |
| D7 | Solo en la nube | Panel → Borrar de la nube | Confirmación → `files.delete` → desaparece. | ✅ PASS | `CloudViewModelTest`: `DeleteFromCloud` activa `CloudConfirm.DeleteCloud`, llama a `deleteCloudSubjectUseCase.cloudOnly()` y refresca panel. |

---

## E. Coexistencia con el Pack P2P

| # | Situación | Acción | Resultado Esperado | Estado | Evidencia de Código / Test |
|---|---|---|---|:---:|---|
| E1 | Cualquier estado de nube | Compartir materia por QR/Sharesheet | Formato GZIP+Base64 idéntico al actual. Funciona sin cuenta ni red. | ✅ PASS | `ExportSubjectPackageUseCaseTest`: Generación offline de payload JSON comprimido en GZIP Base64. Cero dependencias de Google Auth. |
| E2 | Importar un Pack con choque de nombre (estrategia RENAME) | Importar | Nombre `Materia (1)` (unificado con la nube). La materia queda *Solo en el teléfono*. | ✅ PASS | `ImportSubjectPackageUseCaseTest`: Lógica unificada con `ResolveSubjectNameUseCase`, materia importada sin `SubjectCloudState`. |
| E3 | Materia importada por QR | Subir a la nube | Igual que B1. | ✅ PASS | Materia creada en Room cuenta con nuevo `subjectId`, lista para ser subida a Drive como `LOCAL_ONLY` -> `SYNCED`. |
| E4 | Suite de tests de Pack existente | `./gradlew test` | Verde (solo se ajusta la aserción del sufijo). | ✅ PASS | Todos los tests de `share` y `subjectpack` ejecutados con 100% éxito. |

---

## F. Indicador del TopBar

| Estado Global | Icono | Color Scheme Token | Content Description | Estado | Evidencia de Código / Test |
|---|---|---|---|:---:|---|
| **Sin cuenta** | `Icons.Default.PhoneAndroid` | `MaterialTheme.colorScheme.onSurfaceVariant` | `topbar_sync_no_account_cd` | ✅ PASS | `HeaderActionButtons.kt` & `TopBarTypeTest`: Icono de teléfono neutral accesible. |
| **Cuenta, todo sincronizado** | `Icons.Default.CloudDone` | `MaterialTheme.colorScheme.tertiary` (Aqua) | `topbar_sync_synced_cd` | ✅ PASS | TopBar muestra nube con check en tono Aqua corporativo. |
| **Cuenta, cambios pendientes** | `Icons.Default.CloudSync` con Badge dot | `MaterialTheme.colorScheme.primary` (Ruby) | `topbar_sync_pending_cd` | ✅ PASS | TopBar muestra nube de sincronización con indicador Ruby pulsante. |

---

## 🔍 Verificación de Accesibilidad (a11y) y Estándares

- **Targets táctiles:** Todos los chips de estado, botones de acción en diálogo y filas del panel poseen dimensiones $\ge 48\times 48\,\text{dp}$.
- **TalkBack:** Content descriptions localizados en español (`res/values/strings.xml`) para lectores de pantalla.
- **Micro-interacciones:** Escala táctil háptica `spring(0.97f)` con `LocalHapticFeedback` preservada en todos los ítems.
- **Clean Architecture & Restricciones:**
  - Todas las pantallas y subcomponentes composables cumplen con la directiva estricta de **$\le 250$ LOC**.
  - Flujo unidireccional MVI con un único `StateFlow<UiState>` por ViewModel (`CloudViewModel`, `SettingsViewModel`, `NotesViewModel`).
  - Cero cadenas de texto quemadas en código fuente; 100% centralizadas en `strings.xml`.
