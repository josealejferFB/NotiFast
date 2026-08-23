# 🚀 Kickoff Oficial — Fase 3.2: Búsqueda Global Multientidad y Pack Académico P2P

> **Propósito:** Guía maestra de arranque y prompt copiable de sesión para ejecutar la **Fase 3.2 (Cierre de Fase 3: Sprints 3.3 y 3.4)** de NotiFast v3.0, tras haber completado exitosamente el motor de notificaciones proactivas (Sprint 3.1) y el dashboard predictivo en Canvas Jetpack Compose (Sprint 3.2).  
> **Referencias Obligatorias:** [`PRD-MAESTRO-NOTIFAST.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md), [`STATUS.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md), [`AGENTS.md`](file:///home/p0xd/Documentos/NotiFast/AGENTS.md), [`DESIGN.md`](file:///home/p0xd/Documentos/NotiFast/DESIGN.md) y [`PRODUCT.md`](file:///home/p0xd/Documentos/NotiFast/PRODUCT.md).

---

## 📋 1. Prompt Copiable para Iniciar la Sesión

Copia y pega el siguiente bloque para iniciar de inmediato el desarrollo en una nueva sesión:

```text
Actúa como Ingeniero de Software IA de Élite y Diseñador UX/UI de Producto en NotiFast. Vamos a ejecutar al 100% la Fase 3.2 (Cierre de Fase 3: Sprints 3.3 y 3.4 — Búsqueda Global Multientidad en Tiempo Real y Pack Académico P2P / Offline), siguiendo estrictamente docs/prd/KICKOFF_FASE_3.2.md, docs/prd/PRD-MAESTRO-NOTIFAST.md, docs/prd/STATUS.md, DESIGN.md y AGENTS.md.

Contexto previo:
- Sprint 3.1 (Motor de Notificaciones Proactivas con WorkManager, Workers, Receivers y UseCases de alerta) ya está 100% completado y testeado.
- Sprint 3.2 (Analíticas de Rendimiento Académico y Dashboard Predictivo en Canvas nativo Jetpack Compose) ya está 100% completado, integrado en GradesScreen y con pruebas unitarias pasando.
- Fase 3 se encuentra actualmente al 66% de progreso global.

Skills obligatorias requeridas:
- `codegraph`: Toda exploración de símbolos y blast radius DEBE hacerse con `codegraph_explore`.
- `systematic-debugging`: Aislamiento de causa raíz y trazabilidad matemática ante cualquier error.
- `test-driven-development`: Red ➔ Green ➔ Refactor estricto para UseCases de búsqueda, serialización P2P y ViewModels.
- `impeccable`: Diseño Tactile Studio 3.0 para el GlobalSearchBottomSheet y ShareSubjectBottomSheet con chips de filtro, monogramas HSL accesibles y micro-bordes.
- `verification-before-completion`: Ejecutar `./gradlew testDebugUnitTest` y `./gradlew assembleDebug` con evidencia antes de dar por terminada cualquier tarea.

Estándares de higiene y arquitectura no negociables:
1. Máximo ~200 líneas por archivo composable; modularizar sub-componentes en `components/`.
2. Cero strings hardcodeados en UI (100% en `res/values/strings.xml`).
3. Cero colores arbitrarios quemados (usar `MaterialTheme.colorScheme` y `ThemeUtils`).
4. MVI estricto: un solo `StateFlow<UiState>` por ViewModel y eventos vía `onEvent()`.
5. Preservar la barra inferior de navegación en 4 pestañas (`Apuntes`, `Agenda`, `Calificaciones`, `Ajustes`).

Plan de Trabajo a Ejecutar en esta sesión:
- Sprint 3.3 — Búsqueda Global Multientidad en Tiempo Real (SearchGlobalUseCase con debounce de 300ms, GlobalSearchBottomSheet modal accesible, SearchViewModel MVI, e integración con el botón de búsqueda en NotiFastTopBar).
- Sprint 3.4 — Compartir Materia ("Pack Académico"), Intercambio P2P y Resiliencia Free-Tier (ExportSubjectPackageUseCase / ImportSubjectPackageUseCase, compresión .notifast en Base64/GZIP, ShareSubjectBottomSheet y códigos QR offline).
- Protocolo de Cierre: Ejecutar suite de pruebas completa, verificar build con assembleDebug, actualizar docs/prd/STATUS.md (marcando Fase 3 al 100%) y generar el Kickoff de Fase 4 (Pulido, Accesibilidad a11y y Rendimiento).

Comienza revisando el estado actual con CodeGraph y arrancando con el diseño e implementación del Sprint 3.3.
```

---

## 🎯 2. Objetivos y Matriz de Transformación

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                          MATRIZ DE TRANSFORMACIÓN FASE 3.2                             │
├─────────────────────────┬──────────────────────────────────┬───────────────────────────┤
│ Módulo                  │ Estado Actual                    │ Transformación Fase 3.2   │
├─────────────────────────┼──────────────────────────────────┼───────────────────────────┤
│ 1. Búsqueda             │ Filtros locales separados por    │ Búsqueda global unificada │
│    Global (3.3)         │ pantalla (solo notas o materias) │ multientidad con debounce │
│                         │                                  │ (Apuntes, Horarios, Evals)│
├─────────────────────────┼──────────────────────────────────┼───────────────────────────┤
│ 2. Pack Académico       │ Asignaturas aisladas sin         │ "Pack Académico" P2P      │
│    y Social P2P (3.4)   │ mecanismo de respaldo o envío    │ (.notifast, QR y Nearby)  │
│                         │                                  │ para intercambio offline  │
└─────────────────────────┴──────────────────────────────────┴───────────────────────────┘
```

---

## 🛠️ 3. Desglose Detallado de Sprints Restantes

### 🔹 Sprint 3.3 — Búsqueda Global Multientidad en Tiempo Real

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **3.3.1** | `SearchGlobalUseCase.kt` | Use Case puro que ejecuta búsquedas reactivas con debounce de 300ms sobre: **1)** Materias (nombre), **2)** Apuntes (título, contenido, tags), **3)** Evaluaciones (título, descripción, tipo) y **4)** Horarios (profesor, salón, día de clase). Normaliza acentos y mayúsculas. |
| **3.3.2** | `SearchViewModel.kt` & `SearchContract.kt` | ViewModel MVI con StateFlow unificado que gestiona: query de búsqueda, chips de filtro (`TODOS`, `APUNTES`, `MATERIAS`, `EVALUACIONES`, `HORARIOS`), historial de búsquedas recientes y resultados agrupados. |
| **3.3.3** | `GlobalSearchBottomSheet.kt` | Modal táctil desplegable con campo de búsqueda pergamino, foco automático, chips interactivos de filtro y lista categorizada con monogramas HSL accesibles y badges de tipo. |
| **3.3.4** | Integración en `NotiFastTopBar` / Atajo | Al pulsar el icono de lupa en cualquier pantalla del Hub, despliega el modal de búsqueda y navega directamente al destino seleccionado (`SubjectDetailScreen`, `AddNote`, `EditEvaluation`, `Agenda`). |
| **3.3.5** | Tests de Búsqueda | Pruebas unitarias en `SearchGlobalUseCaseTest` y `SearchViewModelTest` validando debounce, normalización de caracteres especiales y filtrado por entidad. |

---

### 🔹 Sprint 3.4 — Compartir Materia ("Pack Académico"), Intercambio P2P y Resiliencia Free-Tier

| ID | Componente / Archivo | Criterio de Aceptación |
|---|---|---|
| **3.4.1** | `ExportSubjectPackageUseCase.kt` & `ImportSubjectPackageUseCase.kt` | Empaqueta una materia completa (datos básicos, horarios semanales, syllabus de evaluaciones con ponderaciones y notas clave) en un schema JSON `.notifast` comprimido en Base64/GZIP. Al importar, valida nombres duplicados y resuelve colisiones ofreciendo renombrar o sobrescribir. |
| **3.4.2** | `QrCodeShareDialog.kt` & `QrScannerBottomSheet.kt` | Generador de código QR comprimido para materias/horarios y lector óptico offline para transferencia instantánea en el aula sin internet y con 0 consumo de backend. |
| **3.4.3** | `ShareSubjectBottomSheet.kt` | Modal táctil en `SubjectDetailScreen` con opciones de: **1)** *Exportar Archivo `.notifast` vía Android Sharesheet*, **2)** *Mostrar Código QR en Pantalla*, **3)** *Copiar Resumen de Materia al Portapapeles*. |
| **3.4.4** | Tests de Importación/Exportación | Pruebas unitarias de serialización/deserialización, validación de integridad de datos y resolución de colisiones de nombres. |

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

## 🏁 5. Protocolo de Cierre de Fase 3 y Transición a Fase 4

Al completar los Sprints 3.3 y 3.4 al 100%:
1. Ejecutar `./gradlew testDebugUnitTest` y `./gradlew assembleDebug`.
2. Actualizar [`docs/prd/STATUS.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md) marcando Fase 3 al 100%.
3. Generar automáticamente [`docs/prd/KICKOFF_FASE_4.md`](file:///home/p0xd/Documentos/NotiFast/docs/prd/KICKOFF_FASE_4.md) (Pulido de Micro-animaciones, Accesibilidad Universal WCAG AAA y Optimización de Rendimiento Compose).
