# PRD Maestro: NotiFast (Product Requirements Document)

| Metadato | Valor |
|---|---|
| **Versión** | 3.2.0 (Consolidado Fases 0 a 5) |
| **Fecha de Aprobación** | Octubre 2026 |
| **Estado** | 🟢 Producción / Fase 5 (Tesis de Grado) |
| **Documentos Relacionados** | [Visión](../00-vision/product-vision.md) · [Glosario](../00-vision/glossary.md) · [Historias de Usuario](./user-stories.md) · [Personas](./personas.md) · [Roadmap](./roadmap.md) · [Métricas](./success-metrics.md) · [Arquitectura](../03-architecture/system-overview.md) |

---

## 1. Resumen Ejecutivo y Alcance

**NotiFast** es una aplicación móvil Android nativa construida con **Kotlin** y **Jetpack Compose** que funciona como un **cuaderno de estudio táctil, inteligente y 100% Offline-First**, diseñado para eliminar la sobrecarga cognitiva y la ansiedad académica en estudiantes de educación secundaria y universitaria.

El sistema consolida en un solo núcleo local:
1. **Cuaderno de apuntes por bloques (estilo OneNote)** con soporte de Markdown, fórmulas KaTeX/LaTeX, dibujo libre sobre fotos de pizarras y persistencia a prueba de fallos.
2. **Gestión académica integral:** materias con sección, colores de alto contraste, directorio docente con enlace directo a WhatsApp y borrado transaccional en cascada.
3. **Agenda inteligente y reactiva:** selector semanal con ventana estricta, temporizador en vivo de la clase en curso e importación automática 100% offline de horarios oficiales emitidos en PDF por DACE-UNERG.
4. **Motor de calificaciones multiescala (`GradePolicy`):** promedios ponderados exactos en base 20, 10, 100% o GPA 4.0 y simulador predictivo de nota requerida.
5. **Motor híbrido de notificaciones proactivas:** alertas de clases y evaluaciones al minuto exacto mediante `AlarmManager.setExactAndAllowWhileIdle()` Doze-safe + `WorkManager` para resumen matutino diario.
6. **Transferencia P2P Offline:** paquetes portables `.notifast` (`GZIP + Base64`) y códigos QR generados/escaneados sin necesidad de internet.
7. **Copia de seguridad en la nube privada:** sincronización modular y global hacia la carpeta oculta `appDataFolder` de Google Drive del usuario mediante Google Credential Manager y diffing incremental SHA-256.
8. **Motor de Temas Táctiles WCAG AAA:** 6 acentos y 6 lienzos de papel desacoplados más un Estudio Espectral Hex/HSL continuo con validación matemática de contraste $\ge 4.5:1$.

---

## 2. Objetivos, No-Objetivos y Anti-Objetivos

### 2.1 Objetivos Principales (Goals)
- **G1 (Inmediatez Offline):** El 100% de la funcionalidad de estudio, consulta, edición y cálculo debe operar instantáneamente sin conexión a internet desde el primer segundo.
- **G2 (Rigor Matemático):** Eliminar el 100% de los errores en promedios y ponderaciones mediante una política matemática centralizada (`GradePolicy`), garantizando consistencia en todas las pantallas.
- **G3 (Soberanía y Privacidad):** El usuario es el único dueño de su información; cero almacenamiento de datos personales en servidores ajenos o bases de datos no privadas.
- **G4 (Calidad de Software para Tesis):** Validar la arquitectura del sistema con una suite automatizada superior a 800 pruebas unitarias con 100% de éxito y cero crashes.

### 2.2 No-Objetivos (Out of Scope)
- Sincronización en tiempo real multi-usuario estilo Google Docs colaborativo (colisiones simultáneas en el mismo apunte).
- Red social interna con muros de comentarios o feeds de chat propios (la comunicación externa se apoya en WhatsApp `wa.me`).
- Plataforma de exámenes en línea o proctoring con bloqueo de pantalla.
- Aplicación nativa iOS en la fase de tesis (el código de dominio y use cases está preparado para migración a Kotlin Multiplatform en futuras fases).

### 2.3 Anti-Objetivos (Criterios de Fracaso Absoluto)
- **Bloqueo por falta de red:** Si un estudiante no puede abrir un apunte o registrar una nota por no tener conexión, el producto ha fallado.
- **Pérdida silenciosa de información:** Sobrescribir notas o perder un borrador al recibir una llamada o cambiar de aplicación cuenta como defecto crítico inadmisible.
- **Cálculos engañosos de notas:** Mostrar `0.0` en materias que aún no han sido evaluadas en lugar de un estado pendiente `—` que preserve la tranquilidad del alumno.
- **Invasión publicitaria:** Mostrar banners o anuncios emergentes en momentos de estudio.

---

## 3. Requisitos Funcionales (Functional Requirements)

### 3.1 Módulo de Apuntes y Cuaderno por Bloques (`FR-NOTE`)
- **FR-NOTE-001:** El sistema debe permitir crear, editar y eliminar apuntes organizados por materia con título, fecha y estado de fijado (`isPinned`).
- **FR-NOTE-002:** El editor debe soportar arquitectura por bloques ordenados (`NoteBlockEntity`): Párrafo de texto plano, bloque Markdown formateado, bloque de Fórmula Matemática (KaTeX/LaTeX) y bloque de Checklist interactivo.
- **FR-NOTE-003:** El sistema debe permitir capturar o adjuntar fotos desde la cámara o galería, almacenándolas de forma local y optimizada en el directorio privado de la aplicación (`filesDir/images/`).
- **FR-NOTE-004:** El sistema debe proporcionar un lienzo táctil de dibujo a mano alzada para realizar anotaciones visuales y trazos vectoriales directamente sobre las fotos de pizarras o diagramas adjuntos.
- **FR-NOTE-005:** El editor debe disponer de una barra de accesorios acoplada sobre el teclado virtual (`DockedAccessoryToolbar`) que no tape el cursor ni el contenido en edición.
- **FR-NOTE-006:** El sistema debe proteger los borradores activos en `SavedStateHandle` ante recreación del proceso o muerte por bajo consumo de memoria del sistema operativo.
- **FR-NOTE-007:** El sistema debe aplicar Three-Tier Back Handling para evitar el descarte involuntario de apuntes con cambios sin guardar.
- **FR-NOTE-008:** El sistema debe permitir exportar apuntes a formato texto plano o compartirlos a través de la hoja del sistema (Android Sharesheet).

### 3.2 Módulo de Materias y Directorio Docente (`FR-SUBJ` / `FR-PROF`)
- **FR-SUBJ-001:** El sistema debe permitir registrar materias con nombre único (case-insensitive), código de cátedra, **Sección** (`section`), color semántico accesible e ícono representativo.
- **FR-SUBJ-002:** El sistema debe permitir el borrado en cascada transaccional (`withTransaction` en Room) de una materia, eliminando automáticamente sus apuntes, evaluaciones, horarios y archivos multimedia asociados sin dejar registros huérfanos.
- **FR-SUBJ-003:** El sistema debe mostrar el detalle de materia con navegación fluida entre pestañas: Apuntes, Evaluaciones, Horarios y Ficha del Profesor.
- **FR-PROF-001:** El sistema debe permitir registrar docentes con nombre completo, correo institucional, número telefónico (+58 u otros), oficina/cubículo y notas de asesoría.
- **FR-PROF-002:** El sistema debe formatear los números telefónicos docentes y ofrecer un botón de acceso directo para abrir chat de WhatsApp con saludo y contexto de materia preconfigurado (`wa.me/<phone>?text=...`).

### 3.3 Módulo de Agenda Semanal y Parser PDF UNERG (`FR-SCHED`)
- **FR-SCHED-001:** El sistema debe gestionar bloques de horario semanal con día de la semana (Lunes a Domingo), hora de inicio, hora de fin, aula y minutos de recordatorio previo.
- **FR-SCHED-002:** El sistema debe detectar y advertir de forma reactiva ante solapamientos horarios de clases entre diferentes materias (`ValidateScheduleOverlapUseCase`).
- **FR-SCHED-003:** La pantalla de Agenda debe mostrar la tarjeta de estado en vivo (`ScheduleLiveStatusCard`) con el progreso porcentual, tiempo restante de la clase actual y la próxima clase del día.
- **FR-SCHED-004:** El selector semanal de días debe calcular los indicadores de evaluaciones exclusivamente dentro de la ventana de la semana visible `[startOfWeek, startOfWeek + 7 days)`.
- **FR-SCHED-005:** El sistema debe incluir un motor nativo 100% offline (`UnergPdfScheduleParser`) capaz de extraer materias, secciones, aulas y horarios presenciales desde los comprobantes oficiales de inscripción en PDF emitidos por DACE-UNERG, ignorando clases a distancia y filas vacías.
- **FR-SCHED-006:** Al importar un PDF de horarios, el sistema debe presentar una hoja modal de confirmación con asignación automática de colores e íconos temáticos y resolución de conflictos de duplicados.

### 3.4 Módulo de Evaluaciones y Calificaciones Multiescala (`FR-EVAL` / `FR-GRADE`)
- **FR-EVAL-001:** El sistema debe permitir registrar evaluaciones con título, fecha/hora límite, materia, porcentaje de peso (ponderación de 0 a 100%), tipo (Parcial, Taller, Tesis, Proyecto, Quiz) y estado (Pendiente, Entregada, Calificada).
- **FR-GRADE-001:** El sistema debe centralizar todo cálculo de calificaciones en la política pura `GradePolicy`.
- **FR-GRADE-002:** El sistema debe soportar 4 escalas oficiales: Base 20 (`0–20`), Base 10 (`0–10`), Porcentual (`0–100%`) y GPA (`0.0–4.0` / A-F), con redondeo exacto a un decimal o dos decimales según la escala.
- **FR-GRADE-003:** El promedio acumulado de una materia debe calcularse dividiendo la suma de puntos ponderados obtenidos entre la suma de pesos de las evaluaciones ya calificadas (nunca promedio de notas directas sin peso).
- **FR-GRADE-004:** El promedio global de la carrera debe ponderarse de acuerdo a la carga y calificaciones de todas las materias con notas válidas.
- **FR-GRADE-005:** El sistema debe incluir un simulador de meta deseada que resuelva la ecuación:
  $$\text{Nota Requerida} = \frac{\text{Meta} - \sum (\text{Nota}_i \times \text{Peso}_i)}{\sum \text{Pesos Pendientes}}$$
  indicando si la meta es viable, si ya está alcanzada o si es matemáticamente inalcanzable.
- **FR-GRADE-006:** Toda cifra de calificación, porcentaje y cuenta regresiva debe renderizarse en tipografía monoespaciada tabular (`JetBrains Mono`).

### 3.5 Módulo de Notificaciones Proactivas Híbridas (`FR-NOTIF`)
- **FR-NOTIF-001:** El sistema debe programar recordatorios exactos de clases $M$ minutos antes (10, 15, 30 min) y de exámenes $N$ días antes (1, 2, 3 o 7 días) mediante `ExactAlarmScheduler` utilizando `AlarmManager.setExactAndAllowWhileIdle()`.
- **FR-NOTIF-002:** El sistema debe programar una notificación periódica matutina (07:00 AM) mediante `WorkManager` con el resumen de clases del día y evaluaciones de las próximas 48 horas.
- **FR-NOTIF-003:** El sistema debe registrar un `BootCompletedReceiver` que reprograme automáticamente todas las alarmas exactas tras el reinicio del dispositivo (`BOOT_COMPLETED`), cambio de hora (`TIME_SET`) o cambio de huso horario (`TIMEZONE_CHANGED`).
- **FR-NOTIF-004:** Las notificaciones deben categorizarse en 3 canales independientes de Android O+: `notifast_evaluations` (Importancia MAX con sonido y heads-up), `notifast_classes` (Importancia HIGH con vibración táctil) y `notifast_daily_summary` (Importancia DEFAULT).
- **FR-NOTIF-005:** El sistema debe gestionar el permiso de Android 13+ (`POST_NOTIFICATIONS`) y `SCHEDULE_EXACT_ALARM` con diálogos pedagógicos explicativos.
- **FR-NOTIF-006:** Al tocar una notificación, el sistema debe dirigir al estudiante mediante Deep-Linking hacia la pantalla específica de la materia, horario o evaluación correspondiente.

### 3.6 Módulo de Transferencia P2P Offline (`FR-P2P`)
- **FR-P2P-001:** El sistema debe permitir empaquetar una materia completa en un archivo binario `.notifast` (`SubjectPackage` comprimido con GZIP y codificado en Base64).
- **FR-P2P-002:** El sistema debe permitir generar un código QR de alta densidad con ZXing para compartir el paquete sin cables ni internet.
- **FR-P2P-003:** El sistema debe permitir escanear códigos QR con la cámara utilizando Google Code Scanner (o selector manual de archivo) e importar el paquete de inmediato.
- **FR-P2P-004:** Al importar una materia existente, el sistema debe resolver colisiones de nombres mediante `ResolveSubjectNameUseCase`, ofreciendo la opción de sobrescribir o agregar con sufijo correlativo `(1)`, `(2)`.

### 3.7 Módulo de Respaldo Privado en Google Drive (`FR-CLOUD`)
- **FR-CLOUD-001:** El sistema debe permitir al usuario autenticar voluntariamente su cuenta de Google mediante Google Credential Manager (sin Firebase Auth).
- **FR-CLOUD-002:** El acceso a la nube debe solicitar únicamente el alcance privado `https://www.googleapis.com/auth/drive.appdata` (`appDataFolder`).
- **FR-CLOUD-003:** El sistema debe permitir el respaldo modular por materia independiente (`.nfbak` comprimido con apuntes, imágenes optimizadas, horarios y evaluaciones) y el respaldo del perfil global con directorio docente.
- **FR-CLOUD-004:** El sistema debe calcular un hash SHA-256 canónico (`ContentHasher`) para cada materia para reflejar instantáneamente su estado de sincronización (*Sincronizada*, *Cambios sin subir*, *Solo en teléfono*, *Solo en la nube*).
- **FR-CLOUD-005:** El cliente HTTP debe ser un cliente REST v3 ligero sobre OkHttp, optimizado para conexiones móviles lentas con subidas multipart en streaming.
- **FR-CLOUD-006:** El usuario debe poder desvincular su cuenta de Google en cualquier momento sin afectar los datos locales en el dispositivo.

### 3.8 Módulo de Área de Estudio (Study Hub) (`FR-STUDY`)
- **FR-STUDY-001:** El sistema debe proporcionar un temporizador táctil Pomodoro integrado con ciclos de trabajo (25 min) y descanso (5 min), con alertas de finalización.
- **FR-STUDY-002:** El sistema debe permitir fijar metas de estudio diarias y asociar documentos y apuntes clave a cada sesión.

### 3.9 Módulo de Personalización y Sistema de Temas (`FR-THEME`)
- **FR-THEME-001:** El sistema debe ofrecer 6 paletas de acento predefinidas (`classic`, `forest_sage`, `midnight_indigo`, `warm_mocha`, `sakura_zen`, `ocean_cyan`) y 6 lienzos de papel de fondo independientes (`Pergamino`, `Menta`, `Polar`, `Papiro`, `Lavanda`, `Glaciar`).
- **FR-THEME-002:** El sistema debe incluir un Estudio Espectral continuo con sliders de Matiz, Saturación y Luminosidad más entrada directa Hex `#RRGGBB`.
- **FR-THEME-003:** `ThemePaletteGenerator` debe validar y ajustar algorítmicamente la luminancia para garantizar contraste accesible WCAG AAA ($\ge 4.5:1$) en cualquier combinación de color.
- **FR-THEME-004:** Toda la UI debe respetar el tema interno de la app vía `MaterialTheme.colorScheme.isDark` sin depender del tema global del sistema operativo.

---

## 4. Requisitos No Funcionales (Non-Functional Requirements)

| ID | Categoría | Requisito y Criterio Medible |
|---|---|---|
| **NFR-001** | **Rendimiento** | Tiempo de arranque en frío (*Cold Start*) $\le 800$ ms en Android de gama media. Desplazamiento fluido a 60 fps constantes en Compose LazyLists sin jank. |
| **NFR-002** | **Offline-First** | 100% de las funciones de lectura, escritura, edición, búsqueda y cálculo deben completarse en menos de 50 ms sobre Room SQLite local sin red. |
| **NFR-003** | **Accesibilidad (a11y)** | Cumplimiento estricto del estándar **WCAG AAA/AA**: contraste cromático mínimo $\ge 4.5:1$ en textos estándar y $\ge 3:1$ en textos grandes/iconos; touch targets interactivos con dimensión mínima de $48 \times 48$ dp. |
| **NFR-004** | **Resiliencia UX** | Cero pérdida de datos ante cambios de orientación, pulsaciones accidentales de atrás o muerte del proceso en segundo plano (Three-Tier Back y `SavedStateHandle`). |
| **NFR-005** | **Privacidad Absoluta** | Cero persistencia en bases de datos compartidas (cero Firestore). El respaldo en la nube viaja exclusivamente cifrado en tránsito HTTPS hacia la carpeta oculta de Google Drive del propio estudiante. |
| **NFR-006** | **Calidad y Mantenibilidad** | 100% de las pruebas unitarias automatizadas en verde (suite actual de **805 tests** pasando sin fallos). Límite de tamaño composable $\le 250$ LOC por archivo. Cero cadenas de texto hardcodeadas en Compose (100% en `strings.xml`). |
| **NFR-007** | **Optimización de Almacenamiento** | Compresión inteligente de imágenes a lado largo máximo de 1600 px y calidad JPEG 80, eliminando metadatos EXIF sensibles antes de respaldar. Tamaño del APK empaquetado optimizado con R8 y minificación de recursos. |
| **NFR-008** | **Eficiencia Energética** | Consumo de batería en reposo $< 0.5\%$ en 24 horas. Las alarmas exactas se ejecutan sin mantener WakeLocks innecesarios. |

---

## 5. Matriz de Fases del Ecosistema NotiFast

```
Fase 0: Estabilización, Refactor de Navegación y Auditoría Técnica Inicial (✅ 100%)
Fase 0.1: Hardening de UX, Limpieza de Listeners y Erradicación de Deuda Técnica (✅ 100%)
Fase 1: Fundamentos de Arquitectura, Hilt DI, Clean Architecture y ViewModels MVI (✅ 100%)
Fase 2: Experiencia Core (CRUD de Materias, Apuntes con Imágenes, Evaluaciones) (✅ 100%)
Fase 2.1: Ergonomía de Formularios, Validación Inmediata y Teclado Virtual (✅ 100%)
Fase 3: Features Avanzadas (Agenda Semanal, GradePolicy Multiescala, Paquete P2P) (✅ 100%)
Fase 3.5: Transición Estricta 100% Offline-First con Room SQLite y DataStore (✅ 100%)
Fase 3.6: Sanitización Lógica, Normalización de Claves y Persistencia en Cascada (✅ 100%)
Fase 3.7: Estandarización de Hojas Modales BottomSheet y FAB Expandible (✅ 100%)
Sprints de Innovación:
  - Editor Premium OneNote-like con Bloques, Markdown y LaTeX (✅ 100%)
  - Directorio Docente con Integración de WhatsApp wa.me (✅ 100%)
  - Respaldo Privado en Google Drive appDataFolder con diffing SHA-256 (✅ 100%)
  - Motor de Notificaciones Híbrido AlarmManager Doze-safe + WorkManager (✅ 100%)
  - Sistema de Temas Dinámicos 3.2 WCAG AAA con Estudio Hex/HSL (✅ 100%)
  - Parser Nativo Offline de Horarios PDF DACE-UNERG (✅ 100%)
  - Área de Estudio (Study Hub) con Pomodoro y Metas (✅ 100%)
  - Blindaje UX Three-Tier Back y Anti-Doble Submit (✅ 100%)
Fase 4: Pulido de Experiencia, Accesibilidad y Rendimiento Final (🟢 99%)
Fase 5: Documentación Modular, Pruebas en Aula de Clases y Tesis de Grado UNERG (🚀 En curso)
```
