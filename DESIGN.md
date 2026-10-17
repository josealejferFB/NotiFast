---
name: NotiFast Design System
version: "3.2.0"
archetype: "Tactile Papercraft & Dynamic Academic Studio (WCAG AAA)"
platform: "Android (Jetpack Compose + Material 3) & Interactive Web Showcase"
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

# Design System NotiFast 3.2: "Tactile Papercraft & Dynamic Academic Studio"

## 1. Visión y Estrella Polar de Diseño

**Creative North Star: "El Cuaderno de Estudio Táctil, Adaptable y de Alta Precisión"**

NotiFast consolida una identidad visual **EdTech de autor**: cálida, estructurada, táctil y profundamente enfocada en el bienestar del estudiante. A partir de la versión 3.2, el sistema de diseño desacopla el **esquema de acentos (`ThemePreset`)** del **tono de lienzo de fondo (`BackgroundPreset`)**, permitiendo 36 combinaciones predefinidas más un **Estudio Espectral Hex/HSL continuo** con validación matemática de contraste **WCAG AAA**.

---

## 2. Motor de Temas Personalizables & Lienzos Independientes

### 2.1 Las 6 Colecciones de Acento Oficiales (`ThemePreset.kt`)

| ID (`presetId`) | Nombre en UI | Primario Claro / Oscuro | Secundario Claro / Oscuro | Personalidad Académica |
|---|---|---|---|---|
| `classic` | **NotiFast Classic** | `#9E1B32` / `#E05A6E` | `#2A7B7B` / `#38B2AC` | Identidad insignia: Carmesí Tenue & Aqua Studio |
| `forest_sage` | **Campus Verde** | `#15803D` / `#34D399` | `#854D0E` / `#FBBF24` | Botánico y descansado para ciencias y lectura larga |
| `midnight_indigo` | **Cyber Nocturno** | `#4338CA` / `#818CF8` | `#0891B2` / `#22D3EE` | Índigo eléctrico y cian para sesiones de programación |
| `warm_mocha` | **Café Nórdico** | `#C2410C` / `#FB923C` | `#B45309` / `#FCD34D` | Terracota y miel cálida estilo biblioteca clásica |
| `sakura_zen` | **Sakura Zen** | `#6D28D9` / `#A78BFA` | `#BE185D` / `#F472B6` | Violeta y flor de cerezo para máxima serenidad |
| `ocean_cyan` | **Minimalista Nórdico** | `#0284C7` / `#38BDF8` | `#0F766E` / `#2DD4BF` | Azul océano y teal de alta claridad estructural |
| `custom` | **Estudio Personalizado** | Hex/HSL del usuario | Derivado armónico | Calibrado algorítmicamente por `ThemePaletteGenerator` |

### 2.2 Los 6 Lienzos de Papel Independientes (`BackgroundPreset.kt`)

| ID (`presetId`) | Nombre en UI | Lienzo Claro (`bg` / `surface` / `container`) | Lienzo Oscuro (`bg` / `surface` / `container`) |
|---|---|---|---|
| `classic` | **Pergamino** | `#FAF9F6` / `#FFFFFF` / `#F3EFEA` | `#0E131F` / `#161E2D` / `#1C2638` |
| `forest_sage` | **Menta** | `#EAF4ED` / `#F6FAF7` / `#D7EADC` | `#0A1610` / `#12241B` / `#1A3527` |
| `midnight_indigo` | **Polar** | `#E6F0F8` / `#F5F9FD` / `#D1E4F3` | `#09101D` / `#111E35` / `#192B4B` |
| `warm_mocha` | **Papiro** | `#F5ECE1` / `#FCFAF6` / `#E8DAC8` | `#150F0B` / `#221913` / `#32251D` |
| `sakura_zen` | **Lavanda** | `#F2EAF7` / `#FAF6FC` / `#E4D3EE` | `#130B1C` / `#20122E` / `#2F1B43` |
| `ocean_cyan` | **Glaciar** | `#E5F3F5` / `#F5FAFB` / `#CCE8EB` | `#061418` / `#0D2026` / `#142E37` |
| `custom` | **Lienzo Hex Libre** | Color verdadero del estudiante | Contraste `onBackground` auto-calibrado según luminancia |

### 2.3 Estudio Espectral Continuo (`HexColorStudioPicker` & `ThemePaletteGenerator`)
- **Sliders Continuos Independientes:** Barras táctiles de Matiz (`0°–360°`), Saturación (`0%–100%`) y Luminosidad (`0%–100%`) más entrada directa `#RRGGBB`.
- **Aislamiento Total (`key(CustomStudioTab)`):** Las pestañas de Color de Énfasis y Color de Fondo operan con estados desacoplados sin interferencia mutua.
- **Garantía Matemática WCAG AAA:** `ThemePaletteGenerator` ajusta iterativamente la luminancia del acento para asegurar contraste $\ge 4.5:1$ y calcula automáticamente si un fondo personalizado requiere tinta clara u oscura (`isBgDark = bg.luminance() < 0.5f`).

---

## 3. Tipografía & Jerarquía de Imprenta

NotiFast utiliza **Plus Jakarta Sans** para interfaz y lectura editorial, y **JetBrains Mono** (`font-variant-numeric: tabular-nums`) para promedios, ponderaciones, temporizadores de clase y códigos hexadecimales.

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
│ Mono Academic Sm  │ 11.5sp   │ 16sp     │ Medium 500 0sp    │
└───────────────────┴──────────┴──────────┴──────────┴────────┘
```

---

## 4. Componentes Atómicos, BottomSheets y Física Táctil

1. **Tarjetas Táctiles (`NotiFastCard`):** Superficie tonal con micro-borde de `1dp` (`outlineVariant`), esquinas de `16dp–20dp` y escala elástica a `0.97f` al presionar.
2. **Flujos Modales Unificados (`ModalBottomSheet`):** Creación y edición de apuntes (`NoteEditorBottomSheet`), horarios (`ScheduleEditorBottomSheet`), evaluaciones (`EvaluationEditorBottomSheet`), docentes (`AddEditTeacherBottomSheet`), temas (`CustomThemePaletteSheet`) y respaldo en la nube (`CloudHubSheet`) con `skipPartiallyExpanded = true` y acoplamiento de barra sobre el teclado (`DockedAccessoryToolbar`).
3. **Apego al Tema Interno (`MaterialTheme.colorScheme.isDark`):** Prohibido usar `isSystemInDarkTheme()` directamente en componentes; toda superficie consulta la luminancia del esquema activo de NotiFast.
