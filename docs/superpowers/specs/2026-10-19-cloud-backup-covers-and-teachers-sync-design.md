# Cloud Backup: Sincronización de Portadas, Profesores y Feedback Granular

**Fecha:** 19 de Octubre de 2026  
**Autor:** Antigravity AI  
**Estado:** Especificación de Diseño  
**Área:** Capa de Datos (Room / Google Drive) y Presentación (MVI CloudHubSheet)

---

## 1. Contexto y Objetivos

NotiFast opera bajo una filosofía **100% Offline-First**, complementada con un sistema de **Copia de Seguridad Privada en Google Drive** (`appDataFolder`).

El usuario ha solicitado tres mejoras y refuerzos clave para la sincronización con la nube:
1. **Apartado de Estudio (Study Hub):** Sincronizar **únicamente las Portadas** (`CoverDraft`), **NO los archivos pesados** (`StudyDocument`, `.docx` o `.pdf` físicos importados/almacenados en el dispositivo).
2. **Sincronización de Materias:** Asegurarse de que en la sincronización de materias **se sincronicen también los profesores** asociados a cada materia (tanto al subir a la nube como al restaurar en el dispositivo).
3. **Mayor Feedback y Selección Granular:** Mejorar la interfaz de usuario de `CloudHubSheet` para que el usuario **vea y elija claramente qué elementos sincroniza** (no solo las materias y las preferencias, sino también esta nueva feature de Portadas de Estudio), con conteos, insignias (badges), estado de sincronización y desglose visual durante y después de la operación.

---

## 2. Decisiones de Arquitectura y Modelo de Datos

### 2.1 Profesores en Materias (`SubjectSnapshot` y `SubjectPackage`)

Actualmente, los profesores se respaldaban únicamente en el snapshot global (`GlobalSnapshot`). Al sincronizar o restaurar una materia de forma individual, los profesores asignados a ella no viajaban con el paquete de la materia.

**Solución:**
- Modificar `SubjectSnapshot` para incluir `val teachers: List<Teacher> = emptyList()`.
- Modificar `SubjectPackage` para incluir `val teachers: List<Teacher> = emptyList()`.
- En `RoomSubjectBackupStore.readSnapshot`: consultar los profesores asociados a la materia (aquellos que contengan el nombre de la materia en `subjectNames` o cuyo nombre coincida con el profesor asignado en los horarios de la materia) e incorporarlos en `SubjectSnapshot.teachers`.
- En `RoomSubjectBackupStore.insertSnapshot` y `replaceSnapshot`: al restaurar una materia, fusionar los profesores (`snapshot.teachers`) en la base de datos local para el `userId` correspondiente mediante la función de deduplicación ya existente en `LocalGlobalBackupStore` (`teacherKey`: por teléfono, email o nombre completo), evitando duplicados y vinculando la materia a sus `subjectNames`.
- En `ContentHasher.hash(snapshot: SubjectSnapshot)`: incluir los profesores normalizados y ordenados por ID para generar una firma SHA-256 exacta.
- En `DataBreakdownBadges`: mostrar una micro-insignia con icono de docente (`Icons.Rounded.Person` / `School`) indicando la cantidad de profesores asociados a la materia (ej. `1 doc.` o `2 prof.`).
- En `CloudSubjectItem`: agregar `val teacherCount: Int = 0`.

### 2.2 Sincronización Exclusiva de Portadas (CoverDrafts)

En el Área de Estudio coexisten dos tipos de datos:
1. **Documentos físicos (`StudyDocument` / `study_docs/`):** Archivos pesados (.pdf, .docx importados) de varios megabytes. **No se sincronizan** para mantener la cuota reducida, máxima velocidad y privacidad sin saturar Google Drive.
2. **Plantillas y Borradores de Portadas (`CoverDraft`):** Datos estructurados ligeros de las portadas creadas por el usuario (APA 7ma Edición, IEEE, Institucional, UNERG TEG). **Se sincronizan al 100%**.

**Estructura del Archivo de Respaldo de Portadas:**
- Archivo en Google Drive: `covers.nfbak`.
- Metadatos Drive (`props`):
  - `kind` = `"covers"`
  - `schemaVersion` = `"1"`
  - `contentHash` = SHA-256 calculado por `ContentHasher.hashCovers(drafts)`
  - `coverCount` = número de borradores de portada
  - `uploadedAt` = marca de tiempo UTC
  - `deviceModel` = modelo de dispositivo
- Contenido del ZIP `covers.nfbak`:
  - `manifest.json`: `{ "format": "notifast-backup", "schemaVersion": 1, "kind": "covers", "createdAt": ... }`
  - `covers.json`: lista serializada en JSON de `CoverDraft`.

**Componentes de Dominio y Datos:**
- `CoverBackupStore`: Interfaz con métodos `read(userId: String): Result<List<CoverDraft>>` y `apply(userId: String, drafts: List<CoverDraft>): Result<Unit>`.
- `RoomCoverBackupStore`: Implementación que interactúa con `CoverDraftDao` (leyendo borradores y fusionando/upserting borradores restaurados).
- `UploadCoversUseCase`: Caso de uso para empaquetar y subir `covers.nfbak` si el hash de contenido difiere de la nube.
- `RestoreCoversUseCase`: Caso de uso para descargar y aplicar `covers.nfbak` restaurando los borradores en la base de datos local.
- `BackupArchiveCodec`: Funciones `writeCovers(drafts, out, workDir)` y `readCovers(zip, extractDir)`.

### 2.3 Mayor Feedback y Selección Granular en `CloudHubSheet`

El usuario ahora puede visualizar y controlar 3 categorías principales de sincronización:
1. **Perfil y Preferencias:**
   - Tarjeta interactiva `GlobalSettingsRow` con checkbox independiente.
   - Muestra: Nombre, foto, escala de notas y directorio general.
   - Chip de estado (Sincronizado / Cambios sin subir / etc.).
2. **Portadas de Estudio:**
   - Nueva tarjeta interactiva `CoverDraftsRow` con checkbox independiente.
   - Muestra: Número de portadas locales (ej. "3 portadas guardadas"), descripción clara ("Plantillas APA 7, IEEE, UNERG y tesis institucional • Sin archivos pesados").
   - Chip de estado (Sincronizado / Cambios sin subir / Solo en el teléfono / Solo en la nube).
3. **Materias Académicas:**
   - Lista filtrable de materias con selección individual o grupal.
   - Micro-insignias detalladas: Apuntes, Evaluaciones, Horarios y **Profesores vinculados**.
   - Chip de estado de cada materia.

**Feedback Dinámico de Selección y Progreso:**
- **Barra de Acciones (`CloudActionBar`):**
  - Muestra un resumen semántico de lo seleccionado: e.g. *"2 materias, ajustes y portadas seleccionadas"*.
  - Los botones "Subir a la nube" y "Restaurar" se habilitan reactivamente si al menos un elemento seleccionado (materia, ajustes o portadas) requiere la acción.
- **Overlay de Progreso (`CloudProgressOverlay`):**
  - Muestra el nombre legible del elemento en proceso actual: ej. *"Subiendo: Portadas de Estudio..."*, *"Subiendo: Cálculo II (con 2 docentes)..."*.
- **Tarjeta de Resumen (`CloudSummaryCard`):**
  - Desglose con métricas claras de materias, preferencias y portadas sincronizadas con éxito.

---

## 3. Matriz de Componentes Afectados

| Capa | Archivo | Modificación |
|---|---|---|
| Data / Model | `SubjectSnapshot.kt` | Agregar `val teachers: List<Teacher> = emptyList()`. |
| Data / Model | `SubjectPackage.kt` | Agregar `val teachers: List<Teacher> = emptyList()`. |
| Data / Backup | `ContentHasher.kt` | Hashear profesores en `hash(SubjectSnapshot)` y crear `hashCovers(drafts: List<CoverDraft>)`. |
| Data / Backup | `BackupArchiveCodec.kt` | Serializar/deserializar `teachers` en `writeSubject`/`readSubject`, y añadir `writeCovers`/`readCovers`. |
| Data / Backup | `RoomSubjectBackupStore.kt` | Consultar docentes asignados al leer el snapshot y fusionar docentes al insertar/reemplazar. |
| Domain / Repository | `CoverBackupStore.kt` | Nueva interfaz para leer y aplicar copias de portadas. |
| Data / Local | `RoomCoverBackupStore.kt` | Implementación de `CoverBackupStore` sobre `CoverDraftDao`. |
| Data / Remote | `DriveCloudBackupRepositoryImpl.kt` | Reconocer `KIND_COVERS = "covers"`. |
| Domain / UseCase | `UploadCoversUseCase.kt` | Caso de uso de subida de portadas. |
| Domain / UseCase | `RestoreCoversUseCase.kt` | Caso de uso de restauración de portadas. |
| Domain / UseCase | `BuildCloudOverviewUseCase.kt` | Incorporar estado de `covers` y conteo de docentes por materia. |
| Domain / Model | `CloudOverview.kt` / `CloudFile.kt` | Agregar soporte para `covers` y `coverCount` / `teacherCount`. |
| Presentation | `CloudContract.kt` | Agregar `coversSelected`, `coversStatus`, `localCoverCount`, `ToggleCovers`. |
| Presentation | `CloudViewModel.kt` | Gestionar eventos y orquestación de subida/restauración de portadas y feedback. |
| Presentation | `CoverDraftsRow.kt` | Nuevo componente visual para la selección y estado de portadas en `CloudHubSheet`. |
| Presentation | `DataBreakdownBadges.kt` | Agregar micro-badge de profesores (`cloud_badge_teachers`). |
| Presentation | `CloudSubjectList.kt` | Incorporar `CoverDraftsRow`. |
| Resources | `strings.xml` | Strings localizados en español para portadas en la nube y docentes vinculados. |
