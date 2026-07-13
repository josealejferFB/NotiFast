---
target: subjectdetail
total_score: 27
p0_count: 0
p1_count: 2
timestamp: 2026-07-13T15-05-26Z
slug: xample-notifast-presentation-feature-subjectdetail
---
Method: dual-agent (A: manual fallback · B: skipped due to native platform)

### Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 3 | Tabs make navigation clear, but visual hierarchy is weak. |
| 2 | Match System / Real World | 4 | Domain language is good. |
| 3 | User Control and Freedom | 3 | Good. |
| 4 | Consistency and Standards | 2 | Background gradients and subtle cards don't match the bold, modern intent of the app. |
| 5 | Error Prevention | 3 | Good. |
| 6 | Recognition Rather Than Recall | 4 | Tabs solved the cognitive overload issue from V1. |
| 7 | Flexibility and Efficiency | 3 | Speed Dial helps, but content scanning is slow due to basic card designs. |
| 8 | Aesthetic and Minimalist Design | 1 | Background gradient feels dated. Subject color is underutilized. Notes cards are too subtle and lack context. |
| 9 | Error Recovery | 3 | Good. |
| 10 | Help and Documentation | 1 | N/A |
| **Total** | | **27/40** | **Needs Polish** |

### Anti-Patterns Verdict

**LLM assessment**: The V1.5 refactor solved the "Wall of Information" problem by introducing Tabs, but the visual aesthetic remains dated and inconsistent with the "premium" goal. The background gradient feels like an afterthought. The Notes section is too basic (just generic cards), missing the opportunity to tell a story about the student's study timeline. The Evaluations section lacks a strong thematic visual identity (e.g., real calendar sheets).

### Overall Impression
The structure is now solid thanks to the Tabs, but the visual design needs a complete "Version 2.0" overhaul. We need to move away from subtle, generic UI towards bold, opinionated design that heavily leverages the subject's primary color to create a deeply immersive experience for that specific class.

### Priority Issues (V2.0 Goals)

- **[P1] Immersive Subject Theming**:
  - **Why it matters**: The current gradient is generic. The user should instantly "feel" they are inside the specific subject (e.g., Math = Blue, History = Orange).
  - **Fix**: Remove the generic background. Implement a bold, modern hero section that uses the subject's color as a vibrant, large-scale element (e.g., a solid color block or an elegant mesh gradient), contrasting sharply with clean white/dark cards below.
  
- **[P1] Notes Chronology (Timeline UX)**:
  - **Why it matters**: The current notes are just a basic grid/list of pale cards. They don't communicate the progression of learning.
  - **Fix**: Redesign the "Apuntes" tab into a rich chronological timeline. Each note should be a node on a vertical line, showing exact dates, snippet previews, and strong visual hierarchy.

- **[P2] Calendar Sheet Aesthetic for Evaluations**:
  - **Why it matters**: Evaluations are currently just standard list items.
  - **Fix**: Redesign the "Evaluaciones" tab cards to look like physical, tear-off calendar sheets or premium date-blocks. The date should be massive and separated from the details.

### Persona Red Flags

**Taylor (The Aesthete)**: Taylor finds the current gradient distracting and cheap-looking. The notes cards feel like standard Material templates with no soul. Taylor expects an app that feels like a premium digital notebook.

### Recommended Action Plan for V2.0
We need to run a complete visual overhaul on the 3 tabs. I will prepare an `implementation_plan.md` to design this V2.0 step-by-step.
