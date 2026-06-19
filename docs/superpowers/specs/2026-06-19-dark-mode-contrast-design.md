# Educational Hub & Contrast Design

## Goal
Improve the user experience across both Dark and Light modes by ensuring optimal color contrast, and elevate the overall UI to feel like a modern "Educational Hub" rather than a generic notes app.

## 1. Intelligent Contrast (Color Accessibility)
- Introduce a utility (`ThemeUtils.getAccessibleColor`) that dynamically adjusts the user's custom `subjectColorHex`:
  - **Dark Mode:** Desaturate and slightly darken overly bright colors (e.g. Neon Yellow -> Olive).
  - **Light Mode:** Darken overly pale colors so they don't blend into the white background.
- Soften the extreme blacks of the Dark Mode background (`#132B2B` -> slate/blue-gray) to reduce eye strain.

## 2. Hero Redesign (Subject Detail)
- **Current:** Harsh linear gradient that clashes with bright custom colors.
- **New (Glassmorphism):** Remove the linear gradient. Use a solid slate background with a subtle radial "glow" behind the icon. The subject icon will be housed inside a frosted glass (Glassmorphism) circle.

## 3. Section Hierarchy (Pills)
- Replace plain text headers ("Apuntes", "Calendario") with pill-shaped badges (rounded corners, 10% opacity background of the subject color, accompanied by an emoji/icon).

## 4. Calendar Cards (Physical Aesthetic)
- Retain the square/rigid look of a physical wall calendar for the `SubjectDetailCalendarCard.kt` (8dp or 12dp radius, high contrast header for the month, large bold day number).

## 5. Unified Speed Dial FAB
- Replace the two stacked Floating Action Buttons ("Add Note", "Add Evaluation") with a single primary FAB that expands into a Speed Dial menu, decluttering the screen.

## Affected Components
- `presentation/theme/color/Color.kt`
- `presentation/atoms/NotiFastStudyBackground.kt`
- `presentation/feature/subjectdetail/components/SubjectDetailHero.kt`
- `presentation/feature/subjectdetail/components/SubjectDetailCalendarCard.kt`
- `presentation/feature/subjectdetail/SubjectDetailScreen.kt` (FAB and Section Headers)
