# Especificación de Diseño: Importación Automática de Horarios PDF (DACE-UNERG)

**Fecha:** 19 de Octubre de 2026  
**Módulo:** Agenda (`presentation/feature/schedules`), Dominio (`domain/usecase/schedule`, `domain/util`), Datos (`data/local`, `data/repository`)  
**Estado:** Aprobado para Planificación (Fase de Ejecución pendiente de orden del usuario)

---

## 1. Propósito y Contexto

Los estudiantes de la **Universidad Nacional Experimental Rómulo Gallegos (UNERG)** descargan su comprobante oficial de inscripción/horario desde el sistema **DACE** en formato PDF (`Horario_de_Clases_<cedula>_<id>.pdf`). Actualmente deben transcribir manualmente cada materia, su sección y cada bloque de clases (día, hora de inicio, hora de fin y aula) dentro de NotiFast.

Esta funcionalidad permite que, desde la vista **Agenda**, el estudiante suba directamente el PDF oficial de DACE-UNERG para que NotiFast:
1. Extraiga e interprete de forma **100% Offline** las materias inscritas, su código, su **Sección**, el período académico y sus bloques de clases.
2. **Ignore por completo** los bloques marcados como `NO PRESENCIAL` (`A distancia`), conservando exclusivamente las horas presenciales con día de la semana asignado (`LUNES` a `DOMINGO`).
3. Presente una **Hoja Modal de Vista Previa (`UnergScheduleImportSheet`)** con estándar visual AAA donde el estudiante revise las materias detectadas (con su sección, ícono semántico y color sugerido), sus bloques horarios y elija cómo proceder ante materias/horarios ya existentes antes de confirmar la creación masiva.
4. Guarde el dato de la **Sección (`section`)** en la base de datos Room directamente vinculado a cada materia (`SubjectEntity` / `SubjectData`).

---

## 2. Objetivos, No-Objetivos y Anti-Objetivos

### Objetivos
- **Persistencia de Sección en Base de Datos (`v8 ➔ v9`):** Añadir el campo `section: String = ""` a `SubjectEntity` y `SubjectData`, con migración no destructiva `MIGRATION_8_9` en Room y propagación completa en repositorios (`LocalNoteRepositoryImpl`), respaldos (`RoomSubjectBackupStore`), paquetes P2P (`ImportSubjectPackageUseCase`) y formularios de creación/edición de materias.
- **Motor de Parsing PDF Nativo 100% Offline (`UnergSchedulePdfParser`):** Decodificar sin librerías pesadas externas los flujos PDF generados por ReportLab en DACE-UNERG (`/Filter [ /ASCII85Decode /FlateDecode ]` y `/FlateDecode` estándar, codificación `WinAnsiEncoding` con secuencias octales como `\311` ➔ `É`, `\323` ➔ `Ó`, `\315` ➔ `Í`, `\341` ➔ `á`).
- **Filtrado Estricto de Modalidad:** Descartar el 100% de las filas `NO PRESENCIAL` / `A distancia` y las filas vacías (`-`), normalizando los rangos horarios de `HH:mm:ss - HH:mm:ss` a `HH:mm` (`12:31:00 - 14:00:00` ➔ `12:31` a `14:00`).
- **Asignación Semántica Inteligente:** Sugerir automáticamente un ícono de `SubjectIconMapper` y un color de `ACCESSIBLE_STUDY_PALETTE` en función del nombre de la materia (ej. *Sistemas Distribuidos / Informática* ➔ `Computer`, *Cálculo / Matemática* ➔ `Calculate`, *Proyecto de Grado / Gerencia* ➔ `Book`/`School`), evitando colisiones de color entre las materias del mismo PDF.
- **Vista Previa Interactiva AAA en Agenda:** Mostrar un `ModalBottomSheet` con metadatos del documento (estudiante, carrera, período, conteo de materias y bloques presenciales), lista de tarjetas por materia (con su chip `Sección X`, aula y horario), selector de estrategia en caso de materias existentes y confirmación en un toque con programación automática de alarmas exactas (`ScheduleClassAlertUseCase`).

### No-Objetivos
- No se requiere OCR por visión artificial ni conexión a servidores externos; los PDFs oficiales de DACE-UNERG contienen texto vectorial estructurado.
- No se crearán bloques de horario para las filas `NO PRESENCIAL`.

### Anti-Objetivos (Lo que haría fallar la solución aunque funcione técnicamente)
- Agregar dependencias pesadas de terceros (ej. PDFBox de +15 MB) que inflen el APK para leer un flujo ReportLab de 3 KB.
- Crear bloques duplicados si el estudiante sube dos veces el mismo PDF.
- Perder el campo `section` al editar una materia, al exportar un paquete `.notifast` o al sincronizar con Google Drive Cloud Backup.

---

## 3. Anatomía Técnica del PDF Oficial DACE-UNERG

Ambos documentos reales analizados (`Horario_de_Clases_30961367_19 (2).pdf` y `Horario_de_Clases_31053939_19.pdf`) comparten la misma estructura generada por **ReportLab PDF Library (`%PDF-1.4`)**:

1. **Compresión del Stream de Página:**
   - El objeto de contenido de página utiliza `/Filter [ /ASCII85Decode /FlateDecode ]` (o `/FlateDecode` directo si el PDF fue re-guardado por un visor).
   - El decodificador debe soportar ambos: si el stream está en ASCII85 (`<~ ... ~>` o terminado en `~>`), aplicar `Ascii85Decoder` seguido de `Inflater` (`zlib`); si empieza con cabecera zlib directo, aplicar `Inflater`; y si ya es texto plano con operadores `BT ... ET`, leerlo directamente.
2. **Codificación de Caracteres (`WinAnsiEncoding` / ISO-8859-1):**
   - Los acentos vienen como escapes octales de 3 dígitos dentro de literales de texto PDF `(...) Tj`:
     - `\301` = `Á`, `\311` = `É`, `\315` = `Í`, `\323` = `Ó`, `\332` = `Ú`, `\321` = `Ñ`, `\341` = `á`, `\351` = `é`, `\355` = `í`, `\363` = `ó`, `\372` = `ú`, `\361` = `ñ`.
   - Los paréntesis literales vienen escapados como `\(` y `\)`.
3. **Secuencia de Bloques de Texto (`BT ... ET`):**
   - Encabezado institucional: `UNIVERSIDAD NACIONAL EXPERIMENTAL RÓMULO GALLEGOS`, `HORARIO DE CLASES`.
   - Datos del estudiante (opcionales para el resumen de vista previa):
     - `C.I.: <cedula> - <Nombre Estudiante>`
     - `CARRERA: <carrera>`
     - `PERÍODO: <periodo> - Inscripcion` (ej. `20262`)
   - Cabecera de tabla: `COD. MATERIA`, `NOMBRE MATERIA`, `SECCIÓN`, `CLASE`, `AULA`.
   - **Fila cabecera de cada materia:**
     - `code`: código de asignatura (`EL`, `PG0083`, `IS0222`, `ID0221`, `EA0244`, `IT9241`).
     - `name`: nombre de la asignatura (`ELECTIVA LIBRE II`, `PROYECTO DE GRADO II`, etc.), el cual puede ocupar una o múltiples líneas `(Línea 1) Tj T* (Línea 2) Tj` dentro del mismo bloque `BT ... ET`.
     - `section`: número o código de sección (`1`, `2`, `3`, etc.).
     - Marcadores vacíos de clase/aula en la fila principal: `-`, `-`.
   - **Sub-filas de clase de la materia actual:**
     - Par `(claseText, aulaText)`:
       - Si `claseText` comienza por `NO PRESENCIAL:` (o `aulaText` es `A distancia`), **se ignora por completo**.
       - Si `claseText` coincide con `<DÍA>: HH:mm(:ss)? - HH:mm(:ss)?` (donde `<DÍA>` $\in$ `{LUNES, MARTES, MIÉRCOLES, MIERCOLES, JUEVES, VIERNES, SÁBADO, SABADO, DOMINGO}`), se extrae:
         - `dayOfWeek`: `Domingo=0, Lunes=1, Martes=2, Miércoles=3, Jueves=4, Viernes=5, Sábado=6` (compatible con `Schedule.DAY_NAMES`).
         - `startTime`: `HH:mm` (recortando los segundos `:00`).
         - `endTime`: `HH:mm` (recortando los segundos `:00`).
         - `classroom`: valor de la celda `AULA` (ej. `Aula 2`, `Aula 1 Auleta`, `LAB 3`).
   - Fin de la tabla: token que comienza por `SCIH -` o `Este documento sin el sello`.

### Verificación contra los 2 PDFs de Ejemplo

| PDF | Materias Detectadas | Período | Bloques Presenciales Creados | Bloques No Presenciales Ignorados |
|---|---|---|---|---|
| `30961367_19 (2).pdf` | 6 (`Electiva Libre II` Sec. 2, `Proyecto de Grado II` Sec. 3, `Informática Educativa` Sec. 3, `Gerencia de Proyecto` Sec. 2, `Electiva de Área III` Sec. 2, `Sistemas Distribuidos` Sec. 1) | `20262` | **6 bloques** (Lun 10:16-12:30, Mar 12:31-14:45, Mié 09:31-11:00, Mié 12:31-14:00, Jue 08:00-10:15, Jue 10:16-11:45) | 6 ignorados |
| `31053939_19.pdf` | 4 (`Proyecto de Grado II` Sec. 3, `Informática Educativa` Sec. 3, `Gerencia de Proyecto` Sec. 2, `Sistemas Distribuidos` Sec. 1) | `20262` | **4 bloques** (Lun 10:16-12:30, Mié 09:31-11:00, Jue 08:00-10:15, Jue 10:16-11:45) | 4 ignorados |

---

## 4. Cambios en Base de Datos y Dominio (`section`)

### 4.1 Esquema Room v9 y Migración `MIGRATION_8_9`
- En `SubjectEntity`:
  ```kotlin
  data class SubjectEntity(
      val userId: String = "",
      val id: String = "",
      @ColumnInfo(collate = ColumnInfo.NOCASE)
      val name: String = "",
      val icon: String = "📚",
      val colorHex: String = "",
      val cycle: String = "",
      val section: String = "",
      val createdAt: Date? = null
  )
  ```
- En `NotiFastMigrations`:
  ```kotlin
  val MIGRATION_8_9 = object : Migration(8, 9) {
      override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL("ALTER TABLE `subjects` ADD COLUMN `section` TEXT NOT NULL DEFAULT ''")
      }
  }
  ```
- En `NotiFastDatabase`: incrementar `version = 9` y registrar `MIGRATION_8_9` en `DatabaseModule`.

### 4.2 Modelo de Dominio y Repositorios
- En `SubjectData`: añadir `val section: String = ""`.
- En `NoteRepository.addSubject(...)`: añadir parámetro `section: String = ""`.
- En `LocalNoteRepositoryImpl`:
  - `getSubjects`: mapear `section = it.section`.
  - `addSubject`: persistir `section = section.trim()`.
  - `updateSubject`: actualizar `section = newSubject.section.trim().ifBlank { existing.section }` (o permitir edición explícita desde `EditSubjectDialog` y `AddSubjectScreen`).
- En `RoomSubjectBackupStore` e `ImportSubjectPackageUseCase`: propagar `section` en `toDomain()`, `insertSnapshot()`, `replaceSnapshot()` e importación de paquetes `.notifast`.

### 4.3 Visibilidad de `section` en la UI
- **Tarjetas de Materias (`SubjectBentoCard`) y Detalle de Materia (`SubjectDetailHeroBento`):** Mostrar chip/subtítulo `Sec. X` cuando `section.isNotBlank()`.
- **Tarjetas de Horario en Agenda (`ScheduleCard`):** Mostrar el chip de sección junto al aula o como chip de detalle (`Sec. X`) tomando la sección de la materia asociada.
- **Creación y Edición de Materia (`SubjectEditorFormFields`, `EditSubjectDialog`):** Incluir campo opcional de `Sección (ej. 1, 2, 3)` junto al nombre de la materia.

---

## 5. Flujo de Usuario y Vista Previa en Agenda (`UnergScheduleImportSheet`)

1. **Puntos de Entrada en `SchedulesScreen` (Agenda):**
   - **FAB Expandible (`buildSchedulesFabActions`):** Nueva tercera acción `"Importar PDF (UNERG)"` con ícono `Icons.Default.PictureAsPdf` y color semántico accesible.
   - **Estado Vacío (`ScheduleTimelineView`):** Botón secundario/acción rápida `"Importar horario PDF"` cuando la agenda aún no tiene clases registradas.
2. **Selector de Documento del Sistema:**
   - `ActivityResultContracts.OpenDocument()` filtrado a `arrayOf("application/pdf")`.
   - Al seleccionar el `Uri`, `SchedulesViewModel` lee los bytes mediante `ContentResolver.openInputStream(uri)` (con límite de seguridad de 5 MB) y ejecuta `ParseUnergSchedulePdfUseCase`.
3. **Hoja Modal de Vista Previa (`UnergScheduleImportSheet`):**
   - **Cabecera Bento Resumen:**
     - Badge institucional `DACE • UNERG` + Período detectado (ej. `Período 20262`) + Nombre/C.I. si está presente.
     - Contadores clave en tipografía `JetBrainsMonoFamily`: `N Materias` • `M Clases presenciales`.
   - **Selector de Modo de Importación (`UnergImportMode`):**
     - `MERGE_SMART` (*Combinar sin duplicar — Recomendado*): Crea las materias que no existan (o actualiza su `section` y `cycle` si ya existían con el mismo nombre ignorando mayúsculas/acentos) y agrega únicamente los bloques horarios que no estén ya registrados en el mismo día y hora para esa materia.
     - `REPLACE_ALL_SCHEDULES` (*Reemplazar horario actual*): Conserva las materias existentes (sus apuntes y evaluaciones intactos), elimina todos los bloques horarios anteriores del usuario y registra únicamente los horarios del nuevo PDF.
   - **Lista de Materias y Horarios Detectados (con selección individual):**
     - Cada materia detectada aparece con un `Checkbox` (activado por defecto), su ícono semántico sugerido, color accesible, nombre formateado en Title Case limpio (ej. `Sistemas Distribuidos`), badge `Sección 1`, indicador de si es `Nueva` o `Ya existe`, y la lista de sus bloques presenciales (`Lunes • 10:16 - 12:30 • Aula 1 Auleta`).
   - **Acción de Confirmación:**
     - Botón primario `NotiFastButton`: `"Importar N materias y M horarios"`.
     - Ejecuta `ImportUnergScheduleUseCase`, programa las alarmas de clase mediante `ScheduleClassAlertUseCase`, cierra el BottomSheet, salta automáticamente al primer día con clases importadas y muestra alerta de éxito `AlertSeverity.SUCCESS`.
4. **Manejo de Errores:**
   - Si el archivo subido no es un PDF válido, está dañado o no corresponde al formato de horario DACE-UNERG (cero materias o sin tabla de horario), se muestra un mensaje claro y accionable en el banner de alerta sin crashear ni alterar datos existentes.

---

## 6. Blindaje de Ingeniería (Auditoría `Systematic Debugging`)

1. **Preservación de `section` y `cycle` en Actualizaciones Parciales (`LocalNoteRepositoryImpl.updateSubject`):**
   - `updateSubject` preservará `cycle = newSubject.cycle.ifBlank { existing.cycle }` y `EditSubjectDialog` (tanto en `NotesScreen` como en `SubjectDetailScreen`) recibirá y enviará el valor actual de `section`, evitando que editar el color, ícono o nombre de una materia borre accidentalmente su sección o período.
2. **Emparejamiento Insensible a Tildes/Diacríticos y Límite de 50 Caracteres (`SubjectNameRules.MAX_LENGTH`):**
   - SQLite `COLLATE NOCASE` solo pliega ASCII (`A-Z`), no vocales acentuadas (`Informática` vs `INFORMATICA`). Tanto `ParseUnergSchedulePdfUseCase` como `ImportUnergScheduleUseCase` usarán normalización `NFD` sin diacríticos para detectar materias ya creadas por el estudiante y reutilizarán su `id` y `name` exactos en Room. Además, todo nombre importado respetará `.take(SubjectNameRules.MAX_LENGTH)`.
3. **Cero Estados Bloqueantes en `SchedulesScreen`:**
   - Nunca se activará `uiState.isLoading` durante el parseo o importación del PDF (ya que `isLoading` desmonta el contenido del `Scaffold` y cerraría abruptamente el `ModalBottomSheet`). Se emplean flags independientes (`isParsingPdf`, `isImportingPdf`) envueltos en bloques `try / finally`, y si el usuario cancela el selector de archivos (`uri == null`), no se emite ningún evento de carga.
   - Si todas las materias están desmarcadas en la vista previa (`selectedCount == 0`) o no hay bloques, el botón de confirmación se deshabilita limpiamente.
   - Si una materia importada tiene 0 bloques presenciales (`firstImportedDayOfWeek == null`), se mantiene el `selectedDay` actual sin índices inválidos.
4. **Compatibilidad Retroactiva de `ContentHasher` (Cloud Backup SHA-256):**
   - `ContentHasher.hash(snapshot)` solo añadirá la propiedad `"section"` al JSON canónico cuando `snapshot.subject.section.isNotBlank()`. Así, el 100% de las materias existentes sin sección conservan su hash SHA-256 idéntico (sin falsos positivos de sincronización ni rotura de tests), mientras que cualquier cambio en `section` actualiza el hash para Google Drive.
5. **Cumplimiento Estricto del Límite de 250 Líneas en `SchedulesScreen.kt`:**
   - `SchedulesScreen.kt` tiene actualmente 237 líneas; la lógica del launcher PDF y los diálogos/sheets modales se modularizará en `presentation/feature/schedules/components/` para mantener `SchedulesScreen.kt` por debajo de 235 líneas.
6. **Compatibilidad con Constructores de `SchedulesViewModel`:**
   - Los nuevos casos de uso (`ParseUnergSchedulePdfUseCase` e `ImportUnergScheduleUseCase`) se inyectan con valor por defecto `= null` en el constructor primario de `SchedulesViewModel`, manteniendo intactos el constructor secundario y todos los tests existentes en `SchedulesViewModelTest`.

