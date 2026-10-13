# Landing Page NotiFast — Especificación de Diseño Técnico & Visual

- **Fecha:** 2026-10-13
- **Rama:** `landing-page`
- **Estado:** Propuesta para Aprobación
- **Autor / Rol:** Ingeniero & Diseñador UI/UX Pro Max Master
- **Documentos de Referencia:** `PRODUCT.md`, `DESIGN.md`, `app/src/main/res/values/strings.xml`, `NotiFastStudyBackground.kt`

---

## 1. Intención y Propósito ("The Why")

### Contexto
NotiFast es un cuaderno académico inteligente y hub de estudio para estudiantes de secundaria y universidad (Android nativo). Esta landing page es la pieza de marketing y adquisición principal del producto.

### Propósito Nuclear
Transmitir en menos de 5 segundos la promesa central del producto:
> **"Todo tu estudio, en un solo lugar."**
> Apuntes limpios, materias personalizadas, cálculo automático de promedios ponderados y agenda con detección de solapamientos, sin tener que saltar entre aplicaciones dispersas.

### Estrella Polar de Conversión
Llevar al estudiante al botón de acción principal (CTA): **"Descargar en Google Play"** (botón configurado con selector/enlace centralizado para enlazar a la tienda o APK en cuanto esté disponible).

---

## 2. Objetivos, No-Objetivos y Anti-Metas

### Objetivos (Goals)
1. **Identidad Visual 100% Coherente:** Implementar fielmente el arquetipo *Tactile Papercraft / Muted Crimson & Aqua Green* definido en `DESIGN.md`:
   - Fondo cálido pergamino (`#FAF9F6`) con contraste AAA sobre tinta Oxford (`#181A1F`).
   - Acentos primarios Muted Crimson (`#9E1B32`) y secundarios Studio Aqua Green (`#2A7B7B`), con Forest Sage (`#15803D`) para estados de aprobación.
   - Tipografía dual: `Plus Jakarta Sans` para titulares y narrativa; `JetBrains Mono` para métricas académicas (promedios, porcentajes, horas).
2. **Dinámica de Fondo Reactiva (Linear + Duolingo):**
   - Fondo vivo con stickers vectoriales SVG inspirados en `NotiFastStudyBackground` (cuaderno, matraz, calculadora, birrete, lápiz, bombilla, funciones matemáticas).
   - Movimiento parallax sutil y flotación suave con resortes que responden al scroll del usuario.
3. **Showcase del Dispositivo Interactivo (Scrollytelling tipo Linear):**
   - Mockup hiper-realista de un smartphone Android con marco estilizado y pantalla que recrea con precisión milimétrica las vistas reales de la app:
     1. **Vista Apuntes & Materias:** Tarjetas Bento con materias coloreadas según la paleta del estudiante (Cálculo, Física, Literatura), tags y conteo de apuntes.
     2. **Vista Calificaciones & Promedio:** Dashboard con promedio ponderado general (`8.7/10.0`), progreso circular y tarjetas de materia con estado "Aprobando".
     3. **Vista Agenda & Horarios:** Timeline del día con detección visual de solapamiento y badge de "Próxima clase".
     4. **Vista Detalle & Simulación:** Tarjeta de simulación rápida de nota necesaria para aprobar.
   - A medida que el usuario hace scroll por las secciones de texto explicativo a la izquierda, la pantalla del teléfono transiciona fluidamente a la vista correspondiente con animaciones GSAP.
4. **Despliegue Cero-Fricción (Turnkey & Easy Deploy):**
   - Arquitectura Jamstack pura: HTML5 semántico, CSS3 moderno con Design Tokens y Vanilla JS + GSAP/ScrollTrigger.
   - 0 dependencias de compilación externas obligatorias, carga en <300ms, puntuación Lighthouse 100/100.
   - Compatible directo con Firebase Hosting mediante `"public": "landing"` en `firebase.json`, GitHub Pages, Vercel o Netlify.
5. **Modo Oscuro & Accesibilidad:**
   - Soporte automático para `prefers-color-scheme: dark` ("Mesa de Estudio Nocturna" `#0E131F`).
   - Respeto estricto a `prefers-reduced-motion` (desactiva transiciones complejas manteniendo el diseño estático impecable).
   - Cumplimiento estricto WCAG AA/AAA en todos los textos e iconos.

### No-Objetivos (Non-Goals)
- No incluir testimonios de estudiantes ficticios o reseñas fabricadas.
- No incluir estadísticas infladas ni menciones académicas de tesis de grado (enfoque puramente comercial de producto).
- No requerir backend dinámico o base de datos en la landing (es 100% estática y ligera).

### Anti-Metas (Anti-Goals)
- **Cero Gamificación Estridente:** No usar sonidos estridentes, mascotas invasivas ni popups agresivos tipo ruleta de descuentos. La estética es de calma, estudio y alta precisión.
- **Cero Sombras Negras Pesadas o Desenfocadas:** El volumen se logra con micro-bordes de 1px (`#E5DFD7`) y superficies tonales pergamino.
- **Cero Bloqueo de Scroll (Scrolljacking forzado):** El scroll del usuario conserva la inercia nativa del navegador; las animaciones reaccionan a la posición de scroll sin secuestrar la rueda del ratón ni el swipe táctil.

---

## 3. Arquitectura de Información y Secciones de la Landing

```
┌─────────────────────────────────────────────────────────────┐
│ 1. NAVBAR SUPERIOR STICKY                                   │
│    Logo NotiFast + Badge "v3.0 Cuaderno Inteligente"        │
│    Links rápidos: Apuntes | Calificaciones | Agenda         │
│    Botón CTA: "Descargar App"                               │
├─────────────────────────────────────────────────────────────┤
│ 2. HERO SECTION ("El Cuaderno que Piensa Contigo")          │
│    Titular de alto impacto + Subtítulo de serenidad         │
│    Botones CTA: [Descargar en Google Play] [Ver demo]       │
│    Floating Subject Chips interactivos                      │
├─────────────────────────────────────────────────────────────┤
│ 3. SCROLLYTELLING SHOWCASE ("Todo tu Estudio en una Sola App")│
│    Teléfono fijo sticky con cambio de pantalla por fase:    │
│    • Paso 1: Apuntes & Cuaderno Digital (Markdown + Bento)  │
│    • Paso 2: Calificaciones & Promedio Ponderado en Vivo    │
│    • Paso 3: Agenda Inteligente & Cero Choques de Horario   │
├─────────────────────────────────────────────────────────────┤
│ 4. BENTO GRID DE SUPERPODERES ACADÉMICOS                   │
│    4.1 Cálculo Automático de Promedios Ponderados           │
│    4.2 Sincronización Segura en la Nube                     │
│    4.3 Notificaciones Preventivas sin Ansiedad              │
│    4.4 Paleta Cromática Personalizable por Materia          │
├─────────────────────────────────────────────────────────────┤
│ 5. HERO CTA FINAL & DESCARGA                                │
│    Caja de descarga destacada con micro-confetti sutil      │
│    Badge oficial de Google Play + Requisitos (Android 8+)   │
├─────────────────────────────────────────────────────────────┤
│ 6. FOOTER EDITORIAL                                         │
│    Identidad de marca, enlaces legales y modo de estudio    │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. Sistema Visual y Design Tokens (Directo de `DESIGN.md`)

### Colores
```css
:root {
  /* Fondo y Superficies — Warm Paper Canvas */
  --bg-canvas: #FAF9F6;
  --bg-surface: #FFFFFF;
  --bg-surface-container: #F3EFEA;
  --bg-surface-container-high: #EBE6DF;
  --border-hairline: #E5DFD7;

  /* Tipografía y Énfasis */
  --text-high: #181A1F;
  --text-medium: #4B5563;
  --text-muted: #6B7280;

  /* Identidad Primaria & Secundaria */
  --brand-primary: #9E1B32;        /* Muted Crimson */
  --brand-primary-light: #FDE8EB;
  --brand-secondary: #2A7B7B;      /* Studio Aqua Green */
  --brand-secondary-light: #D6F2F0;
  --brand-success: #15803D;        /* Forest Sage */
  --brand-success-light: #DCFCE7;
  --brand-amber: #D97706;          /* Studio Amber */

  /* Materias de Demostración */
  --subject-calculus: #2563EB;     /* Azul Cálculo */
  --subject-physics: #7C3AED;      /* Violeta Física */
  --subject-literature: #D97706;   /* Ámbar Literatura */
  --subject-chemistry: #059669;    /* Esmeralda Química */
}

@media (prefers-color-scheme: dark) {
  :root {
    --bg-canvas: #0E131F;
    --bg-surface: #161E2D;
    --bg-surface-container: #1C2638;
    --bg-surface-container-high: #202A3C;
    --border-hairline: #2A384E;
    --text-high: #F1F5F9;
    --text-medium: #94A3B8;
    --text-muted: #64748B;
    --brand-primary: #E05A6E;
    --brand-primary-light: #5C1120;
    --brand-secondary: #38B2AC;
    --brand-secondary-light: #0E4B47;
    --brand-success: #34D399;
    --brand-success-light: #065F46;
  }
}
```

### Tipografía
- Títulos y cuerpo: `Plus Jakarta Sans` (pesos 400, 500, 600, 700).
- Métricas, promedios y etiquetas técnicas: `JetBrains Mono` (pesos 500, 600).
- Cargadas localmente vía `@font-face` con respaldo de fuentes del sistema (`system-ui`, `monospace`).

---

## 5. Decisiones Técnicas y de Despliegue

1. **Ubicación en el Repositorio:**
   - La landing page reside en `/landing/`:
     - `landing/index.html`: Estructura semántica, accesibilidad y SVGs inline de alta resolución.
     - `landing/css/styles.css`: Sistema de tokens, layout responsive (mobile-first a desktop ultrawide), micro-bordes y temas.
     - `landing/js/main.js`: Lógica de interacción, inicialización de GSAP ScrollTrigger, física de flotación de stickers y alternancia de vistas del teléfono.
     - `landing/assets/`: Iconos vectoriales, favicons y recursos gráficos.
2. **Despliegue:**
   - Configuración en `firebase.json` agregando la sección `hosting`:
     ```json
     "hosting": {
       "public": "landing",
       "ignore": ["firebase.json", "**/.*", "**/node_modules/**"]
     }
     ```
   - Despliegue con 1 solo comando: `firebase deploy --only hosting`.
   - Previsualización local inmediata: `python3 -m http.server 3000 --directory landing` o cualquier servidor estático.

---

## 6. Verificación del Constructor (Builder Check)

- **¿Tiene dependencias externas que requieran Node o npm para funcionar?**
  No. Los scripts de GSAP y ScrollTrigger se incluyen de forma autónoma (vendor local o CDN de alta disponibilidad), garantizando que el proyecto sea 100% reproducible y liviano.
- **¿Qué ocurre si el usuario tiene una pantalla pequeña (móvil)?**
  En pantallas `<768px`, el showcase de scrollytelling se transforma de forma natural en un carrusel de tarjetas apiladas o pestañas interactivas, asegurando que el teléfono siempre se visualice sin recortes horizontales ni scroll roto.
- **¿Qué ocurre con el botón de Google Play?**
  Se define una constante configurable `const PLAY_STORE_URL = "#descargar"` en `main.js` y en el atributo `href` del botón. Al hacer clic antes del lanzamiento, muestra un toast elegante: *"NotiFast estará disponible muy pronto en Google Play Store"*, con opción de descargar el APK directo si se especifica.
