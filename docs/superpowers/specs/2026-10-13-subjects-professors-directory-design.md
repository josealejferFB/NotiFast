# Diseño de Sanitización de Materias y Directorio de Profesores

**Fecha:** 2026-10-13  
**Estado:** Propuesto (Brainstorming validado)  
**Alcance:** Proyecto con diseño escrito (Full Design Spec)

---

## 1. Intención y Justificación (Intent & The Why)

En NotiFast, las materias y los horarios son el corazón del cuaderno académico. Sin embargo, existían tres fricciones clave identificadas en el uso diario de los estudiantes:
1. **Falta de personalización post-creación:** Una vez creada una materia, el estudiante no podía cambiar su color corporativo ni su icono temático si reorganizaba su sistema visual o se equivocaba al seleccionarlo.
2. **Desconexión entre horarios y docentes:** Al agendar un horario de clase, el campo de profesor era un simple texto libre sin estructura, sin validación y sin sincronización con los datos de la materia.
3. **Ausencia de directorio de contacto rápido para emergencias:** Ante una duda urgente, entrega tardía o justificación médica, el estudiante no tenía forma de acceder a los datos de contacto de sus profesores ni escribirles por WhatsApp o SMS de manera inmediata.

Este diseño define una arquitectura integral, offline-first y modular para dotar a NotiFast de un **Directorio de Profesores**, integración bidireccional con **Materias** y **Horarios**, y herramientas completas de **edición de color e icono** para las materias.

---

## 2. Objetivos, No-Objetivos y Anti-Objetivos

### Objetivos (Goals)
1. **Entidad `Teacher` de Primer Nivel:** Modelar el profesor con nombre, apellido, teléfono, email opcional y relación muchos-a-muchos con materias (`subjectNames`). Almacenamiento offline-first en Room (`TeacherEntity` y `TeacherDao`) y sincronización en Firebase Firestore.
2. **Navegación Limpia (Opción A):** En la pestaña "Materias" de `NotesScreen`, incorporar un control segmentado interactivo: `[ Materias (Cuadrícula) | Profesores (Directorio) ]`.
3. **Ficha de Detalle y Contacto AAA:** Visualización de ficha docente con iniciales con gradiente temático, materias que imparte y acciones de 1 toque:
   - **WhatsApp:** Abre `https://wa.me/<telefono>?text=<saludo_codificado>` prellenando un mensaje de saludo respetuoso: *"Hola Profesor/a [Nombre], le escribo con respecto a la materia [Materia]..."*.
   - **SMS:** Dispara `Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:<telefono>"))`.
   - **Llamada:** Dispara `Intent(Intent.ACTION_DIAL, Uri.parse("tel:<telefono>"))`.
4. **Sincronización Contextual en Horarios (`AddSchedule` / `EditSchedule`):**
   - Si la materia seleccionada **no tiene profesor asignado**: Muestra una tarjeta de advertencia destacada *"Esta materia aún no tiene un profesor asignado"*, solicitando el nombre y vinculándolo automáticamente al guardar el horario.
   - Si la materia **ya tiene profesores**: Muestra un selector desplegable con sus docentes registrados para autocompletar con un toque, más la opción de registrar uno nuevo.
5. **Edición Completa de Materias (Color e Icono):**
   - Modal/Diálogo "Editar Materia" accesible desde la cabecera de `SubjectDetailScreen` y desde el menú contextual de cada tarjeta en `NotesScreen`. Permite cambiar nombre, color (paleta temática accesible) e icono (catálogo de iconos) sin afectar notas, evaluaciones ni horarios.
6. **Gestión de Profesores en Detalles de Materia (`SubjectDetailScreen`):**
   - Sección en el cuadrante de Panorama o pestaña dedicada para ver los profesores de esa materia, acceder a su ficha y añadir más profesores.

### No-Objetivos (Non-Goals)
- No implementar un cliente de chat interno propio dentro de NotiFast (se delega a WhatsApp y a la app de mensajería del sistema operativo vía Android Intents nativos).
- No gestionar calificaciones o evaluaciones hacia el docente por parte del alumno.
- No requerir inicio de sesión obligatorio para usar el directorio de docentes (funciona 100% offline).

### Anti-Objetivos (Anti-Goals)
- **Cero pérdida o corrupción de datos existentes:** La incorporación de Room para profesores debe implementarse mediante migración segura de base de datos (`Migration` o incremento de versión con `fallbackToDestructiveMigration` controlado para entidades desacopladas).
- **Cero archivos Monstruo / Dios:** Mantener el estándar de `AGENTS.md`: composables modulares de un máximo de ~250 líneas.
- **Cero strings quemados:** Todo texto en la UI debe residir en `res/values/strings.xml` en español.
- **Cero colores hardcodeados:** Uso estricto de tokens de Material 3 y `ThemeUtils.getAccessibleColor`.

---

## 3. Modelo de Datos y Arquitectura de Persistencia

### Modelo de Dominio (`data/model/Teacher.kt`)
```kotlin
package com.notifast.app.data.model

import java.util.Date

data class Teacher(
    val id: String = "",
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val subjectNames: List<String> = emptyList(),
    val notes: String = "",
    val createdAt: Date? = null,
    val updatedAt: Date? = null
) {
    val fullName: String
        get() = "$firstName $lastName".trim()

    val formattedPhoneNumber: String
        get() = phoneNumber.filter { it.isDigit() || it == '+' }

    val hasPhoneNumber: Boolean
        get() = formattedPhoneNumber.isNotBlank()
}
```

### Entidad Room (`data/local/entity/TeacherEntity.kt`)
```kotlin
@Entity(
    tableName = "teachers",
    primaryKeys = ["userId", "id"],
    indices = [
        Index(value = ["userId", "firstName", "lastName"])
    ]
)
data class TeacherEntity(
    val userId: String = "",
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val subjectNamesJson: String = "[]",
    val notes: String = "",
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
```

### DAO (`data/local/dao/TeacherDao.kt`)
- `getTeachers(userId: String): Flow<List<TeacherEntity>>`
- `getTeacherById(userId: String, id: String): Flow<TeacherEntity?>`
- `insertTeacher(teacher: TeacherEntity)`
- `insertTeachers(teachers: List<TeacherEntity>)`
- `updateTeacher(teacher: TeacherEntity)`
- `deleteTeacher(userId: String, id: String)`

### Repositorio (`domain/repository/TeacherRepository.kt` & `data/repository/LocalTeacherRepositoryImpl.kt`)
- Repositorio local reactivo basado 100% en Room con `Flow` y retorno seguro `Result<T>`, consistente con la arquitectura Offline-First de NotiFast (`LocalNoteRepositoryImpl`, `LocalScheduleRepositoryImpl`).
- Migración de base de datos a versión 4 con tabla `teachers` agregada de forma no destructiva.

---

## 4. Diseño de Experiencia de Usuario (UI/UX AAA)

### A. Sub-pestaña en Materias (`NotesScreen.kt`)
- Al estar activa la pestaña **Materias**, se renderiza un `NotiFastSegmentedButtons` o selector de chips M3:
  - Tab 1: **Materias** (Grid actual de tarjetas con color, icono, promedio y porcentaje).
  - Tab 2: **Profesores** (Lista de tarjetas de profesores con avatar, teléfono, chips de materias y botón de contacto).
- FAB dinámico: Al estar en la vista de Profesores, el FAB o botón secundario permite **"+ Añadir Profesor"**.

### B. Ficha de Detalle de Profesor (`TeacherDetailBottomSheet.kt`)
- Componente Modal BottomSheet de Material 3 con esquinas redondeadas de 28.dp.
- Cabecera con avatar grande estilizado (iniciales con degradado temático), botón de editar (lápiz) y botón de eliminar (papelera con confirmación).
- Nombre completo en `titleLarge` y teléfono en `bodyMedium`.
- Si el profesor no tiene teléfono registrado (por ejemplo, creado rápidamente desde horarios): se muestra un banner suave indicando *"Sin teléfono registrado — Edita para añadir WhatsApp y llamadas"*, deshabilitando temporalmente los botones de acción directa.
- Sección de materias impartidas como chips clicables.
- Botonera de acciones primarias (habilitada si `hasPhoneNumber == true`):
  - **WhatsApp:** Botón verde corporativo (`#25D366`) con icono y mensaje introductorio prellenado: *"Hola Profesor/a [Nombre], le escribo con respecto a la materia [Materia]..."*.
  - **SMS:** Botón secundario con icono de chat.
  - **Llamar:** Botón de marcado directo.

### C. Creador y Editor de Horarios (`AddScheduleScreen` / `EditScheduleScreen`)
- Al seleccionar una materia:
  - Si la materia no cuenta con profesores en la base de datos: Se despliega un contenedor de advertencia suave (`surfaceContainerHigh` con borde ámbar accesible) indicando: *"Esta materia aún no tiene un profesor registrado"*, con un campo de texto para ingresar el nombre del profesor y vincularlo en automático al guardar (creando una entidad de profesor con nombre y vinculación a esa materia).
  - Si la materia ya cuenta con uno o más profesores: Se muestra un dropdown para seleccionar el docente de esa sesión específica con 1 toque.
- Compatibilidad retroactiva: `Schedule` preserva `teacher: String` (nombre visual) e incorpora `teacherId: String = ""` opcional para enlace unívoco.

### D. Modal "Editar Materia" y Transacción en Cascada (`EditSubjectDialog.kt`)
Permite modificar:
1. **Nombre de la materia:** Validando que no colisione con otra materia existente del mismo usuario (búsqueda insensible a mayúsculas/minúsculas).
2. **Paleta de colores** (`SubjectColorPickerBento`).
3. **Cuadrícula de iconos** (`SubjectIconGridSelector`).

#### Transacción Atómica de Renombrado en Cascada (Systematic Debugging):
Para garantizar que **cero notas, evaluaciones, horarios o profesores queden huérfanos ni se generen estados inconsistentes o bloqueantes**, el renombrado se ejecuta dentro de un bloque `@Transaction` en Room (`RoomDatabase.withTransaction`):
1. **Validación Previa:** Comprueba que `newName` no exista en `subjects` para ese `userId` (si existe, retorna error amigable sin tocar la base de datos).
2. **Actualización de Materia:** Actualiza la fila en `subjects` (`name = newName, colorHex = newColor, icon = newIcon`).
3. **Cascada en Notas:** `UPDATE notes SET subject = :newName WHERE userId = :userId AND subject = :oldName`.
4. **Cascada en Evaluaciones:** `UPDATE evaluations SET subjectName = :newName WHERE userId = :userId AND subjectName = :oldName`.
5. **Cascada en Horarios:** `UPDATE schedules SET subjectName = :newName WHERE userId = :userId AND subjectName = :oldName`.
6. **Cascada en Profesores:** Actualiza las entidades `teachers` reemplazando `oldName` por `newName` en su lista serializada.
7. **Consistencia de Navegación:** En `SubjectDetailViewModel`, al completarse la transacción, se actualiza el flujo de estado interno `_uiState.update { it.copy(subjectName = newName) }` y se reinicia la observación reactiva con el nuevo nombre, notificando a la pantalla para evitar pantallas en blanco o desincronización de rutas.

Accesible desde:
- El botón de editar en `SubjectDetailHeroBento` (junto al botón compartir).
- El menú contextual de opciones de cada tarjeta en `SubjectGridBento`.

### E. Gestión de Profesores en Detalles de Materia (`SubjectDetailScreen.kt`)
- En el cuadrante **Panorama** (`SubjectPanoramaTab`), se integra una tarjeta dedicada `SubjectFacultyCard` que lista los profesores asignados a esa materia, permite abrir su ficha de contacto con 1 toque e incluye un botón rápido de *"+ Asignar Profesor"*.
- Mantiene intacta la navegación de 4 cuadrantes (`PANORAMA`, `EVALUATIONS`, `NOTES`, `SCHEDULES`) sin sobrecargar la pantalla en dispositivos móviles.

---

## 5. Análisis Sistemático de Riesgos y Mitigación (Systematic Debugging)

| Riesgo Potencial | Causa Raíz | Mitigación Rigurosa |
|---|---|---|
| **Notas o notas huérfanas tras renombrar** | Clave de enlace por string literal `subjectName` | Ejecución en una única `@Transaction` de Room que actualiza las 4 tablas atómicamente. Cero estado intermedio. |
| **Colisión de nombres de materia** | Usuario renombra una materia a un nombre ya existente | Validación previa en DAO: `getSubjectByName(userId, newName)`. Si existe, se aborta y se muestra Snackbar/error en el diálogo. |
| **Pantalla en blanco en SubjectDetailScreen tras renombrar** | La pantalla observa datos con el `subjectName` viejo de la ruta | El ViewModel emite nuevo nombre en UiState y cancela/reinicia su `loadJob` con el nuevo nombre, manteniendo la vista viva y reactiva. |
| **Profesor sin teléfono intentando abrir WhatsApp** | Creación rápida desde creador de horarios | `hasPhoneNumber` desactiva botones y muestra banner educativo invitando a completar el número con 1 toque en Editar. |
| **Migración de base de datos Room destructiva** | Incompatibilidad al añadir tabla `teachers` a la v3 existente | Migración explícita `MIGRATION_3_4`: `db.execSQL("CREATE TABLE IF NOT EXISTS teachers ...")` sin pérdida de materias, notas ni horarios existentes. |

1. **Número de teléfono sin código de país:** Al disparar WhatsApp, si el número no empieza con `+`, la aplicación lo limpia eliminando caracteres no numéricos y permitiendo que WhatsApp gestione el prefijo o sugiriendo un formateo claro en el formulario.
2. **WhatsApp no instalado en el dispositivo:** Si el intent `whatsapp://send` falla con `ActivityNotFoundException`, se realiza un fallback automático abriendo el navegador con `https://wa.me/<telefono>?text=...`.
3. **Eliminación de una Materia:** Si se elimina una materia, los profesores que la impartían no se eliminan, únicamente se desvincula esa materia de su lista de materias asociadas.
4. **Múltiples profesores con el mismo nombre:** Cada profesor cuenta con un `id` único generado mediante UUID, permitiendo distinguir homónimos sin colisiones.

---

## 6. Lo que Queda en Manos del Implementador (Builder Choices)
- Estructura exacta de los Convertidores Room (`TypeConverter`) para la lista de strings de materias.
- Detalle visual de micro-animaciones en el selector segmentado de Materias/Profesores (`animateContentSize` o `AnimatedContent`).
- Ubicación exacta del test unitario de validación de teléfonos y formateo de URLs de WhatsApp en `domain/util/`.
