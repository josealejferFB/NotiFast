# Roadmap y Estado de Ejecución: NotiFast

> **Documento de Control y Seguimiento del Ciclo de Vida del Proyecto**  
> **Última Actualización:** Octubre 2026 | **Versión:** 3.2.0  
> **Estado Global:** 🟢 Fases 0 a 4 Completadas (99%) · Fase 5 en Ejecución Activa

---

## 1. Tablero de Progreso Global

```
Fase 0: Estabilización & Auditoría Inicial      [████████████████] 100% (16/16 tareas) ✅
Fase 0.1: Hardening UX & Erradicación Deuda     [████████████████] 100% (4/4 bloques)  ✅
Fase 1: Fundamentos Clean Architecture & DI     [████████████████] 100% (10/10 tareas) ✅
Fase 2: Experiencia Core (Materias/Notas/Eval)  [████████████████] 100% (13/13 tareas) ✅
Fase 2.1: Ergonomía Forms & Teclado Virtual     [████████████████] 100% (9/9 tareas)   ✅
Sprint Design 3.0: Tactile Studio & Bento Grid  [████████████████] 100% (3/3 Sprints)  ✅
Fase 3: Features Avanzadas & Multiescala        [████████████████] 100% (18/18 tareas) ✅
Fase 3.5: Transición 100% Offline-First (Room)  [████████████████] 100% (9/9 tareas)   ✅
Fase 3.6: Sanitización Lógica & Cascada         [████████████████] 100% (10/10 tareas) ✅
Fase 3.7: Normalización Sheets & FAB            [████████████████] 100% (5/5 tareas)   ✅
Sprints de Innovación & Resiliencia:
  - Editor Premium por Bloques (OneNote-like)   [████████████████] 100% (12/12 tareas) ✅
  - Directorio Docente con WhatsApp wa.me       [████████████████] 100% (9/9 tareas)   ✅
  - Respaldo Privado Google Drive appDataFolder [████████████████] 100% (21/21 tareas) ✅
  - Motor Notificaciones AlarmManager Doze-safe [████████████████] 100% (8/8 tareas)   ✅
  - Sistema de Temas Dinámicos 3.2 WCAG AAA     [████████████████] 100% (15/15 tareas) ✅
  - Importación Offline Horarios PDF UNERG      [████████████████] 100% (5/5 tareas)   ✅
  - Área de Estudio (Study Hub) & Pomodoro      [████████████████] 100% (6/6 tareas)   ✅
  - Blindaje UX Three-Tier Back & Anti-Doble    [████████████████] 100% (5/5 tareas)   ✅
Fase 4: Pulido de Experiencia, a11y & Perf      [████████████████] 100% (12/12 tareas) ✅
Fase 5: Tesis de Grado, Testing Aula & Release  [████████░░░░░░░░]  50% (8/16 tareas)
---------------------------------------------------------------------------------------
PROGRESO TOTAL DEL PROYECTO:                    [███████████████░]  97% Completado
SUITE DE PRUEBAS AUTOMATIZADA:                  805 tests unitarios en verde (100%) ✅
```

---

## 2. Detalle de Fases Ejecutadas

### Fase 0: Estabilización y Auditoría Técnica (100% ✅)
- Identificación y erradicación de cuellos de botella en la navegación entre pantallas.
- Corrección de fugas de memoria por listeners Firebase no cancelados en la versión preliminar.
- Centralización de rutas de navegación fuertemente tipadas en `Screen.kt`.

### Fase 0.1: Hardening UX y Erradicación de Deuda Técnica (100% ✅)
- Eliminación de side-effects en la composición de Jetpack Compose.
- Unificación del manejo de diálogos de confirmación accesibles (`NotiFastDialog`).
- Corrección del layout en pantallas con insets del sistema (barras de estado y navegación).

### Fase 1: Fundamentos de Clean Architecture e Inyección de Dependencias (100% ✅)
- Adopción estricta del patrón Clean Architecture: separación formal en capas `domain/`, `data/` y `presentation/`.
- Configuración de Hilt (Dagger) para inyección de dependencias (`AppModule`, `RepositoryModule`, `UseCaseModule`).
- Adopción del patrón MVI (Model-View-Intent) con un único `StateFlow<UiState>` inmutable por ViewModel.

### Fase 2 & 2.1: Experiencia Core y Ergonomía de Formularios (100% ✅)
- CRUD completo de materias con asignación de iconos semánticos y colores de alto contraste.
- CRUD de notas con imágenes locales y almacenamiento en caché privada.
- CRUD de evaluaciones con cálculo de ponderación básica.
- Soporte para teclados virtuales (IME padding dinámico) y navegación secuencial (`Next` / `Done`).

### Sprint Design 3.0: Tactile Studio & Bento Grid (100% ✅)
- Reinvención estética de NotiFast bajo el concepto "Tactile Papercraft": bordes de 1dp, micro-elevaciones y escala elástica a `0.97f`.
- Implementación de la cuadrícula interactiva Bento Grid en la pantalla principal.
- Creación de componentes atómicos reutilizables (`NotiFastCard`, `NotiFastButton`, `NotiFastTextField`).

### Fase 3 & 3.5: Features Avanzadas y Transición 100% Offline-First (100% ✅)
- Reemplazo total de Firebase Firestore por **Room SQLite local (esquema inicial v1 evolucionado a v9)**.
- Adopción de UUIDs locales y DataStore Preferences para la gestión del perfil activo sin internet.
- Implementación de la política matemática pura `GradePolicy` con soporte de 4 escalas (0-20, 0-10, 0-100%, GPA 4.0).
- Desarrollo del motor de intercambio de materias P2P (.notifast comprimido en GZIP + Base64 y QR bidimensional con ZXing).

### Sprints de Especialización Técnica (100% ✅)
1. **Editor Premium OneNote-like:** Arquitectura por bloques modulares, fórmulas matemáticas KaTeX, Markdown en vivo y dibujo vectorial sobre fotografías.
2. **Directorio Docente:** Gestión de cuerpo profesoral con enlace directo a WhatsApp `wa.me`.
3. **Respaldo Privado en Google Drive:** Sincronización modular por materia hacia `appDataFolder` con cliente OkHttp REST v3 delgado y diffing incremental SHA-256.
4. **Motor de Notificaciones Híbrido AAA:** Alertas exactas Doze-safe con `AlarmManager.setExactAndAllowWhileIdle()` y reprogramación automática tras reinicio (`BootCompletedReceiver`).
5. **Sistema de Temas Dinámicos 3.2:** Desacoplamiento entre acentos y lienzos de papel, con picker espectral Hex/HSL y contraste garantizado WCAG AAA.
6. **Importación PDF UNERG:** Parser nativo en Kotlin para extraer inscripciones de DACE-UNERG en < 100ms.
7. **Resiliencia Three-Tier Back & Anti-Doble Submit:** Protección de formularios y modales ante pérdidas accidentales de progreso.

### Fase 4: Pulido de Experiencia, Accesibilidad y Rendimiento (100% ✅)
- Verificación exhaustiva de contraste WCAG AAA ($\ge 4.5:1$) en las 36 combinaciones de temas.
- Auditoría de jank y renderizado a 60 fps en Compose.
- Suite de pruebas unitarias consolidada en **805 tests automatizados pasando en verde (100% de éxito)**.

---

## 3. Fase 5: Tesis de Grado y Despliegue (En Ejecución)

| Tarea / Hito | Alcance y Entregable | Estado |
|---|---|---|
| **T5.1** | Reestructuración y consolidación de la documentación técnica modular en `docs/` (Estándar SHOPYSAN). | 🟢 Completada |
| **T5.1.1** | Portal Web Interactivo de Documentación (`landing/docs.html`) desplegado en Cloudflare Pages con SPA, búsqueda instantánea `Ctrl+K`, renderizado KaTeX/Mermaid y navegación táctil WCAG AAA. | 🟢 Completada |
| **T5.1.2** | Portal Web de Políticas de Privacidad (`landing/privacy.html`, ruta limpia `/privacy`) con Privacidad Radical, 4 Pilares, permisos y derecho al olvido, en vivo en `notifast.pages.dev/privacy` con la estética del portal de docs. | 🟢 Completada |
| **T5.1.3** | Acceso en la app Android a la política de privacidad en línea: apartado en el Onboarding (login) y fila en Ajustes → Almacenamiento, Privacidad & Acerca de, vía `PrivacyPolicyIntents` (3 tests unitarios nuevos, suite en 808 en verde). | 🟢 Completada |
| **T5.2** | Preparación del documento formal de Tesis de Grado (Capítulos I a V: Planteamiento, Marco Teórico, Metodología, Desarrollo y Resultados). | 🟡 En redacción |
| **T5.3** | Ejecución de pruebas de campo en aula real con estudiantes del Área de Ingeniería de Sistemas de la UNERG (Encuesta SUS y Likert). | 🟡 En preparación |
| **T5.4** | Firma de artefacto de producción (Release AAB con R8 y optimización Proguard). | ⚪ Pendiente |
| **T5.5** | Publicación oficial en Google Play Store con ficha de producto y capturas de pantalla de alta resolución. | ⚪ Pendiente |
| **T5.6** | Defensa oral del Proyecto Especial de Grado ante el jurado evaluador de la UNERG. | ⚪ Pendiente |
