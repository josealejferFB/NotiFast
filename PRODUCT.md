# PRODUCT.md — NotiFast: Cuaderno Académico Inteligente & Hub de Estudio

## 1. Registro de Producto

- **Nombre:** NotiFast
- **Versión Actual:** `1.0 RC` (Design System 3.0 — Tactile Studio AAA)
- **Categoría:** EdTech / Productividad Académica / Cuaderno Estudiantil Offline-First
- **Plataformas:**
  - **App Móvil:** Android Nativo (`minSdk 26`, `targetSdk 36`, Kotlin `2.0.21`, Jetpack Compose, Material 3, Room SQLite `2.7.2`)
  - **Web Showcase (Cloudflare Pages):** [`https://notifast.pages.dev/`](https://notifast.pages.dev/) (`landing/index.html`) con Estudio de Temas en Vivo, Simulador de Notificaciones Proactivas y Teléfono Android Interactivo
- **Arquetipo Visual:** Tactile Papercraft / Digital Studio Notebook con Motor de Temas Personalizables WCAG AAA

---

## 2. Usuarios y Contexto Emocional

### Audiencia Primaria
1. **Estudiantes de Secundaria (14–18 años):** Sobrecargados por múltiples materias, tareas simultáneas y calendarios cambiantes. Necesitan simplicidad visual instantánea, recordatorios proactivos puntuales y confirmación clara de lo que ya completaron.
2. **Estudiantes Universitarios y de Grado (18–25+ años):** Manejan esquemas de ponderación complejos (porcentajes de notas, escalas 0–10, 0–20, 0–100% y GPA A–F, solapamiento de horarios de cátedra y laboratorio, y directorio de profesores). Requieren precisión matemática IEEE 754, apuntes estructurados por bloques con fórmulas y fotos de pizarra anotadas, y un entorno sin distracciones.
3. **Investigadores / Jurado de Tesis (Demostración de Ingeniería):** NotiFast funge como producto comercial para Google Play Store y como proyecto de tesis con Clean Architecture + MVI, **684 pruebas unitarias automatizadas**, persistencia local transaccional y sincronización cifrada con Google Drive.

### Contexto Emocional y Estado Mental del Usuario
El estudiante recurre a NotiFast en momentos de **alta carga cognitiva y estrés pre-exámenes**. La aplicación opera como un **"anclaje de calma"**:
- **Cero ansiedad por alertas estridentes:** Los recordatorios locales (clases, evaluaciones y resumen matutino 07:00 AM) acompañan con claridad contextual y acciones rápidas útiles.
- **Sensación de control, pertenencia y orden:** La interfaz transmite la tranquilidad de un cuaderno físico de alta gama, permitiendo a cada estudiante elegir entre **6 paletas de acento**, **6 tonos de lienzo de papel** o crear su propio tema en el **Estudio Hex/HSL** con contraste WCAG AAA garantizado.
- **Fricción mínima y autonomía 100% Offline-First:** Toda operación ocurre instantáneamente sobre Room SQLite local sin depender de conexión a internet en el aula.

---

## 3. Propósito del Producto & Estrella Polar (North Star)

> **"Transformar la sobrecarga académica en serenidad y enfoque mediante un cuaderno digital táctil, personalizable, 100% offline-first y libre de fricción."**

El éxito del producto no se mide por el tiempo que el usuario pasa dentro de la app (métrica de engagement tóxico), sino por **la velocidad con la que obtiene claridad sobre su día académico y retoma sus sesiones de estudio o descanso**.

---

## 4. Posicionamiento y Capacidades Reales del Ecosistema

NotiFast se sitúa en la intersección dorada entre:
1. **La inmediatez táctil de un cuaderno de papel personalizable:** Editor por bloques (estilo OneNote) con Markdown, fórmulas matemáticas, listas interactivas y dibujo a mano alzada sobre fotos de pizarra, sobre 6 lienzos de lectura descansada.
2. **La potencia de un asistente académico autónomo:** Cálculo instantáneo de promedios ponderados (`GradePolicy` única), simulador interactivo de nota requerida, directorio de profesores con contacto contextual a WhatsApp, alertas híbridas exactas (`AlarmManager` + `WorkManager`), paquetes P2P `.notifast` / QR offline y copia de seguridad privada en Google Drive (`appDataFolder`).

```
┌────────────────────────────────────────────────────────────────────────┐
│                          NOTIFAST EDTECH HUB                           │
├───────────────────────────────────┬────────────────────────────────────┤
│      CUADERNO TÁCTIL DIGITAL      │       INTELIGENCIA ACADÉMICA       │
│  - Editor por bloques + Markdown  │  - Promedios ponderados multiescala│
│  - Fotos de pizarra anotables     │  - Simulador de nota requerida     │
│  - 6 Acentos + 6 Lienzos + Hex    │  - Alertas exactas (Alarm + Work)  │
│  - Búsqueda global instantánea    │  - Directorio docente + WhatsApp   │
│  - 100% Offline-First Room SQLite │  - Pack P2P .notifast/QR + Drive   │
└───────────────────────────────────┴────────────────────────────────────┘
```

---

## 5. Personalidad de Marca & Tono de Voz

- **Táctil y Artesanal (Studio Craft):** Cuida los detalles, las proporciones, los micro-bordes de `1dp` y la respuesta elástica (`scale 0.97f`).
- **Sereno y Personalizable (Calm & Adaptive):** Parte del tema insignia *NotiFast Classic* (Muted Crimson `#9E1B32` y Studio Aqua `#2A7B7B` sobre Lienzo Pergamino `#FAF9F6`), y se adapta a 6 colecciones editoriales (*Campus Verde*, *Cyber Nocturno*, *Café Nórdico*, *Sakura Zen*, *Minimal Nórdico*) o al color exacto del estudiante.
- **Riguroso y Preciso:** Los datos de notas, fechas, pesos porcentuales y cuentas regresivas se muestran en tipografía tabular **JetBrains Mono**.
- **Alentador y Empático:** Si una materia aún no tiene evaluaciones calificadas, muestra `—` y orientación constructiva en lugar de un `0.0` alarmista.

---

## 6. Anti-Referencias Críticas (Lo que NotiFast NUNCA será)

| Anti-Referencia | Por qué se rechaza | Enfoque NotiFast |
|---|---|---|
| **LMS Burocrático Tradicional (ej. Moodle 2010)** | Interfaces densas, formularios grises interminables, tablas saturadas y navegación laberíntica. | Tarjetas táctiles `NotiFastCard`, Bento Grid limpio y modales `ModalBottomSheet` unificados. |
| **App Dependiente de Nube que Falla en el Aula** | Pantallas de carga infinitas o pérdida de apuntes en sótanos universitarios sin señal. | Persistencia primaria en **Room SQLite v4** y respaldo opcional en `appDataFolder` de Google Drive. |
| **Gamificación Hiperactiva Infantil** | Sonidos estridentes, avatares invasivos y barras de energía que distraen del estudio real. | Micro-interacciones táctiles sutiles, háptica calibrada y diseño editorial maduro. |
| **Gráficos Estadísticos Incomprensibles** | Histogramas gaussianos abstractos o correlaciones artificiales que no ayudan a tomar decisiones. | Simulador directo *«¿Cuánto necesito sacar en lo que falta?»* y lista clara de salud por materia. |

---

## 7. Principios Fundamentales de Diseño y Usabilidad

1. **La Regla del Papel Inteligente (Smart Paper Canvas):** La pantalla es un pliego de estudio con tonos de lienzo independientes del color de acento (`BackgroundPreset`), micro-bordes nítidos y cero sombras pesadas.
2. **Claridad en Menos de 3 Segundos:** Al abrir la aplicación, el estudiante identifica de inmediato su clase en curso (`ScheduleLiveStatusCard`), su próxima evaluación y su promedio global.
3. **Ergonomía Pulgar-First & Teclado Acoplado:** Los flujos de creación y edición viven en `ModalBottomSheet` expandibles con barras de herramientas acopladas sobre el teclado (`DockedAccessoryToolbar`) y FAB expandible (`NotiFastExpandableFab`).
4. **Privacidad y Propiedad Total de Datos:** Los datos pertenecen exclusivamente al estudiante en su dispositivo y en su carpeta privada de Google Drive, sin servidores de terceros que lean sus apuntes.

---

## 8. Estándares de Accesibilidad e Inclusión (WCAG AAA/AA)

- **Calibración Algorítmica de Contraste (`ThemePaletteGenerator`):** Cualquier color elegido en los presets o en el Estudio Hex/HSL se calibra dinámicamente para asegurar un ratio de contraste $\ge 4.5:1$ en textos y controles tanto en Modo Claro como en Modo Oscuro.
- **Apego Estricto al Tema Interno (`MaterialTheme.colorScheme.isDark`):** Toda la UI respeta el modo Claro/Oscuro elegido dentro de NotiFast sin fugas visuales del tema del sistema operativo.
- **No Dependencia Exclusiva del Color:** Todo estado semántico (Al día, En riesgo, Evaluada, Pendiente, Conflicto Cloud) combina color + icono vectorial + etiqueta textual localizada en `strings.xml`.
