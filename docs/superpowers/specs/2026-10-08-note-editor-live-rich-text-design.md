# Especificación de Diseño: Live Rich-Text Canvas y Docked Accessory Bar para NotiFast

> **Documento de Diseño Arquitectural**  
> **Fecha:** 8 de Octubre, 2026  
> **Estado:** Aprobado  
> **Módulo:** `presentation/feature/noteeditor/`  
> **Referencias:** `AGENTS.md`, `docs/prd/PRD-MAESTRO-NOTIFAST.md`, `docs/prd/STATUS.md`

---

## 1. Contexto y Objetivos

El editor de notas actual de NotiFast cuenta con persistencia desacoplada y exportación a PDF, pero presenta fricciones de usabilidad para los estudiantes en dispositivos móviles:
1. **Residuos de debug visual:** Existe una cabecera con el texto `"FORMATO Y ESTILOS (2 COLUMNAS)"` en `NoteMarkdownToolbar.kt` que distrae y debe eliminarse.
2. **Ocupación vertical ineficiente:** La barra de herramientas se encuentra posicionada sobre el campo de texto, restando valioso espacio para escribir apuntes extensos.
3. **Fricción de cambio de pestaña:** Los estudiantes deben alternar forzosamente a la pestaña "Vista Previa" para ver el resultado visual de encabezados, fórmulas o negritas.
4. **Ergonomía deficiente:** Los controles no están organizados por categorías semánticas ni ubicados en la zona natural de alcance de los pulgares.

### Objetivos Principales:
* **Lienzo Unificado en Vivo (*Live Rich-Text Canvas*):** Edición en tiempo real estilo Word/Docs/Obsidian donde el texto se visualiza con jerarquía tipográfica enriquecida (H1, H2, negrita, cursiva, código, listas, citas) directamente en el campo de texto mediante `MarkdownVisualTransformation` con tokens atenuados.
* **Barra de Accesorios Acoplada (*Docked Accessory Bar*):** Ubicada en la parte inferior de la pantalla y acoplada fluidamente sobre el teclado virtual mediante `Modifier.imePadding()`.
* **Reagrupación Semántica y Accesible:** 4 grupos con micro-divisores visuales y touch targets de 42dp con feedback háptico:
  * **Texto:** Negrita (`[B]`), Cursiva (`[I]`), Cita (`[“]`)
  * **Estructura:** Título 1 (`[H1]`), Subtítulo 2 (`[H2]`)
  * **Listas:** Tarea checkbox (`[☑]`), Viñetas (`[•]`)
  * **Insertar:** Fórmula LaTeX (`[Σ]`), Separador (`[—]`), Foto de pizarra (`[📷]`)
* **Modo Lectura:** El TabRow superior fijo se sustituye por un icono discreto de "Modo Lectura" (ojo) en el `EditorTopBar`, dejando el lienzo despejado para escribir.
* **Persistencia Inmutable:** Compatibilidad 100% con Room, Firestore y PDF: el texto se guarda como Markdown puro estándar.

---

## 2. Arquitectura de Componentes

### 2.1 Módulos Involucrados

```
app/src/main/java/com/notifast/app/
├── presentation/
│   ├── atoms/
│   │   └── MarkdownVisualTransformation.kt  <-- [NUEVO] Motor de estilos en vivo
│   └── feature/noteeditor/
│       ├── NoteEditorContent.kt             <-- [MODIFICADO] Lienzo unificado
│       ├── NoteEditorBottomSheet.kt         <-- [MODIFICADO] Integración de barra acoplada
│       ├── NoteEditorScreen.kt              <-- [MODIFICADO] Integración de barra acoplada
│       ├── NoteEditorViewModel.kt           <-- [MODIFICADO] Inserción de formato y foto
│       ├── NoteEditorContract.kt            <-- [MODIFICADO] Estado y eventos limpios
│       └── components/
│           ├── DockedAccessoryToolbar.kt    <-- [NUEVO] Reemplaza a NoteMarkdownToolbar
│           ├── EditorTopBar.kt              <-- [MODIFICADO] Icono de modo lectura
│           └── NoteAttachmentPreview.kt     <-- [PRESERVADO] Preview de foto adjunta
```

### 2.2 `MarkdownVisualTransformation` (Motor de Estilizado en Vivo)
* Implementa `VisualTransformation` de Compose.
* Convierte un `AnnotatedString` de texto plano Markdown aplicando `SpanStyle` en una sola pasada:
  * `# Encabezado`: `fontSize = 22.sp`, `fontWeight = FontWeight.ExtraBold`, `#` atenuado (`alpha = 0.35f`).
  * `## Subtítulo`: `fontSize = 18.sp`, `fontWeight = FontWeight.Bold`, color `Ruby`, `##` atenuado (`alpha = 0.35f`).
  * `### Sección`: `fontSize = 16.sp`, `fontWeight = FontWeight.SemiBold`, `###` atenuado (`alpha = 0.35f`).
  * `**Negrita**`: `fontWeight = FontWeight.Bold`, delimitadores `**` atenuados (`alpha = 0.35f`).
  * `*Cursiva*` o `_Cursiva_`: `fontStyle = FontStyle.Italic`, delimitadores atenuados (`alpha = 0.35f`).
  * `` `Código` `` y `$$ LaTeX $$`: `fontFamily = JetBrainsMonoFamily`, fondo `surfaceContainerHigh`.
  * `> Cita`: `fontStyle = FontStyle.Italic`, delimitador `>` en color de marca `Aqua`.
  * `- [ ] Tarea` y `- [x] Tarea`: 체크box resaltado en color primario.
* **Garantía Invariante:** Utiliza `OffsetMapping.Identity`. Dado que la longitud del texto transformado es estrictamente idéntica a la longitud original (`1:1`), el cursor y la selección jamás sufren desfasajes ni saltos.

### 2.3 `DockedAccessoryToolbar`
* Composable de una sola fila compacta (`height = 52.dp`), ubicado sobre el teclado virtual con `Modifier.imePadding()`.
* Fondo: `MaterialTheme.colorScheme.surfaceContainer`.
* Borde superior sutil con `outlineVariant`.
* Botones con superficie táctil de 42dp con feedback háptico (`LocalHapticFeedback`).
* Scroll horizontal suave para compatibilidad con pantallas estrechas.
* Micro-separadores visuales (1dp) entre categorías semánticas.

### 2.4 Ergonomía del Lienzo
* Altura mínima del campo de contenido: `minLines = 8` con expansión automática según el contenido.
* Estilo tipográfico base: `bodyLarge` (16sp) con `lineHeight = 24.sp` para máxima legibilidad en apuntes largos.
* Cero strings quemados: Todos los tooltips, etiquetas y content descriptions residen en `res/values/strings.xml`.

---

## 3. Plan de Testing (TDD)

1. **`MarkdownVisualTransformationTest`:**
   * Validar longitud 1:1 (`transformed.text.length == original.text.length`).
   * Validar aplicación de `FontWeight.Bold` para `**palabra**`.
   * Validar aplicación de tamaños tipográficos para H1 (`# `), H2 (`## `) y H3 (`### `).
   * Validar tolerancia a sintaxis incompleta (`**sin cerrar`, `# `, `*a`).
2. **`NoteEditorViewModelTest`:**
   * Inserción de formato prefix/suffix al contenido existente.
   * Alternancia de modo lectura (`TogglePreview`).
   * Validación de conteo de palabras y cálculo de lectura.
3. **Verificación en Dispositivo Real (Samsung Galaxy A06):**
   * Compilación sin advertencias: `./gradlew testDebugUnitTest assembleDebug`.
   * Instalación en dispositivo físico `R8YL40QFYYJ`.
   * Captura de pantalla de la barra de accesorios y el lienzo formateado.

---

## 4. Criterios de Aceptación (DoD)
* [x] El texto `"2 COLUMNAS"` y la cabecera transitoria han sido eliminados por completo.
* [x] El texto en el lienzo de escritura refleja encabezados grandes, negritas, cursivas, listas y código en vivo.
* [x] La barra de herramientas está acoplada al fondo y sobre el teclado virtual (`imePadding`).
* [x] Todos los archivos Composable cumplen con la regla de $\le 250$ líneas de código.
* [x] Todos los textos se encuentran en `strings.xml`.
* [x] No hay colores hardcodeados (se usa `MaterialTheme.colorScheme` y colores corporativos `Ruby`/`Aqua`).
* [x] Todos los tests unitarios pasan exitosamente (`./gradlew testDebugUnitTest`).
* [x] La app se instala y ejecuta sin errores en el Samsung Galaxy A06.
