# 🚀 Kickoff y Guía de Inicio — Fase 0.1: Hardening UX & Sanity Checks

> **Propósito:** Sesión de estabilización intermedia para ejecutar pruebas destructivas ("prueba de tontos"), validación de backstack, prevención de acciones accidentales y depuración sistemática en dispositivo real.  
> **Ubicación:** `docs/prd/KICKOFF_FASE_0_1.md`  
> **Referencias:**
> - [PRD Maestro](file:///home/p0xd/Documentos/NotiFast/docs/prd/PRD-MAESTRO-NOTIFAST.md)
> - [Estado del Roadmap](file:///home/p0xd/Documentos/NotiFast/docs/prd/STATUS.md)
> - [Guía de Reglas de Agente (AGENTS.md)](file:///home/p0xd/Documentos/NotiFast/AGENTS.md)

---

## 📋 Master Prompt para Copiar y Pegar en la Siguiente Sesión

```markdown
Hola. Vamos a iniciar la sesión de **Fase 0.1: Hardening UX, Sanity Checks y Resiliencia en Dispositivo Real** para NotiFast v2.0.

### 🎯 Contexto y Diagnóstico Previo:
- En la sesión anterior se estabilizó la Fase 0 y se identificó/resolvió el crash de arranque en `LoginScreen` (causado por `GoogleSignInOptions.requestIdToken` con `WEB_CLIENT_ID` vacío).
- La app ya compila y corre en el dispositivo físico (`2511FPC34G`), pero debemos realizar una ronda exhaustiva de **Systematic Debugging** y "prueba de tontos" (monkey/chaos testing) antes de iniciar la Fase 1.

### 🛡️ Directivas Operativas Obligatorias:
1. Lee y asimila los documentos en `docs/prd/PRD-MAESTRO-NOTIFAST.md`, `docs/prd/STATUS.md` y `AGENTS.md`.
2. Activa y sigue los protocolos de las siguientes skills:
   - `systematic-debugging` (para investigar cualquier error en logcat de Android con `adb logcat` y aislar causa raíz antes de parchar).
   - `test-driven-development` (crear pruebas unitarias para cualquier regla de validación o cálculo que falle).
   - `verification-before-completion` (compilar con `./gradlew testDebugUnitTest` y probar en dispositivo real con `adb install`).
   - Uso obligatorio de CodeGraph (`codegraph_explore`) para navegar símbolos.
3. Aplica los estándares de higiene:
   - KISS & YAGNI: Soluciones quirúrgicas y limpias.
   - ≤ 250 LOC por composable.
   - Tokens de color corporativo (`Ruby` & `Aqua`) y `stringResource(R.string.xxx)`.

### 📋 Lista de Verificación y Tareas de Hardening en Fase 0.1:
1. **Acciones Destructivas e Irreversibles (Protección contra Toques Accidentales):**
   - Verificar y blindar que borrar notas, materias, horarios o evaluaciones SIEMPRE requiera confirmación explícita con `NotiFastDialog`.
   - Confirmación de cierre de sesión en `Settings` / `EditProfile` para evitar logout por accidente.
2. **Navegación y Backstack ("Prueba del Botón Atrás"):**
   - Asegurar que presionar 'Atrás' en la pantalla principal salga de la app en vez de regresar a Login o ciclar en el backstack.
   - Validar que pulsar repetidamente la barra de navegación inferior no acumule entradas duplicadas (`launchSingleTop`).
   - Agregar confirmación de "descartar cambios" (`BackHandler`) si el usuario está redactando un apunte o evaluación y presiona 'Atrás'.
3. **Input Fuzzing y Casos Límite:**
   - Prevenir nombres de materias vacíos o de puros espacios en blanco.
   - Validar materias duplicadas con feedback visual claro.
   - Proteger campos de entrada de peso (`weight`) y notas (`grade`) contra valores negativos, caracteres no numéricos o `NaN`.
   - Búsqueda resiliente con caracteres especiales en `NoteSearchBar`.
4. **Resiliencia de Ciclo de Vida y Hardware:**
   - Rotación de pantalla sin pérdida de estado en formularios activos.
   - Retorno desde background / selector de imágenes de galería sin recreaciones abruptas.
   - Soporte y legibilidad de textos en Modo Oscuro (Dark Theme).

Por favor, revisa el estado actual del dispositivo con `adb devices`, consulta el logcat si se reporta alguna anomalía, y procede a ejecutar las correcciones sistemáticas que sean necesarias.
```

---

## 🔍 Incidentes Clave Resueltos en la Transición
- **Crash de Inicio (`IllegalArgumentException`):** `GoogleSignInOptions.Builder` arrojaba excepción cuando `BuildConfig.WEB_CLIENT_ID` estaba vacío o no configurado en `local.properties`. Corregido condicionando `requestIdToken` únicamente si el string no está en blanco.
- **Modularización de NotesScreen:** Archivo descompuesto en sub-componentes modulares (`NotesTabContent`, `SubjectsTabContent`, `NoteDetailExpandable`) cumpliendo con el límite de ~250 LOC.
- **Parseo de Colores:** Centralizado en `String?.toSafeComposeColor()` con 9 pruebas unitarias automatizadas.

---

## 🛠️ Matriz de Pruebas de Campo para la Sesión 0.1

| Área | Escenario a Validar | Resultado Esperado |
|---|---|---|
| **Destructivas** | Toque en papelera de apunte | Diálogo `NotiFastDialog` modal, cancelación limpia y borrado atómico |
| **Destructivas** | Borrado de materia con hijos | Advertencia explícita de eliminación en cascada de apuntes/evaluaciones |
| **Navegación** | 10 clics en BottomNav | Un solo toque en 'Atrás' sale de la app (`launchSingleTop = true`) |
| **Navegación** | Atrás post-login | App pasa a background o sale, nunca reabre el formulario de login |
| **Fuzzing** | Guardar materia `"   "` | Botón deshabilitado o error de validación visual |
| **Fuzzing** | Peso de examen `"abc"` o `"-5"` | Input no permite caracteres inválidos |
| **Hardware** | Rotar pantalla durante edición | Formulario conserva el texto escrito |
| **Hardware** | Cambiar a Modo Oscuro | Contraste legible según tokens Slate y Ruby/Aqua |
