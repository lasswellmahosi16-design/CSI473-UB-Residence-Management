# UB-DormHub - Quality scenarios to architecture 

## 1. The three scenarios that shape the architecture most

| ID | Scenario (Phase 1) | Why it drives the structure |
|---|---|---|
| **QS-04 Reliability** | A persistence failure happens during a complaint / certification / work-order transaction. Result must be a controlled failure with zero partial records and zero false-success confirmations. | Fault reporting, verification and assignment each write more than one record. Where the transaction boundary sits is the biggest structural decision. This is the scenario that most influenced the architecture. |
| **QS-03 Authorisation** | A user without the required role tries verification, assignment or a repair update. Result: denied, no state change, 100% of tested attempts. | Five roles have different protected actions. The check must exist on the server, in one place, and run before any domain call. |
| **QS-08 Maintainability** | A future notification adapter or display label is changed. No more than two production components outside tests/configuration may change. | SMS, Teams and offline sync are deferred, not cancelled. Attaching them later must not touch the domain or the other services. |

Supporting scenarios that follow from the same design (not drivers on their own):
**QS-02 Status accuracy** (single write path for status), **QS-07 Privacy** (WorkOrder view hides unrelated student data).
QS-01, QS-05 and QS-06 are mostly met at UI and query level, so they do not push the component structure.

## 2. Design obligations

| Obligation | From | What the architecture must do |
|---|---|---|
| **O-1** | QS-04 | Any use case that writes more than one record (report + form, verify + certification, assign + work order) runs in **one transaction owned by its application service**. The new complaint state is committed only if every write succeeds. |
| **O-2** | QS-04 | A persistence failure becomes a controlled error (`PERSISTENCE_UNAVAILABLE`), never a success message. The transaction is rolled back. |
| **O-3** | QS-04 | A retry after a timeout must not create duplicate records (idempotency key + unique constraint). |
| **O-4** | QS-03, FR-12, BR-13 | Every protected use case calls the Authentication & Access Service **before** it touches the domain or repositories. Hiding buttons in the UI is not enough. |
| **O-5** | QS-03 | The role-to-permission table lives in one component, so a rule change is made once. |
| **O-6** | QS-08 | Future adapters attach through a port (`NotificationPort`). Domain and services never import adapter code. |
| **O-7** | QS-08 | Status names and display labels are defined once in the Domain and mapped to text in the UI. |
| **O-8** | QS-02, BR-09 | All status changes go through `MaintenanceComplaint` in the Domain. Services never set `status` directly. |
| **O-9** | QS-07 | Work Order Service returns a work-order view that excludes unrelated complaints and unnecessary student fields. |

## 3. Obligation to architecture element to test

| Obligation | Architecture element(s) | Related FR / BR / AC | Planned verification |
|---|---|---|---|
| O-1, O-2 | Verification Service, Work Order Service, Complaint Service, Repository Interfaces (Unit of Work), Persistence Adapter | FR-04, FR-05, FR-06; BR-04, BR-06; AC-03, AC-06 | Fault-injection test: make `CertificationRepository.save` throw during a VERIFY decision. Expect complaint still `Reported`, no CertificationRecord, error `PERSISTENCE_UNAVAILABLE`. For REJECT, inject a failure after the conditional status/reason update and expect the whole transaction to roll back. Same test for WorkOrder save. |
| O-3 | Verification Service, Persistence Adapter, `idempotency_record` | FR-04; BR-03, BR-09; AC-05 | Send the same request twice with one Idempotency-Key: one CertificationRecord, same response both times. |
| O-4, O-5 | Authentication & Access Service, all Application Services | FR-11, FR-12; BR-02, BR-13; AC-12 | Role x operation matrix test: every wrong-role attempt returns 403 and leaves complaint and work-order state unchanged. |
| O-6 | Repository Interfaces / `NotificationPort`, Deferred Integration Adapters | QS-08 | Change-impact exercise: add a dummy notification adapter, count production components modified (limit 2). |
| O-7, O-8 | Domain Model & Business Rules (`MaintenanceComplaint`) | FR-04, FR-14; BR-01, BR-03, BR-08, BR-09 | Transition table test: every disallowed transition (e.g. `Reported -> Assigned`, `In Progress -> Closed`) is rejected and state is unchanged. |
| O-9 | Work Order Service, Web UI | FR-07; QS-07 | Inspect the work-order response: no other student's data, no unrelated complaints. |

## 4. Highest architectural risk (Lab 7 exit)

Several services (Verification, Work Order) change the same `MaintenanceComplaint` state. If the transaction boundary is not applied consistently, or a service sets status directly, a complaint could be `Verified` with no CertificationRecord. Mitigation: O-1, O-2 and O-8, checked by the fault-injection and transition tests above.
