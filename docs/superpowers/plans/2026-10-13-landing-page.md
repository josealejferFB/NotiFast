# Landing Page NotiFast Implementation Plan (Hardened with UI/UX Pro Max & Systematic Debugging)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Construir una landing page interactiva, moderna y de alto impacto para la app móvil NotiFast, con diseño inspirado en Duolingo (stickers e iconografía colorida de estudio) y scrollytelling al estilo Linear (mockup de smartphone reactivo al scroll), desplegable en Firebase Hosting con cero fricción.

**Architecture:** Arquitectura Jamstack pura basada en estándares web modernos (HTML5 semántico, CSS3 con tokens de `DESIGN.md`, Vanilla JS modular y GSAP + ScrollTrigger para animaciones suaves a 60fps). Los componentes y pantallas de la app móvil se recrean vectorialmente con micro-bordes táctiles y tipografía de precisión.

**Tech Stack:** HTML5 Semántico, CSS3 Custom Properties (Material 3 + Tactile Papercraft), Vanilla JavaScript (ES6+), GSAP 3.12 + ScrollTrigger, SVG vectoriales inline (zero-CORS), Firebase Hosting.

**Spec:** `docs/superpowers/specs/2026-10-13-landing-page-design.md`

## Global Constraints

- Directorio de la landing: `landing/` en la raíz del repositorio.
- Paleta obligatoria: Muted Crimson (`#9E1B32`), Studio Aqua (`#2A7B7B`), Warm Paper (`#FAF9F6`), Ink Oxford (`#181A1F`), Forest Sage (`#15803D`).
- Soporte estricto para `prefers-color-scheme: dark` ("Mesa de Estudio Nocturna" `#0E131F`).
- Soporte estricto para `prefers-reduced-motion` (desactivación de movimientos complejos manteniendo la UI legible).
- Cero dependencias pesadas de empaquetado en tiempo de compilación (cero fallos por falta de Node o npm global).
- El botón de Google Play no debe romperse: debe abrir un toast interactivo que avisa sobre la disponibilidad o redirige al enlace configurado.
- La configuración de Firebase Hosting debe estar en `firebase.json` bajo la clave `"hosting": { "public": "landing" }`.
- Accesibilidad estricta WCAG AA/AAA: contraste de texto $\ge 4.5:1$ ($\ge 7:1$ para títulos), áreas táctiles mínimas de 48px, skip link para navegación por teclado y anillos `:focus-visible` de alto contraste.

## Review Focus (Hardened by Systematic Debugging & UI/UX Pro Max)

1. **Pantallas pequeñas (< 992px):** No usar pinning forzado en móviles para evitar bloqueos de scroll (scroll-traps). Emplear `gsap.matchMedia()` para activar el pin solo en desktop ($\ge 992px$) y en móvil ofrecer pestañas interactivas accesibles con teclado y toque.
2. **Preferencia de movimiento reducido (`prefers-reduced-motion`):** Detener inmediatamente flotaciones continuas y parallax en el fondo; renderizar el teléfono y las secciones en su estado final legible sin transiciones intrusivas.
3. **Prevención de fallos CORS con SVGs:** Utilizar un sistema de sprites SVG embebido inline en el HTML para evitar bloqueos de seguridad de origen en entornos locales (`file://` o puertos dinámicos).
4. **Resistencia a fallos de red / CDN Fallback (Degradación elegante):** Si GSAP o ScrollTrigger no cargan desde la CDN, el contenido debe mantenerse 100% visible y legible mediante CSS por defecto (`opacity: 1`), con interacción de pestañas mediante Vanilla JS nativo.
5. **Ergonomía táctil y física de resortes:** Botones con altura mínima de 48px, separación $\ge 8px$ y micro-interacción al presionar de resorte táctil (`scale(0.97)` con easing elástico).

---

### Task 1: Configuración de Firebase Hosting, Skip Link y Suite de Pruebas de Estructura

**Files:**
- Create: `tests/test_landing_structure.py`
- Modify: `firebase.json`
- Create: `landing/index.html` (esqueleto base inicial con skip-link)

**Interfaces:**
- Consumes: Configuración del proyecto `firebase.json`
- Produces: Estructura de carpetas `/landing/` y suite de validación `tests/test_landing_structure.py`

- [ ] **Step 1: Write the failing test**

Crear `tests/test_landing_structure.py` validando que `firebase.json` tiene la sección `hosting` apuntando a `landing`, que `landing/index.html` existe con marcado semántico (`<header>`, `<main id="main-content">`, `<footer>`), skip-link accesible (`<a href="#main-content" class="skip-link">`), etiquetas meta de viewport y título que contenga "NotiFast".

```python
import json
import os
import unittest

class TestLandingStructure(unittest.TestCase):
    def test_firebase_hosting_configured(self):
        self.assertTrue(os.path.exists("firebase.json"))
        with open("firebase.json", "r", encoding="utf-8") as f:
            data = json.load(f)
        self.assertIn("hosting", data)
        self.assertEqual(data["hosting"].get("public"), "landing")

    def test_landing_index_exists_and_valid(self):
        self.assertTrue(os.path.exists("landing/index.html"))
        with open("landing/index.html", "r", encoding="utf-8") as f:
            content = f.read()
        self.assertIn("<!DOCTYPE html>", content)
        self.assertIn("<meta name=\"viewport\"", content)
        self.assertIn("NotiFast", content)
        self.assertIn("skip-link", content)
        self.assertIn("<header", content)
        self.assertIn("<main id=\"main-content\"", content)
        self.assertIn("<footer", content)

if __name__ == "__main__":
    unittest.main()
```

- [ ] **Step 2: Run test to verify it fails**

Run: `python3 -m unittest tests/test_landing_structure.py`
Expected: FAIL (`AssertionError` o `FileNotFoundError` porque `landing/index.html` aún no existe).

- [ ] **Step 3: Implement firebase hosting config & landing scaffolding**

Actualizar `firebase.json` para incluir `"hosting": { "public": "landing", "ignore": ["firebase.json", "**/.*"] }` y crear el esqueleto base `landing/index.html` con skip link accesible.

- [ ] **Step 4: Run test to verify it passes**

Run: `python3 -m unittest tests/test_landing_structure.py`
Expected: PASS (OK).

- [ ] **Step 5: Commit**

```bash
git add firebase.json landing/index.html tests/test_landing_structure.py
git commit -m "chore: configurar firebase hosting y esqueleto base accesible de landing"
```

---

### Task 2: Sistema de Tokens CSS, Tipografía, Focus Rings y Estilos Base

**Files:**
- Create: `landing/css/tokens.css`
- Create: `landing/css/styles.css`
- Modify: `landing/index.html`
- Modify: `tests/test_landing_structure.py`

**Interfaces:**
- Consumes: Tokens de `DESIGN.md` (colores, espaciados, radios de borde) y guías de UI/UX Pro Max
- Produces: Variables CSS `:root` con modo claro, modo oscuro, focus rings visibles y tipografía importada

- [ ] **Step 1: Write the failing test**

Agregar test en `tests/test_landing_structure.py` verificando que `landing/css/tokens.css` y `landing/css/styles.css` existen, y que contienen las variables obligatorias `--bg-canvas: #FAF9F6`, `--brand-primary: #9E1B32`, `--brand-secondary: #2A7B7B`, `:focus-visible`, el selector `@media (prefers-color-scheme: dark)` y `@media (prefers-reduced-motion)`.

```python
    def test_css_tokens_defined(self):
        self.assertTrue(os.path.exists("landing/css/tokens.css"))
        with open("landing/css/tokens.css", "r", encoding="utf-8") as f:
            css = f.read()
        self.assertIn("#FAF9F6", css)
        self.assertIn("#9E1B32", css)
        self.assertIn("#2A7B7B", css)
        self.assertIn("prefers-color-scheme: dark", css)
        self.assertIn("prefers-reduced-motion", css)

        with open("landing/css/styles.css", "r", encoding="utf-8") as f:
            styles = f.read()
        self.assertIn(":focus-visible", styles)
```

- [ ] **Step 2: Run test to verify it fails**

Run: `python3 -m unittest tests/test_landing_structure.py`
Expected: FAIL (`tokens.css` no existe).

- [ ] **Step 3: Implement tokens.css and styles.css**

Crear `landing/css/tokens.css` con todas las variables semánticas de `DESIGN.md` (Light y Dark mode, radios, fuentes `Plus Jakarta Sans` y `JetBrains Mono`) y `landing/css/styles.css` con reset moderno, micro-bordes de 1px, `:focus-visible` ring de 2px, botones táctiles con resorte `scale(0.97)` y áreas de toque mínimas de 48px.

- [ ] **Step 4: Run test to verify it passes**

Run: `python3 -m unittest tests/test_landing_structure.py`
Expected: PASS (OK).

- [ ] **Step 5: Commit**

```bash
git add landing/css/ tests/test_landing_structure.py landing/index.html
git commit -m "feat(landing): implementar sistema de tokens css, tema claro/oscuro y accesibilidad"
```

---

### Task 3: Pack de Stickers de Estudio Vectoriales (Zero-CORS) y Fondo Parallax Reactivo

**Files:**
- Create: `landing/assets/stickers/stickers.svg`
- Create: `landing/js/background.js`
- Modify: `landing/index.html` (inclusión de sprite SVG inline para prevenir bloqueos CORS)
- Modify: `tests/test_landing_structure.py`

**Interfaces:**
- Consumes: Íconos y animaciones de `NotiFastStudyBackground.kt`
- Produces: Sistema de fondo flotante con SVGs coloridos interactivos al scroll

- [ ] **Step 1: Write the failing test**

Agregar test validando que los 8 símbolos vectoriales clave de la app existen (cuaderno `icon-book`, lápiz `icon-pencil`, birrete `icon-school`, matraz `icon-science`, calculadora `icon-calculator`, bombilla `icon-bulb`, funciones `icon-function`, trofeo `icon-trophy`), embebidos inline en `landing/index.html` para garantizar 0 fallos CORS. También verificar que `landing/js/background.js` gestiona la flotación y atiende `prefers-reduced-motion`.

```python
    def test_stickers_and_background_js_exist(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        for icon in ["icon-book", "icon-school", "icon-science", "icon-calculator", "icon-bulb"]:
            self.assertIn(f'id="{icon}"', html)

        self.assertTrue(os.path.exists("landing/js/background.js"))
        with open("landing/js/background.js", "r", encoding="utf-8") as f:
            bg_js = f.read()
        self.assertIn("prefers-reduced-motion", bg_js)
```

- [ ] **Step 2: Run test to verify it fails**

Run: `python3 -m unittest tests/test_landing_structure.py`
Expected: FAIL (`icon-book` y `background.js` no se encuentran).

- [ ] **Step 3: Implement vector stickers and dynamic background**

1. Diseñar el set de vectores en SVG con estilo plano y vibrante de estudio (Duolingo style) con los colores del subject engine de NotiFast.
2. Embeber el `<svg style="display:none"><defs>...` directamente en `landing/index.html` para máxima fiabilidad (zero CORS).
3. Implementar `landing/js/background.js` con generación de partículas de fondo flotantes, efecto parallax ligado al scroll y detección automática de `prefers-reduced-motion`.

- [ ] **Step 4: Run test to verify it passes**

Run: `python3 -m unittest tests/test_landing_structure.py`
Expected: PASS (OK).

- [ ] **Step 5: Commit**

```bash
git add landing/assets/ landing/js/background.js landing/index.html tests/test_landing_structure.py
git commit -m "feat(landing): agregar stickers vectoriales zero-cors y fondo interactivo reactivo"
```

---

### Task 4: Recreación Vectorial del Smartphone y Pantallas de la App con Degradación Elegante

**Files:**
- Create: `landing/css/phone-mockup.css`
- Modify: `landing/index.html`
- Modify: `tests/test_landing_structure.py`

**Interfaces:**
- Consumes: Diseños de `NotesScreen`, `GradesScreen` (MainAverageDashboard), `SchedulesScreen`
- Produces: Componente de smartphone con 3 pantallas hiper-fieles alternables mediante clases CSS y visibles sin JS

- [ ] **Step 1: Write the failing test**

Agregar test validando que `landing/index.html` contiene el contenedor del teléfono (`#phone-mockup`), la barra de estado superior, la barra de navegación inferior con los 4 iconos (`Apuntes`, `Agenda`, `Calificaciones`, `Ajustes`), y los 3 paneles de pantalla:
1. `.screen-notes`: con tarjetas Bento de materias (Cálculo, Física, Literatura) y tags de apuntes.
2. `.screen-grades`: con el promedio general `8.7 / 10.0`, indicador circular y badge "Aprobando".
3. `.screen-agenda`: con eventos de timeline y advertencia visual de solapamiento.

```python
    def test_phone_mockup_screens_exist(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn("phone-mockup", html)
        self.assertIn("screen-notes", html)
        self.assertIn("screen-grades", html)
        self.assertIn("screen-agenda", html)
        self.assertIn("8.7", html)
        self.assertIn("Aprobando", html)
```

- [ ] **Step 2: Run test to verify it fails**

Run: `python3 -m unittest tests/test_landing_structure.py`
Expected: FAIL (`screen-notes`, `screen-grades`, etc. no se encuentran).

- [ ] **Step 3: Implement phone mockup and screens in HTML/CSS**

Construir en `landing/index.html` y `landing/css/phone-mockup.css`:
- Chasis de teléfono con borde metálico sutil, isla de cámara o notch pulido, sombra ambiental suave.
- Pantalla interna con aspect ratio adaptable.
- **Pantalla 1 (Apuntes):** Top bar "Mis Apuntes", buscador táctil, Bento de materias con acentos cromáticos reales, tarjetas de apuntes recientes con texto en markdown renderizado.
- **Pantalla 2 (Calificaciones):** Top bar "Mis Calificaciones", tarjeta hero con promedio `8.7`, chip verde Forest Sage "Aprobando", histograma de notas y lista de asignaturas ponderadas.
- **Pantalla 3 (Agenda):** Top bar "Mi Agenda", selector de día, timeline con bloques de clases y tarjeta de alerta proactiva "Conflicto de horario detectado".
- Regla CSS de degradación elegante: en modo sin JS o móvil, las pantallas se presentan apiladas o en pestañas interactivas nativas.

- [ ] **Step 4: Run test to verify it passes**

Run: `python3 -m unittest tests/test_landing_structure.py`
Expected: PASS (OK).

- [ ] **Step 5: Commit**

```bash
git add landing/css/phone-mockup.css landing/index.html tests/test_landing_structure.py
git commit -m "feat(landing): recrear mockup de smartphone con pantallas fieles de apuntes, notas y agenda"
```

---

### Task 5: Motor de Scrollytelling con `gsap.matchMedia()` y Fallback Nativo

**Files:**
- Create: `landing/js/scrollytelling.js`
- Create: `landing/js/main.js`
- Modify: `landing/index.html`
- Modify: `tests/test_landing_structure.py`

**Interfaces:**
- Consumes: Secciones de texto `.story-step` y pantallas `.screen-*` del teléfono
- Produces: Transición fluida del smartphone fijado con GSAP ScrollTrigger en desktop ($\ge 992px$), pestañas en móvil (< 992px) y fallback sin GSAP

- [ ] **Step 1: Write the failing test**

Agregar test validando que `landing/js/scrollytelling.js` y `landing/js/main.js` existen, que utilizan `gsap.matchMedia()` para aislar el pinning en desktop y desactivarlo en móvil, y que incluyen selector de pestañas accesible para teclado y móvil.

```python
    def test_scrollytelling_js_exists(self):
        self.assertTrue(os.path.exists("landing/js/scrollytelling.js"))
        self.assertTrue(os.path.exists("landing/js/main.js"))
        with open("landing/js/scrollytelling.js", "r", encoding="utf-8") as f:
            code = f.read()
        self.assertIn("ScrollTrigger", code)
        self.assertIn("matchMedia", code)
        self.assertIn("992px", code)
```

- [ ] **Step 2: Run test to verify it fails**

Run: `python3 -m unittest tests/test_landing_structure.py`
Expected: FAIL (`scrollytelling.js` no existe).

- [ ] **Step 3: Implement GSAP matchMedia scrollytelling**

1. Cargar GSAP 3.12 y ScrollTrigger en `landing/index.html` con CDN optimizado y fallback condicional en `main.js`.
2. En `landing/js/scrollytelling.js`, configurar `gsap.matchMedia()`:
   - `(min-width: 992px)`: pin del smartphone container durante las 3 secciones de historia, scrub suave (1s) y cross-fade elástico entre pantallas.
   - `(max-width: 991px)`: sin pin; pestañas interactivas sincronizadas con botones táctiles y scroll spy sin atrapar el scroll del usuario.
   - `(prefers-reduced-motion: reduce)`: mostrar controles de pestañas instantáneas sin animación.

- [ ] **Step 4: Run test to verify it passes**

Run: `python3 -m unittest tests/test_landing_structure.py`
Expected: PASS (OK).

- [ ] **Step 5: Commit**

```bash
git add landing/js/ landing/index.html tests/test_landing_structure.py
git commit -m "feat(landing): implementar motor de scrollytelling responsivo con gsap matchmedia"
```

---

### Task 6: Bento Grid de Funcionalidades, Toast de Descarga y Pulido UI/UX Pro Max

**Files:**
- Modify: `landing/index.html`
- Modify: `landing/css/styles.css`
- Modify: `landing/js/main.js`
- Modify: `tests/test_landing_structure.py`

**Interfaces:**
- Consumes: Bento Grid de superpoderes académicos, CTAs y Footer
- Produces: Landing completa y responsive con micro-interacciones pulidas y áreas táctiles de 48px

- [ ] **Step 1: Write the failing test**

Validar en `tests/test_landing_structure.py` que existen:
- La sección Bento Grid (`.bento-grid`) con 4 tarjetas de superpoderes (Ponderación, Sincronización, Notificaciones, Temas).
- El botón de descarga Google Play con ID `#btn-download-hero` y `#btn-download-cta` con min-height de 48px.
- El componente modal/toast interactivo `#download-toast`.
- El footer con copyright de NotiFast y links semánticos.

```python
    def test_bento_grid_and_cta_elements(self):
        with open("landing/index.html", "r", encoding="utf-8") as f:
            html = f.read()
        self.assertIn("bento-grid", html)
        self.assertIn("btn-download-hero", html)
        self.assertIn("btn-download-cta", html)
        self.assertIn("download-toast", html)
        self.assertIn("Google Play", html)
```

- [ ] **Step 2: Run test to verify it fails**

Run: `python3 -m unittest tests/test_landing_structure.py`
Expected: FAIL (`bento-grid` o elementos CTA no encontrados).

- [ ] **Step 3: Implement Bento grid, download interactions and polish**

1. Agregar la cuadrícula Bento en `landing/index.html` con micro-ilustraciones vectoriales y bordes táctiles.
2. Implementar en `landing/js/main.js` la lógica del toast/modal de descarga: al hacer clic en cualquier botón de Google Play, despliega un toast accesible con botón para copiar enlace o recibir aviso cuando esté lista la versión en tienda.
3. Añadir el footer con tipografía editorial de alta gama.
4. Ajustar micro-interacciones de hover, focus visible accesible (`outline: 2px solid var(--brand-primary)`), y resorte táctil al presionar botones.

- [ ] **Step 4: Run test to verify it passes**

Run: `python3 -m unittest tests/test_landing_structure.py`
Expected: PASS (OK).

- [ ] **Step 5: Commit**

```bash
git add landing/ tests/test_landing_structure.py
git commit -m "feat(landing): agregar bento grid, modal de descarga interactivo y pulido ui/ux pro max"
```

---

### Task 7: Verificación Integral, Servidor Local y Validación E2E

**Files:**
- Modify: `tests/test_landing_structure.py`
- Create: `tests/test_landing_e2e.py`

**Interfaces:**
- Consumes: Servidor web local en puerto temporal
- Produces: Validación end-to-end completa con código de salida 0 y confirmación de build listo para Firebase

- [ ] **Step 1: Write comprehensive e2e verification tests**

Crear `tests/test_landing_e2e.py` que levanta un servidor HTTP local en Python, realiza peticiones GET a `/`, `/css/tokens.css`, `/css/styles.css`, `/css/phone-mockup.css`, `/js/main.js`, `/js/background.js`, `/js/scrollytelling.js`, valida cabeceras `Content-Type`, tamaño de archivos y ausencia de enlaces rotos o referencias 404.

- [ ] **Step 2: Run test to verify it fails**

Run: `python3 -m unittest tests/test_landing_e2e.py`
Expected: FAIL if any asset is missing or returns non-200.

- [ ] **Step 3: Fix any asset references and verify all tests pass**

Corregir cualquier detalle o ruta relativa que pueda fallar y asegurar que los archivos carguen de forma atómica.

- [ ] **Step 4: Run all test suites**

Run: `python3 -m unittest discover -s tests -p "test_*.py"`
Expected: ALL PASS (OK).

- [ ] **Step 5: Commit**

```bash
git add tests/
git commit -m "test(landing): agregar suite de verificación e2e y validación de assets"
```

---

## Plan Self-Review Checklist

1. **Spec Coverage:** Cubre todos los aspectos de `2026-10-13-landing-page-design.md`: tokens, modo claro/oscuro, stickers vectoriales con física reactiva, scrollytelling con teléfono fijo y pantallas de la app, Bento grid y configuración de Firebase Hosting.
2. **Step Scan:** Cada tarea contiene pasos de TDD estrictos (test que falla, comando de ejecución, implementación, verificación, commit).
3. **Type and Token Consistency:** Todos los selectores y variables CSS concuerdan exactamente entre las tareas.
4. **Review Focus (UI/UX Pro Max & Systematic Debugging):**
   - Atiende la prevención de scroll-traps en móviles mediante `gsap.matchMedia(992px)`.
   - Previene bloqueos CORS con sprite SVG inline.
   - Garantiza degradación elegante sin JS o si falla el CDN.
   - Cumple accesibilidad WCAG con Skip Link, `:focus-visible` y áreas de toque de 48px.
5. **Proporción:** Equilibrado, conciso y modular.
