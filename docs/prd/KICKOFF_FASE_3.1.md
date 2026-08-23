# 🚀 Kickoff Oficial — Fase 3.1: Inteligencia Predictiva, Búsqueda Global y Pack Académico P2P

> **Propósito:** Guía maestra de arranque y prompt copiable de sesión para ejecutar la **Fase 3.1 (Continuación de Fase 3: Sprints 3.2, 3.3 y 3.4)** de NotiFast v3.0, tras haber completado exitosamente el motor de notificaciones proactivas con WorkManager (Sprint 3.1).  
> **Referencias Obligatorias:** [`PRD-MAESTRO-NOTIFAST.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md), [`STATUS.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md), [`AGENTS.md`](file:///home/p0xd/Documentos/NotiFast/AGENTS.md), [`DESIGN.md`](file:///home/p0xd/Documentos/NotiFast/DESIGN.md) y [`PRODUCT.md`](file:///home/p0xd/Documentos/NotiFast/PRODUCT.md).

---

## 📋 1. Prompt Copiable para Iniciar la Sesión

Copia y pega el siguiente bloque para iniciar de inmediato el desarrollo en una nueva sesión:

```text
Actúa como Ingeniero de Software IA de Élite y Diseñador UX/UI de Producto en NotiFast. Vamos a ejecutar al 100% la Fase 3.1 (Continuación de Fase 3: Sprints 3.2, 3.3 y 3.4 — Inteligencia Predictiva, Búsqueda Global Multientidad y Pack Académico P2P), siguiendo estrictamente docs/prd/KICKOFF_FASE_3.1.md, docs/prd/PRD-MAESTRO-NOTIFAST.md, docs/prd/STATUS.md, DESIGN.md y AGENTS.md.

Contexto previo:
- El Sprint 3.1 (Motor de Notificaciones Proactivas con WorkManager, Workers, Receivers y UseCases de alerta) ya está 100% completado, testeado y compilado.
- El diseño y plan del Sprint 3.2 ya están especificados en:
  * docs/superpowers/specs/2026-08-23-sprint-3.2-academic-analytics-predictive-dashboard-design.md
  * docs/superpowers/plans/2026-08-23-sprint-3.2-academic-analytics-predictive-dashboard.md

Skills obligatorias requeridas:
- `codegraph`: Toda exploración de símbolos y blast radius DEBE hacerse con `codegraph_explore`.
- `systematic-debugging`: Aislamiento de causa raíz y trazabilidad matemática ante cualquier error.
- `test-driven-development`: Red ➔ Green ➔ Refactor estricto para UseCases de dominio, algoritmos estadísticos y ViewModels.
- `impeccable`: Gráficos en Canvas nativo Jetpack Compose, tipografía monoespaciada académica (JetBrains Mono) y tokens Tactile Studio.
- `verification-before-completion`: Ejecutar `./gradlew testDebugUnitTest` y `./gradlew assembleDebug` con evidencia antes de dar por terminada cualquier tarea.

Estándares de higiene y arquitectura no negociables:
1. Máximo ~200 líneas por archivo composable; modularizar componentes en `components/`.
2. Cero strings hardcodeados en UI (100% en `res/values/strings.xml`).
3. Cero colores arbitrarios quemados (usar `MaterialTheme.colorScheme` y `ThemeUtils`).
4. MVI estricto: un solo `StateFlow<UiState>` por ViewModel y eventos vía `onEvent()`.
5. Preservar la barra inferior de navegación en 4 pestañas (`Apuntes`, `Agenda`, `Calificaciones`, `Ajustes`).

Plan de Trabajo a Ejecutar en esta sesión:
- Sprint 3.2 — Analíticas de Rendimiento Académico y Dashboard Predictivo (CalculateAcademicStatsUseCase, AcademicProgressChart en Canvas, GradeDistributionHistogram, AcademicEarlyWarningCard, tab switcher en GradesScreen).
- Sprint 3.3 — Búsqueda Global Multientidad en Tiempo Real (SearchGlobalUseCase con debounce de 300ms, GlobalSearchBottomSheet, categorización multientidad).
- Sprint 3.4 — Pack Académico P2P, Intercambio Offline y Resiliencia Free-Tier (ExportSubjectPackageUseCase / ImportSubjectPackageUseCase, compresión .notifast, códigos QR y Nearby P2P offline).
- Protocolo de Cierre: Ejecutar suite de pruebas completa, verificar build con assembleDebug, actualizar docs/prd/STATUS.md y generar el Kickoff de Fase 4.

Comienza revisando el estado actual con CodeGraph y arrancando con la ejecución del Sprint 3.2.
```

---

## 🎯 2. Objetivos y Matriz de Transformación

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                          MATRIZ DE TRANSFORMACIÓN FASE 3.1                             │
├─────────────────────────┬──────────────────────────────────┬───────────────────────────┤
│ Módulo                  │ Estado Actual                    │ Transformación Fase 3.1   │
├─────────────────────────┼──────────────────────────────────┼───────────────────────────┤
│ 1. Analíticas           │ Promedio ponderado básico y      │ Dashboard predictivo con  │
│    Predictivas (3.2)    │ simulador aislado en materia     │ gráficos Canvas, alertas  │
│                         │                                  │ de riesgo y correlación   │
├─────────────────────────┼──────────────────────────────────┼───────────────────────────┤
│ 2. Búsqueda             │ Filtros locales separados por    │ Búsqueda global unificada │
│    Global (3.3)         │ pantalla (solo notas o materias) │ multientidad con debounce │
├─────────────────────────┼──────────────────────────────────┼───────────────────────────┤
│ 3. Pack Académico       │ Asignaturas aisladas sin         │ "Pack Académico" P2P      │
│    y Social P2P (3.4)   │ mecanismo de respaldo o envío    │ (.notifast, QR y Nearby)  │
└─────────────────────────┴──────────────────────────────────┴───────────────────────────┘
```

---

## 🛠️ 3. Desglose Detallado de Sprints Restantes

### 🔹 Sprint 3.2 — Analíticas de Rendimiento Académico y Dashboard Predictivo

> **Spec & Plan ya preparados:**
> - Spec: [`docs/superpowers/specs/2026-08-23-sprint-3.2-academic-analytics-predictive-dashboard-design.md`](file:///home/p0xd/Documentos/NotiFast/docs/superpowers/specs/2026-08-23-sprint-3.2-academic-analytics-predictive-dashboard-design.md)
> - Plan: [`docs/superpowers/plans/2026-08-23-sprint-3.2-academic-analytics-predictive-dashboard.md`](file:///home/p0xd/Documentos/NotiFast/docs/superpowers/plans/2026-08-23-sprint-3.2-academic-analytics-predictive-dashboard.md)

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **3.2.1** | `CalculateAcademicStatsUseCase.kt` | Use Case puro que computa: total de créditos/materias aprobadas vs en curso, tasa de aprobación proyectada (basada en notas actuales y peso restante), materia con mayor/menor rendimiento, correlación de carga horaria semanal vs calificaciones, y lista de materias en riesgo crítico ($< 11.0$ o nota mínima configurada). |
| **3.2.2** | `AcademicProgressChart.kt` | Componente gráfico declarativo en **Canvas puro / Jetpack Compose** con barras comparativas estilizadas con la paleta de materias y fuentes `JetBrains Mono`. |
| **3.2.3** | `GradeDistributionHistogram.kt` | Histograma de distribución de notas agrupadas por 4 rangos (Sobresaliente, Bueno, Regular, En Riesgo) con tokens de diseño Tactile Studio. |
| **3.2.4** | `AcademicAnalyticsContent.kt` | Contenedor Bento modular (< 200 LOC) con tarjetas de diagnóstico temprano (`AcademicEarlyWarningCard`), correlación de carga horaria (`AcademicWorkloadCard`) e insights de estudio. |
| **3.2.5** | Conmutador en `GradesScreen.kt` | Integración ergonómica en `GradesScreen` manteniendo SRP ($\le 150$ LOC) mediante conmutador segmentado táctil `[ Resumen Materias | Analíticas & Proyecciones ]`. |
| **3.2.6** | Tests de Analíticas | Pruebas unitarias exhaustivas en `CalculateAcademicStatsUseCaseTest` y `GradesViewModelTest` para casos de borde (sin materias, materias sin notas, 100% aprobadas, escala personalizada 0-10 y 0-100). |

---

### 🔹 Sprint 3.3 — Búsqueda Global Multientidad en Tiempo Real

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **3.3.1** | `SearchGlobalUseCase.kt` | Use Case puro que ejecuta búsquedas concurrentes en memoria y repositorios con debounce de 300ms sobre Materias, Apuntes (título, contenido y tags), Evaluaciones (nombre y tipo) y Horarios (salón, profesor, día). |
| **3.3.2** | `SearchViewModel.kt` & `SearchContract.kt` | ViewModel MVI con StateFlow unificado que gestiona query, histórico de búsquedas recientes, filtros rápidos por entidad y estados vacío/cargando. |
| **3.3.3** | `GlobalSearchBottomSheet.kt` | Sheet táctil modal de búsqueda rápida con campo de texto pergamino, chips de filtro (`Todos`, `Apuntes`, `Materias`, `Evaluaciones`, `Horarios`) y resultados categorizados con monogramas y fechas. |
| **3.3.4** | Integración TopBar / Atajo | Al pulsar el icono de lupa en `NotiFastTopBar`, despliega el modal de búsqueda y navega instantáneamente al destino seleccionado (`SubjectDetailScreen`, `EditNote`, `EditEvaluation`, `Agenda`). |
| **3.3.5** | Tests de Búsqueda | Pruebas unitarias para `SearchGlobalUseCaseTest` y `SearchViewModelTest` validando debounce, normalización de acentos/mayúsculas y ordenamiento por relevancia. |

---

### 🔹 Sprint 3.4 — Compartir Materia ("Pack Académico"), Intercambio P2P y Resiliencia Free-Tier

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **3.4.1** | `ExportSubjectPackageUseCase.kt` & `ImportSubjectPackageUseCase.kt` | Empaqueta una materia completa (datos básicos, horarios semanales, syllabus de evaluaciones con ponderaciones y apuntes clave en texto/markdown) en un schema JSON compacto `.notifast` comprimido en Base64/GZIP. Al importar, valida nombres duplicados y ofrece fusionar o crear nueva materia con 0 esfuerzo. |
| **3.4.2** | `NearbyShareManager.kt` (P2P Android Nearby) | Integración con Google Play Services Nearby Connections API para descubrimiento y transferencia directa dispositivo a dispositivo en el aula sin internet y con 0 consumo de Firebase. |
| **3.4.3** | `QrCodeShareDialog.kt` & `QrScannerBottomSheet.kt` | Generador de código QR comprimido para materias/horarios y escáner de cámara integrado para transferencia óptica instantánea en persona (100% offline). |
| **3.4.4** | `ShareSubjectBottomSheet.kt` | Modal táctil en `SubjectDetailScreen` con 4 canales de distribución: **1)** *Quick Share / Nearby P2P*, **2)** *Código QR en Pantalla*, **3)** *Exportar Archivo `.notifast` vía Sharesheet*, **4)** *Enlace en la Nube con TTL de 7 días*. |
| **3.4.5** | Tests de Importación/Exportación | Pruebas unitarias de serialización/deserialización, validación de integridad de datos y resolución de colisiones de nombres. |

---

## 🧭 4. Entorno de Desarrollo y Comandos de Verificación

```bash
# Variables de entorno requeridas en Linux:
export JAVA_HOME=/home/p0xd/.jdks/temurin-17
export PATH=$JAVA_HOME/bin:$PATH

# Ejecutar suite de pruebas unitarias:
./gradlew testDebugUnitTest

# Compilación completa de APK debug:
./gradlew assembleDebug
```

---

## 🏁 5. Protocolo de Cierre de Fase 3

Al completar los Sprints 3.2, 3.3 y 3.4 al 100%:
1. Ejecutar `./gradlew testDebugUnitTest` y `./gradlew assembleDebug`.
2. Actualizar [`docs/prd/STATUS.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md) marcando Fase 3 al 100%.
3. Generar automáticamente [`docs/prd/KICKOFF_FASE_4.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_4.md) (Pulido, Accesibilidad a11y y Rendimiento).
