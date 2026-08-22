# 🚀 Kickoff y Guía de Inicio — Fase 1: Fundamentos de Arquitectura

> **Propósito:** Documento de arranque rápido para la sesión de desarrollo de la Fase 1.  
> **Ubicación:** `docs/prd/KICKOFF_FASE_1.md`  
> **Referencias:**
> - [PRD Maestro](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md)
> - [Estado del Roadmap](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md)
> - [Guía de Reglas de Agente (AGENTS.md)](file:///home/p0xd/Documentos/NotiFast/AGENTS.md)

---

## 📋 Prompt para Copiar y Pegar en la Siguiente Sesión

```markdown
Hola. Vamos a iniciar la ejecución de la **Fase 1: Fundamentos de Arquitectura** del PRD Maestro de NotiFast v2.0.

Por favor, sigue estrictamente las siguientes directivas antes de tocar cualquier archivo:
1. Lee y asimila los documentos en `docs/prd/PRD-MAESTRO-NOTIFAST.md`, `docs/prd/STATUS.md` y `AGENTS.md`.
2. Activa y sigue los protocolos de las siguientes skills:
   - `test-driven-development` (escribir pruebas unitarias que fallen antes de implementar Use Cases y ViewModels).
   - `systematic-debugging` (aislar causa raíz ante cualquier fallo en corrutinas o inyección).
   - `verification-before-completion` (compilar con `./gradlew testDebugUnitTest` y `./gradlew assembleDebug` antes de dar por terminada una tarea).
   - Uso obligatorio de CodeGraph (`codegraph_explore`) para navegar símbolos en lugar de lecturas ciegas.
   - `impeccable` para el refactor y pulido visual del tema corporativo Ruby & Aqua.
3. Aplica de forma obligatoria el estándar de **Calidad de Código y Limpieza de Anti-Patrones**:
   - **KISS & YAGNI:** Soluciones directas y simples; cero sobre-ingeniería ni abstracciones especulativas.
   - **Límite de tamaño:** Máximo ~250 líneas por archivo composable. Dividir pantallas monolíticas restantes (`SubjectCard`, `CalendarScreen`, `GradesScreen`, `AddEvaluationScreen`).
   - **Tokens de Diseño:** Refactorizar paleta Material 3 con Ruby (`#BF092F`) y Aqua (`#3B9797`) soportando Dark Mode real.
   - **Cero Strings Hardcodeados:** Todo texto visible para el usuario debe usar `stringResource(R.string.xxx)`.
   - **SRP y Clean Architecture:** Toda lógica compuesta va en Use Cases en `domain/usecase/`.
   - **Manejo de Errores:** Operaciones de repositorio retornan `Result<T>` y propagan errores al `UiState`.
4. Ejecuta de forma secuencial y quirúrgica las tareas de la Fase 1:
   - **Sprint 1.1 (Arquitectura y Refactor de Dominio/UI):**
     * 1.1.1: Capa de Use Cases en `domain/usecase/` para operaciones compuestas (ej. `DeleteSubjectCascadeUseCase`, `GetNotesWithSubjectColorUseCase`).
     * 1.1.2: Refactorizar Material 3 Theme con paleta corporativa `Ruby` & `Aqua` + Dark Mode real.
     * 1.1.3: Actualizar Compose BOM y version catalog a versiones estables.
     * 1.1.4: Modularizar y dividir pantallas monolíticas restantes (>250 LOC).
     * 1.1.5: Crear `UserPrefsRepository` con DataStore para configuraciones del estudiante.
     * 1.1.6: Limpieza de firmas de métodos reactivos (`Flow` vs `suspend`).
   - **Sprint 1.2 (Arnés de Pruebas Unitarias e Integración):**
     * 1.2.1: Consolidar infraestructura de tests (MockK, Turbine, Coroutines Test).
     * 1.2.2: Tests unitarios para todos los Use Cases nuevos y existentes.
     * 1.2.3: Tests unitarios para ViewModels core (`NotesViewModel`, `GradesViewModel`, `CalendarViewModel`, `LoginViewModel`).
     * 1.2.4: Tests de integración con emuladores/mocks de repositorios.
5. Tras completar cada tarea, ejecuta la verificación con `./gradlew testDebugUnitTest` y actualiza `docs/prd/STATUS.md`. Al completar el 100%, genera automáticamente `docs/prd/KICKOFF_FASE_2.md`.
```

---

## 🧹 Buenas Prácticas y Pautas de Calidad para Fase 1

### 1. Extracción a Use Cases en `domain/usecase/`
- **Pauta:** Las pantallas y ViewModels no deben coordinar múltiples repositorios directamente. Toda operación de negocio compuesta (como borrado en cascada, validación de solapes de horarios o filtrado inteligente) debe encapsularse en un `UseCase` testeable con `operator fun invoke(...)`.

### 2. Sistema de Diseño Material 3 y Dark Mode
- **Pauta:** Centralizar la definición del tema en `presentation/theme/Theme.kt`, `Color.kt`, `Type.kt` y `Shape.kt`. Soporte completo de Dark Theme (`SurfaceDark = #1B262C`, `OnSurfaceDark = #E6E1E5`) sin componentes con fondos quemados en blanco.

### 3. Cobertura de Tests (TDD Estricto)
- **Pauta:** Cada nuevo `UseCase` y ViewModel debe contar con su correspondiente clase de prueba en `app/src/test/java/...` utilizando MockK y Turbine para observar `StateFlow`.

---

## 🛠️ Matriz de Skills Requeridas para Fase 1

| Skill | Cuándo y Cómo Usarla |
|---|---|
| **CodeGraph** (`codegraph_explore`) | **Obligatorio** antes de abrir o editar cualquier archivo para mapear callers y dependencias. |
| **Test-Driven Development** (`test-driven-development`) | Ciclo Red-Green-Refactor al construir los Use Cases en `domain/usecase/` y ViewModels. |
| **Impeccable** (`impeccable`) | Al rediseñar y pulir el tema corporativo `Ruby` / `Aqua` y Dark Mode en `presentation/theme/`. |
| **Verification Before Completion** (`verification-before-completion`) | Ejecutar `./gradlew testDebugUnitTest` y `./gradlew assembleDebug` antes de cerrar cada sub-tarea. |
| **Executing Plans** (`executing-plans`) | Mantener el checklist actualizado en `docs/prd/STATUS.md`. |

---

## 🎯 Lista Detallada de Tareas en Fase 1 (10 Tareas)

### Bloque A: Sprint 1.1 (Arquitectura y Dominio)
1. **1.1.1 — Capa de Use Cases:**
   - Crear `DeleteSubjectCascadeUseCase`, `GetSubjectDetailUseCase`, `GetNotesWithSubjectInfoUseCase`.
2. **1.1.2 — Refactor Material 3 Theme:**
   - Actualizar `Theme.kt` para conmutar dinámicamente entre Light y Dark schemes con contraste óptimo.
3. **1.1.3 — Actualización de Version Catalog:**
   - Auditar `libs.versions.toml` manteniendo versiones estables compatibles con Kotlin 2.0.21 y Gradle 9.
4. **1.1.4 — Modularización de Pantallas (>250 LOC):**
   - Subdividir `SubjectCard.kt`, `AddEvaluationScreen.kt`, `EditEvaluationScreen.kt`, `CalendarScreen.kt`.
5. **1.1.5 — `UserPrefsRepository`:**
   - Implementar persistencia de preferencias de usuario con `androidx.datastore:datastore-preferences`.
6. **1.1.6 — Limpieza de Firmas Reactivas:**
   - Asegurar que consultas continuas retornen `Flow<T>` y escrituras/cargas puntuales sean `suspend fun ...: Result<T>`.

### Bloque B: Sprint 1.2 (Testing Automatizado)
7. **1.2.1 — Infraestructura de Testing:**
   - Verificar `MainDispatcherRule` y utilidades de test con Coroutines y Turbine.
8. **1.2.2 — Unit Tests para Use Cases:**
   - Tests unitarios exhaustivos para todos los Use Cases en `domain/usecase/`.
9. **1.2.3 — Unit Tests para ViewModels:**
   - Tests para `NotesViewModelTest`, `GradesViewModelTest`, `CalendarViewModelTest`, `LoginViewModelTest`.
10. **1.2.4 — Tests de Integración:**
    - Validar flujos de interacción repositorio-viewmodel con mocks de Firestore.

---

## 🛡️ Reglas de Oro de Ejecución
- **Clean Architecture estricta:** `data/` implementa interfaces de `domain/`; `presentation/` consume `domain/`.
- **KISS & YAGNI:** Implementar solo la lógica necesaria para los casos de uso reales del estudiante.
- **≤ 250 líneas por archivo composable.**
- **Preservar el idioma:** Código en inglés, UI en español.
