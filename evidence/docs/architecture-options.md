# UB-DormHub - Architecture options and comparison (Lab 7)

Three realistic structures for **this** project were compared using the same criteria. Names follow the Phase 1 report and decision records D-001 to D-003.

## 1. The alternatives

### A. Layered modular application (one deployable) - SELECTED
One web application with four layers: Presentation, Application Services, Domain, Repository/Persistence. Application Services are split by use-case area: Authentication & Access, Complaint, Verification, Work Order and Reporting. The Domain holds `MaintenanceComplaint`, `WorkOrder`, `CertificationRecord`, `Room` and the lifecycle rules. Repository interfaces sit between services/domain and a single data store. One database transaction covers each use case.

### B. Feature-sliced application (one deployable, vertical slices)
One web application split into slices: *Report Fault*, *Verify*, *Assign and Repair*, *Track Status*. Each slice has its own screens, logic and SQL. Shared code is kept small. Lifecycle rules would either be copied into each slice or placed in a shared kernel.

### C. Microservices
Separate deployable services: Complaint, Verification, Work Order, Reporting and Identity, behind an API gateway, each with its own database. Verification plus certification across services needs a saga or events instead of one transaction.

## 2. Criteria (same for all alternatives)

| # | Criterion | Why it matters here | Weight |
|---|---|---|---|
| 1 | Transaction safety for multi-record operations | QS-04: zero partial records | 5 |
| 2 | Central enforcement of lifecycle and role rules | QS-03, BR-09, BR-13 | 5 |
| 3 | Localised change (adapters, labels) | QS-08 | 3 |
| 4 | Buildable and demonstrable by a team of five in one semester | Approved scope and constraints | 4 |
| 5 | Operational cost (deploy, monitor, host) | No evidence of need for independent scaling | 3 |
| 6 | Testability of failure paths | Failure tests are part of the verification plan | 3 |

Scores run from 1 (poor) to 5 (strong). They are the team's judgement, so the reasons are written down.

| Criterion (weight) | A Layered modular | B Feature-sliced | C Microservices |
|---|---|---|---|
| 1 Transaction safety (5) | **5** - one local transaction per use case | **4** - local DB, but each slice manages its own transaction | **2** - cross-service saga; a Verified complaint with no CertificationRecord becomes possible during failure |
| 2 Central rules (5) | **5** - one Domain component owns transitions | **2** - rules copied per slice unless a shared kernel is added | **3** - Complaint service owns the lifecycle, but other services must call it correctly over the network |
| 3 Localised change (3) | **4** - add an adapter behind a port | **4** - changes stay inside a slice | **4** - add or replace a service |
| 4 Buildable in a semester (4) | **5** - one codebase, one process | **4** - one process, but more duplication to review | **1** - several services, gateway and inter-service contracts |
| 5 Operational cost (3) | **5** - one app, one database | **5** - one app, one database | **1** - multiple deployments, monitoring and network failure modes |
| 6 Failure-path testability (3) | **4** - fake repository can inject failures | **3** - failures must be injected per slice | **2** - needs distributed failure simulation |
| **Weighted total (max 115)** | **109** | **82** | **50** |

Arithmetic for A: 5x5 + 5x5 + 3x4 + 4x5 + 3x5 + 3x4 = 25 + 25 + 12 + 20 + 15 + 12 = 109.
B: 20 + 10 + 12 + 16 + 15 + 9 = 82. C: 10 + 15 + 12 + 4 + 3 + 6 = 50.

## 3. Outcome

**A is selected** (recorded in `decisions/ADR-001-architecture.md`, which refines the preliminary D-003).

- B is the closest alternative. It loses mainly because lifecycle rules would be duplicated (criterion 2). It becomes attractive again only if the team later splits into sub-teams that own separate features.
- C is not rejected as a bad pattern. It is rejected because nothing in the approved scope needs independent deployment or scaling, while it adds the exact failure mode QS-04 forbids.

## 4. What would change the answer

- Even if criteria 4 and 5 were weighted near zero and criterion 3 very heavily, A would still lead: C scores only 2 and 3 on the two highest-weighted criteria (1 and 2).
- Real evidence of independent scaling, ownership or release needs (see the reconsideration triggers in ADR-001).
