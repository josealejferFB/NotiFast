---
target: AddEvaluation and EditEvaluation screens
total_score: 25
p0_count: 0
p1_count: 1
timestamp: 2026-07-13T19-55-33Z
slug: addevaluation-editevaluation
---
| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 3 | Button disable state correctly indicates when form is incomplete |
| 2 | Match System / Real World | 4 | Academic terms are familiar to the target audience |
| 3 | User Control and Freedom | 3 | Back button on TopBar provides a clear exit |
| 4 | Consistency and Standards | 2 | Custom toggle for "Presentado", hardcoded cursor colors break Material theming |
| 5 | Error Prevention | 2 | Grade input allows typing any decimal without immediate inline range constraint (1-10) |
| 6 | Recognition Rather Than Recall | 3 | Dropdown for subjects helps recognition |
| 7 | Flexibility and Efficiency | 2 | Linear vertical form forces scrolling; no quick save shortcuts |
| 8 | Aesthetic and Minimalist Design | 2 | High cognitive load: 7+ inputs stacked vertically with varying icon styles |
| 9 | Error Recovery | 2 | No visible form-level error messages before submission |
| 10 | Help and Documentation | 2 | No contextual help for what "Peso" means if the student is new |
| **Total** | | **25/40** | **Acceptable** |

#### Anti-Patterns Verdict

**LLM assessment**: The UI exhibits several anti-patterns for product UI. It reinvents a standard affordance (a toggle switch) as a custom clickable `Surface` for the "Presentado/Pendiente" state. It also overrides system theme colors with hardcoded hex values for cursors (`0xFFFBC02D`, `Aqua`), causing inconsistency. The layout is a straightforward "wall of inputs" that increases cognitive load without progressive disclosure.

**Deterministic scan**: Detector unavailable (Android Native Kotlin project).

#### Overall Impression
The forms are functional and correctly use Material 3 Compose for the most part, but they suffer from cognitive overload and minor deviations from Material standards (like the custom toggle and hardcoded colors). The biggest opportunity is grouping the inputs logically to reduce vertical scrolling.

#### What's Working
- **Dynamic Fields**: The form smartly hides the "Weight" field depending on the selected `EvaluationType`.
- **Save Guardrails**: The save button is correctly disabled until required fields are filled, preventing empty submissions.

#### Priority Issues

- **[P1] Custom Toggle Reinvents Standard Control**
  - **Why it matters**: The "Presentado/Pendiente" toggle is a custom `Surface`. Users expect standard Android controls (like a Switch or SegmentedButton) for boolean states.
  - **Fix**: Replace the custom `Surface` with a standard Material 3 Switch or SegmentedButton.
  - **Suggested command**: `/impeccable adapt`

- **[P2] Wall of Options (High Cognitive Load)**
  - **Why it matters**: 7+ form fields are stacked vertically, forcing a long scroll to reach the primary action (Save).
  - **Fix**: Group related fields (e.g., Title/Subject/Type as Primary; Date/Image/Description as Details) using visual sections or cards.
  - **Suggested command**: `/impeccable layout`

- **[P2] Hardcoded Colors Break Theming**
  - **Why it matters**: `TextField` cursor colors are hardcoded to `Aqua` or `0xFFFBC02D`. This breaks Dark Mode support and general theme consistency.
  - **Fix**: Remove hardcoded colors and let them inherit from `MaterialTheme.colorScheme.primary` or `secondary`.
  - **Suggested command**: `/impeccable polish`

#### Persona Red Flags

**Alex (Power User)**:
- Forced to scroll to the very bottom to save; no quick action available at the top.
- Dropdown for Subject requires multiple taps; an autocomplete/combo text field would be faster.

**Casey (Mobile User)**:
- Long vertical scroll means the Save button is out of the initial viewport.
- The most common inputs might be buried below optional ones.
