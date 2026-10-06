# ADR-001 - Layered modular application with a transactional use-case boundary

* **Status:** Accepted for the Lab 7 architecture baseline
* **Decision date:** 2026-09-25
* **Reviewed:** 2026-10-04 for Lab 8 consistency
* **Deciders:** Team 5 - Lasswell Mahosi, Jayson Maleya, Siphosethu Tsela, Thobo Modise, Tony Moroke
* **Refines:** D-003 (preliminary layered boundary). Builds on D-001 (verification inside the system) and D-002 (domain responsibility).
* **Traceability:** QS-04, QS-03, QS-08 (drivers); QS-02, QS-07 (supporting); FR-04..FR-08, FR-11, FR-12, FR-14, FR-15; BR-02, BR-03, BR-04, BR-09, BR-13, BR-15.
* **Evidence:** `docs/architecture-options.md`, `docs/quality-to-architecture.md`, `models/component-architecture.puml`

## 1. Context

UB-DormHub moves a maintenance complaint through `Reported -> Verified -> Assigned -> In Progress -> Resolved -> Closed`, with `Rejected` and a controlled rework path. Several use cases update related records atomically: report fault creates a `MaintenanceComplaint` and `ComplaintForm`; successful verification changes the complaint to `Verified` and creates a `CertificationRecord`; assignment changes the complaint to `Assigned` and creates/updates its `WorkOrder`. Five system roles have different protected actions. SMS, Teams and offline synchronisation are deferred. The prototype uses synthetic or anonymised data for one residence or a controlled set of sample rooms.

The quality scenario that most influenced this decision is **QS-04 Reliability**: a persistence failure must never leave partial state or a false success.

## 2. Decision

Build UB-DormHub as **one deployable layered modular application** with the following components.

| Component | Responsibility | Depends on | Persistence responsibility |
|---|---|---|---|
| Responsive Web Client / Web UI | Role-specific screens and basic input feedback. No business rules. | Application Services over HTTPS | None |
| Authentication & Access Service | Authentication, token validation and role-to-permission decisions. | Repository Interfaces for user/role data | Read only in the current slice |
| Complaint Service | Report fault, validate room association, create complaint + form, retrieve status. | Access, Domain, Repository Interfaces | Coordinates `MaintenanceComplaint` creation and `ComplaintForm` persistence |
| Verification Service | Verify or reject a `Reported` complaint (UC-04). | Access, Domain, Repository Interfaces | VERIFY: status + `CertificationRecord`; REJECT: status + rejection reason |
| Work Order Service | Assign, progress, resolve, rework/reassign and close. | Access, Domain, Repository Interfaces | Coordinates `WorkOrder` and complaint lifecycle updates |
| Reporting Service | Read-only operational and performance summaries. | Access, Repository Interfaces | None |
| Domain Model & Business Rules | Entities and lifecycle rules; rejects invalid transitions. | No outward infrastructure dependency | Owns lifecycle rules, not SQL |
| Repository Interfaces / Unit of Work | Find/save/query contracts and transaction boundary. | Domain types | Contract only |
| Persistence Adapter | Implements repository contracts. Only component containing SQL. | Repository Interfaces, Data Store | Performs all database I/O |
| Data Store | Authoritative persistent state. | None | Stores data |

**Architecture rules:**

1. **R1 - One transaction per state-changing use case.** Related writes commit together or not at all.
2. **R2 - Authorise on the server before protected domain changes.** UI visibility is not an access-control mechanism.
3. **R3 - Single lifecycle write path.** Services ask `MaintenanceComplaint` to perform a permitted transition; they do not assign arbitrary status values.
4. **R4 - Dependencies point inward.** UI -> application services -> domain/repository ports; the Persistence Adapter implements inner interfaces. UI code never uses SQL or repositories directly.
5. **R5 - Deferred integrations attach through ports/adapters.** They do not become dependencies of the core domain.
6. **R6 - Reporting is read-only.** Reporting cannot change complaint or work-order state.
7. **R7 - Rework reuses the complaint's existing WorkOrder.** The WorkOrder is returned/reassigned for further repair, matching the Phase 1 glossary; a second WorkOrder is not created for the same complaint.

**Boundaries:**
- **Security boundary:** browser -> application over HTTPS; token and role are re-checked server-side.
- **Data boundary:** only the Persistence Adapter reaches the Data Store.
- **Failure boundary:** data-store failures roll back the current Unit of Work and return a controlled error.

## 3. Alternatives considered

Full comparison using the same six criteria is in `docs/architecture-options.md`.

| Alternative | Score (max 115) | Outcome |
|---|---:|---|
| **A. Layered modular application** | **109** | **Selected** |
| B. Feature-sliced application | 82 | Feasible, but lifecycle/role rules risk duplication across slices |
| C. Microservices | 50 | Rejected for this scope because distributed transactions/failures and operating cost add risk without an independent-scaling requirement |

## 4. Consequences

### Positive
- Local transactions strongly protect QS-04 from partial state.
- Lifecycle rules and access checks have clear homes (QS-03, BR-09, BR-13).
- Repository interfaces make failure paths testable.
- One codebase and one deployment fit the semester scope.
- Deferred adapters can be introduced without making the domain depend on integration technology (QS-08).

### Negative (accepted)
- **Single deployable / shared availability:** if the application or database is unavailable, all functions are affected.
- **No independent scaling:** reporting and state-changing operations share application/database resources; QS-01 must therefore be tested.
- **Boundary erosion risk:** developers could bypass R1-R6 unless code review and tests enforce them.
- **Coupling around `MaintenanceComplaint`:** Verification and Work Order services both coordinate changes to the same lifecycle owner.
- **More interfaces/mapping code** than direct controller-to-database code.

### Operational cost
One application process and one relational database to deploy, back up and monitor. No service discovery, gateway or cross-service tracing is required for the assessed slice.

## 5. Risks and mitigations

| Risk | Mitigation | Verification |
|---|---|---|
| **Highest:** use-case writes are not in the same Unit of Work, leaving partial state | R1, R3; database constraints and conditional updates | Fault-injection test for verification and assignment |
| Authorisation exists only in the UI | R2 | Role x operation test: 403 and no state change |
| Layer shortcuts appear over time | R4; review checklist | Review every state-changing path against R1-R7 |
| Rework accidentally creates a second active WorkOrder | R7; `UNIQUE(complaint_id)` in the Lab 8 logical model | Database integrity test and rework service test |

## 6. Evidence that would make us reconsider

| Trigger | Response |
|---|---|
| QS-01 is missed after indexing and one agreed tuning pass | Separate reporting read workload first; do not immediately split the whole system |
| Scope expands to independently owned residence teams requiring separate releases | Re-run the comparison, including feature-sliced/service-split options |
| A deferred integration makes core transactions slow or unreliable | Add an asynchronous adapter/outbox behind a port |
| More than two production components must change for an adapter, or UI code reaches repositories | Tighten/restructure module boundaries |
| Selected database cannot provide the required atomic transaction semantics | Re-open R1 and the persistence choice |

## 7. Deferred decisions
Programming language, web framework, database product, hosting platform and authentication provider remain implementation decisions for later evidence.
