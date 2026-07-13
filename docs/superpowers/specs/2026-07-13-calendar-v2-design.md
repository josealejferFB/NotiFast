# Calendar V2.0 — Tipos de Actividad Académica

## Objetivo

Evolucionar el sistema de evaluaciones de NotiFast para soportar **4 tipos de actividades académicas** (Examen, Exposición, Entrega de Trabajo, Tarea), con indicadores visuales diferenciados en el calendario mensual y un formulario de creación adaptativo que muestre/oculte campos según el tipo seleccionado.

## Contexto

Actualmente el modelo `Evaluation` no tiene campo `type`. Toda actividad se trata como una evaluación genérica. El calendario muestra un solo color de indicador (punto Aqua) para todos los eventos. El formulario de creación (`AddEvaluationScreen`) y edición (`EditEvaluationScreen`) muestran siempre los mismos campos sin importar la naturaleza de la actividad.

---

## Requisitos Funcionales

### RF-1: Tipos de Actividad

Se definen exactamente 4 tipos. Cada uno tiene un nombre en español para la UI, un color de identificación, un ícono y reglas de campos:

| Tipo (enum) | Label UI | Color | Ícono | Peso obligatorio | Nota aplicable |
|---|---|---|---|---|---|
| `EXAM` | Examen | `Ruby` (#BF092F) | `Quiz` | Sí | Sí |
| `PRESENTATION` | Exposición | `Color(0xFFFF9800)` naranja | `PresentToAll` | Sí | Sí |
| `PROJECT` | Entrega de Trabajo | `Aqua` (#3B9797) | `Assignment` | Opcional | Sí |
| `HOMEWORK` | Tarea | `Color(0xFF7E57C2)` violeta | `Task` | No (se oculta) | Opcional |

### RF-2: Cambios en Firestore (Base de Datos)

**Firestore es NoSQL** — no requiere migraciones de esquema formales. Los cambios en la base de datos se manejan de la siguiente manera:

#### Estructura actual del documento (colección `users/{userId}/evaluations/{docId}`):
```
{
  userId: "abc123",
  subjectName: "Matemáticas",
  title: "Parcial 1",
  description: "...",
  date: Timestamp,
  noteIds: [],
  imageUrl: "https://...",
  quickTips: "...",
  grade: 8.5,
  weight: 20.0,
  isPresented: false,
  createdAt: Timestamp,
  updatedAt: Timestamp
}
```

#### Estructura actualizada (campo nuevo marcado con ⭐):
```
{
  userId: "abc123",
  subjectName: "Matemáticas",
  title: "Parcial 1",
  description: "...",
  date: Timestamp,
  noteIds: [],
  imageUrl: "https://...",
  quickTips: "...",
  grade: 8.5,
  weight: 20.0,
  isPresented: false,
  type: "EXAM",          ⭐ NUEVO — String, valores: "EXAM" | "PRESENTATION" | "PROJECT" | "HOMEWORK"
  createdAt: Timestamp,
  updatedAt: Timestamp
}
```

#### Estrategia de retrocompatibilidad:
1. **Documentos existentes** no tienen campo `type`. Firestore devuelve `null` para campos inexistentes.
2. El data class `Evaluation.kt` declara `val type: String = "EXAM"` — Firestore usa este default al deserializar documentos viejos.
3. **Documentos nuevos** siempre escriben el campo `type` explícitamente en el mapa de escritura del repository.
4. **No se migran documentos viejos.** Se leen con el default y solo se actualizan cuando el usuario edita una evaluación existente (en ese momento el campo `type` se escribe por primera vez).
5. **No se requieren índices compuestos nuevos** en Firestore, ya que actualmente se leen todas las evaluaciones del usuario sin filtrar por tipo.

#### Impacto en las Firestore Security Rules:
- Las reglas actuales validan `request.auth.uid == userId`. No necesitan cambios ya que el campo `type` es simplemente un String más en el documento.
- **Recomendación futura:** agregar validación de que `type` sea uno de los 4 valores válidos en las security rules.

### RF-3: Indicadores Visuales en el Calendario

- Cada día del `CalendarGrid` puede tener **hasta 3 puntos (dots)** debajo del número, uno por cada tipo de actividad distinto presente ese día.
- Si hay 4 tipos distintos el mismo día (caso extremo), se muestran 3 dots y el tercero representa "y más".
- Los dots se ordenan por prioridad visual: `EXAM` > `PRESENTATION` > `PROJECT` > `HOMEWORK`.

### RF-4: Formulario Adaptativo

- Al crear una actividad (`AddEvaluationScreen`), lo **primero** que el usuario ve es un selector de tipo (fila de chips / `FilterChip`). Por defecto se preselecciona `EXAM`.
- El título del TopAppBar cambia dinámicamente: "Añadir Examen", "Añadir Exposición", etc.
- Los campos `Peso / Porcentaje` y `Nota` se muestran u ocultan según las reglas de la tabla de RF-1.
- Al editar (`EditEvaluationScreen`), el tipo se muestra como chip de solo lectura (no editable después de creado, para evitar inconsistencias de datos).

### RF-5: Lista del Calendario y Bottom Sheet

- El `EvaluationListItem` en la lista diaria del calendario muestra el ícono y color correspondiente al tipo (en vez de siempre `Event` con `Ruby`).
- El `EvaluationDetailContent` del Bottom Sheet también refleja el tipo con su ícono y color correspondiente.

---

## Requisitos No Funcionales

- **Sin nuevas dependencias.** Todos los íconos necesarios existen en `material-icons-extended`.
- **Sin migración de datos.** Firestore no requiere schema migrations; los documentos viejos se deserializan con el default `"EXAM"`.
- **Mantener AGENTS.md.** Seguir las convenciones de nombres y arquitectura documentadas.

---

## Componentes Afectados

### Capa de Datos
- `Evaluation.kt` → agregar campo `type: String = "EXAM"`.
- `EvaluationRepositoryImpl.kt` → incluir `"type" to evaluation.type` en los mapas de escritura de `addEvaluation()` y `updateEvaluation()`.

### Capa de Presentación (nuevo)
- `EvaluationType.kt` → enum con `EXAM`, `PRESENTATION`, `PROJECT`, `HOMEWORK` y propiedades auxiliares (`label: String`, `color: Color`, `icon: ImageVector`, `showWeight: Boolean`, `showGrade: Boolean`).

### Capa de Presentación (modificar)
- `AddEvaluationContract.kt` → agregar `selectedType: EvaluationType = EvaluationType.EXAM` al UiState y nuevo evento `SelectType(type)`.
- `AddEvaluationViewModel.kt` → manejar `SelectType` en `onEvent`, incluir `type = state.selectedType.name` al construir el `Evaluation` para guardar.
- `AddEvaluationScreen.kt` → agregar fila de `FilterChip` para seleccionar tipo, adaptar título del TopAppBar dinámicamente, condicionar visibilidad de campo `weight`.
- `EditEvaluationContract.kt` → agregar `type: String = "EXAM"` al UiState.
- `EditEvaluationViewModel.kt` → cargar `type = eval.type` en `loadEvaluation()`.
- `EditEvaluationScreen.kt` → mostrar chip de tipo como solo lectura, condicionar visibilidad de campos según tipo.
- `CalendarScreen.kt` → actualizar `CalendarGrid` para pintar dots multi-color por tipo, actualizar `EvaluationListItem` y `EvaluationDetailContent` para usar ícono y color del tipo.

---

## Fuera de Alcance

- Notificaciones push / recordatorios.
- Sincronización con Google Calendar.
- Filtros por tipo en el calendario (se puede agregar en una fase futura).
- Vista semanal / time-blocking.
- Cambio de tipo después de creado (para evitar inconsistencias de peso/nota).
