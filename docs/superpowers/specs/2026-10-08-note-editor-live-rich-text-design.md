# Especificación de Diseño: Live Rich-Text Canvas y Docked Accessory Bar para NotiFast (v2.0)

> **Documento de Diseño Arquitectural**  
> **Fecha:** 8 de Octubre, 2026  
> **Estado:** Aprobado  
> **Módulo:** `presentation/feature/noteeditor/`  
> **Referencias:** `AGENTS.md`, `docs/prd/PRD-MAESTRO-NOTIFAST.md`, `docs/prd/STATUS.md`

---

## 1. Contexto y Objetivos

El editor de notas de NotiFast debe proporcionar una experiencia fluida estilo **"Word / Google Docs Mobile"** para estudiantes. Tras la evaluación en dispositivo físico, se detectaron y abordaron las siguientes causas raíz de fricción:
1. **Parpadeo y stutters del teclado virtual:** Causado por la instanciación de `MarkdownVisualTransformation(colorScheme)` en cada frame de recomposición sin `remember`, provocando invalidación del `InputConnection` del IME.
2. **Ocultamiento de inputs bajo el teclado:** Falta de auto-scroll con `BringIntoViewRequester` y competencia entre insets de `ModalBottomSheet` e `imePadding`.
3. **Cursor desfasado en herramientas de formato:** Al presionar `[B]` o `[I]`, el texto se concatenaba al final en lugar de envolver la selección o colocar el cursor dentro de `**|**`.
4. **Falta de efecto Toggle y feedback visual:** Las herramientas no indicaban visualmente cuándo un formato estaba activo ni permitían desactivarlo con un segundo toque.
5. **Falta de continuidad en listas:** Al presionar `Enter` en viñetas o tareas, el estudiante tenía que escribir manualmente el nuevo bullet.
6. **Jerarquía visual desordenada:** Materia y etiquetas ocupaban la parte superior, restando visibilidad inmediata al título y al contenido.

### Objetivos Clave de la Revisión v2.0:
* **Lienzo de Escritura con Foco Inmediato:** Título y Contenido se ubican en la parte superior; Materia y Etiquetas se desplazan al pie del documento.
* **Auto-Enfoque y Desplazamiento Fluido:** Uso de `BringIntoViewRequester` para mantener el cursor siempre visible sobre la barra acoplada.
* **Smart Formatting & Cursor Centering:** Manejo mediante `TextFieldValue`: al tocar `[B]`, se inserta `****` y el cursor se ubica en medio (`**|**`).
* **Efecto Toggle con Iluminación:** Detección de formato activo en la posición del cursor; los botones de la barra se iluminan con `primaryContainer` cuando están activos.
* **Salto de Línea Inteligente (Smart Enter):** Al pulsar `Enter` en viñetas (`- `) o tareas (`- [ ] `), se genera automáticamente el siguiente elemento. Si el elemento actual está vacío, se limpia y se cancela la lista.
* **Zero-Recomposition Stutter:** `remember(colorScheme) { MarkdownVisualTransformation(colorScheme) }` garantiza 60 fps constantes sin invalidación del IME.

---

## 2. Arquitectura de Componentes

### 2.1 Jerarquía Visual de Pantalla y Sheet

```
ModalBottomSheet / Screen
├── Header: [Cerrar]  Título  [Modo Lectura]  [Guardar]
├── Scrollable Content Column (con BringIntoViewRequester):
│   ├── NotiFastFormField (Título)
│   ├── NotiFastFormField (Contenido con MarkdownVisualTransformation)
│   ├── NoteAttachmentPreview (Foto de pizarra si existe)
│   ├── NoteSubjectSelectorChip (Materia - al pie)
│   ├── NoteTagsEditor (Etiquetas temáticas - al pie)
│   ├── Badge de Estadísticas (Palabras · Minutos de lectura)
│   └── NotiFastButton (Guardar)
└── DockedAccessoryToolbar (Anclada al fondo sobre el teclado con imePadding)
     ├── Botones de formato con iluminación de estado activo (isBoldActive, etc.)
     └── Selector directo de foto [📷]
```

### 2.2 Álgebra de Formato Inteligente (`SmartMarkdownFormatter`)
* **Detección de Estilos Activos (`detectActiveFormatting`):**
  * Analiza la línea actual y el rango de selección en `TextFieldValue`.
  * Detecta si el cursor está dentro de `**...**`, `*...*`, o en líneas con `# `, `## `, `- `, `- [ ] `.
* **Aplicación / Toggle (`toggleStyle`):**
  * **Con selección:** Envuelve con `prefix` y `suffix`. Si ya está envuelto, desenvuelve (Toggle OFF).
  * **Sin selección:** Inserta `prefix + suffix` y coloca el cursor en `start + prefix.length`. Si el cursor ya está dentro de delimitadores vacíos, los remueve (Toggle OFF).
* **Salto Inteligente (`handleEnterKey`):**
  * Al detectar inserción de `\n`:
    * Línea anterior `- ` vacía $\rightarrow$ limpia la línea y no añade viñeta.
    * Línea anterior `- ` con contenido $\rightarrow$ añade `\n- ` y ubica cursor después de `- `.
    * Línea anterior `- [ ] ` vacía $\rightarrow$ limpia la línea y no añade tarea.
    * Línea anterior `- [ ] ` con contenido $\rightarrow$ añade `\n- [ ] ` y ubica cursor después.

---

## 3. Criterios de Aceptación (DoD)
* [x] Cero parpadeo del teclado al tipear o abrir el editor (`remember` en `VisualTransformation`).
* [x] El teclado no oculta el campo de escritura ni el cursor.
* [x] Título y contenido se muestran en la parte superior; materia y etiquetas al pie.
* [x] Pulsar [B] sin selección coloca el cursor dentro de `**|**` e ilumina el botón de negrita.
* [x] Pulsar Enter en listas genera automáticamente el siguiente bullet o cancela si está vacío.
* [x] Todos los tests unitarios pasan exitosamente (`./gradlew testDebugUnitTest`).
* [x] APK instalado en Samsung Galaxy A06 (`R8YL40QFYYJ`) con verificación visual satisfactoria.
