# UB-DormHub - Alternative UI states (Lab 10)

**Traceability:** QS-02, QS-03, QS-04, QS-05, QS-06, QS-07; AC-02, AC-03, AC-04, AC-05, AC-09, AC-12; error codes from `docs/api-contracts/core-operation.md`.
Screen IDs match `models/navigation.puml` and `prototype/wireframes.html` (frame numbers in brackets).

Only states that matter for this project are designed. Offline caching and push notifications are **not** designed (out of scope per the approval conditions). The prototype assumes an online connection and shows a clear message when it is lost.

## 1. State table

| Screen | State | Trigger | What the user sees | What the user can do | Source |
|---|---|---|---|---|---|
| S1 My complaints [F1] | **Loading** | List request in progress | "Loading your complaints..." text with a spinner; page layout stays in place | Wait; Report a fault stays usable | QS-01 |
| S1 | **Empty** | Student has no complaints | "You have not reported any faults. If something in your room is broken, report it here." + Report a fault button | Start T1 | QS-05 |
| S1 | **Normal** | Complaints exist | One card per complaint: reference, category, date, status word + icon | Open a complaint (T2) | FR-09 |
| S1 | **Unavailable** | Server or network failure | "We could not load your complaints. Your reports are safe. Try again." + Try again button | Retry | QS-04 |
| S2 Report a fault [F2] | **Normal** | Opened | Room shown as text (Block 3, Room 101); category list; description box; Submit | Complete T1 | FR-01 |
| S2 | **Validation** | Category missing or description under 10 characters | Summary at top ("2 things need fixing") and a message under each field; focus moves to the summary; typed text is kept | Fix and resubmit | QS-05, QS-06 |
| S2 | **Submitting** | Submit pressed | Button text "Submitting..." and disabled to stop double submit | Wait | QS-04 |
| S2 | **Rule failure** | No room on student record, or an active duplicate for the same fault | "We could not find a room linked to your account. Ask Student Welfare to update it." or "You already reported this fault (ref UBD-1042). See its status." with a link | Open the existing complaint or contact Welfare | AC-02, BR-12 |
| S2 | **Error** | Server fails to save | "Your report was not saved. Nothing was sent. Try again." Text is kept. No success message is ever shown | Retry | QS-04 |
| S3 Complaint detail [F3] | **Normal** | Opened | Reference, status word + icon, one line "what happens next", timeline of steps done and pending | Read status; go back | FR-09, QS-02 |
| S3 | **Stale** | Status changed since last list load | On open the page shows the current persisted status; list is refreshed when the user goes back | None needed | QS-02 |
| S3 | **Unauthorised** | Student opens another student's complaint | 403 screen: "You do not have access to this page." + link to own complaints. No complaint data is shown | Go home | QS-03, QS-07 |
| W1 Verification queue [F4] | **Loading / Empty / Unavailable** | As S1 | Empty text: "No complaints are waiting for verification." | Retry or wait | FR-04 |
| W1 | **Normal** | Reported complaints exist | Table: reference, category, room, date reported; oldest first; row link opens W2 | Open a complaint | FR-04 |
| W2 Review complaint [F5] | **Normal** | Opened | Full details, Verify and Reject buttons far apart | Verify (T3) or Reject | AC-03, AC-04 |
| W2 | **Success** | Verify returns 201 | Return to queue with "Complaint UBD-1042 verified" message | Next complaint | AC-03 |
| W2 | **Stale / conflict** | API returns 409 INVALID_STATE | "This complaint was already verified by another officer. Current status: Verified." + Back to queue | Back to queue | AC-05 |
| W2 | **Error** | 503 PERSISTENCE_UNAVAILABLE | "The decision was not saved. The complaint is still Reported. Try again." Retry uses the **same** Idempotency-Key | Retry | QS-04 |
| W2 | **Unauthorised** | 403 FORBIDDEN_ROLE | 403 screen, no data shown | Go home | AC-12, QS-03 |
| W3 Confirm rejection [F6] | **Validation** | Reason empty or under 10 characters | Message under the reason field, focus on the field | Fix | API contract |
| W3 | **Confirm** | Reject chosen | Reason box + "Confirm rejection" and "Cancel" | Confirm or cancel | AC-04 |
| Any | **Session expired** | 401 UNAUTHENTICATED | "Your session has ended. Log in again." Work in progress is not silently lost; user returns to the same screen after login | Log in | FR-11 |
| Any | **Connection lost** | Browser offline | Banner "You are offline. Changes cannot be sent until you reconnect." Submit buttons disabled | Wait / reconnect | QS-04 |

## 2. Message rules (apply to every state)

1. Say what happened, whether data was saved, and what to do next.
2. Never use codes alone (`PERSISTENCE_UNAVAILABLE` is for developers; the user sees plain text).
3. A success message appears only after the server confirms.
4. An error never clears what the user typed.
5. Messages are text with an icon, never colour alone, and are announced to screen readers.
