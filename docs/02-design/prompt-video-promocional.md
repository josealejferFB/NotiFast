# PROMPT MAESTRO — Video promocional vertical de NotiFast

**Remotion · 1080×1920 · 30 fps · 58 s · sin audio · 100 % animación programada**

> Pega este documento completo como primer mensaje de una sesión nueva. La sesión debe **ejecutar y renderizar**, no solo proponer un plan. Trabaja dentro del repositorio `/home/p0xd/Documentos/proyectos/NotiFast`.

---

## 0. Rol: orquestador multiagéntico de producción audiovisual

Actúa como **director técnico y creativo** de un video promocional vertical para NotiFast. Eres el único responsable del resultado final: un MP4 renderizado, verificado y reproducible.

Tu trabajo no es escribir una propuesta. Tu trabajo es:

1. Leer el contexto real de la app y su sistema de diseño.
2. Dirigir la narrativa y el storyboard.
3. Construir el proyecto Remotion en `promo-video/`.
4. Delegar escenas independientes a subagentes.
5. Integrar, pulir y renderizar.
6. Verificar con evidencia y entregar el video.

**Regla de oro:** si el video necesita audio para entenderse, si parece una presentación de diapositivas, o si el texto tapa la interfaz, está mal. Vuelve a hacerlo.

---

## 1. Misión

Crear un video promocional de **58 segundos** para NotiFast, pensado para un estudiante de la **UNERG** que ve Reels, TikTok o Shorts **sin sonido**, con el pulgar listo para seguir scrolleando.

El video debe lograr tres cosas:

1. **Detener el scroll en 1,5 s** con un dolor reconocible: el horario DACE.
2. **Mostrar 6 beneficios** sin convertirlos en una lista: horario ordenado, aviso de clase en curso, apuntes, profesor + WhatsApp, portadas y nota.
3. **Cerrar con una instalación clara**: gratis, Android, 100 % offline.

**Promesa central:** *"Todo tu semestre, en orden."*
**Concepto visual:** *"El semestre que se ordena solo."*
**Tono:** calmado, táctil, preciso, optimista. No es un anuncio agresivo; es la sensación de abrir un cuaderno ordenado.

---

## 2. Restricciones duras (no negociables)

| # | Restricción |
|---|---|
| 1 | **Formato:** 1080 × 1920 px, 30 fps, 1740 frames = 58,0 s. Nunca superar 60 s. |
| 2 | **Sin audio:** cero `<Audio>`, cero archivos de sonido, cero pista silenciosa. Verificar con `ffprobe`. |
| 3 | **100 % código:** toda la UI se construye con React/CSS/SVG. Prohibido usar footage, screen recordings, video generado por IA, personas reales o capturas de pantalla incrustadas. |
| 4 | **Activos permitidos:** solo el logo local (`landing/assets/logo.png`), las fuentes locales (`landing/vistas/fonts/`) y, para la portada UNERG, los logos institucionales locales (`app/src/main/res/drawable/logo_unerg_ais.png` y `logo_unerg_banner.png`). Nada de red durante el render. |
| 5 | **Sistema de diseño:** tokens exactos de NotiFast. Plus Jakarta Sans + JetBrains Mono. Lienzo pergamino claro, un beat oscuro para notificaciones. Bordes de 1 dp, superficies tonales, radios de 16–24 dp (escala 2× dentro del teléfono). Sin sombras pesadas, sin glassmorphism, sin neón, sin 3D. |
| 6 | **Copy:** español de Venezuela, máximo 8 palabras por escena fuera del teléfono (la CTA final puede llegar a 12). Una sola frase a la vez. Sin emojis. Sin muros de texto. Sin promesas inventadas. |
| 7 | **Zonas seguras:** top 220 px, bottom 340 px, left 96 px, right 120 px. La copy y el CTA esenciales deben vivir dentro. El teléfono puede sangrar; el texto no. |
| 8 | **Continuidad:** sensación de toma continua. Transiciones por elemento compartido; sin cortes secos, salvo el beat de notificaciones. Prohibido el efecto diapositiva. |
| 9 | **Primer frame:** debe funcionar como miniatura. El hook se lee completo en el frame 0. No empezar en negro ni con fade desde negro. |
| 10 | **Determinismo:** nada de `setTimeout`, `requestAnimationFrame`, `Date.now()`, aleatoriedad sin semilla ni red durante el render. Toda animación sale de `useCurrentFrame()`. |
| 11 | **Alcance:** trabajar solo en `promo-video/`. No tocar `app/`, `landing/`, `docs/`, Gradle ni el historial de git. |

---

## 3. Audiencia e insight

- Estudiante UNERG de 18–25 años, Ingeniería o carrera afín.
- Conoce DACE, los PDFs de horario, los salones, los profesores y los parciales.
- Toma apuntes en WhatsApp o en hojas sueltas. Pierde portadas. Calcula notas a mano.
- Ve el video en el móvil, con sonido apagado, en el autobús o entre clases.
- No le importa la arquitectura de la app. Le importa **llegar a clase sin dudas**, **no perder apuntes**, **encontrar al profe** y **saber su nota**.

**Insight:** el caos académico no es falta de esfuerzo; es falta de un lugar donde todo caiga en su sitio. NotiFast es ese lugar.

**Mensaje emocional:** *"No tienes que recordarlo todo. NotiFast lo ordena."*
**Mensaje racional:** gratis, Android, 100 % offline, sin anuncios.

Prohibido en copy: "revolucionario", "la mejor app", "transforma tu vida", "805 tests", "Clean Architecture", "tesis", "Room", "Hilt". Eso es para la defensa académica, no para el estudiante que va a instalar la app.

---

## 4. Sistema de diseño obligatorio

### 4.1 Paleta clara (base del video)

| Token | Hex | Uso |
|---|---|---|
| `background` | `#FAF9F6` | Lienzo pergamino principal |
| `surface` | `#FFFFFF` | Tarjetas elevadas |
| `surfaceContainer` | `#F3EFEA` | Campos, chips, contenedores suaves |
| `surfaceContainerHigh` | `#EBE6DF` | Contraste intermedio |
| `border` | `#E5DFD7` | Micro-borde de 1 dp |
| `primary` | `#9E1B32` | Carmesí NotiFast: acentos, CTA, subrayado |
| `onPrimary` | `#FFFFFF` | Texto sobre primario |
| `primaryContainer` | `#FDE8EB` | Fondo de acento suave |
| `secondary` | `#2A7B7B` | Aqua Studio: datos, WhatsApp, física |
| `tertiary` | `#15803D` | Verde académico: aprobado, check |
| `error` | `#B91C1C` | Alertas |
| `ink` | `#181A1F` | Texto principal |
| `muted` | `#4B5563` | Texto secundario |

### 4.2 Paleta oscura (solo beat de notificaciones)

| Token | Hex | Uso |
|---|---|---|
| `background` | `#0E131F` | Lienzo nocturno |
| `surface` | `#161E2D` | Tarjeta de notificación |
| `surfaceContainer` | `#1C2638` | Contenedor |
| `border` | `#2A384E` | Borde |
| `primary` | `#E05A6E` | Acento nocturno |
| `secondary` | `#38B2AC` | Aqua nocturno |
| `tertiary` | `#34D399` | Verde nocturno |

### 4.3 Colores de materia

| Materia | Hex |
|---|---|
| Cálculo Diferencial | `#9E1B32` |
| Física General | `#2A7B7B` |
| Programación I | `#4338CA` |
| Inglés Instrumental | `#C2410C` |

### 4.4 Tipografía

| Rol | Fuente | Tamaño en video | Peso | Notas |
|---|---|---|---|---|
| Hook / headline exterior | Plus Jakarta Sans | 84–96 px | 800 | Tracking −0,03 em; máximo 2 líneas |
| Subcopy exterior | Plus Jakarta Sans | 40–44 px | 500 | Máximo 2 líneas |
| Título de UI (dentro del teléfono) | Plus Jakarta Sans | 18 sp → 36 px | 700 | Escala 2× |
| Cuerpo de UI | Plus Jakarta Sans | 15 sp → 30 px | 400/500 | Interlínea 1,35 |
| Label / chip | Plus Jakarta Sans | 12 sp → 24 px | 600 | Mayúsculas solo en chips pequeños |
| Datos, horas, notas, porcentajes | JetBrains Mono | 14 sp → 28 px | 600 | `font-variant-numeric: tabular-nums` |
| DACE / UNERG / URL | JetBrains Mono | 24–28 px | 500 | Tracking 0,04 em |

**Regla de escala:** construye la pantalla del teléfono en un lienzo lógico de **360 × 780 px** y escálala **2×** a 720 × 1560 px. Así los tamaños, radios y espaciados coinciden con la app real.

### 4.5 Superficies y detalles

- Tarjetas: radio 20 dp (40 px a 2×), borde 1 dp (2 px a 2×), superficie tonal, sin sombra.
- Botones: radio 12 dp (24 px a 2×), altura mínima 48 dp (96 px a 2×).
- Sheets: radio 28 dp (56 px a 2×), borde superior de 1 dp.
- Si necesitas profundidad, usa un tinte suave del 8 % con blur máximo de 24 px; nunca una sombra negra difusa.
- Grano de papel: opcional, SVG estático al 2–3 % de opacidad. Nunca parpadea.
- Iconos: Material Symbols Rounded, SVG inline, trazo 2 px. Sin emojis.
- Sin gradientes llamativos. El único degradado permitido es un resplandor radial muy sutil detrás del logo o de la notificación.

### 4.6 Movimiento

| Elemento | Spring / easing |
|---|---|
| Tarjetas | `damping: 18, stiffness: 140, mass: 0.9` |
| Texto | `damping: 20, stiffness: 180, mass: 0.8` |
| Cámara | `damping: 22, stiffness: 90, mass: 1` |
| Easing general | `cubic-bezier(0.22, 1, 0.36, 1)` |
| Stagger | 3 frames por tarjeta, 2 por palabra, 4 por fila |
| Transiciones | 10–14 frames; máximo 0,5 s |
| Push-in | 1,00 → 1,04 por escena; pan máximo 1,5 % |
| Duración máxima | 700 ms salvo cámara o ambiente |

Prohibido: rebotes exagerados, confeti, glitch, zoom punch, parallax de 5 capas, rotaciones de 360°, animaciones que duren más de 1 s.

---

## 5. Dirección creativa

**La idea:** una mesa de estudio táctil. El teléfono es el escenario fijo. Cada función entra como una tarjeta de papel que se acomoda. La cámara nunca corta: sigue la tarjeta que se convierte en la siguiente escena.

**El dispositivo:** un teléfono persistente + tarjetas de UI flotantes. El teléfono no es un mockup genérico: es la app. Micro-bordes de 1 dp, superficies tonales, radios de 20 dp, Plus Jakarta Sans y JetBrains Mono para horas y notas. Fuera del teléfono, tipografía editorial grande sobre pergamino, como un anuario de diseño, no como una plantilla de TikTok.

**Ritmo:** hook (2,5 s) → importación DACE (7 s) → horario (8 s) → notificaciones (7,5 s, oscuro) → apuntes (8 s) → profesores/WhatsApp (7 s) → portadas (7 s) → nota (6 s) → CTA (5 s). Total: 58 s.

**Ritmo de color:** pergamino claro en 1–3, oscuro en 4, claro en 5–8, pergamino claro en CTA. El carmesí guía; el aqua y el verde son recompensas de datos. Sin arcoíris.

**Lo que NO se hace:** carrusel de pantallas falsas, capturas flotando, lista de 10 iconos, teléfono genérico en una mano, blobs de gradiente, "¡REVOLUCIONARIO!", memes, bailes, confeti, personas generadas por IA.

**Fotograma 0:** el hook completo, legible, sobre pergamino. El PDF DACE asoma desde abajo. El subrayado carmesí se dibuja después. El movimiento empieza con el PDF y el subrayado, no con el texto.

---

## 6. Storyboard exacto (58 s)

| # | Tiempo | Frames | Escena | Copy exterior | Transición de salida |
|---|---|---|---|---|---|
| 1 | 0:00–0:02,5 | 75 | Hook DACE | "Tu horario DACE," / "por fin en orden." | El PDF entra al teléfono |
| 2 | 0:02,5–0:09,5 | 210 | Importar DACE | "Sube tu PDF del DACE." → "Todo se ordena." | El check se expande al horario |
| 3 | 0:09,5–0:17,5 | 240 | Horario y materias | "Tu semana, clara." | Push-in a la clase de Cálculo |
| 4 | 0:17,5–0:25 | 225 | Notificación en curso | "La clase ya empezó." → "Tú ya lo sabes." | "Ver apuntes" se expande al editor |
| 5 | 0:25–0:33 | 240 | Apuntes | "Apuntes que no se pierden." | La pila de notas gira a profesor |
| 6 | 0:33–0:40 | 210 | Profesor + WhatsApp | "Tu profe, a un toque." | La burbuja enviada se vuelve papel |
| 7 | 0:40–0:47 | 210 | Portadas | "Portadas listas para entregar." | El documento baja; sube la nota |
| 8 | 0:47–0:53 | 180 | Nota | "Tu nota, clara." | El anillo se convierte en aura del logo |
| 9 | 0:53–0:58 | 150 | CTA | "NotiFast." / "Todo tu semestre, en orden." / "Descargar gratis" / "Android · 100 % offline" | Hold final |

### S1 — Hook (75 frames)

- **Visual:** fondo pergamino con grano sutil. Headline en dos líneas, ya legible en el frame 0. Un PDF DACE estilizado ("DACE", "UNERG", "2026-02") asoma desde abajo. Detrás, la silueta del teléfono sube lentamente. La palabra "orden" recibe un subrayado carmesí dibujado a mano.
- **Copy:** "Tu horario DACE," / "por fin en orden."
- **Movimiento:** las palabras ya están al 100 %; solo hacen un settle de 2 px. El PDF cae con spring (14 frames). El teléfono sube 20 frames. El subrayado se dibuja en 12 frames. Sin fade desde negro.
- **Transición:** el PDF rota 3° y se desliza dentro de la zona de carga del teléfono. Match cut al sheet de importación.

### S2 — Importar DACE (210 frames)

- **Visual:** pantalla del teléfono con un bottom sheet "Importar horario UNERG". El PDF cae en una zona punteada; una línea de escaneo barre el documento (12 frames). Aparecen 4 filas de materias con check y chips de horario: Cálculo Diferencial (Lun/Mié 08:00, Aula 204), Física General (Mar/Jue 10:00, Lab 3), Programación I (Lun/Mié 14:00, Lab 2), Inglés Instrumental (Vie 09:00, Aula 12). Resumen "4 materias · 12 clases". Botón "Importar 4 materias" se llena de carmesí; tap ripple; check de éxito y toast "Horario importado". Chip "100 % offline" arriba.
- **Copy:** "Sube tu PDF del DACE." (primeros 4 s) → "Todo se ordena." (últimos 3 s).
- **Movimiento:** escaneo 12 frames; filas con stagger de 4; checks se dibujan; botón con ripple; éxito con pop.
- **Si se siente cargado:** reduce a 3 materias visibles y ajusta el resumen a "3 materias · 9 clases". La escena debe respirar.
- **Transición:** el check se expande como un círculo que revela el horario completo.

### S3 — Horario y materias (240 frames)

- **Visual:** grilla semanal Lun–Vie, 07:00–17:00. Los bloques caen en su lugar con los colores de materia. Aparece "Hoy · Miércoles"; una línea de tiempo en 08:00 barre la grilla; el bloque de Cálculo brilla suavemente. La grilla se comprime en una tira semanal; 4 tarjetas de materia suben desde abajo con icono, nombre, sección y promedio. La cámara hace push-in a Cálculo.
- **Copy:** "Tu semana, clara."
- **Movimiento:** bloques con stagger de 3; líneas de la grilla se dibujan; línea de tiempo barre; tarjetas suben; push-in de 1,00 a 1,05.
- **Transición:** push-in al bloque de Cálculo; el bloque se convierte en la notificación.

### S4 — Notificación en curso (225 frames)

- **Visual:** lock screen oscura. Hora "08:00" en JetBrains Mono, "Miércoles" pequeño. Notificación: icono de NotiFast, "Clase en curso", "Cálculo Diferencial · Aula 204", "08:00–09:30 · Prof. Rivas". Segunda tarjeta apilada: "Próxima · Física General 10:00". Un anillo de progreso alrededor del icono se dibuja. Tap: la notificación se expande a la vista "Hoy" de la app, con chip "EN CURSO", contador "42 min" en mono, profesor y botón "Ver apuntes". El fondo pasa de oscuro a claro con un wipe radial cálido.
- **Copy:** "La clase ya empezó." → "Tú ya lo sabes."
- **Movimiento:** la notificación cae con spring; la segunda se apila; el anillo se dibuja; expansión por elemento compartido; wipe de 18 frames.
- **Transición:** "Ver apuntes" se expande al editor de notas.

### S5 — Apuntes (240 frames)

- **Visual:** editor de notas "Derivadas — clase 4". Se arman bloques: título, fórmula, checklist y tarjeta de pizarra ilustrada en SVG (sin fotos reales). La fórmula `f'(x) = lim h→0 (f(x+h) − f(x))/h` se revela con un wipe. La pizarra recibe un círculo y una flecha dibujados a mano (SVG path draw). Guardar: "Guardado" toast; chip "Offline".
- **Copy:** "Apuntes que no se pierden."
- **Movimiento:** bloques con stagger de 3; fórmula con wipe de 18 frames; trazo SVG de 24; check de guardado.
- **Transición:** la pila de notas se desliza a la izquierda; la tarjeta del profesor entra desde la derecha, como naipes sobre la mesa.

### S6 — Profesor + WhatsApp (210 frames)

- **Visual:** tarjeta de profesor "Prof. Luis Rivas", "Cálculo Diferencial", "+58 412 123 4567", acciones "WhatsApp" y "Llamar". Tap en WhatsApp: la tarjeta se expande a un chat. Se compone el mensaje "Buenas tardes, profe. ¿Revisa mi avance?"; el botón de enviar se pulsa; la burbuja vuela a la derecha; doble check se dibuja; pequeño glifo de WhatsApp en verde solo en el icono.
- **Copy:** "Tu profe, a un toque."
- **Movimiento:** la tarjeta se eleva; el chat se expande; la burbuja entra con spring; el avión de enviar viaja; los checks se dibujan.
- **Transición:** la burbuja enviada se transforma en una hoja de papel: la portada.

### S7 — Portadas (210 frames)

- **Visual:** hub "Estudio & Portadas". Tres plantillas se abanican: APA 7, IEEE y UNERG. La UNERG pasa al frente: membrete, logo institucional, título "Trabajo de Grado", autora "María F. Pérez", tutor "Prof. L. Rivas", fecha "Noviembre 2026". Las líneas se arman; el badge "UNERG" se estampa con escala y rotación leve; icono DOCX y check "Listo para entregar".
- **Copy:** "Portadas listas para entregar."
- **Movimiento:** abanico con stagger de 4; la seleccionada escala; líneas se dibujan; campos con wipe; estampado del badge.
- **Transición:** el documento baja; la tarjeta de nota sube.

### S8 — Nota (180 frames)

- **Visual:** pantalla de calificaciones. Tarjeta "Cálculo Diferencial" con tres evaluaciones que se llenan: Parcial I 16/20, Parcial II 18/20, Taller 20/20. Un anillo se llena hasta 87,5 %; el número sube de 0.0 a 17.5 en JetBrains Mono; chip "Aprobado" en verde; línea pequeña "Meta 16". Una segunda tarjeta "Física General 16.0" queda al fondo, atenuada, para sugerir más materias.
- **Copy:** "Tu nota, clara."
- **Movimiento:** dígitos ruedan; anillo se dibuja en 36 frames; check pop; sin confeti.
- **Transición:** el anillo se colapsa y se convierte en el aura circular del logo.

### S9 — CTA (150 frames)

- **Visual:** lockup del logo al centro; "NotiFast"; tagline "Todo tu semestre, en orden."; tres píldoras: "Gratis", "Android", "100 % offline"; botón "Descargar gratis"; URL "notifast.pages.dev" en mono. Abajo, el teléfono asoma inclinado mostrando el home con las materias. Hold final de 1,5 s.
- **Copy:** "NotiFast." / "Todo tu semestre, en orden." / "Descargar gratis" / "Android · 100 % offline".
- **Movimiento:** logo con spring; letras con stagger de 2; píldoras pop; botón pulsa una vez; hold.
- **Transición:** ninguna. El último frame se queda quieto.

---

## 7. Reglas de copy

- Usa la copy exacta del storyboard. Puedes ajustar puntuación, no la longitud.
- Una sola frase exterior a la vez. Puedes cambiarla una vez a mitad de escena (máximo dos frases por escena). Las etiquetas de la UI son datos de apoyo, no copy principal.
- Si una pantalla tiene más de 6 elementos activos, elimina hasta que respire. La simplicidad es parte del mensaje.
- Sentence case. Mayúsculas solo en chips pequeños ("EN CURSO", "UNERG").
- Sin "¡Descarga ya!", sin "no esperes más", sin urgencia falsa.
- Español de Venezuela: "profe" en el mensaje de WhatsApp, "profesor" en la UI. "Apuntes", "materias", "horario", "nota", "portada".
- Números con punto decimal, como la app: "17.5". Horas en 24 h: "08:00".
- No inventes testimonios, reseñas, métricas ni "miles de estudiantes". No hay prueba social en este video.
- La marca aparece en la app bar del teléfono y en la CTA. No agregues un logo flotante extra que compita con la copy.

---

## 8. Orquestación multiagéntica

### 8.1 Roles

| Rol | Quién | Responsabilidad | Archivos que toca |
|---|---|---|---|
| **Orquestador** | Tú, sesión principal | Dirección, integración, stage, render final | `src/Video.tsx`, `src/components/*`, `package.json`, `remotion.config.ts` |
| **Recon + design tokens** | Subagente A | Leer repo, capturas y docs; confirmar tokens, fuentes, logo, nombres reales | Solo lectura; entrega `out/qa/recon.md` |
| **Director creativo** | Subagente B | Storyboard, copy, ritmo, transiciones; validar contra restricciones | `src/storyboard.ts`, `out/qa/storyboard-review.md` |
| **Fundación Remotion** | Subagente C | Scaffold, fuentes, `PhoneStage`, `PhoneFrame`, `Headline`, `TapRipple`, `PaperBackground` | `src/components/*`, `src/Root.tsx`, `src/index.ts` |
| **Scene builders** | Subagentes D1–D3 | Construir pantallas independientes en paralelo | Solo sus `src/scenes/SXX*.tsx` |
| **QA + render** | Subagente E | Stills, overflow, contraste, ffprobe, contact sheet, QA report | Solo lectura + comandos; escribe `out/qa/*` |

### 8.2 Fases y gates

**Fase 0 — Recon (orquestador + A).**
Leer `AGENTS.md`, `DESIGN.md`, `docs/02-design/design-system.md`, `landing/vistas/screens/` (solo referencia) y `landing/vistas/fonts/`. Confirmar:
- Logo transparente 750×750 en `landing/assets/logo.png`.
- Fuentes locales en `landing/vistas/fonts/`.
- Capturas de referencia: `20-unerg-import-light.png`, `07-schedules-light.png`, `05-note-editor-light.png`, `11-grades-light.png`, `10-study-hub-light.png`, `25-cover-template-editor-light.png`.
- Nombres de materias, horas, aulas y copy real de la app.
**Gate 0:** existen todos los activos y no hace falta red.

**Fase 1 — Storyboard (B + orquestador).**
Convertir este documento en `src/storyboard.ts` con escenas, tiempos, copy, tema y transición. Revisar que se cumplan las 11 restricciones duras.
**Gate 1:** 9 escenas, 1740 frames, copy ≤ 8 palabras por escena, CTA claro, sin audio.

**Fase 2 — Fundación (C).**
Crear el proyecto Remotion, cargar fuentes locales, construir el stage persistente y una escena de prueba de 3 s con teléfono + headline + logo.
**Gate 2:** `npx remotion render` produce un MP4 de prueba; las fuentes cargan; no hay FOUT; el teléfono se ve nítido.

**Fase 3 — Escenas (D1–D3, en paralelo).**
Repartir:
- D1: S1, S2, S3.
- D2: S4, S5.
- D3: S6, S7, S8, S9.
Cada builder entrega su componente + 3 stills en `out/qa/`. No edita archivos compartidos.
**Gate 3:** cada escena compila, no desborda, usa tokens y se entiende en 3 s.

**Fase 4 — Integración (orquestador).**
Unir el timeline, ajustar transiciones, ritmo, cámara y continuidad. Eliminar cualquier corte seco que rompa la toma continua.
**Gate 4:** el video completo se ve fluido; no hay frames negros; la duración es 58,0 s.

**Fase 5 — Render + QA (E + orquestador).**
Render final, stills, contact sheet, ffprobe, verificación de audio y reporte.
**Gate 5:** todos los checks de la sección 11 en verde.

### 8.3 Contrato para subagentes

Cada subagente recibe:
- Ruta absoluta del repo y de `promo-video/`.
- Su alcance exacto y los archivos que puede editar.
- El contenido de `src/storyboard.ts` y `src/tokens.ts`.
- El contrato de pantalla: `360 × 780` lógicos, sin teléfono, sin fondo, sin headline exterior.
- Criterios de aceptación y comando de verificación.

Cada subagente devuelve:
- Archivos creados/modificados.
- Comando ejecutado y resultado.
- 3 stills en `out/qa/` (inicio, medio, fin de su escena).
- Riesgos o dependencias.

**Plantilla de despacho para un scene builder:**

```text
Eres scene-builder de NotiFast Promo. Trabaja en /home/p0xd/Documentos/proyectos/NotiFast/promo-video.
Alcance: S02 y S03.
Puedes editar solo: src/scenes/S02DaceImportScreen.tsx y src/scenes/S03ScheduleScreen.tsx.
No edites: src/Video.tsx, src/Root.tsx, src/components/*, src/tokens.ts, src/storyboard.ts, package.json.
Entrada: lee src/tokens.ts, src/storyboard.ts y los componentes compartidos existentes.
Entrega: los dos componentes + 3 stills por escena en out/qa/ + el comando que ejecutaste.
Criterios: lienzo 360x780, sin teléfono/fondo/headline, sin overflow, tokens exactos, se entiende en 3 s, código en inglés, copy en español.
Si algo del contrato no está claro, elige la opción más simple y documenta la decisión.
```

**Reglas de convivencia:**
- Solo el orquestador edita `src/Video.tsx`, `src/Root.tsx`, `src/components/*`, `package.json` y `tokens.ts`.
- Cada builder edita solo sus archivos de escena.
- Máximo 3 subagentes en paralelo.
- Si no hay herramienta de subagentes, ejecuta las fases tú mismo en el mismo orden; no omitas los gates.

### 8.4 Contrato de escena

```tsx
// src/scenes/S02DaceImportScreen.tsx
import React from "react";
import { useCurrentFrame, useVideoConfig } from "remotion";

export const S02DaceImportScreen: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  // UI lógica de 360 × 780; el frame es local a la secuencia.
  return <div style={{ width: 360, height: 780 }}>{/* ... */}</div>;
};
```

- La escena **no** renderiza el teléfono, el fondo ni el headline exterior.
- La escena **no** define tiempos globales; el master la coloca con `<Sequence from={...} durationInFrames={...}>`.
- La escena **no** usa `staticFile` para imágenes de UI; todo es CSS/SVG.
- La escena **sí** puede usar `staticFile` para el logo y los logos UNERG.
- La escena debe verse bien en el tercio superior de la pantalla; el tercio inferior puede quedar cubierto por la UI de la plataforma.

---

## 9. Estructura del proyecto

```text
promo-video/
├── package.json
├── remotion.config.ts
├── tsconfig.json
├── public/
│   ├── fonts/
│   │   ├── PlusJakartaSans-Regular.ttf
│   │   ├── PlusJakartaSans-Medium.ttf
│   │   ├── PlusJakartaSans-SemiBold.ttf
│   │   ├── PlusJakartaSans-Bold.ttf
│   │   ├── JetBrainsMono-Regular.ttf
│   │   ├── JetBrainsMono-Medium.ttf
│   │   └── JetBrainsMono-SemiBold.ttf
│   └── brand/
│       ├── notifast-logo.png
│       ├── unerg-ais.png
│       └── unerg-banner.png
├── src/
│   ├── index.ts
│   ├── Root.tsx
│   ├── Video.tsx
│   ├── tokens.ts
│   ├── storyboard.ts
│   ├── components/
│   │   ├── PaperBackground.tsx
│   │   ├── PhoneStage.tsx
│   │   ├── PhoneFrame.tsx
│   │   ├── Headline.tsx
│   │   ├── TapRipple.tsx
│   │   ├── SafeArea.tsx
│   │   └── ui/
│   │       ├── AppBar.tsx
│   │       ├── StatusBar.tsx
│   │       ├── SubjectChip.tsx
│   │       ├── NotiFastCard.tsx
│   │       └── ProgressRing.tsx
│   └── scenes/
│       ├── S01Hook.tsx
│       ├── S02DaceImportScreen.tsx
│       ├── S03ScheduleScreen.tsx
│       ├── S04NotificationsScreen.tsx
│       ├── S05NotesScreen.tsx
│       ├── S06TeachersScreen.tsx
│       ├── S07CoversScreen.tsx
│       ├── S08GradesScreen.tsx
│       └── S09Cta.tsx
├── out/
│   ├── qa/
│   ├── notifast-promo-1080x1920.mp4
│   ├── poster.jpg
│   └── contact-sheet.jpg
├── README.md
└── QA.md
```

**No** commitees `node_modules/` ni `out/qa/` si el repo prefiere artefactos fuera de git. Sí commitea el código fuente, el README, el QA.md y el MP4 final si el tamaño es razonable.

---

## 10. Implementación técnica

### 10.1 Stack

- Remotion 4 + TypeScript + React 18.
- `remotion`, `@remotion/cli`, `react`, `react-dom`.
- Sin librerías de animación externas. Usa `spring`, `interpolate`, `Easing`, `Sequence`, `AbsoluteFill`.
- Si el entorno ya tiene **HyperFrames** instalado y funcional, puedes usarlo como alternativa. En ese caso, mantén el mismo storyboard, los mismos tokens y el mismo contrato de salida; usa HTML/CSS/GSAP con seek determinista y no mezcles ambos frameworks.

### 10.2 Composición

```tsx
<Composition
  id="NotiFastPromo"
  component={NotiFastPromo}
  durationInFrames={1740}
  fps={30}
  width={1080}
  height={1920}
/>
```

### 10.3 Stage persistente

`PhoneStage` recibe:
- `theme`: "light" | "dark".
- `headline`: string.
- `highlight`: palabra o frase a subrayar.
- `phoneY`: posición vertical.
- `phoneScale`: escala.
- `children`: la pantalla de la escena.

El teléfono se mantiene en todas las escenas de UI. La cámara hace push-in/out. El fondo cambia a oscuro solo en S4. La headline exterior se anima con `Headline` y desaparece antes de la transición.

### 10.4 Teléfono

- Marco exterior: 760 × 1600 px, radio 56 px, bisel 12 px, color `#0B0F17`, borde exterior 2 px `#1F2937`.
- Pantalla: 720 × 1560 px, radio 44 px, `overflow: hidden`.
- Contenido lógico: 360 × 780, `transform: scale(2)`, `transformOrigin: "top left"`.
- Cámara frontal: punto de 8 px en el centro superior, color `#0B0F17`.
- Sin reflejos, sin brillos, sin sombras 3D. Es un mockup plano, no un render.
- Posición por defecto: `x = 160`, `y = 560`; el inferior puede quedar recortado por el frame. El texto exterior vive entre 220 y 480 px.

### 10.5 Fuentes

- Copia los `.ttf` desde `landing/vistas/fonts/` a `public/fonts/`.
- Define `@font-face` con `font-display: block`.
- Usa `delayRender("fonts")` + `document.fonts.load(...)` + `continueRender(handle)` para evitar FOUT.
- Prohibido usar Google Fonts por red en el render. Si una fuente falta, el render debe fallar, no caer a Arial/Inter.

### 10.6 Animación

- Todo se calcula con `useCurrentFrame()` y `useVideoConfig()`.
- `spring()` para entradas; `interpolate()` con clamp para opacidad, escala y posición.
- Los números (17.5, 42 min, 4 materias) se animan con `interpolate` + `Math.round` y `tabular-nums`.
- Los checks y subrayados se dibujan con `strokeDasharray`/`strokeDashoffset`.
- Las transiciones de escena usan 10–14 frames de solape; el teléfono no se mueve más de 40 px entre escenas.
- No uses `setTimeout` ni animaciones CSS con `animation-delay`; Remotion necesita control por frame.

### 10.7 Rendimiento

- Evita decenas de capas con blur. El grano y el aura son SVG/CSS estáticos.
- Renderiza previews a 540 × 960 para revisar; el final siempre a 1080 × 1920.
- Usa `--concurrency=4` o el valor que permita el equipo.
- Si el render tarda demasiado, simplifica sombras y blur, no la UI.

---

## 11. QA y verificación

### 11.1 Comandos

```bash
cd promo-video
npm install
npx remotion studio
npx remotion render NotiFastPromo out/notifast-promo-1080x1920.mp4 \
  --codec=h264 --crf=18 --pixel-format=yuv420p --concurrency=4
npx remotion still NotiFastPromo out/poster.jpg --frame=0
```

### 11.2 Verificación técnica

```bash
ffprobe -v error -select_streams v:0 \
  -show_entries stream=width,height,r_frame_rate,duration,codec_name,pix_fmt \
  -of default=noprint_wrappers=1 out/notifast-promo-1080x1920.mp4

# Debe imprimir vacío: no debe existir stream de audio.
ffprobe -v error -select_streams a -show_entries stream=index -of csv=p=0 \
  out/notifast-promo-1080x1920.mp4
```

Si aparece una pista de audio vacía, remuxea:

```bash
ffmpeg -i out/notifast-promo-1080x1920.mp4 -c copy -an \
  out/notifast-promo-1080x1920-silent.mp4
```

### 11.3 Checklist de aceptación

- [ ] 1080 × 1920, 30 fps, 58,0 s (±0,1 s), H.264, `yuv420p`.
- [ ] Sin stream de audio.
- [ ] Frame 0 legible como miniatura; no empieza en negro.
- [ ] 9 escenas; una headline exterior a la vez; copy ≤ 8 palabras (CTA ≤ 12).
- [ ] Todo el texto esencial dentro de las zonas seguras (top 220, bottom 340, left 96, right 120).
- [ ] Contraste ≥ 4,5:1 en todo el texto.
- [ ] Sin capturas de pantalla incrustadas; toda la UI es código.
- [ ] Sin emojis, sin stock, sin imágenes generadas por IA, sin personas.
- [ ] Sin cortes secos salvo el beat de notificaciones; sin frames negros.
- [ ] El teléfono se mantiene estable; la cámara no marea.
- [ ] La notificación se entiende sin audio: "Clase en curso", materia, aula y hora.
- [ ] El CTA se lee: "Descargar gratis", "Android · 100 % offline", URL.
- [ ] Todos los activos son locales; el render funciona sin red.
- [ ] `README.md` explica cómo renderizar; `QA.md` incluye evidencia.
- [ ] El video se entiende completo con el sonido apagado.

### 11.4 Stills obligatorios

Genera y revisa al menos:
- Frame 0 (hook/miniatura).
- Frame 90 (importación DACE).
- Frame 360 (horario).
- Frame 600 (notificación).
- Frame 850 (apuntes).
- Frame 1090 (WhatsApp).
- Frame 1300 (portada).
- Frame 1500 (nota).
- Frame 1700 (CTA).
- Último frame.

Guárdalos en `out/qa/frame-XXXX.jpg`. Si algo se desborda o se ve pequeño, corrige antes del render final.

---

## 12. Criterios de éxito

El video está listo cuando:

1. Un estudiante de la UNERG reconoce el DACE en los primeros 3 segundos.
2. Entiende los 6 beneficios sin que se los enumeren con texto.
3. Siente que la app es simple, rápida y hecha para él.
4. Sabe exactamente qué hacer al final: descargar NotiFast.
5. Se ve bien sin audio, en un móvil, a 1080 × 1920.
6. No hay un solo frame que parezca una diapositiva.
7. El render es reproducible con `npm install && npm run render`.

**Fracaso:** si el video necesita explicaciones, si el texto tapa la UI, si hay más de una idea por escena, si el CTA no se lee, si el resultado se siente como una plantilla genérica de TikTok o si el estudiante no reconoce su propio horario DACE.

---

## 13. Entregables

1. `promo-video/` con todo el código fuente.
2. `promo-video/out/notifast-promo-1080x1920.mp4` — video final.
3. `promo-video/out/poster.jpg` — frame 0 o póster diseñado.
4. `promo-video/out/contact-sheet.jpg` — 9–12 stills en una hoja.
5. `promo-video/out/qa/` — stills y evidencia.
6. `promo-video/README.md` — comandos, estructura, créditos de fuentes y activos.
7. `promo-video/QA.md` — checklist, salida de ffprobe, duración, codec, audio y notas.

---

## 14. Reglas finales para el orquestador

- **No entregues un plan.** Entrega el MP4 y la evidencia.
- **No preguntes por detalles creativos.** Las decisiones están tomadas. Solo pregunta si hay un bloqueo técnico que impida renderizar.
- **Elige lo simple.** Si una transición es demasiado compleja, usa un crossfade de 10 frames con posiciones coincidentes. Si una escena tiene demasiados elementos, elimina hasta que se entienda en 3 segundos.
- **No rompas la marca.** Si dudas, vuelve a `docs/02-design/design-system.md` y a los tokens de este prompt.
- **Verifica antes de decir "listo".** Sin `ffprobe`, sin stills y sin render final, no está terminado.
- **Reporta al final:** ruta del MP4, duración, resolución, fps, codec, tamaño, confirmación de ausencia de audio, 3 stills clave y cualquier desviación del storyboard.

> **Norte creativo:** si el video se sintiera como una app que ordena tu semestre, y no como un anuncio que te grita, está bien. Si se sintiera como una lista de funciones con música, está mal.
