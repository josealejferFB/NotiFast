# Theme Customizer Engine AAA — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a comprehensive study theme customizer in NotiFast from `SettingsScreen`, offering 6 accessible presets, custom color generation with WCAG AAA compliance, a live preview micro-card, DataStore persistence, and seamless Google Drive cloud backup compatibility.

**Architecture:** MVI architecture with single StateFlow in `SettingsViewModel` and `MainViewModel`. Core algorithms in pure functions (`ThemePaletteGenerator`, `ThemePreset`). Persistence via Jetpack DataStore in `UserPrefsRepository` and serialized in `GlobalSnapshot` for Google Drive backup. Modular Compose UI under `presentation/feature/settings/components/` with strict $\le 250$ LOC per file.

**Tech Stack:** Kotlin 2.0.21, Jetpack Compose Material 3 (BOM 2024.09.00), Hilt 2.59, DataStore Preferences, JUnit4, MockK, Robolectric.

**Spec:** [`docs/superpowers/specs/2026-10-16-theme-customizer-engine-design.md`](../specs/2026-10-16-theme-customizer-engine-design.md). Executors read both.

---

## Global Constraints

- All UI strings must be in `res/values/strings.xml` in Spanish. Zero hardcoded strings in composables.
- Strict limit of $\le 250$ lines of code per Composable file.
- Strict Clean Architecture: zero business logic or color math in composables; all math in `ThemePaletteGenerator` and `ThemeUtils`.
- Material 3 Design Tokens: use `MaterialTheme.colorScheme.*`. Backgrounds and surface tokens (`BackgroundLight`, `SurfaceLight`, `SurfaceContainerLight`, `OutlineVariantLight`, etc.) must be preserved to maintain the tactile papercraft aesthetic.
- Backward compatibility: old cloud backups or missing DataStore keys must fall back to `"classic"` / `null` without throwing `NullPointerException`.
- Verification: `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` must remain 100% green at every step.

## Review Focus

1. **Accessibility & Contrast:** Both Light and Dark generated schemes must strictly guarantee readable contrast ratios ($\ge 4.5:1$ AA / $\ge 7.0:1$ AAA) for `primary` and `onPrimary`, `primaryContainer` and `onPrimaryContainer`.
2. **Missing/Corrupted Hex Values:** Deserializing or passing an invalid or blank hex string must gracefully fall back to the preset's default color without crashing.
3. **Old Cloud Backup Restore:** Restoring a backup snapshot without `theme_preset_id` or `custom_theme_primary_hex` must safely assign defaults without altering existing notes, subjects, or evaluations.
4. **Dark Mode Switcher Coexistence:** The Dark/Light/System mode switch must continue to toggle luminance seamlessly across all presets and custom colors.
5. **Tactile Haptic Feedback:** Swatch selection and custom palette interactions must perform tactile haptic feedback without crashing when haptics are disabled in settings.

---

## Task Breakdown

### Task 1: Domain Models & Presets (`ThemePreset.kt`)

**Files:**
- Create: `app/src/main/java/com/notifast/app/domain/model/theme/ThemePreset.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Test: `app/src/test/java/com/notifast/app/domain/model/theme/ThemePresetTest.kt`

**Interfaces:**
- Produces: `ThemePreset` enum with `id`, `nameRes`, `descriptionRes`, `primaryLightHex`, `secondaryLightHex`, `primaryDarkHex`, `secondaryDarkHex`, `fromId(id: String?): ThemePreset`, `availablePresets: List<ThemePreset>`.

- [ ] **Step 1: Write failing test `ThemePresetTest.kt`**
  Verify `fromId` mapping, default fallback to `CLASSIC`, and availability of the 6 curated presets (`classic`, `forest_sage`, `midnight_indigo`, `warm_mocha`, `sakura_zen`, `ocean_cyan`).
- [ ] **Step 2: Add theme preset strings to `strings.xml`**
  Add localized names and descriptions in Spanish for all 6 presets.
- [ ] **Step 3: Implement `ThemePreset.kt`**
  Implement the enum with full hex definitions and helper functions.
- [ ] **Step 4: Run tests**
  Run `./gradlew testDebugUnitTest --tests com.notifast.app.domain.model.theme.ThemePresetTest`.
- [ ] **Step 5: Commit**
  Commit `feat(theme): add ThemePreset model and localized academic study collection`.

---

### Task 2: Pure ColorScheme Engine (`ThemePaletteGenerator.kt`)

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/theme/ThemePaletteGenerator.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/theme/ThemePaletteGeneratorTest.kt`

**Interfaces:**
- Consumes: `ThemePreset`, `ThemeUtils`
- Produces:
  - `ThemePaletteGenerator.generateColorScheme(preset: ThemePreset, isDark: Boolean, customPrimaryHex: String? = null): ColorScheme`
  - `ThemePaletteGenerator.calculateContrastRatio(foreground: Color, background: Color): Float`
  - `ThemePaletteGenerator.createCustomScheme(primaryColor: Color, isDark: Boolean): ColorScheme`

- [ ] **Step 1: Write failing test `ThemePaletteGeneratorTest.kt`**
  Test contrast ratios ($\ge 4.5:1$ for text/primary on background, $\ge 7.0:1$ for onPrimary on primary), color generation for all presets in light and dark modes, and custom color scheme generation with invalid hex fallback.
- [ ] **Step 2: Implement `ThemePaletteGenerator.kt`**
  Implement the mathematical algorithm calculating `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`, `secondary`, `onSecondary`, `secondaryContainer`, `onSecondaryContainer` while locking papercraft surface and background tokens.
- [ ] **Step 3: Run tests**
  Run `./gradlew testDebugUnitTest --tests com.notifast.app.presentation.theme.ThemePaletteGeneratorTest`.
- [ ] **Step 4: Commit**
  Commit `feat(theme): implement ThemePaletteGenerator with WCAG AAA contrast guarantees`.

---

### Task 3: DataStore Persistence & Cloud Backup Compatibility

**Files:**
- Modify: `app/src/main/java/com/notifast/app/domain/repository/UserPrefsRepository.kt`
- Modify: `app/src/main/java/com/notifast/app/data/repository/UserPrefsRepositoryImpl.kt`
- Test: `app/src/test/java/com/notifast/app/data/repository/UserPrefsRepositoryTest.kt`
- Test: `app/src/test/java/com/notifast/app/domain/usecase/cloud/BuildCloudOverviewUseCaseTest.kt`

**Interfaces:**
- Consumes: DataStore preferences
- Produces:
  - `val themePresetIdFlow: Flow<String>`
  - `suspend fun setThemePresetId(presetId: String): Result<Unit>`
  - `val customPrimaryHexFlow: Flow<String?>`
  - `suspend fun setCustomPrimaryHex(hex: String?): Result<Unit>`
  - Updated `exportAll()` and `importAll()` including `theme_preset_id` and `custom_theme_primary_hex`.

- [ ] **Step 1: Write failing tests in `UserPrefsRepositoryTest.kt`**
  Test setting and observing `themePresetId`, `customPrimaryHex`, and exporting/importing them with fallback behavior when keys are absent.
- [ ] **Step 2: Update `UserPrefsRepository` and `UserPrefsRepositoryImpl`**
  Add keys `THEME_PRESET_ID` and `CUSTOM_THEME_PRIMARY_HEX`, flows, setters, and export/import mapping.
- [ ] **Step 3: Run unit tests**
  Run `./gradlew testDebugUnitTest --tests com.notifast.app.data.repository.UserPrefsRepositoryTest` and `BuildCloudOverviewUseCaseTest`.
- [ ] **Step 4: Commit**
  Commit `feat(prefs): add theme preset and custom primary hex persistence and cloud backup mapping`.

---

### Task 4: Global App Theme Wire-Up (`MainViewModel.kt`, `MainActivity.kt`, `Theme.kt`)

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/main/MainViewModel.kt`
- Modify: `app/src/main/java/com/notifast/app/MainActivity.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/theme/theme/Theme.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/main/MainViewModelTest.kt` (or create if missing)

**Interfaces:**
- Consumes: `UserPrefsRepository`, `ThemePaletteGenerator`
- Produces: `NotiFastTheme` reactive to `themePreset` and `customPrimaryHex`.

- [ ] **Step 1: Expose `themePresetId` and `customPrimaryHex` StateFlows in `MainViewModel`**
- [ ] **Step 2: Update `NotiFastTheme` in `Theme.kt`**
  Support `themePreset: ThemePreset = ThemePreset.CLASSIC` and `customPrimaryHex: String? = null` using `ThemePaletteGenerator.generateColorScheme(...)`.
- [ ] **Step 3: Update `MainActivity.kt`**
  Collect state flows and pass them to `NotiFastTheme`.
- [ ] **Step 4: Run `./gradlew assembleDebug`**
  Verify compilation and clean wiring.
- [ ] **Step 5: Commit**
  Commit `feat(theme): wire dynamic theme preset and custom palette into NotiFastTheme and MainActivity`.

---

### Task 5: Settings MVI Contract & ViewModel Updates

**Files:**
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/settings/SettingsContract.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/settings/SettingsViewModel.kt`
- Test: `app/src/test/java/com/notifast/app/presentation/feature/settings/SettingsViewModelTest.kt`

**Interfaces:**
- Produces:
  - `SettingsUiState.themePresetId`, `customPrimaryHex`, `isCustomThemeSheetOpen`, `customDraftColorHex`
  - `SettingsEvent.SelectThemePreset`, `OpenCustomThemeSheet`, `CloseCustomThemeSheet`, `UpdateCustomDraftColor`, `ApplyCustomTheme`

- [ ] **Step 1: Write unit tests in `SettingsViewModelTest.kt`**
  Test selecting a preset, opening sheet, changing draft color, applying custom theme.
- [ ] **Step 2: Update `SettingsContract.kt`**
  Add state fields and events.
- [ ] **Step 3: Update `SettingsViewModel.kt`**
  Observe `themePresetIdFlow` and `customPrimaryHexFlow`, implement event handlers with `Result<T>` and logging.
- [ ] **Step 4: Run tests**
  Run `./gradlew testDebugUnitTest --tests com.notifast.app.presentation.feature.settings.SettingsViewModelTest`.
- [ ] **Step 5: Commit**
  Commit `feat(settings): support theme preset selection and custom palette studio in SettingsViewModel`.

---

### Task 6: UI Components — Mini-Card Previewer & Custom Palette Sheet

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/settings/components/ThemePreviewMiniCard.kt` ($\le 250$ LOC)
- Create: `app/src/main/java/com/notifast/app/presentation/feature/settings/components/CustomThemePaletteSheet.kt` ($\le 250$ LOC)
- Modify: `app/src/main/res/values/strings.xml`

**Interfaces:**
- Consumes: `NotiFastCard`, `NotiFastButton`, `ThemePaletteGenerator`
- Produces:
  - `ThemePreviewMiniCard(colorScheme: ColorScheme, modifier: Modifier)`
  - `CustomThemePaletteSheet(isOpen: Boolean, draftHex: String, onColorSelected: (String) -> Unit, onApply: () -> Unit, onDismiss: () -> Unit)`

- [ ] **Step 1: Implement `ThemePreviewMiniCard.kt`**
  Micro papercraft card showing simulated subject title, evaluation pill with dynamic container, and `NotiFastButton`.
- [ ] **Step 2: Implement `CustomThemePaletteSheet.kt`**
  Modal bottom sheet with a 12-color curated academic grid, live mini-card preview, tactile feedback on selection, and cancel/apply actions.
- [ ] **Step 3: Commit**
  Commit `feat(settings): add ThemePreviewMiniCard and CustomThemePaletteSheet components`.

---

### Task 7: UI Components — ThemeSelectorCard & SettingsScreen Integration

**Files:**
- Create: `app/src/main/java/com/notifast/app/presentation/feature/settings/components/ThemeSelectorCard.kt` ($\le 250$ LOC)
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/settings/components/SettingsSectionsContent.kt`
- Modify: `app/src/main/java/com/notifast/app/presentation/feature/settings/SettingsScreen.kt`

**Interfaces:**
- Consumes: `ThemePreset`, `SettingsUiState`, `SettingsEvent`
- Produces:
  - `ThemeSelectorCard` embedded in appearance section
  - Quick-selection row of swatches with animated checkmark and haptic feedback
  - Button to open Custom Studio

- [ ] **Step 1: Implement `ThemeSelectorCard.kt`**
  Papercraft card displaying the active theme, horizontal swatch picker with animated spring scale/check, and Dark/Light/System segment toggle.
- [ ] **Step 2: Integrate into `SettingsSectionsContent.kt` and `SettingsScreen.kt`**
  Render `ThemeSelectorCard` and wire `CustomThemePaletteSheet`.
- [ ] **Step 3: Run `./gradlew assembleDebug`**
  Verify build succeeds with zero errors.
- [ ] **Step 4: Commit**
  Commit `feat(settings): integrate ThemeSelectorCard and Custom Studio into SettingsScreen`.

---

### Task 8: End-to-End Verification, Device Testing & Roadmap Status Update

**Files:**
- Modify: `docs/prd/STATUS.md`

- [ ] **Step 1: Run full unit test suite**
  `./gradlew testDebugUnitTest` — verify 614+ tests green.
- [ ] **Step 2: Assemble Debug APK**
  `./gradlew assembleDebug`.
- [ ] **Step 3: Install on connected Android device**
  `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
- [ ] **Step 4: Live verification via adb / UI checks**
  Verify visual rendering in light and dark mode, preset switching, custom theme selection, and persistence across app restarts.
- [ ] **Step 5: Update `docs/prd/STATUS.md`**
  Document the completion of Theme Customizer Engine AAA and update roadmap metrics.
- [ ] **Step 6: Final commit**
  Commit `docs: update STATUS.md with Theme Customizer Engine AAA completion`.
