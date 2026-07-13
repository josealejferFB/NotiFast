---
target: presentation/feature/notes
total_score: 29
p0_count: 1
p1_count: 1
timestamp: 2026-07-13T13-12-27Z
slug: presentation-feature-notes
---
Method: ⚠️ DEGRADED: single-context (no sub-agent tool exposed)

#### Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 3 | Good loading states and error feedback. |
| 2 | Match System / Real World | 4 | Excellent use of academic terminology. |
| 3 | User Control and Freedom | 2 | Missing back button in EditNoteScreen top bar. |
| 4 | Consistency and Standards | 3 | Search bar shadow violates Flat-By-Default rule. |
| 5 | Error Prevention | 1 | Free-text subject field allows breaking data relationships. |
| 6 | Recognition Rather Than Recall | 3 | Filters and subjects are always visible. |
| 7 | Flexibility and Efficiency | 3 | Quick actions via expandable FAB. |
| 8 | Aesthetic and Minimalist Design | 3 | Mostly clean, but search bar shadow is unnecessary noise. |
| 9 | Error Recovery | 3 | Clear error states with retry buttons. |
| 10 | Help and Documentation | 4 | Empty states provide excellent contextual guidance. |
| **Total** | | **29/40** | **[Good]** |

#### Anti-Patterns Verdict

**LLM assessment**: The design generally feels native and purposeful, avoiding standard AI layout traps. The `NotiFastStudyBackground` and tonal surfaces adhere well to the brand's academic focus. However, the `EditNoteScreen`'s free-text field for the subject name is a significant functional and UX anti-pattern for relational data, as it puts the burden of exact string matching on the user. Additionally, the search bar in `NotesScreen` includes a 2.dp shadow that explicitly contradicts the `DESIGN.md` "Flat-By-Default" rule.

**Deterministic scan**: Deterministic scan unavailable (native Android Compose code).

#### Overall Impression
The foundation is strong and the visual system correctly prioritizes clarity and academic focus. The major letdown is the functional design of the Edit screen, which allows users to unknowingly break their own data structure, and minor inconsistencies in the elevation rules.

#### What's Working
- **Empty States**: The contextual guidance ("Empieza creando tu primera materia...") is excellent and perfectly placed.
- **Tonal Hierarchy**: The use of `Ruby` and `Aqua` for selection states and borders feels distinct and clearly communicates the active filters without overwhelming the page.

#### Priority Issues

- **[P0] Free-text Subject allows broken data relationships**
  - **Why it matters**: `EditNoteScreen` allows users to manually type the "Materia" in a `NotiFastTextField`. Because subject names act as document IDs in Firestore, a simple typo will orphan the note into a nonexistent subject.
  - **Fix**: Change the subject input to a read-only field (if it shouldn't change) or a dropdown selector from existing subjects.
  - **Suggested command**: `/impeccable harden`

- **[P1] Missing Back Navigation**
  - **Why it matters**: `EditNoteScreen` receives an `onBackClick` parameter, but it's never passed to the `NotiFastCenterAlignedTopBar`. Users without a system back gesture are trapped.
  - **Fix**: Pass the `onBackClick` parameter to the TopBar so it renders a navigation icon.
  - **Suggested command**: `/impeccable harden`

- **[P2] Inconsistent Elevation Pattern**
  - **Why it matters**: The search bar in `NotesScreen` uses `shadowElevation = 2.dp`. This violates the `DESIGN.md` "Flat-By-Default" rule, which dictates that separation should be achieved through color and borders, not shadows.
  - **Fix**: Remove the elevation from the `Surface` and rely solely on the tonal border stroke.
  - **Suggested command**: `/impeccable polish`

#### Persona Red Flags

**Riley (Deliberate Stress Tester)**
- Editing a note and changing the subject to include special characters or a very long string will break the UI grouping and potentially the Firestore path.

**Alex (Impatient Power User)**
- Navigates into `EditNoteScreen`, decides not to make changes, and finds no visible way to cancel or go back without relying on system gestures.

#### Minor Observations
- The `subjectToDelete` dialog warns that "se borrarán todos los apuntes asociados". Ensure the backend actually performs a batch delete, as deleting a document in Firestore doesn't automatically delete its subcollections.
- The `NotesTabContent` search bar uses `BasicTextField` with a custom cursor brush, but could benefit from standard `OutlinedTextField` with no borders to maintain perfect input semantics.

#### Questions to Consider
- Should users be allowed to change a note's subject after it's created, or should the subject be fixed?
- Does the search bar need to be persistently visible, or could it be an expanding action in the top bar to save vertical space?
