# UB-DormHub - Usability and accessibility traceability

Adds the user-interface layer to the Phase 1 traceability matrix (requirement -> use case -> element -> verification).

| Requirement / quality | Use case / task | UI element | Alternative states | Verification |
|---|---|---|---|---|
| FR-01 Report fault; FR-03 ComplaintForm | UC-01, T1 | S1 -> S2 -> S3 (F1, F2, F3) | Validation, rule failure (AC-02, BR-12), error, submitting | Usability test T1; AC-01, AC-02 |
| FR-09 Track status; QS-02 Status accuracy | UC-09, T2 | S1, S3 (F1, F3) | Loading, empty, unavailable, stale | Usability test T2; AC-09 |
| FR-04 / FR-05 Verify and certify | UC-04, T3 | W1, W2, W3 (F4, F5, F6) | Empty queue, conflict 409, error 503, validation | Usability test T3; AC-03, AC-04, AC-05 |
| FR-11 / FR-12 Authenticate, role access; QS-03 | All | L1, U1 403 screen | Unauthorised, session expired | AC-12; role x screen test |
| QS-04 Reliability | UC-01, UC-04 | Error and submitting states (F2, F5) | Error never shows success, text kept, same key on retry | Fault-injection test (Lab 8) plus UI check |
| QS-05 Usability | T1, T2, T3 | Navigation shortest paths, plain wording | All | 3-person check now; 5-user test on working prototype |
| QS-06 Accessibility | All | `docs/accessibility-checklist.md` A1 to A15 | Errors, focus, non-colour cues | Checklist "How to verify" column |
| QS-07 Privacy | T3 | F5 shows only needed fields | Unauthorised | Content and API response inspection |

Rule IDs follow the Phase 1 report and acceptance criteria. Error codes follow `docs/api-contracts/core-operation.md`.
