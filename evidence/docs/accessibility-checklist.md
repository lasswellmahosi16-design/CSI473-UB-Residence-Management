# UB-DormHub - Accessibility checklist


**Status key:** Designed = built into the wireframe/spec. To verify = must be tested on the working prototype. Nothing here is marked "Passed" because there is no implementation yet.

| # | Requirement | How the design meets it | Wireframe ref | Status | How to verify after implementation |
|---|---|---|---|---|---|
| A1 | **Keyboard access** to every control | Only native HTML controls (button, a, input, select, textarea); logical tab order top to bottom | All frames | Designed | Unplug the mouse and complete T1, T2, T3 using Tab, Shift+Tab, Enter, Space |
| A2 | **No keyboard trap** | Rejection confirmation is an inline panel, not a modal that holds focus; Cancel is reachable | F6 | Designed | Tab through every screen; focus can always leave |
| A3 | **Visible focus** | 3px dark outline with offset on every focusable element; never removed | All frames | Designed | Tab through; focus is visible on every stop against its background |
| A4 | **Persistent labels** | Every field has a visible label above it, not a placeholder; placeholders only hold examples | F2, F6 | Designed | Check no field relies on placeholder as its label |
| A5 | **Non-colour cues** | Status = word + icon + position in timeline; errors = icon + "Error:" text + message; required = word "required" | F1, F3, F4, F2 | Designed | View in greyscale; every state still distinguishable |
| A6 | **Clear errors** | Error summary at top, message beside each field, linked with `aria-describedby`; focus moves to the summary; typed text kept | F2, F6 | Designed | Submit empty form with a screen reader; messages are announced and reachable |
| A7 | **Readable responsive layout** | Student screens single column from 320px wide; text reflows at 200% zoom with no sideways scrolling; officer table scrolls inside its own box | F1-F3, F4 | Designed | Test at 320px width and 200% zoom on a phone and a laptop |
| A8 | **Text size and contrast** | Body text 16px or larger; dark text on light background, contrast ratio at least 4.5:1 | All | Designed | Run a contrast checker on final colours |
| A9 | **Touch targets** | Buttons and list rows at least 44px high on the student layout | F1, F2 | Designed | Measure in browser dev tools |
| A10 | **Headings and landmarks** | One `h1` per screen, `main`, `nav`, `header` landmarks | All | Designed | Screen reader landmark list; heading outline |
| A11 | **Status announcements** | Loading, success and error messages in an `aria-live` region | F1, F2, F5 | Designed | Screen reader announces each state change |
| A12 | **Double-submit protection** | Submit disabled while sending, and server idempotency (Lab 8) | F2 | Designed | Press Enter twice quickly; one complaint exists |
| A13 | **Plain language** | Short sentences, no jargon (no "persistence", no codes); statuses explained in one line | F1, F3 | Designed | Usability test: testers can explain each status in their own words |
| A14 | **Privacy on screen** | Officer sees only what verification needs; maintenance views (later) hide unrelated student data | F5 | Designed | Inspect screen content and API responses |
| A15 | **Session safety** | Session expiry message; login required for every protected screen | Any | Designed | Expire a token and repeat an action |

## Not covered (and why)

- Full screen-reader testing on multiple products: only a spot check with one screen reader is realistic this semester.
- Offline use: out of scope for this project.
- Languages other than English: not required by the approved scope.
