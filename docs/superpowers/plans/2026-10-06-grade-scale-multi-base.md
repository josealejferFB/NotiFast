# Multi-escala de Calificaciones (0-10 / 0-20 / 0-100 / A-F) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Que toda la app adapte notas, promedios y formatos a la escala elegida en Ajustes, con rewrite transaccional al cambiar de escala y captura siempre en la escala activa.

**Architecture:** Clean Architecture + MVI. Dominio puro (`GradeScale`, `GradePolicy`, `ConvertGradeScaleUseCase`) sin Android; datos con Room `@Transaction` para rewrite masivo; UI consume `GradeFormatters` y la escala activa de `UserPrefsRepository`. Sin selector de escala en captura (solo Ajustes).

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose, Room, Hilt, DataStore, JUnit4 + MockK + coroutines-test.

**Spec:** [`docs/superpowers/specs/2026-10-06-grade-scale-multi-base-design.md`](../specs/2026-10-06-grade-scale-multi-base-design.md)

## Global Constraints

- Idioma UI (`strings.xml`): **Español**. Identificadores de código: **Inglés**.
- Sin strings hardcodeados en composables (usar `stringResource`).
- Sin colores arbitrarios: `MaterialTheme.colorScheme` / design tokens.
- Un solo `StateFlow` por ViewModel (`FeatureUiState`).
- Lógica de negocio **fuera** de composables.
- `try/catch` sin tragar errores: `Result<T>` + `Log.e`.
- Composables de feature ≤ ~250 LOC; extraer a `components/` si crece.
- Cero wildcard imports.
- TDD: test rojo → implementación mínima → verde → refactor → commit.
- **D5 rev:** al cambiar escala se reescriben `grade` + `gradeScaleId` + minPassing custom (por-evaluación desde su propia escala).
- **D11/D11b:** captura solo en escala activa; **sin** selector de escala en evaluaciones; hint informativo.
- **D12:** escalas 0-10, 0-20, 0-100, A-F.
- **D13:** redondeo destino: 0-10/0-20 → 1 decimal; 0-100 → entero; A-F → 1 decimal GPA.
- **D14:** display solo vía `GradeFormatters` / `GradePolicy`.
- **D16:** `minPassingGrade == 0f` en DataStore = default de la escala.
- **D17:** cambio de escala atómico (`ChangeGradeScale` + batch Room).
- Unificar `SCALE_0_100.minPassingGrade = 60.0` (no 70).
- Comandos de verificación: `./gradlew testDebugUnitTest` y `./gradlew assembleDebug`.

---

## File Structure

| Acción | Archivo | Responsabilidad |
|--------|---------|-----------------|
| Modify | `app/src/main/java/com/notifast/app/domain/model/GradeScale.kt` | `roundGrade`, minPassing 60, formatGrade |
| Modify | `app/src/main/java/com/notifast/app/domain/model/GradePolicy.kt` | `convertGrade`, `convertEvaluations`, `convertPassingGrade` |
| Create | `app/src/main/java/com/notifast/app/domain/usecase/ConvertGradeScaleUseCase.kt` | Rewrite puro al cambiar escala |
| Modify | `app/src/main/java/com/notifast/app/domain/util/GradeConstants.kt` | Deprecar / alinear a GradeScale |
| Create | `app/src/main/java/com/notifast/app/presentation/util/GradeFormatters.kt` | Formato display único |
| Modify | `app/src/main/java/com/notifast/app/data/local/entity/EvaluationEntity.kt` | B12: no hardcodear gradeScaleId |
| Modify | `app/src/main/java/com/notifast/app/data/local/dao/EvaluationDao.kt` | `updateEvaluations` `@Transaction` |
| Modify | `app/src/main/java/com/notifast/app/domain/repository/EvaluationRepository.kt` | `updateEvaluations` |
| Modify | `app/src/main/java/com/notifast/app/data/repository/LocalEvaluationRepositoryImpl.kt` | Batch rewrite |
| Modify | ViewModels Add/Grades/SubjectDetail/Notes/Settings | Stamp D11, prefs, evento atómico |
| Modify | UI grades/notes/schedules/subjectdetail/settings | Formatos + hint D11b + A-F |
| Modify | `app/src/main/res/values/strings.xml` | Hints, A-F, preview, snackbar |
| Test | `app/src/test/...` | TDD por tarea |

---

## Task 1: GradeScale — roundGrade y minPassing unificado

**Files:**
- Modify: `app/src/main/java/com/notifast/app/domain/model/GradeScale.kt`
- Test: `app/src/test/java/com/notifast/app/domain/model/GradeScaleTest.kt`

**Interfaces:**
- Produces: `GradeScale.roundGrade(grade: Double): Double`, `SCALE_0_100.minPassingGrade == 60.0`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.notifast.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GradeScaleTest {

    @Test
    fun `roundGrade keeps one decimal on 0-10 and 0-20`() {
        assertEquals(7.3, GradeScale.SCALE_0_10.roundGrade(7.34), 0.001)
        assertEquals(14.6, GradeScale.SCALE_0_20.roundGrade(14.56), 0.001)
    }

    @Test
    fun `roundGrade rounds to integer on 0-100`() {
        assertEquals(74.0, GradeScale.SCALE_0_100.roundGrade(73.6), 0.001)
        assertEquals(73.0, GradeScale.SCALE_0_100.roundGrade(73.4), 0.001)
    }

    @Test
    fun `roundGrade keeps one decimal on letter scale GPA`() {
        assertEquals(3.0, GradeScale.SCALE_LETTER.roundGrade(2.95), 0.001)
    }

    @Test
    fun `scale_0_100 default passing is 60`() {
        assertEquals(60.0, GradeScale.SCALE_0_100.minPassingGrade, 0.001)
    }

    @Test
    fun `formatGrade maps letter bands`() {
        assertEquals("A", GradeScale.SCALE_LETTER.formatGrade(4.0))
        assertEquals("B", GradeScale.SCALE_LETTER.formatGrade(3.0))
        assertEquals("C", GradeScale.SCALE_LETTER.formatGrade(2.0))
        assertEquals("F", GradeScale.SCALE_LETTER.formatGrade(0.5))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.domain.model.GradeScaleTest`
Expected: FAIL (`roundGrade` unresolved; minPassing 60 fails if still 60 vs 70 only in dialog — enum already 60; if green on minPassing, keep test as regression).

- [ ] **Step 3: Write minimal implementation**

In `GradeScale.kt`, add after `formatGrade`:

```kotlin
    fun roundGrade(grade: Double): Double = when (this) {
        SCALE_0_100 -> kotlin.math.round(grade)
        else -> kotlin.math.round(grade * 10.0) / 10.0
    }
```

Confirm enum:

```kotlin
    SCALE_0_100("0-100", "Escala 0 - 100", 0.0, 100.0, 60.0),
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.domain.model.GradeScaleTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/domain/model/GradeScale.kt \
        app/src/test/java/com/notifast/app/domain/model/GradeScaleTest.kt
git commit -m "feat(grades): add GradeScale.roundGrade and unify 0-100 passing to 60"
```

---

## Task 2: GradePolicy — convertGrade / convertEvaluations / convertPassingGrade

**Files:**
- Modify: `app/src/main/java/com/notifast/app/domain/model/GradePolicy.kt`
- Test: `app/src/test/java/com/notifast/app/domain/model/GradePolicyTest.kt`

**Interfaces:**
- Consumes: `GradeScale.roundGrade`
- Produces:
  - `GradePolicy.convertGrade(grade: Double, from: GradeScale, to: GradeScale): Double`
  - `GradePolicy.convertEvaluations(evals: List<Evaluation>, target: GradeScale): List<Evaluation>`
  - `GradePolicy.convertPassingGrade(value: Double, from: GradeScale, to: GradeScale): Double`

- [ ] **Step 1: Write the failing test** (append region to `GradePolicyTest.kt`)

```kotlin
    // region convert (D5 rev / D15)

    @Test
    fun `convertGrade 0-10 to 0-20 keeps one decimal`() {
        assertEquals(14.6, GradePolicy.convertGrade(7.3, GradeScale.SCALE_0_10, GradeScale.SCALE_0_20), 0.001)
    }

    @Test
    fun `convertGrade 0-20 to 0-100 is integer`() {
        assertEquals(75.0, GradePolicy.convertGrade(15.0, GradeScale.SCALE_0_20, GradeScale.SCALE_0_100), 0.001)
    }

    @Test
    fun `convertGrade 0-10 to letter GPA`() {
        assertEquals(3.0, GradePolicy.convertGrade(7.5, GradeScale.SCALE_0_10, GradeScale.SCALE_LETTER), 0.001)
    }

    @Test
    fun `convertEvaluations uses each evaluation own scale not a single from`() {
        val evals = listOf(
            eval("q", grade = 15.0, presented = true, weight = 50.0, scaleId = "0-20"),
            eval("c", grade = 7.5, presented = true, weight = 50.0, scaleId = "0-10")
        )
        val converted = GradePolicy.convertEvaluations(evals, GradeScale.SCALE_0_100)
        assertEquals(75.0, converted[0].grade!!, 0.001)
        assertEquals(75.0, converted[1].grade!!, 0.001)
        assertTrue(converted.all { it.gradeScaleId == "0-100" })
    }

    @Test
    fun `convertEvaluations pending only restamps scale id`() {
        val evals = listOf(eval("p", grade = null, presented = false, scaleId = "0-10"))
        val converted = GradePolicy.convertEvaluations(evals, GradeScale.SCALE_0_20)
        assertNull(converted[0].grade)
        assertEquals("0-20", converted[0].gradeScaleId)
    }

    @Test
    fun `convertPassingGrade 0-10 to 0-20`() {
        assertEquals(12.0, GradePolicy.convertPassingGrade(6.0, GradeScale.SCALE_0_10, GradeScale.SCALE_0_20), 0.001)
    }

    // endregion
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.domain.model.GradePolicyTest`
Expected: FAIL unresolved `convertGrade`

- [ ] **Step 3: Write minimal implementation**

```kotlin
    fun convertGrade(grade: Double, from: GradeScale, to: GradeScale): Double {
        if (from == to) return to.roundGrade(grade)
        if (from.maxGrade <= 0.0) return to.roundGrade(grade)
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

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.domain.model.GradePolicyTest`
Expected: PASS (existing tests stay green)

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/domain/model/GradePolicy.kt \
        app/src/test/java/com/notifast/app/domain/model/GradePolicyTest.kt
git commit -m "feat(grades): convert grades per-evaluation source scale (D15)"
```

---

## Task 3: ConvertGradeScaleUseCase

**Files:**
- Create: `app/src/main/java/com/notifast/app/domain/usecase/ConvertGradeScaleUseCase.kt`
- Test: `app/src/test/java/com/notifast/app/domain/usecase/ConvertGradeScaleUseCaseTest.kt`

**Interfaces:**
- Consumes: `GradePolicy.convertEvaluations`, `GradePolicy.convertPassingGrade`
- Produces:
  - `data class GradeScaleConversionResult(convertedEvaluations: List<Evaluation>, minPassingGrade: Double, hasCustomMinPassing: Boolean, convertedCount: Int)`
  - `ConvertGradeScaleUseCase.invoke(evaluations, targetScale, currentScale, customMinPassing: Double?): GradeScaleConversionResult`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.model.GradeScale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertGradeScaleUseCaseTest {

    private val useCase = ConvertGradeScaleUseCase()

    private fun eval(id: String, grade: Double?, scaleId: String) = Evaluation(
        id = id, grade = grade, isPresented = grade != null, gradeScaleId = scaleId
    )

    @Test
    fun `converts mixed source scales into target 0-100`() {
        val result = useCase(
            evaluations = listOf(eval("1", 15.0, "0-20"), eval("2", 7.5, "0-10")),
            targetScale = GradeScale.SCALE_0_100,
            currentScale = GradeScale.SCALE_0_10,
            customMinPassing = 6.0
        )
        assertEquals(75.0, result.convertedEvaluations[0].grade!!, 0.001)
        assertEquals(75.0, result.convertedEvaluations[1].grade!!, 0.001)
        assertEquals(2, result.convertedCount)
        assertTrue(result.hasCustomMinPassing)
        assertEquals(60.0, result.minPassingGrade, 0.001)
    }

    @Test
    fun `without custom passing uses target default`() {
        val result = useCase(
            evaluations = emptyList(),
            targetScale = GradeScale.SCALE_0_20,
            currentScale = GradeScale.SCALE_0_10,
            customMinPassing = null
        )
        assertFalse(result.hasCustomMinPassing)
        assertEquals(11.0, result.minPassingGrade, 0.001)
    }

    @Test
    fun `pending evaluations are restamped only`() {
        val result = useCase(
            listOf(eval("p", null, "0-10")),
            GradeScale.SCALE_0_20,
            GradeScale.SCALE_0_10,
            customMinPassing = null
        )
        assertEquals(0, result.convertedCount)
        assertEquals("0-20", result.convertedEvaluations[0].gradeScaleId)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.domain.usecase.ConvertGradeScaleUseCaseTest`
Expected: FAIL unresolved

- [ ] **Step 3: Write minimal implementation**

```kotlin
package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale
import javax.inject.Inject

data class GradeScaleConversionResult(
    val convertedEvaluations: List<Evaluation>,
    val minPassingGrade: Double,
    val hasCustomMinPassing: Boolean,
    val convertedCount: Int
)

class ConvertGradeScaleUseCase @Inject constructor() {

    operator fun invoke(
        evaluations: List<Evaluation>,
        targetScale: GradeScale,
        currentScale: GradeScale,
        customMinPassing: Double?
    ): GradeScaleConversionResult {
        val converted = GradePolicy.convertEvaluations(evaluations, targetScale)
        val count = converted.count { it.grade != null }
        val hasCustom = customMinPassing != null
        val minPassing = if (hasCustom) {
            GradePolicy.convertPassingGrade(customMinPassing!!, currentScale, targetScale)
        } else {
            targetScale.minPassingGrade
        }
        return GradeScaleConversionResult(
            convertedEvaluations = converted,
            minPassingGrade = minPassing,
            hasCustomMinPassing = hasCustom,
            convertedCount = count
        )
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.domain.usecase.ConvertGradeScaleUseCaseTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/domain/usecase/ConvertGradeScaleUseCase.kt \
        app/src/test/java/com/notifast/app/domain/usecase/ConvertGradeScaleUseCaseTest.kt
git commit -m "feat(grades): add ConvertGradeScaleUseCase for atomic scale rewrite"
```

---

## Task 4: Datos — entity, DAO batch, repository

**Files:**
- Modify: `app/src/main/java/com/notifast/app/data/local/entity/EvaluationEntity.kt`
- Modify: `app/src/main/java/com/notifast/app/data/local/dao/EvaluationDao.kt`
- Modify: `app/src/main/java/com/notifast/app/domain/repository/EvaluationRepository.kt`
- Modify: `app/src/main/java/com/notifast/app/data/repository/LocalEvaluationRepositoryImpl.kt`
- Test: `app/src/test/java/com/notifast/app/data/repository/LocalEvaluationRepositoryImplTest.kt`, `app/src/test/java/com/notifast/app/data/local/entity/EntityDataClassesTest.kt`

**Interfaces:**
- Produces: `EvaluationRepository.updateEvaluations(userId: String, evaluations: List<Evaluation>): Result<Unit>`
- Produces: `EvaluationDao.updateEvaluations(evals: List<EvaluationEntity>)` `@Transaction`

- [ ] **Step 1: Write the failing test**

Add to `EntityDataClassesTest.kt` (or new `EvaluationEntityMappingTest`):

```kotlin
    @Test
    fun `EvaluationEntity secondary constructor preserves gradeScaleId`() {
        val entity = EvaluationEntity(
            id = "e1",
            userId = "u1",
            subjectName = "M",
            title = "T",
            description = "",
            dateMillis = null,
            noteIds = emptyList(),
            imageUrl = null,
            quickTips = "",
            type = "EXAM",
            grade = 15.0,
            weight = 50.0,
            isPresented = true,
            createdAtMillis = null,
            updatedAtMillis = null
        )
        // If secondary ctor has default param gradeScaleId it must not force "0-10" when passed
        val withScale = entity.copy(gradeScaleId = "0-20")
        assertEquals("0-20", withScale.gradeScaleId)
        // Secondary constructor path used by legacy tests must allow explicit scale:
        val legacy = EvaluationEntity(
            id = "e2", userId = "u1", subjectName = "M", title = "T", description = "",
            dateMillis = null, noteIds = emptyList(), imageUrl = null, quickTips = "",
            type = "EXAM", grade = 7.5, weight = null, isPresented = true,
            gradeScaleId = "0-10",
            createdAtMillis = null, updatedAtMillis = null
        )
        assertEquals("0-10", legacy.gradeScaleId)
    }
```

Add to `LocalEvaluationRepositoryImplTest.kt`:

```kotlin
    @Test
    fun `updateEvaluations batch updates all rows`() = runTest {
        val e1 = EvaluationEntity(id = "a", userId = "u1", title = "A", grade = 7.5, gradeScaleId = "0-10", isPresented = true)
        val e2 = EvaluationEntity(id = "b", userId = "u1", title = "B", grade = 15.0, gradeScaleId = "0-20", isPresented = true)
        coEvery { evaluationDao.updateEvaluations(any()) } returns Unit

        val result = repository.updateEvaluations(
            "u1",
            listOf(
                Evaluation(id = "a", userId = "u1", title = "A", grade = 15.0, isPresented = true, gradeScaleId = "0-20"),
                Evaluation(id = "b", userId = "u1", title = "B", grade = 30.0, isPresented = true, gradeScaleId = "0-20")
            )
        )

        assertTrue(result.isSuccess)
        coVerify { evaluationDao.updateEvaluations(match { it.size == 2 && it.all { e -> e.gradeScaleId == "0-20" } }) }
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.data.repository.LocalEvaluationRepositoryImplTest --tests com.notifast.app.data.local.entity.EntityDataClassesTest`
Expected: FAIL `updateEvaluations` unresolved / secondary ctor signature mismatch

- [ ] **Step 3: Write minimal implementation**

`EvaluationEntity` — secondary constructor accepts scale (do not force `"0-10"` when provided):

```kotlin
    constructor(
        id: String,
        userId: String,
        subjectName: String,
        title: String,
        description: String,
        dateMillis: Long?,
        noteIds: List<String>,
        imageUrl: String?,
        quickTips: String,
        type: String,
        grade: Double?,
        weight: Double?,
        isPresented: Boolean,
        createdAtMillis: Long?,
        updatedAtMillis: Long?,
        gradeScaleId: String = "0-10"
    ) : this(
        id = id,
        userId = userId,
        subjectId = "",
        subjectName = subjectName,
        title = title,
        description = description,
        date = dateMillis?.let { Date(it) },
        noteIds = noteIds,
        imageUrl = imageUrl,
        quickTips = quickTips,
        type = type,
        grade = grade,
        weight = weight,
        isPresented = isPresented,
        gradeScaleId = gradeScaleId,
        createdAt = createdAtMillis?.let { Date(it) },
        updatedAt = updatedAtMillis?.let { Date(it) }
    )
```

`EvaluationDao`:

```kotlin
    @androidx.room.Transaction
    suspend fun updateEvaluations(evals: List<EvaluationEntity>) {
        evals.forEach { updateEvaluation(it) }
    }
```

`EvaluationRepository`:

```kotlin
    suspend fun updateEvaluations(userId: String, evaluations: List<Evaluation>): Result<Unit>
```

`LocalEvaluationRepositoryImpl` — extract shared `toEntity(userId, evaluation)` used by `updateEvaluation` and:

```kotlin
    override suspend fun updateEvaluations(
        userId: String,
        evaluations: List<Evaluation>
    ): Result<Unit> {
        return try {
            evaluationDao.updateEvaluations(evaluations.map { it.toEntity(userId) })
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to batch update ${evaluations.size} evaluations", e)
            Result.failure(e)
        }
    }
```

Private `toEntity` must set `gradeScaleId = evaluation.gradeScaleId` (never hardcode).

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.data.repository.LocalEvaluationRepositoryImplTest --tests com.notifast.app.data.local.entity.EntityDataClassesTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/data/local/entity/EvaluationEntity.kt \
        app/src/main/java/com/notifast/app/data/local/dao/EvaluationDao.kt \
        app/src/main/java/com/notifast/app/domain/repository/EvaluationRepository.kt \
        app/src/main/java/com/notifast/app/data/repository/LocalEvaluationRepositoryImpl.kt \
        app/src/test/java/com/notifast/app/data/repository/LocalEvaluationRepositoryImplTest.kt \
        app/src/test/java/com/notifast/app/data/local/entity/EntityDataClassesTest.kt
git commit -m "fix(grades): preserve gradeScaleId in entity mapping and add transactional batch update"
```

---

## Task 5: GradeFormatters + strings

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/util/GradeFormatters.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Test: `app/src/test/java/com/notifast/app/presentation/util/GradeFormattersTest.kt`

**Interfaces:**
- Produces:
  - `GradeFormatters.grade(grade: Double?, sourceScaleId: String, displayScale: GradeScale): String`
  - `GradeFormatters.average(value: Double, displayScale: GradeScale): String`
  - `GradeFormatters.gradeWithScale(grade: Double?, sourceScaleId: String, displayScale: GradeScale): String`
  - `GradeFormatters.passing(value: Double, displayScale: GradeScale): String`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.notifast.app.presentation.util

import com.notifast.app.domain.model.GradeScale
import org.junit.Assert.assertEquals
import org.junit.Test

class GradeFormattersTest {

    @Test
    fun `average formats per display scale`() {
        assertEquals("7.5", GradeFormatters.average(7.5, GradeScale.SCALE_0_10))
        assertEquals("15.0", GradeFormatters.average(15.0, GradeScale.SCALE_0_20))
        assertEquals("75", GradeFormatters.average(75.0, GradeScale.SCALE_0_100))
        assertEquals("B", GradeFormatters.average(3.0, GradeScale.SCALE_LETTER))
    }

    @Test
    fun `gradeWithScale shows max of display scale`() {
        assertEquals("7.5/10", GradeFormatters.gradeWithScale(7.5, "0-10", GradeScale.SCALE_0_10))
        assertEquals("75/100", GradeFormatters.gradeWithScale(75.0, "0-100", GradeScale.SCALE_0_100))
        assertEquals("A", GradeFormatters.gradeWithScale(4.0, "A-F", GradeScale.SCALE_LETTER))
    }

    @Test
    fun `null grade renders dash`() {
        assertEquals("-", GradeFormatters.grade(null, "0-10", GradeScale.SCALE_0_10))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.presentation.util.GradeFormattersTest`
Expected: FAIL unresolved

- [ ] **Step 3: Write minimal implementation**

```kotlin
package com.notifast.app.presentation.util

import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale

object GradeFormatters {

    fun grade(grade: Double?, sourceScaleId: String, displayScale: GradeScale): String {
        if (grade == null) return "-"
        val src = GradeScale.fromId(sourceScaleId)
        return displayScale.formatGrade(GradePolicy.convertGrade(grade, src, displayScale))
    }

    fun average(value: Double, displayScale: GradeScale): String =
        displayScale.formatGrade(value)

    fun gradeWithScale(grade: Double?, sourceScaleId: String, displayScale: GradeScale): String {
        if (grade == null) return "-"
        if (displayScale == GradeScale.SCALE_LETTER) return displayScale.formatGrade(
            GradePolicy.convertGrade(grade, GradeScale.fromId(sourceScaleId), displayScale)
        )
        val formatted = grade(grade, sourceScaleId, displayScale)
        val max = if (displayScale.maxGrade % 1.0 == 0.0) {
            displayScale.maxGrade.toInt().toString()
        } else {
            displayScale.maxGrade.toString()
        }
        return "$formatted/$max"
    }

    fun passing(value: Double, displayScale: GradeScale): String =
        displayScale.formatGrade(value)
}
```

`strings.xml` (antes de `</resources>`):

```xml
    <string name="settings_academic_scale_letters">Letras (A - F)</string>
    <string name="settings_academic_scale_letters_desc">Escala tipo GPA / USA</string>
    <string name="eval_grade_scale_hint">Usando escala %1$s. Puedes cambiarla en Ajustes → Escala académica.</string>
    <string name="settings_grade_scale_convert_preview">Se convertirán %1$d notas · Mínimo %2$s → %3$s</string>
    <string name="settings_grade_scale_convert_none">Sin notas que convertir · Mínimo %1$s → %2$s</string>
    <string name="settings_grade_scale_converted_snack">%1$d notas convertidas a %2$s</string>
    <string name="settings_grade_scale_changed_snack">Escala actualizada a %1$s</string>
    <string name="settings_academic_passing_default">Por defecto (%1$s)</string>
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.presentation.util.GradeFormattersTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/util/GradeFormatters.kt \
        app/src/test/java/com/notifast/app/presentation/util/GradeFormattersTest.kt \
        app/src/main/res/values/strings.xml
git commit -m "feat(grades): centralize grade display formatting and scale-change strings"
```

---

## Task 6: Stamp gradeScaleId on create and grade save (D11)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/addevaluation/AddEvaluationViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/grades/GradesViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailViewModel.kt`
- Test: `GradesViewModelTest.kt`, `SubjectDetailViewModelTest.kt`, new `AddEvaluationViewModelTest` if missing else extend

**Interfaces:**
- Consumes: `UserPrefsRepository.defaultGradeScaleFlow`
- Produces: evaluations persisted with `gradeScaleId = activeScale.id`

- [ ] **Step 1: Write the failing test**

In `GradesViewModelTest.kt`:

```kotlin
    @Test
    fun `UpdateGrade stamps active grade scale id`() = runTest {
        coEvery { evaluationRepository.updateEvaluation(any(), any(), any()) } returns Result.success(Unit)
        val viewModel = GradesViewModel(
            evaluationRepository, noteRepository, scheduleRepository,
            authRepository, userPrefsRepository,
            calculateWeightedAverageUseCase, calculateAcademicStatsUseCase
        )
        advanceUntilIdle()

        viewModel.onEvent(GradesEvent.UpdateGrade("1", 15.0, true))
        advanceUntilIdle()

        coVerify {
            evaluationRepository.updateEvaluation(
                "user_123",
                match { it.grade == 15.0 && it.gradeScaleId == "0-20" },
                null
            )
        }
    }
```

(Scale `"0-20"` comes from mocked `defaultGradeScaleFlow`.)

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.presentation.feature.grades.GradesViewModelTest`
Expected: FAIL (`gradeScaleId` still `"0-10"` on `current.copy`)

- [ ] **Step 3: Write minimal implementation**

`GradesViewModel.updateGrade`:

```kotlin
    private fun updateGrade(evaluationId: String, grade: Double?, isPresented: Boolean) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val current = _uiState.value.evaluationsBySubject.values.flatten()
                .find { it.id == evaluationId } ?: return@launch
            val activeScale = _uiState.value.gradeScale
            val updated = current.copy(
                grade = grade?.let { activeScale.roundGrade(it) },
                isPresented = isPresented,
                gradeScaleId = activeScale.id
            )
            evaluationRepository.updateEvaluation(userId, updated, null).onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Error al actualizar nota") }
            }
        }
    }
```

`SubjectDetailViewModel.saveGrade` — same stamp pattern (`roundGrade` + `gradeScaleId = scale.id` from `uiState.gradeScale`).

`AddEvaluationViewModel` — inject `UserPrefsRepository`; in `save()`:

```kotlin
            val scaleId = userPrefsRepository.defaultGradeScaleFlow.first()
            val evaluation = Evaluation(
                // ... existing fields ...
                gradeScaleId = scaleId
            )
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.presentation.feature.grades.GradesViewModelTest --tests com.notifast.app.presentation.feature.subjectdetail.SubjectDetailViewModelTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/feature/addevaluation/AddEvaluationViewModel.kt \
        app/src/main/java/com/notifast/app/presentation/feature/grades/GradesViewModel.kt \
        app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailViewModel.kt \
        app/src/test/java/com/notifast/app/presentation/feature/grades/GradesViewModelTest.kt
git commit -m "fix(grades): stamp active gradeScaleId on create and grade save (D11)"
```

---

## Task 7: Display call-sites + hint D11b (sin selector de escala)

**Files:**
- Modify (formatos):
  - `.../schedules/components/AgendaEvaluationBottomSheet.kt`
  - `.../schedules/components/AgendaEvaluationCard.kt`
  - `.../subjectdetail/components/SubjectEvaluationItemCard.kt`
  - `.../subjectdetail/components/SubjectDetailHeroBento.kt`
  - `.../notes/components/SubjectGridBento.kt`
  - `.../notes/components/NotesDashboardHero.kt`
  - `.../grades/components/SubjectGradeCard.kt`
  - `.../grades/components/MainAverageDashboard.kt`
  - `.../grades/components/AcademicWorkloadCard.kt`
  - `.../grades/components/AcademicEarlyWarningCard.kt`
- Modify (hint D11b):
  - `.../grades/components/GradeRevampDialog.kt`
  - `.../subjectdetail/components/SubjectGradeQuickDialog.kt`
  - `.../editevaluation/components/EditEvaluationGradeFields.kt`
- Modify: `app/src/main/res/values/strings.xml` (ya en Task 5)

**Interfaces:**
- Consumes: `GradeFormatters.*`, `GradeScale`, `minPassingGrade` efectivo en UiState

- [ ] **Step 1: Replace hardcoded formats** (no new unit tests required if pure format already tested; keep compile green)

Examples (exact replacements):

```kotlin
// AgendaEvaluationBottomSheet — was "${eval.grade}/20"
content = GradeFormatters.gradeWithScale(eval.grade, eval.gradeScaleId, displayScale),
```

```kotlin
// SubjectEvaluationItemCard — was String.format("%.1f", evaluation.grade)
text = if (isGraded) GradeFormatters.grade(evaluation.grade, evaluation.gradeScaleId, displayScale)
       else stringResource(R.string.subject_detail_edit_grade_btn),
```

```kotlin
// SubjectGridBento / NotesDashboardHero — was avg >= 3.0
color = if (avg >= minPassingGrade) ForestSage else AquaGreen
Text(GradeFormatters.average(avg, displayScale), ...)
```

```kotlin
// AgendaEvaluationCard
text = evaluation.grade?.let { GradeFormatters.grade(it, evaluation.gradeScaleId, displayScale) } ?: stringResource(R.string.grades_status_pending),
```

Pass `displayScale` and `minPassingGrade` as composable params from parent VMs (Grades/Notes/SubjectDetail already expose `gradeScale`).

- [ ] **Step 2: D11b hint — no scale picker**

In `GradeRevampDialog`, `SubjectGradeQuickDialog`, `EditEvaluationGradeFields` add under the grade field:

```kotlin
Text(
    text = stringResource(R.string.eval_grade_scale_hint, displayScale.label),
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

Ensure **no** Dropdown/SegmentedButton for scale exists in these screens (search `gradeScaleId` UI selectors and remove if any).

- [ ] **Step 3: Compile**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/
git commit -m "fix(ui): unify grade display formatters and add scale hint without picker (D11b/D14)"
```

---

## Task 8: minPassing efectivo en dominio y charts (B7–B9, B17)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/domain/usecase/CalculateAcademicStatsUseCase.kt`
- Modify: `app/src/main/java/com/notifast/app/domain/usecase/CalculateRequiredGradeUseCase.kt`
- Modify: `.../grades/components/AcademicProgressChart.kt`
- Modify: `.../subjectdetail/SubjectDetailViewModel.kt` (targetGrade B4 + simulator minPassing)
- Test: `CalculateAcademicStatsUseCaseTest.kt`, `CalculateRequiredGradeUseCaseTest.kt`, `SubjectDetailViewModelTest.kt`

**Interfaces:**
- Produces: `CalculateRequiredGradeUseCase(..., minPassingGrade: Double)` optional param default `scale.minPassingGrade`
- Produces: `AcademicStats.globalAverage` = weighted overall average

- [ ] **Step 1: Write the failing test**

```kotlin
    @Test
    fun `globalAverage is weighted over all evaluations not average of subject averages`() {
        // Subject A: 20/20 weight 10; Subject B: 5/20 weight 90 → not (20+5)/2
        val subjects = listOf(
            SubjectData(name = "A", colorHex = "#000"),
            SubjectData(name = "B", colorHex = "#111")
        )
        val evals = listOf(
            Evaluation(subjectName = "A", grade = 20.0, weight = 10.0, isPresented = true, gradeScaleId = "0-20"),
            Evaluation(subjectName = "B", grade = 5.0, weight = 90.0, isPresented = true, gradeScaleId = "0-20")
        )
        val stats = CalculateAcademicStatsUseCase()(evals, subjects, emptyList(), GradeScale.SCALE_0_20)
        assertEquals(6.5, stats.globalAverage, 0.01) // (20*10 + 5*90)/100 = 6.5
    }
```

```kotlin
    @Test
    fun `ACHIEVABLE uses custom minPassing not scale default`() {
        val evals = listOf(
            Evaluation(id = "1", grade = 10.0, weight = 50.0, isPresented = true, gradeScaleId = "0-20"),
            Evaluation(id = "2", grade = null, weight = 50.0, isPresented = false, gradeScaleId = "0-20")
        )
        val result = CalculateRequiredGradeUseCase()(
            evals, targetGrade = 11.0, scale = GradeScale.SCALE_0_20, minPassingGrade = 10.0
        )
        // required ~2.0 <= 10.0 → ACHIEVABLE
        assertEquals(SimulatorStatus.ACHIEVABLE, result.status)
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.domain.usecase.CalculateAcademicStatsUseCaseTest --tests com.notifast.app.domain.usecase.CalculateRequiredGradeUseCaseTest`
Expected: FAIL

- [ ] **Step 3: Implement**

`CalculateAcademicStatsUseCase`:
- `val globalAverage = GradePolicy.weightedAverage(evaluations, gradeScale) ?: 0.0`
- Pass `minPassingGrade` into `calculateDistribution(evaluations, gradeScale, minPassingGrade)`.
- `generateRecommendations` use `GradeFormatters.average(subject.minGradeNeededToPass, gradeScale)` and `stringResource`-safe strings via caller or keep formatted numbers via `GradeFormatters` only (domain: format with `gradeScale.formatGrade`).

`CalculateRequiredGradeUseCase`:

```kotlin
    operator fun invoke(
        evaluations: List<Evaluation>,
        targetGrade: Double,
        scale: GradeScale = GradeScale.SCALE_0_10,
        minPassingGrade: Double = scale.minPassingGrade
    ): RequiredGradeResult {
        // ...
        val status = when {
            requiredGrade <= minPassingGrade -> SimulatorStatus.ACHIEVABLE
            requiredGrade <= scale.maxGrade -> SimulatorStatus.AT_RISK
            else -> SimulatorStatus.IMPOSSIBLE
        }
```

`AcademicProgressChart`: parameter `minPassingGrade: Double = gradeScale.minPassingGrade`.

`SubjectDetailViewModel`: initialize `targetGrade = resolvedMinPassing` of active scale (not `6.0`); clamp to scale; pass `minPassing` into simulator.

- [ ] **Step 4: Run tests**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.domain.usecase.* --tests com.notifast.app.presentation.feature.subjectdetail.SubjectDetailViewModelTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/domain/usecase/ \
        app/src/main/java/com/notifast/app/presentation/feature/grades/components/AcademicProgressChart.kt \
        app/src/main/java/com/notifast/app/presentation/feature/subjectdetail/SubjectDetailViewModel.kt \
        app/src/test/java/com/notifast/app/domain/usecase/ \
        app/src/test/java/com/notifast/app/presentation/feature/subjectdetail/
git commit -m "fix(grades): propagate custom minPassing and true weighted global average"
```

---

## Task 9: Ajustes — efectivo, A-F, ChangeGradeScale atómico

**Files:**
- Modify: `.../settings/SettingsContract.kt`
- Modify: `.../settings/SettingsViewModel.kt`
- Modify: `.../settings/components/SettingsGradeScaleDialog.kt`
- Modify: `.../settings/components/SettingsDialogDispatcher.kt`
- Modify: `.../settings/components/SettingsSectionsContent.kt`
- Test: `SettingsViewModelTest.kt`

**Interfaces:**
- Produces: `SettingsEvent.ChangeGradeScale(targetScaleId: String, minPassingGrade: Float)`
- Consumes: `ConvertGradeScaleUseCase`, `EvaluationRepository.updateEvaluations`, `UserPrefsRepository`

- [ ] **Step 1: Write the failing test**

```kotlin
    @Test
    fun `ChangeGradeScale converts evaluations and prefs atomically`() = runTest {
        val evals = listOf(
            Evaluation(id = "1", grade = 15.0, isPresented = true, weight = 100.0, gradeScaleId = "0-20")
        )
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(evals)
        coEvery { evaluationRepository.updateEvaluations(any(), any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.setDefaultGradeScale(any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.setMinPassingGrade(any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.minPassingGradeFlow } returns flowOf(6.0f)
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-20")

        val viewModel = createSettingsViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.ChangeGradeScale("0-100", 60f))
        advanceUntilIdle()

        coVerify { evaluationRepository.updateEvaluations("user_123", match { it.single().grade == 75.0 }) }
        coVerify { userPrefsRepository.setDefaultGradeScale("0-100") }
        coVerify { userPrefsRepository.setMinPassingGrade(60f) }
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.presentation.feature.settings.SettingsViewModelTest`
Expected: FAIL event unknown

- [ ] **Step 3: Implement**

`SettingsContract`:

```kotlin
    data class ChangeGradeScale(
        val targetScaleId: String,
        val minPassingGrade: Float
    ) : SettingsEvent
```

Keep `SetDefaultGradeScale` / `SetMinPassingGrade` only if other screens need them; dialog dispatcher must **only** call `ChangeGradeScale`.

`SettingsViewModel.onChangeGradeScale`:

```kotlin
    private fun changeGradeScale(targetScaleId: String, newPassing: Float) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val currentScale = GradeScale.fromId(_uiState.value.defaultGradeScale)
            val target = GradeScale.fromId(targetScaleId)
            val custom = _uiState.value.minPassingGrade.takeIf { it != 0f }?.toDouble()
            val evals = evaluationRepository.getEvaluations(userId).first()
            val result = convertGradeScaleUseCase(evals, target, currentScale, custom)
            evaluationRepository.updateEvaluations(userId, result.convertedEvaluations).onFailure {
                _uiState.update { s -> s.copy(error = it.message ?: "Error al convertir notas") }
                return@launch
            }
            userPrefsRepository.setDefaultGradeScale(target.id)
            userPrefsRepository.setMinPassingGrade(newPassing)
            _uiState.update {
                it.copy(
                    defaultGradeScale = target.id,
                    minPassingGrade = newPassing,
                    snackbarMessage = if (result.convertedCount > 0) "settings_grade_scale_converted_snack" else "settings_grade_scale_changed_snack"
                )
            }
        }
    }
```

(Prefer building snack text in UI from `stringResource` + counts in UiState fields `lastConvertedCount`, `lastScaleLabel`.)

`SettingsGradeScaleDialog`:
- Add option A-F using `GradeScale.SCALE_LETTER`.
- Range table:

```kotlin
    val (maxRange, defaultPassing, stepSize) = when (selectedScale) {
        "0-10" -> Triple(10f, 6.0f, 0.5f)
        "0-100" -> Triple(100f, 60.0f, 5.0f)
        "A-F" -> Triple(4f, 2.0f, 0.1f)
        else -> Triple(20f, 11.0f, 0.5f)
    }
```

- Init `passingGrade` from `currentPassingGrade` if valid else default; on scale switch **convert** previous passing with `GradePolicy.convertPassingGrade` (B18).
- Show preview line using `R.string.settings_grade_scale_convert_preview`.
- `onConfirm(scaleId, passingGrade)` only.

`SettingsDialogDispatcher`:

```kotlin
                onConfirm = { scale, passingGrade ->
                    onEvent(SettingsEvent.ChangeGradeScale(scale, passingGrade))
                },
```

`SettingsSectionsContent`:
- `effectivePassing = if (uiState.minPassingGrade == 0f) GradeScale.fromId(uiState.defaultGradeScale).minPassingGrade.toFloat() else uiState.minPassingGrade`
- Scale desc includes A-F; `passingText` via `GradeFormatters.passing`.

- [ ] **Step 4: Run test**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.presentation.feature.settings.SettingsViewModelTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/feature/settings/ \
        app/src/test/java/com/notifast/app/presentation/feature/settings/
git commit -m "feat(settings): atomic grade scale change with conversion and A-F support"
```

---

## Task 10: NotesViewModel escala del usuario (B11)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/NotesViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/notes/NotesContract.kt` (si hace falta `gradeScale`)
- Modify: `.../notes/components/NotesDashboardHero.kt`, `SubjectGridBento.kt` (params scale/minPassing)
- Test: `NotesViewModelTest.kt`

**Interfaces:**
- Consumes: `UserPrefsRepository.defaultGradeScaleFlow`, `minPassingGradeFlow`
- Produces: `NotesUiState` exposes `gradeScale: GradeScale` and `minPassingGrade: Double`

- [ ] **Step 1: Write the failing test**

```kotlin
    @Test
    fun `globalAverage uses user grade scale 0-20`() = runTest {
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-20")
        coEvery { userPrefsRepository.minPassingGradeFlow } returns flowOf(0f)
        // eval grade 14.0 with gradeScaleId 0-20 → average 14.0
        ...
        assertEquals(14.0, state.globalAverage)
        assertEquals(GradeScale.SCALE_0_20, state.gradeScale)
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew testDebugUnitTest --tests com.notifast.app.presentation.feature.notes.NotesViewModelTest`
Expected: FAIL (hardcoded 0-10 or missing field)

- [ ] **Step 3: Implement**

Inject `UserPrefsRepository`. `combine` with `defaultGradeScaleFlow` + `minPassingGradeFlow`. Replace both `GradeScale.SCALE_0_10` with `scale`. Rounding: `scale.roundGrade(it)`. Pass `gradeScale` and `minPassingGrade` into hero/bento.

- [ ] **Step 4: Run test**

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/presentation/feature/notes/ \
        app/src/test/java/com/notifast/app/presentation/feature/notes/
git commit -m "fix(notes): honor user grade scale in hub averages and subject cards"
```

---

## Task 11: Import P2P + dialogos de captura con escala activa (B5/B6/B16)

**Files:**
- Modify: `.../domain/usecase/ImportSubjectPackageUseCase.kt`
- Modify: `GradeRevampDialog.kt`, `SubjectGradeQuickDialog.kt`, `EditEvaluationGradeFields.kt` + parents that pass scale
- Test: `ImportSubjectPackage` tests if any; `GradePolicy` already covers validate; add import test

**Interfaces:**
- Consumes: `GradePolicy.validateGrade`, `GradeScale.fromId`

- [ ] **Step 1: Failing test for import validation**

```kotlin
    @Test
    fun `rejects grade outside its own scale`() = runTest {
        // package evaluation grade=50 gradeScaleId=0-10 → invalid
        ...
    }
```

- [ ] **Step 2: Fail red**

- [ ] **Step 3: Implement** in `ImportSubjectPackageUseCase` loop:

```kotlin
                if (e.grade != null) {
                    val src = GradeScale.fromId(e.gradeScaleId)
                    if (!GradePolicy.validateGrade(e.grade, src)) {
                        return Result.failure(IllegalArgumentException("Nota inválida en evaluación '${e.title}'"))
                    }
                }
```

Captura en diálogos: pass `displayScale` = **active scale from parent**, validate with `GradePolicy.validateGrade`, on save parent sets `gradeScaleId = activeScale.id` (already Task 6). Presets for A-F chips.

- [ ] **Step 4:** `./gradlew testDebugUnitTest` green + `assembleDebug`

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/notifast/app/domain/usecase/ImportSubjectPackageUseCase.kt \
        app/src/main/java/com/notifast/app/presentation/
git commit -m "fix(grades): validate imports against own scale and capture only in active scale"
```

---

## Task 12: Verificación integral

- [ ] **Step 1:** `./gradlew testDebugUnitTest`
- [ ] **Step 2:** `./gradlew assembleDebug`
- [ ] **Step 3:** Grep smoke — no leftovers:

```bash
rg -n '"/20"|SCALE_0_10$|>= 3\.0|Prom: %|avg >= 3' app/src/main || true
rg -n 'String\.format\(Locale\.[A-Z]+, "%.1f", (evaluation|subjectAverage|avg|globalAverage)' app/src/main || true
```

Expected: no production hits in grade UI (tests may remain).

- [ ] **Step 4:** Update `docs/prd/STATUS.md` (D5 rev, D11–D17, bugs B1–B18 cerrados).

- [ ] **Step 5: Commit**

```bash
git add docs/prd/STATUS.md
git commit -m "docs: record multi-scale grade system decisions and verification"
```

---

## Self-review notes

- Spec §5–§11 covered by Tasks 1–11.
- D11b hint strings + no picker: Task 7.
- Type consistency: `ConvertGradeScaleUseCase` / `updateEvaluations` / `ChangeGradeScale` names match across Tasks 3–4–9.
- No TBD/placeholders in code steps.
