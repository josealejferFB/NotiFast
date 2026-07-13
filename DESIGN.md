---
name: NotiFast
description: Hub educativo y cuaderno académico para estudiantes.
colors:
  primary: "#BF092F"
  secondary: "#3B9797"
  surface-light: "#FAFAFA"
  on-surface-light: "#1C1B1F"
  surface-dark: "#1B262C"
  on-surface-dark: "#E6E1E5"
typography:
  body:
    fontFamily: "System Default"
    fontSize: "16sp"
    fontWeight: "Normal"
    lineHeight: "24sp"
    letterSpacing: "0.5sp"
rounded:
  sm: "8dp"
  md: "12dp"
  lg: "16dp"
spacing:
  sm: "8dp"
  md: "16dp"
  lg: "24dp"
components:
  notifast-button:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
    rounded: "{rounded.lg}"
    padding: "16dp 24dp"
  notifast-card:
    backgroundColor: "{colors.surface-light}"
    rounded: "{rounded.lg}"
    padding: "16dp"
---

# Design System: NotiFast

## 1. Overview

**Creative North Star: "El Cuaderno Inteligente"**

NotiFast es un sistema visual diseñado para sentirse como una herramienta académica indispensable: estructurada, clara y táctil. Su estética combina la familiaridad nativa de Android (Material 3) con un enfoque puro en la productividad del estudiante. La interfaz busca reducir la ansiedad académica manteniendo el orden, ofreciendo información jerarquizada y reduciendo la carga visual innecesaria. Este sistema rechaza explícitamente las interfaces tipo LMS corporativas y aburridas, optando por una experiencia dinámica, rápida y amigable.

**Key Characteristics:**
- **Claridad Estructural:** Espacios limpios y superficies tonales para evitar la saturación visual.
- **Familiaridad Nativa:** Uso profundo de Material 3 y Jetpack Compose.
- **Amigable y Táctil:** Componentes redondeados y accesibles que invitan a interactuar.

## 2. Colors

La paleta se centra en brindar acciones rápidas con un rojo vivo y confirmar estados con un aqua tranquilizador, respaldada por superficies neutras para reducir la fatiga visual.

### Primary
- **Rojo Dinámico** (`#BF092F`): Utilizado para acciones principales, destructivas y elementos relacionados con evaluaciones o alertas. Es enérgico, rápido y vivo.

### Secondary
- **Aqua Calma** (`#3B9797`): Empleado para estados positivos, confirmaciones y éxito. Aporta una sensación tranquilizadora y fluida.

### Neutral
- **Fondo Principal Claro** (`#FAFAFA`): Fondo base para el modo claro.
- **Texto Principal Claro** (`#1C1B1F`): Texto de alto contraste sobre el fondo claro.
- **Fondo Principal Oscuro** (`#1B262C`): Fondo base con tono pizarra (slate) para el modo oscuro, reduciendo la dureza del negro puro.
- **Texto Principal Oscuro** (`#E6E1E5`): Texto sobre fondos oscuros.

### Named Rules
**The Tonal Surface Rule.** Las agrupaciones y jerarquías se diferencian mediante ligeros cambios en el color de fondo (superficies tonales), no mediante líneas fuertes o sombras pesadas.

## 3. Typography

**Display Font:** System Default (Roboto/Inter equivalente)
**Body Font:** System Default

**Character:** Tipografía utilitaria y altamente legible, diseñada para la lectura de apuntes, horarios y métricas académicas sin distracciones.

### Hierarchy
- **Body** (Normal, 16sp, 24sp): Usado para contenido principal, notas, descripciones de materias. Texto legible y amplio.
- (El resto de la jerarquía hereda de las definiciones estándar de Material 3 Typography).

### Named Rules
**The Legibility Rule.** Nunca sacrificar el contraste ni el tamaño del texto por estética. El texto es el núcleo del cuaderno académico.

## 4. Elevation

El sistema utiliza superficies tonales (Material 3 puro). En lugar de aplicar sombras pesadas para indicar profundidad, la jerarquía y las áreas interactivas se comunican cambiando ligeramente el tono o color de fondo del componente contenedor. 

### Shadow Vocabulary
- **Plano por defecto:** No se usan `box-shadow` (o `elevation` en Compose) a menos que sea estrictamente necesario (ej. modales flotantes o FABs). La separación se logra con color.

### Named Rules
**The Flat-By-Default Rule.** Las superficies descansan planas. La separación visual entre un NotiFastCard y el fondo principal se da por el contraste de color tonal, no por sombras.

## 5. Components

Los componentes son táctiles y amigables. Utilizan bordes redondeados pronunciados y dan respuestas claras al interactuar.

### Buttons (NotiFastButton)
- **Shape:** Bordes redondeados pronunciados (`16dp`).
- **Primary:** Fondo Rojo Dinámico (`#BF092F`) con texto blanco y padding generoso.
- **Hover / Focus:** Ripple effect nativo de Android.

### Cards / Containers (NotiFastCard)
- **Corner Style:** `16dp`.
- **Background:** Superficie tonal clara (`#FAFAFA` o una variación sutil del fondo principal).
- **Shadow Strategy:** Plano por defecto, sin elevación. Separado por color.
- **Internal Padding:** `16dp`.

### Inputs / Fields (NotiFastTextField / NotiFastFormField)
- **Style:** Campos amigables con label integrado, a menudo acompañados de iconos para facilitar la comprensión rápida.
- **Focus:** Indicador visual claro basado en los estándares de Compose OutlinedTextField o similar.

### Empty States (NotiFastEmptyState)
- **Style:** Gráficos o iconos ilustrativos centrados con texto descriptivo suave para invitar a la acción inicial.

## 6. Do's and Don'ts

Basado en la identidad del producto y las reglas del sistema:

### Do:
- **Do** usar los componentes atómicos definidos en `presentation/atoms/` (como `NotiFastButton`, `NotiFastCard`) antes de crear componentes custom de un solo uso.
- **Do** utilizar el sistema de elevación tonal para separar capas de información, manteniendo el diseño plano y limpio.
- **Do** garantizar que las pantallas ofrezcan calma visual y organización frente al caos académico.

### Don't:
- **Don't** hacer que la aplicación parezca un sistema escolar antiguo y aburrido como Moodle clásico.
- **Don't** usar interfaces de gestión corporativa o tablas de datos pesadas en pantallas móviles.
- **Don't** saturar una pantalla de información; priorizar la claridad sobre la densidad.
- **Don't** aplicar elevaciones (sombras) exageradas en tarjetas y contenedores de listas.
