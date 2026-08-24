# 🚀 KICKOFF — FASE 4: Pulido de Experiencia, Accesibilidad Total (a11y) y Optimización de Rendimiento

> **Documento de Arranque y Prompt Maestro para Agentes AI en NotiFast**  
> **Objetivo:** Ejecutar de punta a punta la **Fase 4 del PRD Maestro**, llevando la aplicación al estándar AAA definitivo de calidad de software: transiciones de navegación fluidas con View Transitions en Compose, soporte integral de accesibilidad (TalkBack, escala dinámica de fuentes, áreas táctiles $\ge 48\text{dp}$), auditoría de recomposición y optimización de memoria.

---

## 📋 PROMPT COPIABLE PARA INICIAR LA SESIÓN

```text
Actúa como un Ingeniero de Software Android Senior de Élite especializado en Jetpack Compose, Rendimiento y Accesibilidad (a11y).
Inicia y ejecuta la FASE 4 (Pulido de Experiencia, Accesibilidad y Rendimiento) de NotiFast siguiendo estrictamente las directivas de:
- docs/prd/PRD-MAESTRO-NOTIFAST.md
- docs/prd/STATUS.md
- AGENTS.md
- DESIGN.md y PRODUCT.md

Invoca y aplica de forma obligatoria las siguientes skills y superpoderes:
1. codegraph: Para exploración estructural y grafo de dependencias en 1 sola llamada sin lecturas ciegas.
2. systematic-debugging: Para aislar y resolver cualquier fallo o recomposición innecesaria antes de parchar.
3. test-driven-development: Para verificar cada caso de uso o regla de negocio bajo ciclo Red -> Green -> Refactor.
4. verification-before-completion: No dar ninguna tarea por terminada sin ejecutar `./gradlew testDebugUnitTest` y `./gradlew assembleDebug`.
5. impeccable: Para auditar y pulir micro-interacciones, accesibilidad (a11y) y contraste WCAG AAA.

REGLAS DE HIGIENE Y ARQUITECTURA:
- Clean Architecture estricta (Data, Domain, Presentation).
- Límite estricto de ~200 líneas por composable (modularizar en components/ o atoms/).
- Un solo StateFlow por ViewModel (MVI Contract).
- Cero strings quemados (todo en res/values/strings.xml).
- Cero colores arbitrarios hardcodeados (usar MaterialTheme.colorScheme y ThemeUtils).
- Cero excepciones tragadas con catch vacío (usar Result<T> y feedback visual).
- Preservar el idioma: código en inglés, UI en español.

ALCANCE ESPECÍFICO DE LA FASE 4:
1. Sprint 4.1: Transiciones de Navegación y Micro-Interacciones
   - AnimatedNavHost con Shared Element Transitions o slide/fade personalizados tipo Studio Notebook.
   - Micro-animaciones en tarjetas, pulsaciones elásticas con spring physics (0.97f) y feedback háptico consistente.
2. Sprint 4.2: Accesibilidad Universal (a11y) y Modo Alto Contraste
   - Content descriptions exhaustivos y significativos para TalkBack en todos los átomos y pantallas.
   - Soporte de Dynamic Type (escalado de fuentes 1.5x y 2.0x sin desbordamientos de layout).
   - Verificación de áreas de toque mínimas (minTouchTargetSize = 48dp).
3. Sprint 4.3: Optimización de Rendimiento y Memoria
   - Auditoría de recomposiciones en Compose con Layout Inspector / métricas estables (`@Immutable`, `@Stable`, `derivedStateOf`, lambdas estables).
   - Optimización de carga de imágenes en Coil con caché en disco y memoria.
   - Auditoría de fugas de memoria con LeakCanary / Profiler.

AL COMPLETAR EL 100% DE LA FASE 4:
- Verificar que el 100% de los tests pasen (`testDebugUnitTest`).
- Compilar el APK exitosamente (`assembleDebug`).
- Actualizar docs/prd/STATUS.md marcando la Fase 4 al 100%.
- Generar docs/prd/KICKOFF_FASE_5.md para la fase final de despliegue y tesis.
```

---

## 🎯 1. Contexto de la Fase 4 en el Roadmap

Habiendo concluido exitosamente las Fases 0, 1, 2 y 3 (cubriendo estabilidad, arquitectura, hub core, notificaciones WorkManager, analíticas predictivas, búsqueda global y pack académico P2P), la **Fase 4** se enfoca en el **refinamiento sensorial, la inclusión y la robustez técnica**:

```
┌─────────────────────────────────────────────────────────────┐
│ FASE 0: Estabilización & Cero Bugs                 [100% ✅] │
├─────────────────────────────────────────────────────────────┤
│ FASE 1: Fundamentos de Arquitectura & Clean Arch   [100% ✅] │
├─────────────────────────────────────────────────────────────┤
│ FASE 2: Experiencia Core & Hub de Cuaderno         [100% ✅] │
├─────────────────────────────────────────────────────────────┤
│ FASE 3: Notificaciones, Analíticas, Búsqueda & P2P [100% ✅] │
├─────────────────────────────────────────────────────────────┤
│ FASE 4: Pulido de Experiencia, a11y & Rendimiento  [ 0% ⚪]  │ ◄ ESTA FASE
├─────────────────────────────────────────────────────────────┤
│ FASE 5: Despliegue, Métricas de Tesis & Play Store [ 0% ⚪]  │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ 2. Matriz de Skills Requeridas

| Momento / Tarea | Skill Requerida | Propósito Operativo |
|---|---|---|
| Exploración de Composables y Modificadores | `codegraph` | Mapear jerarquías de UI, `NavHost`, atoms y callers sin lecturas masivas de archivos. |
| Detección de Lag o Fugas de Memoria | `systematic-debugging` | Formular hipótesis, trazar recomposiciones de Compose y corregir asignaciones redundantes en composición. |
| Tests de Accesibilidad y Semántica | `test-driven-development` | Escribir tests de semántica Compose (`ComposeTestRule`) y validar descripciones y acciones accesibles. |
| Auditoría Heurística y Pulido Táctil | `impeccable` | Evaluar ritmo visual, micro-interacciones, escala tipográfica y contraste WCAG AAA. |
| Verificación de Suite y Build | `verification-before-completion` | Ejecutar `./gradlew testDebugUnitTest` y `./gradlew assembleDebug` antes de dar por cerrada cualquier tarea. |

---

## 📋 3. Desglose de Tareas y Criterios de Aceptación

### 🎬 Sprint 4.1: Transiciones de Navegación y Micro-Interacciones
- **Transiciones de Pantalla Cohesivas:**
  - Configurar en `NavGraph.kt` transiciones de entrada y salida personalizadas (`slideInHorizontally` + `fadeIn` / `slideOutHorizontally` + `fadeOut`) con curvas elásticas `spring(dampingRatio = 0.82f, stiffness = 400f)`.
- **Efectos de Presión Táctil Unificados:**
  - Asegurar que todas las tarjetas (`NotiFastCard`, `NoteEstheticCard`, `SubjectCard`, `SearchResultCard`) apliquen la física `scale(0.97f)` al presionar y haptic feedback leve.

### ♿ Sprint 4.2: Accesibilidad Universal (a11y) y Modo de Alto Contraste
- **Semántica y TalkBack:**
  - Cada botón con icono debe tener un `contentDescription` descriptivo en español o `Modifier.semantics { contentDescription = "..." }`.
  - Los gráficos en Canvas nativo (`AcademicProgressChart`, `GradeDistributionHistogram`) deben exponer texto semántico accesible para lectores de pantalla.
- **Escalabilidad Tipográfica (Dynamic Text):**
  - Probar las pantallas en densidades de texto $1.3\times$ a $2.0\times$ garantizando que ningún botón, badge o título quede cortado o superpuesto.
- **Áreas Táctiles Mínimas:**
  - Garantizar $\ge 48\times 48\,\text{dp}$ en todos los targets interactivos (icon buttons, chips, tabs, switches).

### ⚡ Sprint 4.3: Optimización de Rendimiento y Memoria
- **Estabilidad de Recomposición en Compose:**
  - Asegurar que todos los data classes de UiState usen colecciones inmutables (`PersistentList` o listas estables) y que los lambdas pasados a composables usen `rememberUpdatedState` o method references.
  - Usar `derivedStateOf` para cálculos basados en estados mutables durante scroll.
- **Optimización de Recursos y Memoria:**
  - Configurar límites de memoria y disco en Coil ImageLoader (`AppModule.kt`).
  - Verificar que no haya corrutinas huérfanas en ViewModels o StateFlows sin cancelar.

---

## 🏆 4. Reglas de Oro de Ejecución

1. **Clean Architecture Intacta:** No mezclar llamadas a SDKs o lógica de presentación dentro de repositorios o use cases.
2. **SRP & Máximo ~200 LOC:** Dividir composables grandes en sub-composables modulares bajo `components/`.
3. **Cero Hardcoding:** Usar tokens de `MaterialTheme.colorScheme` y `strings.xml`.
4. **Verificación Continua:** Cada sprint debe validar compilación y pruebas con `./gradlew testDebugUnitTest` y `./gradlew assembleDebug`.
