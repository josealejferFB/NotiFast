# Dark Mode Contrast Re-design

## Goal
Improve the user experience in Dark Mode by resolving the high-contrast issue between very dark backgrounds and highly saturated card colors. This issue currently causes visual fatigue on screens like the main hub, subjects list, and creation forms.

## Architectural & Visual Approach
We will use a combined approach of **Softening the Background (Elevated Dark)** and **Desaturating the Cards (Muted Accents)**:

1. **Background Elevation:**
   - Change the dark mode surface and background colors from near-black/dark green (`#1C1B1F`, `#132B2B`) to a softer slate/blue-gray tone.
   - Proposed Base Dark Background: `linear-gradient(#1B262C, #223038)` or solid `#1B262C` for standard surfaces.

2. **Card Accent Desaturation:**
   - The primary corporate colors (`Ruby` and `Aqua`) are too bright for large solid blocks in dark mode.
   - We will introduce muted/pastel variants specifically for dark mode cards.
   - `RubyDark` changes from `#FF6B7A` to a muted version like `#CC5A68`.
   - `AquaDark` changes from `#6DD3D3` to a muted version like `#4A9696`.

3. **Subtle Elevation / Shadows:**
   - Instead of relying entirely on extreme color contrast for separation, cards will use a subtle drop shadow or lighter border to stand out against the softened background.

## Affected Components
- `Color.kt`: Update `RubyDark`, `AquaDark`, `SurfaceDark`, and `OnSurfaceDark` values.
- `NotiFastBackground.kt` & `NotiFastStudyBackground.kt`: Adjust gradient stops and particle base colors for dark mode to match the new softer palette.
- `NoteEstheticCard.kt` / `NotiFastCard.kt`: Ensure they properly utilize the new theme colors and have the correct elevation/shadows applied.

## Rollout Strategy
The changes are isolated to the presentation layer (theme definitions and atomic background components). All feature screens utilizing `MaterialTheme.colorScheme` and the atomic backgrounds will automatically inherit the improvements without requiring manual updates in each screen file.
