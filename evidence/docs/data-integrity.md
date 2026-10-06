# UB-DormHub - Data integrity (Lab 8)

Links: `models/logical-data-model.puml` / `.sql`, `docs/api-contracts/core-operation.md`, `models/deployment.puml`, `models/failure-recovery.puml`, `decisions/ADR-001-architecture.md`.
Rule IDs follow the submitted Phase 1 report section 6.2.

## 1. Integrity rules and enforcement

The database is the final line of defence, not the only one. **Who may act** and **which transition is legal** are enforced by the Access Service and Domain. Database keys/checks prevent invalid or duplicate persistent state from being committed.

| # | Integrity rule | Source | Main enforcement | Failure outcome / verification |
|---|---|---|---|---|
| I-1 | A complaint has one of the seven approved lifecycle states | BR-01, BR-09 | Domain transition table + DB `CHECK` | Invalid state rejected; test #1 |
| I-2 | Every new complaint starts `REPORTED` | BR-01 | Domain constructor + DB default | Creation test |
| I-3 | Complaint identifies a valid student and room, and the room is the student's allowed room | BR-10, BR-11 | Foreign keys + Complaint Service room-association rule | Validation/FK tests |
| I-4 | Only a `REPORTED` complaint can be decided | BR-03, BR-09 | Domain + conditional update `WHERE status='REPORTED'` | Stale/concurrent request -> 409; test #4 |
| I-5 | **VERIFY** commits `Reported -> Verified` and one `CertificationRecord` together | BR-04, QS-04 | One Unit of Work + `UNIQUE(certification_record.complaint_id)` | Rollback on failure; tests #2 and #5 |
| I-6 | **REJECT** commits `Reported -> Rejected` together with a rejection reason; it does **not** create a `CertificationRecord` | BR-03, BR-14 and Phase 1 glossary/BR-04 semantics | Conditional update + DB check on `rejection_reason` | Invalid rejection rejected; tests #3 and #6 |
| I-7 | Retrying the same operation cannot create a second decision | QS-04 | Idempotency record `UNIQUE(user_id, idempotency_key, operation)` + stored response | Replay returns original response; test #7 |
| I-8 | A complaint has at most one `WorkOrder`; rework returns/reassigns that same WorkOrder | BR-05, BR-06, BR-15; Phase 1 glossary | `UNIQUE(work_order.complaint_id)` + Work Order Service | Second WorkOrder rejected; rework update allowed; tests #8 and #9 |
| I-9 | Assigned/in-progress work always names a maintenance staff member | BR-06, BR-11 | `staff_user_id NOT NULL` + service transaction | Assignment fault injection |
| I-10 | Protected actions require the correct authenticated role | BR-02, BR-13, QS-03 | Access Service before state-changing work; DB role value check | 403 and no state change; test #10 plus role matrix |
| I-11 | `Rejected` has no outgoing maintenance transition | BR-14 | Domain transition table | 409 on attempted assignment |
| I-12 | Room occupancy does not exceed the domain maximum | Domain model (0..2 residents per room) | Room-allocation service rule | Unit test; documented because simple SQL DDL does not express the cross-row count cleanly |

## 2. Logical-model refinements from the Phase 1 domain model

| Phase 1 concept | Lab 8 logical representation | Reason |
|---|---|---|
| Separate actor classes (`ResidentStudent`, `StudentWelfareOfficer`, `MaintenanceManager`, `MaintenanceStaff`, `UniversityManagement`) | `user_account.role` plus `resident_student` / `staff_profile` | Avoids repeating identity fields while preserving role-specific behaviour in domain/application code |
| Resident Assistant is a facilitating stakeholder | No RA persistence table in the assessed slice | The submitted report explicitly keeps RA outside the primary digital transaction unless later evidence changes scope |
| `PerformanceReport` | Computed, not stored | FR-13 reporting is read-only and derived from operational records |
| `CertificationRecord` = evidence of **successful** verification | Row exists only for VERIFY; REJECT stores `maintenance_complaint.rejection_reason` | Aligns Lab 8 with the submitted glossary and BR-04 instead of redefining CertificationRecord as a generic decision record |
| Rework returns the `WorkOrder` for further work | One WorkOrder per complaint; the same row is returned/reassigned | Aligns with the submitted glossary and BR-15, and avoids inventing a second repair record for the same complaint |
| `ComplaintStatus` shown conceptually | Constrained text in DDL | Preserves the seven named values and remains implementation-neutral |
| No conceptual idempotency entity | `idempotency_record` technical table | Supports safe retries for QS-04 without changing the domain model |

## 3. Cross-table invariants that require service transactions/tests

Some rules cannot be guaranteed by a single SQL `CHECK` because they span tables:

1. If a complaint is `VERIFIED`, exactly one matching `CertificationRecord` must exist.
2. If a complaint is `REJECTED`, `rejection_reason` must be present and no `CertificationRecord` should be created by the workflow.
3. Assignment must commit both the complaint's `ASSIGNED` state and its single `WorkOrder` together.
4. Rework must change the complaint and its existing WorkOrder consistently in one transaction.

These are therefore checked by application transactions plus reconciliation/fault-injection tests, not by pretending the DDL alone can express every domain invariant.

## 4. Main integrity risk (Lab 8 exit record)

| | |
|---|---|
| **Risk** | A complaint is marked `VERIFIED` without its CertificationRecord, or a retry/concurrent request records the same decision twice. |
| **Design mechanism** | One transaction per use case (ADR-001 R1), conditional `Reported` update, unique CertificationRecord per complaint, idempotency record with stored response, controlled rollback/error handling. |
| **Test needed** | Inject failure after the conditional status update but before CertificationRecord commit and expect complaint still `REPORTED`; replay the same Idempotency-Key and expect one decision; race two new-key decisions and expect one success and one 409; run a reconciliation query after tests. |

The executable database-level checks are in `tests/data_integrity_check.py`.
