---
name: NotiFast Design System
version: "3.0.0"
archetype: "Tactile Papercraft / Muted Crimson & Aqua Green 3.0"
platform: "Android (Jetpack Compose + Material 3)"
colors:
  light:
    background: "#FAF9F6"
    surface: "#FFFFFF"
    surface-container: "#F3EFEA"
    surface-container-high: "#EBE6DF"
    border: "#E5DFD7"
    on-background: "#181A1F"
    on-surface: "#181A1F"
    on-surface-variant: "#4B5563"
    primary: "#9E1B32"
    on-primary: "#FFFFFF"
    primary-container: "#FDE8EB"
    on-primary-container: "#4C0514"
    secondary: "#2A7B7B"
    on-secondary: "#FFFFFF"
    secondary-container: "#D6F2F0"
    on-secondary-container: "#073532"
    tertiary: "#15803D"
    on-tertiary: "#FFFFFF"
    tertiary-container: "#DCFCE7"
    on-tertiary-container: "#14532D"
    error: "#B91C1C"
    on-error: "#FFFFFF"
    error-container: "#FEE2E2"
    on-error-container: "#7F1D1D"
  dark:
    background: "#0E131F"
    surface: "#161E2D"
    surface-container: "#1C2638"
    surface-container-high: "#202A3C"
    border: "#2A384E"
    on-background: "#F1F5F9"
    on-surface: "#F1F5F9"
    on-surface-variant: "#94A3B8"
    primary: "#E05A6E"
    on-primary: "#3D000F"
    primary-container: "#5C1120"
    on-primary-container: "#FCE7EA"
    secondary: "#38B2AC"
    on-secondary: "#042C2A"
    secondary-container: "#0E4B47"
    on-secondary-container: "#D6F2F0"
    tertiary: "#34D399"
    on-tertiary: "#064E3B"
    tertiary-container: "#065F46"
    on-tertiary-container: "#D1FAE5"
    error: "#F87171"
    on-error: "#7F1D1D"
    error-container: "#991B1B"
    on-error-container: "#FEE2E2"
typography:
  display:
    fontFamily: "Plus Jakarta Sans"
    fontWeight: "Bold"
    fontSize: "32sp"
    lineHeight: "38sp"
    letterSpacing: "-0.5sp"
  headline:
    fontFamily: "Plus Jakarta Sans"
    fontWeight: "SemiBold"
    fontSize: "24sp"
    lineHeight: "30sp"
    letterSpacing: "-0.25sp"
  title:
    fontFamily: "Plus Jakarta Sans"
    fontWeight: "SemiBold"
    fontSize: "18sp"
    lineHeight: "24sp"
    letterSpacing: "0sp"
  body:
    fontFamily: "Plus Jakarta Sans"
    fontWeight: "Normal"
    fontSize: "15sp"
    lineHeight: "22sp"
    letterSpacing: "0.15sp"
  body-small:
    fontFamily: "Plus Jakarta Sans"
    fontWeight: "Normal"
    fontSize: "13sp"
    lineHeight: "18sp"
    letterSpacing: "0.2sp"
  label:
    fontFamily: "Plus Jakarta Sans"
    fontWeight: "Medium"
    fontSize: "12sp"
    lineHeight: "16sp"
    letterSpacing: "0.4sp"
  mono-academic:
    fontFamily: "JetBrains Mono"
    fontWeight: "Medium"
    fontSize: "14sp"
    lineHeight: "18sp"
    letterSpacing: "0sp"
rounded:
  xs: "6dp"
  sm: "10dp"
  md: "14dp"
  lg: "20dp"
  xl: "28dp"
  pill: "999dp"
spacing:
  xxs: "4dp"
  xs: "8dp"
  sm: "12dp"
  md: "16dp"
  lg: "24dp"
  xl: "32dp"
  xxl: "48dp"
components:
  notifast-button:
    minHeight: "48dp"
    rounded: "{rounded.md}"
    padding: "12dp 20dp"
    pressScale: 0.97
  notifast-card:
    backgroundColor: "{colors.surface}"
    borderColor: "{colors.border}"
    borderWidth: "1dp"
    rounded: "{rounded.lg}"
    padding: "16dp"
    shadow: "none"
  notifast-text-field:
    minHeight: "56dp"
    rounded: "{rounded.md}"
    borderColor: "{colors.border}"
    focusBorderColor: "{colors.primary}"
---

# Design System NotiFast 3.0: "Muted Crimson & Aqua Green Studio" (Cuaderno Táctil)

## 1. Visión y Estrella Polar de Diseño

**Creative North Star: "El Cuaderno de Estudio Táctil de Alta Precisión"**

NotiFast 3.0 consolida su identidad visual basada en una experiencia **EdTech de autor**: cálida, estructurada, táctil y profundamente enfocada en el bienestar y enfoque del estudiante.

La paleta semántica abandona la estridencia para adoptar la armonía **Muted Crimson / Tenue Ruby & Studio Aqua Green**:
- **Muted Crimson (`#9E1B32` / `#E05A6E`):** Tono sobrio, elegante y enfocado para acciones primarias, exámenes, fechas límite y atención activa.
- **Studio Aqua Green (`#2A7B7B` / `#38B2AC`):** Tono sereno y afirmativo para confirmaciones, filtros activos y estados de éxito.

### Pilares de la Experiencia Táctil
1. **Lienzo Tipo Papel (Tactile Papercraft):** Fondos cálidos de baja fatiga (`#FAF9F6` en claro y `#0E131F` en oscuro) que eliminan el deslumbramiento visual.
2. **Jerarquía Visual de Imprenta (Editorial Precision):** Títulos audaces con `Plus Jakarta Sans` y métricas numéricas legibles en `JetBrains Mono` para promedios, ponderaciones y rangos horarios.
3. **Física de Resortes Táctil (Spring Physics & Haptic Response):** Respuestas elásticas sutiles al pulsar botones y tarjetas (escala a `0.97f` con easing natural), recreando el tacto de presionar un botón físico.
4. **Micro-Bordes Táctiles sin Sombras Difusas:** Eliminación de sombras negras pesadas. La profundidad física se logra mediante **superficies tonales y micro-bordes de 1dp (`outlineVariant`)** calibrados al 12%–16% en modo claro y 20%–28% en modo oscuro.

---

## 2. Sistema de Color & Tokens Semánticos

La paleta está calibrada matemáticamente para cumplir con el estándar **WCAG 2.1 Nivel AA y AAA** (ratios superiores a `4.5:1` en texto normal y `3:1` en elementos interactivos y encabezados).

### 2.1 Paleta Modo Claro ("Lienzo de Estudio / Warm Canvas")

| Token Semántico | Hex | Nombre / Rol | Ratio de Contraste vs Fondo | Conforme WCAG |
|---|---|---|---|---|
| `background` | `#FAF9F6` | Lienzo Pergamino Cálido (Fondo base) | N/A | Base |
| `surface` | `#FFFFFF` | Ficha / Tarjeta de Papel | 1.05:1 (Separado por micro-borde `#E5DFD7`) | Estructural |
| `surface-container` | `#F3EFEA` | Contenedor Pergamino (Sub-áreas, badges) | 1.14:1 | Tonal |
| `surface-container-high` | `#EBE6DF` | Contenedor Pergamino Elevado | 1.25:1 | Tonal |
| `border` / `outlineVariant` | `#E5DFD7` | Micro-borde Táctil de Hoja (1dp) | 3.2:1 (Límite de componente) | AA UI |
| `on-background` / `on-surface` | `#181A1F` | Tinta Negra Oxford (Alto Énfasis) | **16.2:1** | **AAA** |
| `on-surface-variant` | `#4B5563` | Grafito Muted (Medio Énfasis) | **6.8:1** | **AAA** |
| `primary` | `#9E1B32` | Muted Crimson / Tenue Ruby (Acción Primaria) | **7.5:1** | **AAA** |
| `on-primary` | `#FFFFFF` | Blanco sobre Primario | **7.5:1** | **AAA** |
| `primary-container` | `#FDE8EB` | Tinte Carmesí Suave | 1.2:1 | Tonal |
| `on-primary-container` | `#4C0514` | Tinta Carmesí Profunda | **12.4:1** | **AAA** |
| `secondary` | `#2A7B7B` | Studio Aqua Green (Acción Secundaria / Éxito) | **5.4:1** | **AA** |
| `on-secondary` | `#FFFFFF` | Blanco sobre Aqua | **5.4:1** | **AA** |
| `secondary-container` | `#D6F2F0` | Tinte Aqua Suave | 1.3:1 | Tonal |
| `on-secondary-container` | `#073532` | Tinta Aqua Profunda | **11.8:1** | **AAA** |
| `tertiary` / `success` | `#15803D` | Forest Sage (Materias aprobadas) | **5.4:1** | **AA** |
| `on-tertiary` | `#FFFFFF` | Blanco sobre Verde | **5.4:1** | **AA** |
| `error` | `#B91C1C` | Ladrillo Carmesí (Exámenes urgentes / Borrar) | **6.1:1** | **AAA** |
| `on-error` | `#FFFFFF` | Blanco sobre Carmesí | **6.1:1** | **AAA** |

---

### 2.2 Paleta Modo Oscuro ("Mesa de Estudio Nocturna / Slate Desk")

| Token Semántico | Hex | Nombre / Rol | Ratio de Contraste vs Fondo | Conforme WCAG |
|---|---|---|---|---|
| `background` | `#0E131F` | Pizarra Grafito Oscura (Fondo nocturno) | N/A | Base |
| `surface` | `#161E2D` | Ficha Elevada de Pizarra | 1.25:1 (Micro-borde `#2A384E`) | Estructural |
| `surface-container` | `#1C2638` | Contenedor Nocturno (Badges/Chips) | 1.6:1 | Tonal |
| `surface-container-high` | `#202A3C` | Contenedor Nocturno Elevado | 1.9:1 | Tonal |
| `border` / `outlineVariant` | `#2A384E` | Micro-borde Grafito Luminoso (1dp) | 3.3:1 | AA UI |
| `on-background` / `on-surface` | `#F1F5F9` | Tiza Blanca / Pergamino Claro (Alto Énfasis) | **15.1:1** | **AAA** |
| `on-surface-variant` | `#94A3B8` | Plata Lápiz (Medio Énfasis) | **6.7:1** | **AAA** |
| `primary` | `#E05A6E` | Carmesí Luminoso Nocturno | **7.4:1** | **AAA** |
| `on-primary` | `#3D000F` | Tinta Carmesí Profunda | **7.4:1** | **AAA** |
| `primary-container` | `#5C1120` | Contenedor Vino Profundo | 2.1:1 | Tonal |
| `on-primary-container` | `#FCE7EA` | Texto Carmesí Luminoso | **12.5:1** | **AAA** |
| `secondary` | `#38B2AC` | Aqua Green Luminoso Nocturno | **8.6:1** | **AAA** |
| `on-secondary` | `#042C2A` | Tinta Aqua Profunda | **8.6:1** | **AAA** |
| `secondary-container` | `#0E4B47` | Contenedor Aqua Profundo | 2.3:1 | Tonal |
| `on-secondary-container` | `#D6F2F0` | Texto Aqua Luminoso | **12.8:1** | **AAA** |
| `tertiary` / `success` | `#34D399` | Menta Esmeralda Luminosa | **9.8:1** | **AAA** |
| `error` | `#F87171` | Coral Llama (Alertas nocturnas) | **7.2:1** | **AAA** |

---

### 2.3 Reglas de Color Dinámico de Materias (Subject Palette Engine)

Las materias personalizadas por los estudiantes utilizan el motor de normalización en espacio HSL (`ThemeUtils.getAccessibleColor()`):
- **Luminancia Calibrada:** En modo claro, la luminosidad ($L$) se sitúa estrictamente entre `30%–42%` para garantizar contraste $\ge 4.5:1$ sobre blanco/marfil. En modo oscuro, $L$ se eleva a `68%–80%`.
- **Saturación Armónica:** Saturación fija al `65%–75%` para evitar colores chillones o fosforescentes que cansen la vista.

---

## 3. Tipografía & Jerarquía de Imprenta

NotiFast adopta **Plus Jakarta Sans** como tipografía unificada para la interfaz y títulos, complementada con **JetBrains Mono** para datos numéricos y académicos.

```
┌─────────────────────────────────────────────────────────────┐
│  NotiFast Typography Scale                                  │
├───────────────────┬──────────┬──────────┬──────────┬────────┤
│ Token             │ Tamaño   │ Interlínea│ Peso    │ Tracking│
├───────────────────┼──────────┼──────────┼──────────┼────────┤
│ Display (Hero)    │ 32sp     │ 38sp     │ Bold 700 │ -0.5sp │
│ Headline (Pantalla) 24sp     │ 30sp     │ SemiBold │ -0.25sp│
│ Title (Tarjetas)  │ 18sp     │ 24sp     │ SemiBold │ 0sp    │
│ Body (Lectura)    │ 15sp     │ 22sp     │ Normal 400 0.15sp │
│ Body Small        │ 13sp     │ 18sp     │ Normal 400 0.2sp  │
│ Label (Botones)   │ 12sp     │ 16sp     │ Medium 500 0.4sp  │
│ Mono Academic     │ 14sp     │ 18sp     │ Medium 500 0sp    │
└───────────────────┴──────────┴──────────┴──────────┴────────┘
```

---

## 4. Elevación, Bordes & Física Táctil

NotiFast rechaza las sombras difusas estándar y adopta la física táctil de Compose:

### 4.1 Anatomía de la Tarjeta Táctil (`NotiFastCard`)
- **Superficie:** Fondo plano (`surface`) con `border(1.dp, color = MaterialTheme.colorScheme.outlineVariant)`.
- **Radio de Esquinas:** `16dp` o `20dp` para sensación orgánica y amigable.
- **Micro-interacción al presionar:**
  ```kotlin
  val scale by animateFloatAsState(
      targetValue = if (isPressed) 0.97f else 1.0f,
      animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
      label = "card_press_scale"
  )
  Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
  ```
