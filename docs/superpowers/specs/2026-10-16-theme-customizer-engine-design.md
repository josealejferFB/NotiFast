# Theme Customizer Engine AAA — Design Specification

> **Feature:** Sistema de Temas Personalizados y Paletas Académicas Dinámicas  
> **Package:** `com.notifast.app`  
> **Status:** Approved / In Progress  
> **Target Release:** NotiFast 3.0 AAA  

---

## 1. Intent & Purpose

Provide students with a deeply comfortable, accessible, and personalized study visual environment from `SettingsScreen`. Visual fatigue, high stress, and cognitive overload during extended study sessions are mitigated through scientifically calibrated chromatic palettes (WCAG AAA contrast), a dynamic Material 3 palette generator for custom colors, and an interactive tactile live preview.

---

## 2. Palette Collection ("Colección de Estudio")

| Preset ID | Name (ES) | Primary Light / Dark | Secondary Light / Dark | Psychological Study Target |
|---|---|---|---|---|
| `classic` | NotiFast Classic | `#9E1B32` / `#E05A6E` | `#2A7B7B` / `#38B2AC` | Identidad corporativa táctil, alta energía y precisión académica. |
| `forest_sage` | Campus Verde | `#15803D` / `#34D399` | `#854D0E` / `#FBBF24` | Concentración profunda, lectura prolongada y reducción de fatiga. |
| `midnight_indigo` | Cyber Nocturno | `#4338CA` / `#818CF8` | `#0891B2` / `#22D3EE` | Sesiones nocturnas con mínima emisión de luz azul perjudicial. |
| `warm_mocha` | Café Nórdico | `#C2410C` / `#FB923C` | `#B45309` / `#FCD34D` | Calidez bibliotecaria clásica, confort visual y calma. |
| `sakura_zen` | Sakura Zen | `#6D28D9` / `#A78BFA` | `#BE185D` / `#F472B6` | Reducción de la ansiedad ante exámenes y serenidad mental. |
| `ocean_cyan` | Minimalista Nórdico | `#0284C7` / `#38BDF8` | `#0F766E` / `#2DD4BF` | Claridad mental, estructura y pensamiento analítico. |
| `custom` | Estudio Personalizado | Usuario / Calculado | Armónico / Calculado | Identidad personalizada con ratios accesibles garantizados. |

---

## 3. Dynamic Palette Generator (`ThemePaletteGenerator`)

The generator is a pure algorithmic domain/presentation function that takes:
1. `primaryColor: Color`
2. `isDark: Boolean`
3. Optional `secondaryColor: Color? = null`

And derives a Material 3 `ColorScheme` guaranteeing:
- **Light Mode:**
  - `primary`: Lightness clamped in `[0.30f..0.42f]`, Saturation clamped in `[0.65f..0.75f]`.
  - `onPrimary`: `#FFFFFF` (contrast $\ge 7.0:1$ AAA on primary).
  - `primaryContainer`: Soft pastel wash (Lightness `~0.95f`, Saturation `~0.30f`).
  - `onPrimaryContainer`: Deep ink (Lightness `~0.18f`, Saturation `~0.75f`, contrast $> 10:1$ on container).
  - Tactile surface tokens preserved: `background = #FAF9F6`, `surface = #FFFFFF`, `surfaceContainer = #F3EFEA`, `outlineVariant = #E5DFD7`, `onSurface = #181A1F`.
- **Dark Mode:**
  - `primary`: Lightness clamped in `[0.70f..0.78f]`, Saturation clamped in `[0.65f..0.75f]` (contrast $\ge 7.0:1$ AAA on slate `#0E131F`).
  - `onPrimary`: Deep dark ink (Lightness `~0.12f`, contrast $> 7.0:1$ AAA).
  - `primaryContainer`: Deep rich slate container (Lightness `~0.22f`, Saturation `~0.50f`).
  - `onPrimaryContainer`: Soft luminous chalk ink (Lightness `~0.92f`, Saturation `~0.30f`, contrast $> 7.0:1$ AAA).
  - Tactile dark tokens preserved: `background = #0E131F`, `surface = #161E2D`, `surfaceContainer = #1C2638`, `outlineVariant = #2A384E`, `onSurface = #F1F5F9`.

---

## 4. Local Persistence & Google Cloud Backup

### DataStore Keys
- `theme_preset_id: stringPreferencesKey("theme_preset_id")` (default: `"classic"`)
- `custom_theme_primary_hex: stringPreferencesKey("custom_theme_primary_hex")` (default: `null`)

### Google Drive REST API Backup (`appDataFolder`)
- `GlobalSnapshot.prefs` dictionary serialized via `exportAll()` and `importAll()`.
- Added entries:
  - `"theme_preset_id" -> presetId`
  - `"custom_theme_primary_hex" -> hex` (if custom)
- **Fallback / Migration Safety:** When reading an older snapshot where these keys are missing, `ThemePreset.fromId(null)` defaults to `CLASSIC`, and `customPrimaryHex` remains `null`. Zero crash, zero regression.

---

## 5. UI/UX Architecture

- **`ThemeSelectorCard.kt`** ($\le 250$ LOC):
  - Hero card in `SettingsScreen` showing current preset preview, quick selection horizontal row with circular swatches, spring checkmark indicator, and CTA to open `CustomThemePaletteSheet`.
  - Mode switch (Sistema / Claro / Oscuro) integrated cleanly.
- **`CustomThemePaletteSheet.kt`** ($\le 250$ LOC):
  - Bottom sheet or modal dialog with curated 12-color harmonious chromatic matrix.
  - Live mini-card previewer reflecting color changes before saving.
  - Cancel and Apply buttons.
- **`ThemePreviewMiniCard.kt`** ($\le 250$ LOC):
  - Micro tactile preview containing a mock `NotiFastCard`, `NotiFastButton`, and an evaluation status pill.
