# Design — Sistema de Calificaciones Multi-escala (0-10 / 0-20 / 0-100 / A-F)

**Fecha:** 2026-10-06
**Estado:** Diseño validado con systematic-debugging (18 fallos corregidos)
**Alcance:** Toda la app se adapta a la escala de calificaciones elegida por el usuario en Ajustes, con conversiones y formatos unificados.

---

## 1. Resumen ejecutivo

NotiFast ya tiene `GradeScale`, `GradePolicy` y `Evaluation.gradeScaleId`, pero la adopción es incompleta: hay capturas que no setean la escala, formatos hardcodeados (`/20`, `%.1f`, umbral `3.0`), ViewModels que fuerzan `SCALE_0_10` y un cambio de escala en Ajustes que **no convierte** notas ni nota mínima.

Este diseño cierra el ciclo completo:

1. **Captura** siempre en la escala activa (se persiste `gradeScaleId`).
2. **Display** siempre vía helpers únicos (`GradeFormatters` + `GradePolicy.toDisplayGrade`).
3. **Cambio de escala** reescribe notas y nota mínima con confirmación y transacción (D5 nuevo).
4. **Soporte completo A-F** en Ajustes, captura, presets y formato.

---

## 2. Decisiones de negocio

| ID | Decisión | Nota |
|----|----------|------|
| **D5 (rev)** | Cambiar la escala en Ajustes **reescribe** `grade`, `gradeScaleId` de cada evaluación y la nota mínima custom a la escala destino, previa confirmación. | Reemplaza el D5 anterior (“no reinterpreta”). |
| **D11** | Captura de notas **siempre** en la escala activa del usuario. Se valida contra `minGrade..maxGrade` de esa escala y se persiste `gradeScaleId = escala activa`. | El `gradeScaleId` legacy de la evaluación no limita la captura. |
| **D11b** | **Prohibido** elegir/cambiar la escala al crear o editar una evaluación. La escala es solo configurable en **Ajustes → Escala académica**. Las pantallas de captura muestran un **mensaje informativo**: “Puedes cambiar la escala en Opciones/Ajustes”. | Evita escalas inconsistentes por evaluación; un solo lugar de control. |
| **D12** | Escalas en Ajustes: **0-10, 0-20, 0-100, A-F**. | Letras se mantiene. |
| **D13** | Precisión de guardado según escala destino: `0-10`/`0-20` → 1 decimal; `0-100` → entero; `A-F` → 1 decimal en GPA (0-4). | `GradeScale.roundGrade`. |
| **D14** | Todo valor visible de nota/promedio pasa por `GradeFormatters` / `GradePolicy`. Cero `%.1f`, `/20`, `>= 3.0` hardcodeados. | Incluye monogramas de materia (“avatares”), hero, agenda, analíticas. |
| **D15** | Conversión al cambiar de escala: **cada evaluación se convierte desde su propio `gradeScaleId`**, no desde una escala origen única. | Corrige B1 (notas mixtas). |
| **D16** | `minPassingGrade == 0f` en DataStore = **usar default de la escala**. La UI muestra siempre el valor **efectivo**. | Corrige B3. |
| **D17** | Cambio de escala + nota mínima es **un solo evento atómico** con batch transaccional en Room. | Corrige B2/B15. |

---

## 3. Hallazgos del audit (systematic-debugging)

Ver tabla de 18 ítems B1–B18 en el mensaje de sesión 2026-10-06. Resumen de correcciones aplicadas al diseño:

- **B1** Conversión por-evaluación desde su `gradeScaleId`.
- **B2/B15** Evento único + `EvaluationDao.updateEvaluations` `@Transaction`.
- **B3/B16** `effectivePassingGrade` en Settings/VMs/Dialogs.
- **B4** `targetGrade` del simulador se inicializa al `minPassing` de la escala activa.
- **B5** A-F completo en SettingsGradeScaleDialog y SubjectGradeQuickDialog.
- **B6** Captura valida en escala activa y hace stamp de `gradeScaleId`.
- **B7/B8/B9** `customMinPassing` propagado a distribución, simulador y chart.
- **B10/B14** `GradeFormatters` + umbrales desde `minPassing` efectivo (incl. `SubjectGridBento` avatar, `NotesDashboardHero`).
- **B11** `NotesViewModel` inyecta `UserPrefsRepository`.
- **B12** `EvaluationEntity` deja de hardcodear `gradeScaleId`.
- **B13** `SCALE_0_100.minPassingGrade = 60.0`.
- **B16** Import P2P valida `grade` contra su escala.
- **B17** `AcademicStats.globalAverage` usa `GradePolicy.weightedAverage`.
- **B18** El diálogo convierte la nota mínima actual al cambiar de escala (no resetea).

---

## 4. Arquitectura

```
domain/
├── model/GradeScale.kt          # + roundGrade, formatGrade A-F, minPassing unificado
├── model/GradePolicy.kt         # + convertGrade, convertEvaluations, convertPassingGrade
├── usecase/ConvertGradeScaleUseCase.kt   # NUEVO: rewrite puro + resultado
└── util/GradeFormatters.kt      # NUEVO (o presentation): display único

data/
├── local/entity/EvaluationEntity.kt      # B12: sin hardcode de gradeScaleId
├── local/dao/EvaluationDao.kt            # + updateEvaluations(List) @Transaction
└── repository/UserPrefsRepositoryImpl.kt # efectivo = resolve(0f, scale)

presentation/
├── atoms o util/GradeFormatters.kt       # grade / average / gradeWithScale
├── feature/settings/…                    # evento único ChangeGradeScale + preview + confirm
├── feature/grades/…                      # updateGrade stamp D11
├── feature/subjectdetail/…               # saveGrade stamp D11, targetGrade por escala
├── feature/addevaluation/…               # gradeScaleId = escala activa
├── feature/notes/…                       # escala del usuario (B11)
└── feature/schedules/…                   # formatGrade en agenda
```

### 4.1 `GradeScale` (extensiones)

```kotlin
enum class GradeScale(...) {
    SCALE_0_10("0-10", ..., 0.0, 10.0, 6.0),
    SCALE_0_20("0-20", ..., 0.0, 20.0, 11.0),
    SCALE_0_100("0-100", ..., 0.0, 100.0, 60.0),  // B13: 60, no 70
    SCALE_LETTER("A-F", ..., 0.0, 4.0, 2.0);

    fun roundGrade(grade: Double): Double = when (this) {
        SCALE_0_100 -> kotlin.math.round(grade)
        else -> kotlin.math.round(grade * 10) / 10.0
    }

    fun formatGrade(grade: Double?): String  // existente; A-F → A/B/C/D/F
}
```

### 4.2 `GradePolicy` (nuevos)

```kotlin
fun convertGrade(grade: Double, from: GradeScale, to: GradeScale): Double {
    if (from == to) return from.roundGrade(grade)
    val raw = grade / from.maxGrade * to.maxGrade
    return to.roundGrade(raw)
}

fun convertEvaluations(evals: List<Evaluation>, target: GradeScale): List<Evaluation> =
    evals.map { e ->
        val src = GradeScale.fromId(e.gradeScaleId)
        e.copy(
            grade = e.grade?.let { convertGrade(it, src, target) },
            gradeScaleId = target.id
        )
    }

fun convertPassingGrade(value: Double, from: GradeScale, to: GradeScale): Double =
    convertGrade(value, from, to)
```

`toDisplayGrade` se **mantiene** como red de seguridad (P2P import con escalas ajenas, datos legacy).

### 4.3 `ConvertGradeScaleUseCase` (puro, testeable)

```kotlin
data class GradeScaleConversionResult(
    val convertedEvaluations: List<Evaluation>,
    val minPassingGrade: Double,      // convertida (si había custom) o default destino
    val convertedCount: Int           // evals con nota != null
)

class ConvertGradeScaleUseCase @Inject constructor() {
    operator fun invoke(
        evaluations: List<Evaluation>,
        targetScale: GradeScale,
        currentScale: GradeScale,
        customMinPassing: Double?     // null = sin custom
    ): GradeScaleConversionResult
}
```

- No persiste. El VM orquesta repositorio + prefs.
- Eval sin nota: solo se actualiza `gradeScaleId = target.id` (B15/D11).
- Si `customMinPassing == null`, el resultado trae `targetScale.minPassingGrade` y el VM **reset a 0f** (sentinela default) o guarda el default convertido — se guarda **0f** para seguir usando default de la escala (D16).

### 4.4 `GradeFormatters` (única fuente de display)

```kotlin
object GradeFormatters {
    fun grade(evaluation: Evaluation, displayScale: GradeScale): String
    // = displayScale.formatGrade(GradePolicy.toDisplayGrade(evaluation, displayScale))

    fun average(value: Double, displayScale: GradeScale): String
    // = displayScale.formatGrade(value)

    fun gradeWithScale(grade: Double, scale: GradeScale): String
    // = "${scale.formatGrade(grade)}/${scale.maxGrade.toDisplaySuffix()}"  ej "15.0/20", "A", "75/100"

    fun passing(grade: Double, scale: GradeScale): String
}
```

Formato por escala (D13): 0-10/0-20 `%.1f`; 0-100 `%.0f`; A-F letra.

---

## 5. Capa de datos

| Archivo | Cambio |
|---------|--------|
| `EvaluationEntity` | Eliminar hardcode `gradeScaleId="0-10"` del constructor secundario (aceptar parámetro con default `"0-10"` explícito o borrar constructor secundario si es solo de tests). |
| `EvaluationDao` | `@Transaction suspend fun updateEvaluations(list: List<EvaluationEntity>)` para rewrite atómico (B15). |
| `LocalEvaluationRepositoryImpl` | `updateEvaluations(userId, evals): Result<Unit>` que reutiliza el mapping `gradeScaleId` real. |
| `AddEvaluationViewModel` | `gradeScaleId = userPrefs.defaultGradeScaleFlow.first()` al crear (D11). |
| `GradesViewModel.updateGrade` | `copy(grade, isPresented, gradeScaleId = activeScale.id)` (D11). |
| `SubjectDetailViewModel.saveGrade` | Ídem (D11). |
| `ImportSubjectPackageUseCase` | Validar `grade` con `GradePolicy.validateGrade(g, GradeScale.fromId(e.gradeScaleId))` (B16). Conservar `gradeScaleId` vía `copy` (ya ocurre). |

---

## 6. Capa de dominio — propagación de `minPassing` efectivo

`GradePolicy.resolveMinPassing(scale, custom)` ya existe. Se usa en **todos** los call-sites:

| Site | Fix |
|------|-----|
| `CalculateAcademicStatsUseCase.calculateDistribution` | Recibir `minPassingGrade` ya resuelto (B7) |
| `CalculateAcademicStatsUseCase.globalAverage` | `GradePolicy.weightedAverage(evaluations, gradeScale)` (B17) |
| `CalculateRequiredGradeUseCase` | Parámetro `minPassingGrade` para `ACHIEVABLE` (B8) |
| `AcademicProgressChart` | Parámetro `minPassingGrade` en vez de `gradeScale.minPassingGrade` (B9) |
| `SubjectGradeCard` / `MainAverageDashboard` | Sin default `6.0`; siempre llega el efectivo (B10) |
| `NotesDashboardHero` / `SubjectGridBento` | Color/estado: `avg >= minPassing` (no `3.0`) (B10) |
| `generateRecommendations` | `GradeFormatters.average` + strings (B10) |

---

## 7. Presentación — captura de notas (D11)

Tres puntos de entrada, un solo contrato:

1. **`GradeRevampDialog`** (Calificaciones)
2. **`SubjectGradeQuickDialog`** (Detalle de materia)
3. **`EditEvaluationScreen` / `EditEvaluationGradeFields`**

Comportamiento unificado:

- Escala de validación = **escala activa del usuario** (no `evaluation.gradeScaleId`).
- Rango label/error = `activeScale.minGrade..maxGrade`.
- Al guardar: `grade = activeScale.roundGrade(parsed)`, `gradeScaleId = activeScale.id`.
- **Sin selector de escala** en ningún diálogo/formulario de evaluación (D11b). Solo se **muestra** la escala activa como hint.
- **Mensaje informativo (D11b)** bajo el campo de nota o en el subtítulo del diálogo, con `stringResource`:
  - `eval_grade_scale_hint` → «Usando escala {scale}. Puedes cambiarla en Ajustes → Escala académica.»
- **Presets:**
  - 0-20: 20, 18, 16, 14, 11, 8
  - 0-10: 10, 9, 8, 7, 6, 5
  - 0-100: 100, 90, 80, 70, 60, 50
  - A-F: chips `A, B, C, D, F` → GPA `4.0, 3.0, 2.0, 1.0, 0.0` (B5)

Aplica a `GradeRevampDialog`, `SubjectGradeQuickDialog`, `EditEvaluationGradeFields` y `AddEvaluationScreen` (este último no captura nota hoy; si se añade, misma regla).

---

## 8. Presentación — Ajustes (cambio de escala)

### 8.1 `SettingsGradeScaleDialog` (rework)

- Opciones: 0-20, 0-10, 0-100, **A-F** (B5).
- Slider de nota mínima:
  - 0-10: 0–10 step 0.5, default efectivo 6.0
  - 0-20: 0–20 step 0.5, default efectivo 11.0
  - 0-100: 0–100 step 5, default efectivo 60.0 (B13)
  - A-F: 0–4 step 0.1, default efectivo 2.0
- Inicializar `passingGrade` con el **efectivo actual convertido** al destino al cambiar de opción (B18), no con un default hardcodeado.
- Mostrar **preview** de conversión: “Se convertirán N notas · Mín: 6.0 → 12.0”.

### 8.2 Evento atómico (B2)

```kotlin
// SettingsEvent
data class ChangeGradeScale(
    val targetScaleId: String,
    val minPassingGrade: Float
) : SettingsEvent
```

Flujo `SettingsViewModel`:

```
onEvent(ChangeGradeScale)
  1. currentScale = GradeScale.fromId(uiState.defaultGradeScale)
  2. target = GradeScale.fromId(targetScaleId)
  3. evals = evaluationRepository.getEvaluations(userId).first()
  4. result = convertGradeScaleUseCase(evals, target, currentScale, customMin)
  5. evaluationRepository.updateEvaluations(userId, result.convertedEvaluations)  // @Transaction
  6. userPrefsRepository.setDefaultGradeScale(target.id)
  7. userPrefsRepository.setMinPassingGrade(result.minPassingGrade) // 0f si no había custom
  8. snackbar: "Se convirtieron N notas a {escala}"
```

Si `N == 0` (sin notas con grade), solo pasos 6–7 sin diálogo de conteo pesado (preview puede seguir mostrando “sin notas que convertir”).

### 8.3 `SettingsSectionsContent` (B3/B14)

- `passingText` usa `GradeFormatters.passing(effectivePassing, scale)`.
- `effectivePassing = if (uiState.minPassingGrade == 0f) scale.minPassingGrade else uiState.minPassingGrade`.
- `scaleDesc` cubre A-F.

### 8.4 Defaults unificados (B14)

| Fuente | Valor |
|--------|-------|
| DataStore `default_grade_scale` | `"0-10"` |
| DataStore `min_passing_grade` | `0f` (sentinela default) |
| `SettingsUiState` iniciales | `"0-10"`, `0f` (la UI resuelve efectivo) |
| `GradeScale.fromId` fallback | `SCALE_0_10` |

---

## 9. ViewModels — escala del usuario

| ViewModel | Cambio |
|-----------|--------|
| `NotesViewModel` | Inyectar `UserPrefsRepository`; `combine` con `defaultGradeScaleFlow`; promedios con `scale` (B11). Quitar `SCALE_0_10` hardcodeado. Rounding con `scale.roundGrade`. |
| `SubjectDetailViewModel` | `targetGrade` inicial y clamp en `scale.minGrade..scale.maxGrade`, default `minPassing` efectivo (B4). `saveGrade` stamp D11. Simulator recibe `minPassing` (B8). |
| `GradesViewModel` | `updateGrade` stamp D11. Ya lee prefs. |
| `AddEvaluationViewModel` | `gradeScaleId` = escala activa al crear. |
| `SettingsViewModel` | Evento único `ChangeGradeScale` (B2). |

---

## 10. Call-sites de display (checklist de implementación)

| Componente | Fix |
|------------|-----|
| `AgendaEvaluationBottomSheet` | `GradeFormatters.gradeWithScale` (quitar `/20`) |
| `SubjectEvaluationItemCard` | `GradeFormatters.grade(evaluation, activeScale)` |
| `SubjectGradeCard` | format + `minPassing` sin default 6.0 |
| `SubjectDetailHeroBento` | `GradeFormatters.average` |
| **`SubjectGridBento` (avatar monograma)** | `average` + umbral `>= minPassing` (B10) |
| `NotesDashboardHero` | `average` + umbral `>= minPassing` (B10) |
| `AgendaEvaluationCard` | `GradeFormatters.grade` |
| `AcademicWorkloadCard` | `average` + string resource |
| `AcademicEarlyWarningCard` | `GradeFormatters.average` para nota requerida |
| `MainAverageDashboard` | `GradeFormatters.average` (quitar `%.2f`) |
| `GradeSimulatorCard` | ya usa `formatGrade`; asegurar steps A-F y `minPassing` |
| `GradeRevampDialog` / `SubjectGradeQuickDialog` / `EditEvaluationGradeFields` | escala activa (D11) + presets A-F + **hint D11b** (sin selector de escala) |
| `SettingsGradeScaleDialog` / `SettingsSectionsContent` / `SettingsDialogDispatcher` | ver §8 |
| `EmptyGradesState` | string sin escala hardcodeada |
| `CalculateAcademicStatsUseCase.generateRecommendations` | `GradeFormatters` + strings.xml |

---

## 11. Testing (TDD)

### 11.1 Dominio
- **`GradeScaleTest`**: `roundGrade` (7.33/0-20→7.3, 73.6/0-100→74, 2.95/A-F→3.0); `formatGrade` letras.
- **`GradePolicyTest`**: `convertGrade` 0-10↔0-20↔0-100↔A-F; bordes (0, max); `convertEvaluations` con **escalas mixtas** (B1); `convertPassingGrade`.
- **`ConvertGradeScaleUseCaseTest`**:
  - mixtas 15/20 + 7.5/10 → 0-100 ⇒ 75 + 75
  - pendientes solo cambian `gradeScaleId`
  - custom minPassing convertido
  - sin custom → default destino
  - `convertedCount` correcto

### 11.2 Datos
- **`EvaluationEntityTest` / `EntityDataClassesTest`**: mapping preserva `gradeScaleId` (B12).
- **`LocalEvaluationRepositoryImplTest`**: `updateEvaluations` batch.
- **`ImportSubjectPackageUseCaseTest`**: rechaza nota fuera de rango de su escala (B16).

### 11.3 Presentation
- **`GradesViewModelTest`**: `updateGrade` setea `gradeScaleId` (D11/B6).
- **`SubjectDetailViewModelTest`**: `targetGrade` se adapta a escala 0-20/0-100 (B4); `saveGrade` stamp.
- **`NotesViewModelTest`**: promedios con escala 0-20 y 0-100 (B11).
- **`SettingsViewModelTest`**: `ChangeGradeScale` convierte + persiste en orden; N=0 solo prefs (B2).

### 11.4 UI smoke (manual / compose)
- Monograma de materia con 0-20 y 0-100 (color por minPassing, no `3.0`).
- Agenda muestra `15.0/20` o `A` o `75/100`.
- Dialog Ajustes: A-F visible; preview de conversión; `Mín` nunca `0.0` cuando no hay custom.

---

## 12. Flujo end-to-end (cambio de escala)

```
Usuario: Ajustes → Escala académica → elige 0-20 → nota mínima 11.0 → Guardar
  → SettingsGradeScaleDialog preview: "Convertir 12 notas · Mín 6.0 → 11.0"
  → Confirmar
  → SettingsViewModel.onChangeGradeScale
      → ConvertGradeScaleUseCase (puro)
      → EvaluationDao.updateEvaluations (@Transaction)
      → setDefaultGradeScale("0-20") + setMinPassingGrade(11.0f)
  → Snackbar "12 notas convertidas a Escala 0-20"
  → Toda la app recalcula con displayScale = SCALE_0_20
```

```
Usuario captura nota en SubjectGradeQuickDialog (escala activa 0-20)
  → escribe 15 → validateGrade(15, SCALE_0_20) OK
  → saveGrade(..., grade=15.0, gradeScaleId="0-20")
  → Grades / Notes / Agenda / Analíticas muestran 15.0 (o A / 75 si cambia escala luego)
```

---

## 13. Fuera de alcance (YAGNI)

- Migración masiva al arrancar (el rewrite es solo al cambiar escala; display usa `toDisplayGrade` como red).
- Sync multi-dispositivo de prefs (DataStore local).
- Escala por materia (solo global).
- Cambio de schema Room (solo DAO batch + fix de entity).
- Re-export/import de packs como parte de este esfuerzo (validación sí).

---

## 14. Criterios de aceptación

1. Cambiar escala en Ajustes reescribe todas las notas con nota y la nota mínima custom, previa confirmación con conteo.
2. Capturar una nota en cualquier punto usa la escala activa y persiste `gradeScaleId` acorde.
2b. Ningún formulario de evaluación permite cambiar la escala; se muestra hint “Puedes cambiar la escala en Ajustes”.
3. No quedan `%.1f`, `/20`, `>= 3.0`, `SCALE_0_10` hardcodeados ni `minPassingGrade = 6.0` en defaults de UI. No hay dropdown/radio de escala en captura de notas.
4. Con notas mixtas históricas (P2P/legacy), los promedios siguen siendo correctos (`toDisplayGrade`).
5. Ajustes muestra A-F y el mínimo efectivo (nunca `0.0` por la sentinela).
6. `./gradlew assembleDebug` y suite de tests en verde, con tests nuevos para B1–B18.

---

## 15. Orden de implementación sugerido

1. Dominio: `GradeScale.roundGrade`, `GradePolicy.convert*`, `ConvertGradeScaleUseCase` + tests (Red→Green).
2. Datos: `EvaluationEntity` B12, `EvaluationDao.updateEvaluations`, stamp en Add/Update/SaveGrade.
3. `GradeFormatters` + parche de todos los call-sites de display.
4. Prefs efectivas (B3) + Settings evento atómico + dialog A-F + preview.
5. ViewModels (Notes B11, SubjectDetail B4, propagación minPassing B7–B9).
6. Import P2P (B16) + `AcademicStats.globalAverage` (B17).
7. Verificación: `assembleDebug` + `testDebugUnitTest` + smoke checklist §11.4.

---

## 16. Riesgos y mitigaciones

| Riesgo | Mitigación |
|--------|------------|
| Rewrite masivo lento con miles de evals | Batch `@Transaction`; UX con `isSaving` |
| Usuario confirma y pierde precisión (ej. 7.33→7.3) | D13 documentado en el diálogo de confirmación |
| Packs P2P con escalas desconocidas | `GradeScale.fromId` fallback 0-10 + `toDisplayGrade` |
| Sentinela 0f mal interpretada | Tests de Settings + `effectivePassing` en un solo helper |
| D5 viejo en documentación/STATUS | Actualizar STATUS.md al implementar |
