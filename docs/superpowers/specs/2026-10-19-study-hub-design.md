# Especificación de Diseño Técnico y UX: «Área de Estudio» (Study Hub)

**Fecha:** 2026-10-19  
**Estado:** Borrador para Aprobación (Fase de Brainstorming + `/grill-me` completada)  
**Tamaño del Trabajo:** Proyecto con Diseño Escrito (Multi-módulo Clean Architecture + MVI + Room v6 + Drive API v3)

---

## 1. Intención y Propósito (The Why)

Los estudiantes de secundaria y universidad en **NotiFast** ya gestionan sus apuntes, horarios, calificaciones y directorio de profesores, pero su flujo de trabajo académico diario requiere lidiar con **documentos externos (guías de laboratorio, lecturas en PDF, rúbricas en DOCX)** y **redactar trabajos formales con normas de estilo (APA 7ma edición, IEEE, Portadas Institucionales/Tesis)**.

El **«Área de Estudio» (Study Hub)** nace como la quinta sección principal en la barra de navegación inferior (`Screen.StudyHub`) para resolver ambos problemas sin salir de NotiFast:
1. Centralizar los documentos `.pdf` y `.docx` del estudiante vinculados a cada materia (`Subject`), garantizando acceso **100% Offline-First**, vista previa instantánea sin inflar el APK, duplicación y compartición nativa.
2. Ofrecer un **Catálogo y Editor Inteligente de Plantillas de Portadas Académicas** que se autocompleta con los datos que NotiFast ya conoce (materia, profesor titular, autor del perfil local, institución, fecha) y permite tanto **generar un archivo `.docx` local (100% offline)** como **crear y abrir directamente el documento formateado en Google Docs** en un toque.

---

## 2. Objetivos, No-Objetivos y Anti-Objetivos

### Objetivos (Goals)
- **Módulo 1 — Gestor de Documentos Académicos (`.pdf` y `.docx`):**
  - Importar archivos mediante Storage Access Framework (`ActivityResultContracts.OpenDocument`) copiándolos al sandbox interno (`context.filesDir/study_docs/{uuid}.{ext}`).
  - Vincular o reasignar cualquier documento a una materia existente (`Subject`) y filtrar por materia mediante chips interactivos.
  - Exponer los documentos vinculados a una materia dentro de `SubjectDetailScreen` (sección/cuadrante de estudio o acceso directo en el panorama de la materia).
  - Soportar 4 acciones clave por documento:
    1. **Vista previa in-app:** `PdfRenderer` nativo para PDFs (paginado con zoom) y parser ligero OpenXML (`ZipInputStream` + `XmlPullParser` sobre `word/document.xml`) para DOCX con **0 KB adicionales en el APK**.
    2. **Abrir en Google Docs / App externa:** `Intent.ACTION_VIEW` seguro vía `FileProvider` (`FLAG_GRANT_READ_URI_PERMISSION`) o apertura directa del `webViewLink` si el documento tiene vínculo con Google Docs.
    3. **Crear una copia (Duplicar):** Clonación física en `filesDir/study_docs/` con nombre `<Título> (Copia)` y nuevo registro en Room.
    4. **Compartir:** `Intent.ACTION_SEND` con `Intent.createChooser` vía `FileProvider`.
- **Módulo 2 — Diseñador y Catálogo de Plantillas de Portada:**
  - Catálogo inicial con **3 plantillas académicas estandarizadas**:
    1. **Normas APA (7.ª Edición — Estudiante / Profesional):** Título en negrita centrado, autor(es), afiliación/institución, curso/materia, nombre del profesor, fecha, numeración superior derecha.
    2. **Formato IEEE (Artículos y Laboratorios de Ingeniería):** Título centrado 24pt, bloque de autores e institución/departamento, bloque de Resumen (*Abstract*) y Palabras Clave (*Index Terms*).
    3. **Formato Institucional / Tesis Universitaria:** Encabezado de institución y facultad/escuela, título del trabajo de grado/informe, autor(es), tutor/profesor guía, ciudad y fecha centrada al pie.
  - **Autocompletado Inteligente:** Al seleccionar una `Subject` en el editor de portada, auto-rellenar `subjectName`, nombre del profesor titular asociado en `TeacherRepository`, `authorName` desde el usuario activo (`AuthRepository`) y la última `institutionName` guardada en `UserPrefsRepository`.
  - **Doble Salida (Offline + Cloud):**
    - **Exportar `.docx` Local (100% Offline):** Empaqueta un archivo OpenXML `.docx` válido en `filesDir/study_docs/`, lo registra automáticamente en «Mis Documentos» vinculado a la materia y guarda el borrador en Room.
    - **Crear y Abrir en Google Docs (Online):** Sube el documento estructurado (HTML semántico con estilos inline / DOCX) a Google Drive REST API v3 (`/upload/drive/v3/files?uploadType=multipart`) con `mimeType = "application/vnd.google-apps.document"`, persiste el `googleDocId` y `webViewLink` en Room, y abre inmediatamente `https://docs.google.com/document/d/{fileId}/edit`.
  - **Historial de Borradores de Portada:** Guardar en Room (`CoverDraftEntity`) las portadas creadas/editadas para poder reabrirlas, duplicarlas o volver a abrir su Google Doc vinculado.

### No-Objetivos (Non-Goals)
- No se construirá un procesador de textos WYSIWYG completo para editar el cuerpo libre de un PDF o DOCX existente (la edición avanzada de texto completo se delega a Google Docs o apps externas).
- No se sincronizarán los binarios pesados de `filesDir/study_docs/` dentro del snapshot JSON oculto de `appDataFolder` en Google Drive para evitar agotar la cuota de almacenamiento oculto o degradar la velocidad del respaldo diferencial SHA-256.

### Anti-Objetivos (Anti-Goals — Qué haría fallar este diseño)
- **Prohibido inflar el APK** con librerías pesadas como Apache POI completo (~15 MB) o motores PDF de terceros en C++.
- **Prohibido depender de URIs efímeros de SAF** que lanzan `SecurityException` cuando el usuario mueve o borra el archivo en su carpeta de Descargas.
- **Prohibido pedir scopes invasivos de Google Drive** (como `https://www.googleapis.com/auth/drive` completo); se usará exclusivamente autorización incremental con `https://www.googleapis.com/auth/drive.file` para crear documentos visibles en el Drive del usuario.

---

## 3. Decisiones Técnicas y Restricciones Acordadas

### 3.1. Persistencia Room Database (`v5 ➔ v6`) y Almacenamiento Físico
- **Directorio Sandbox:** `File(context.filesDir, "study_docs")`.
- **Configuración de `FileProvider`:** Añadir `<files-path name="study_docs" path="study_docs/" />` en [`app/src/main/res/xml/file_paths.xml`](file:///home/p0xd/Documentos/proyectos/NotiFast/app/src/main/res/xml/file_paths.xml).
- **Nuevas Entidades en Room (`NotiFastDatabase` versión `6`):**
  1. `StudyDocumentEntity` (`tableName = "study_documents"`):
     - `id: String` (PK, UUID)
     - `userId: String` (Indexado)
     - `subjectId: String` (Indexado, vacío `""` si no tiene materia asignada)
     - `subjectName: String`
     - `title: String` (Nombre visible editable, sin extensión forzada)
     - `fileName: String` (Nombre del archivo físico en `study_docs/`, ej. `{uuid}.pdf` o `{uuid}.docx`)
     - `docType: String` (`"PDF"` o `"DOCX"`)
     - `sizeBytes: Long`
     - `pageCount: Int` (Número de páginas para PDF o párrafos/bloques estimados para DOCX)
     - `sourceType: String` (`"IMPORTED"`, `"DUPLICATED"`, `"GENERATED_TEMPLATE"`)
     - `googleDocId: String?` (Opcional, si fue exportado/vinculado a Google Docs)
     - `googleWebViewLink: String?` (Opcional, URL directa a Google Docs)
     - `createdAt: Long`
     - `updatedAt: Long`
  2. `CoverDraftEntity` (`tableName = "cover_drafts"`):
     - `id: String` (PK, UUID)
     - `userId: String` (Indexado)
     - `templateId: String` (`"APA_7"`, `"IEEE"`, `"INSTITUTIONAL"`)
     - `subjectId: String` (Indexado)
     - `subjectName: String`
     - `workTitle: String`
     - `subtitle: String`
     - `authorsJson: String` (Lista JSON de nombres de autores)
     - `teacherName: String`
     - `institutionName: String`
     - `facultyOrDepartment: String`
     - `city: String`
     - `formattedDate: String`
     - `abstractText: String` (Usado en plantilla IEEE / Tesis)
     - `keywordsText: String` (Usado en plantilla IEEE)
     - `generatedLocalDocId: String?` (FK lógica a `study_documents.id` si se generó `.docx` local)
     - `googleDocId: String?`
     - `googleWebViewLink: String?`
     - `createdAt: Long`
     - `updatedAt: Long`
- **Migración `MIGRATION_5_6` en [`NotiFastMigrations.kt`](file:///home/p0xd/Documentos/proyectos/NotiFast/app/src/main/java/com/notifast/app/data/local/migrations/NotiFastMigrations.kt):** Creación no destructiva de `study_documents` y `cover_drafts` con sus respectivos índices (`index_study_documents_userId`, `index_study_documents_subjectId`, `index_cover_drafts_userId`, `index_cover_drafts_subjectId`).

### 3.2. Motores de Previsualización In-App (Zero-Bloat)
- **Visor PDF (`PdfDocumentPreviewer`):**
  - Usa `android.graphics.pdf.PdfRenderer` abriendo `ParcelFileDescriptor.open(file, MODE_READ_ONLY)` en `Dispatchers.IO`.
  - Renderiza cada página bajo demanda en un `Bitmap` escalado a la densidad de pantalla y lo muestra dentro de un `LazyColumn` con soporte de `transformable` (pinch-to-zoom / doble toque) e indicador de página actual (`Página X de Y`).
- **Visor DOCX (`DocxDocumentPreviewer`):**
  - Descomprime en memoria el stream del `.docx` usando `java.util.zip.ZipInputStream` hasta localizar la entrada `word/document.xml`.
  - Parsea el XML con `android.util.Xml.newPullParser()`:
    - Detecta párrafos (`<w:p>`), estilos de encabezado (`<w:pStyle w:val="Heading1|Title|Subtitle"/>`), alineación (`<w:jc w:val="center|right|both"/>`), corridas de texto (`<w:r>`, `<w:t>`), énfasis (`<w:b/>`, `<w:i/>`, `<w:u/>`) y filas/celdas de tablas (`<w:tbl>`, `<w:tr>`, `<w:tc>`).
  - Transforma el resultado en una lista inmutable de modelos de dominio `DocxPreviewBlock` (`Heading`, `Paragraph`, `Table`) que se dibujan nativamente en Compose respetando la tipografía y el tema claro/oscuro de NotiFast, con un switch opcional de «Vista Papel (Blanco) / Vista Tema».

### 3.3. Generador Local `.docx` (OpenXML) y Exportación a Google Docs (`drive.file`)
- **Generador `.docx` Offline (`OpenXmlDocxGenerator`):**
  - Construye un archivo `.docx` válido utilizando `ZipOutputStream` con las entradas mínimas del estándar ECMA-376 OpenXML (`[Content_Types].xml`, `_rels/.rels`, `word/_rels/document.xml.rels`, `word/styles.xml`, `word/document.xml`).
  - Aplica las reglas tipográficas de cada plantilla (`APA_7`, `IEEE`, `INSTITUTIONAL`): márgenes de 1 pulgada (`1440 twips`), interlineado doble o simple según norma, alineación centrada/izquierda y saltos de página.
- **Integración Google Docs vía Drive API v3:**
  - En [`GoogleAuthDataSource.kt`](file:///home/p0xd/Documentos/proyectos/NotiFast/app/src/main/java/com/notifast/app/data/remote/google/GoogleAuthDataSource.kt):
    - Se agrega `const val DRIVE_FILE_SCOPE = "https://www.googleapis.com/auth/drive.file"` y el método `suspend fun authorizeDriveFile(accountEmail: String): DriveAuthResult`.
    - Si el usuario aún no ha iniciado sesión con Google, el flujo invoca primero `signInWithGoogle(activityContext)` y enseguida `authorizeDriveFile(email)`, manejando `NeedsResolution(pendingIntent)` con `ActivityResultContracts.StartIntentSenderForResult`.
  - En `GoogleDocsExporterDataSource` (usando el cliente `OkHttpClient` existente):
    - Realiza un `POST https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,name,webViewLink`.
    - Parte 1 (JSON Metadata): `{"name": "<workTitle>", "mimeType": "application/vnd.google-apps.document"}`.
    - Parte 2 (Media Payload): HTML semántico enriquecido con estilos CSS inline precisos (o el binario `.docx` generado) para que el conversor de Google Docs cree el documento editable con formato limpio.
    - Al recibir el `id` y `webViewLink`, actualiza el `CoverDraftEntity` (y `StudyDocumentEntity` si existe), y abre la app de Google Docs / navegador mediante `Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/$fileId/edit"))`.

### 3.4. Arquitectura de Navegación y UX (Clean Architecture + MVI)
- **Barra de Navegación Inferior (5 Ítems):**
  - En [`Screen.kt`](file:///home/p0xd/Documentos/proyectos/NotiFast/app/src/main/java/com/notifast/app/presentation/navigation/Screen.kt):
    - Nuevo `data object StudyHub : Screen("estudio", "Estudio", Icons.Default.Description)` (o `Icons.Default.School` / `Icons.Default.FolderCopy`).
    - `bottomNavItems` pasa a: `listOf(Notes, Schedules, StudyHub, Grades, Settings)`.
  - En [`NotiFastBottomNav.kt`](file:///home/p0xd/Documentos/proyectos/NotiFast/app/src/main/java/com/notifast/app/presentation/atoms/NotiFastBottomNav.kt):
    - Ajuste sutil de padding horizontal por ítem (`horizontal = 12.dp` cuando `items.size >= 5`) para que los 5 íconos mantengan holgura táctil AAA en pantallas compactas de 360dp de ancho.
- **Estructura de `StudyHubScreen` (feature `presentation/feature/studyhub/`):**
  - Encabezado con `NotiFastTopBar`, barra de búsqueda rápida y selector segmentado (`StudyHubTab.DOCUMENTS` vs `StudyHubTab.TEMPLATES`).
  - **Tab 1 — «Mis Documentos»:**
    - Fila de chips de filtro por materia (`Todas`, `Sin materia`, + cada `Subject` con su color e ícono) y filtro rápido de tipo (`Todos`, `PDF`, `DOCX`).
    - Tarjetas `StudyDocumentCard` mostrando badge de formato (`PDF` en rojo coral semántico / `DOCX` en azul índigo semántico), título, chip de materia asignada, tamaño en KB/MB, fecha y menú/hoja de acciones (`StudyDocumentActionSheet`).
    - Al tocar un documento o elegir acciones en el BottomSheet:
      1. **Vista previa:** Abre `DocumentPreviewModalSheet` (visor in-app PDF o DOCX).
      2. **Abrir en Google Docs / App externa:** Lanza intent hacia Google Docs o selector de visor externo.
      3. **Vincular / Cambiar Materia:** Abre selector rápido de `Subject`.
      4. **Crear una copia:** Duplica el archivo instantáneamente y muestra confirmación.
      5. **Compartir:** Abre el Sharesheet nativo de Android.
      6. **Eliminar:** Con diálogo de confirmación `NotiFastDialog`.
    - FAB principal para **Importar Documento (`.pdf` / `.docx`)**.
  - **Tab 2 — «Plantillas de Portada»:**
    - Grilla/Carrusel de las 3 plantillas maestras (`APA 7.ª Edición`, `Formato IEEE`, `Institucional / Tesis`) con miniatura esquemática de su diagramación.
    - Sección inferior **«Mis Portadas y Borradores Recientes»** (`CoverDraftCard`) con indicador de si ya tienen vínculo a Google Docs.
    - Al tocar una plantilla o un borrador, se abre `CoverTemplateEditorSheet` (ModalBottomSheet a pantalla completa):
      - Selector de Materia (`Subject`) que dispara el **autocompletado inteligente** (Materia, Profesor titular, Autor del perfil, Institución recordada, Fecha actual formateada en español).
      - Campos editables con `NotiFastTextField` adaptados a la plantilla elegida + vista previa en vivo de la portada (`CoverLivePreviewCard`).
      - Barra inferior con dos acciones claras:
        - Botón secundario: **«Guardar .DOCX Local»** (funciona 100% offline).
        - Botón primario: **«Abrir en Google Docs»** (con ícono de documento en la nube; gestiona OAuth `drive.file` y abre el documento recién creado).
- **Integración en `SubjectDetailScreen`:**
  - En el cuadrante `PANORAMA` (o mediante una sección dedicada en `SubjectDetailScreen`), se muestra la tarjeta **«Documentos de la Materia»** con los PDFs/DOCX vinculados a esa materia, permitiendo previsualizarlos con un toque o ir a crear una portada pre-vinculada a esa materia.

---

## 4. Lo que Queda a Criterio del Implementador (Builder's Calls)

- **Detalles de espaciado y micro-animaciones:** Transiciones suaves (`AnimatedContent`) entre las pestañas «Mis Documentos» y «Plantillas de Portada», y el diseño esquemático en Compose Canvas/Box de las miniaturas de las 3 plantillas.
- **Límite de páginas en caché del `PdfRenderer`:** Mantener en memoria únicamente las páginas visibles en el `LazyColumn` (usando un `Mutex` secuencial porque `PdfRenderer.Page` requiere que solo una página esté abierta a la vez sobre el mismo `PdfRenderer`).
- **Sanitización de nombres de archivo:** Al exportar un `.docx` local o duplicar, limpiar caracteres inválidos del sistema de archivos conservando el `title` íntegro en Room.
- **Cobertura de Tests Unitarios (TDD):**
  - Tests para `ImportStudyDocumentUseCase`, `DuplicateStudyDocumentUseCase`, `GenerateCoverDocxUseCase`, `ExportCoverToGoogleDocsUseCase`, `OpenXmlDocxParserTest`, `OpenXmlDocxGeneratorTest` y `StudyHubViewModelTest`.
  - Actualización de `ScreenTest` y `ScreenInitOrderTest` para reflejar los 5 ítems de `Screen.bottomNavItems`.
