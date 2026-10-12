# UB-DormHub - Pattern and technique decisions

**Scope:** UC-04 Verify Maintenance Report.  
**Traceability:** FR-04, FR-05, FR-11, FR-12; BR-02, BR-03, BR-04, BR-09, BR-13, BR-14; AC-03, AC-04, AC-12; QS-03 Authorisation; QS-04 Reliability.


## 1. Design problems

| ID | Project-specific design problem | Why it matters in UB-DormHub |
|---|---|---|
| DP-01 | Verification logic could become coupled directly to SQL/JPA | UC-04 must be testable under persistence failure, stale state and retry conditions without requiring a live database for every test. |
| DP-02 | Framework classes could leak into the core workflow | The architecture requires the core lifecycle rules to remain stable even if the web or persistence mechanism changes. |
| DP-03 | Verification changes more than one persistent record | VERIFY must commit the complaint transition and CertificationRecord together; failure must not leave a false VERIFIED state (QS-04). |
| DP-04 | Repeated requests can duplicate a decision | Lab 8 requires idempotent replay using the same idempotency key. |
| DP-05 | It is tempting to add events/observers for future notifications | Notifications are not part of the current assessed slice, so an event bus would add hidden dependencies without solving a current requirement. |

## 2. Selected patterns / techniques

| Decision | Problem solved | Participants in this project | Why it fits | Cost / consequence |
|---|---|---|---|---|
| **Repository pattern** | DP-01 | `ComplaintRepository`, `CertificationRepository`, `IdempotencyRepository`; persistence adapters implement them | Application logic works with stable interfaces while database/JPA details stay outside the core workflow. Fake repositories can inject failures in tests. | Extra interfaces and mapping code. Repository methods must remain use-case focused rather than becoming generic data-access dumping grounds. |
| **Dependency inversion + constructor injection** | DP-02 | `VerificationService` depends on ports; framework adapters depend inward on those interfaces | Dependency direction matches ADR-001: core logic does not import controllers, JPA repositories or framework transaction classes. Constructor injection makes dependencies explicit and testable. | More classes than direct controller-to-database code; wiring must be maintained. |
| **Application Service + Unit-of-Work transaction boundary** | DP-03 and DP-04 | `VerificationService`, `TransactionRunner`, repositories, `MaintenanceComplaint`, `CertificationRecord` | One service coordinates authentication/authorisation, idempotency, current-state loading, domain transition and persistence inside one transaction. Domain objects still own lifecycle validity. | The application service must avoid absorbing domain rules; transaction duration must remain short. |

## 3. Dependency direction

The required direction is:

`Web Controller -> VerifyMaintenanceReportUseCase <- VerificationService -> Ports <- Persistence/Security adapters`

The domain (`MaintenanceComplaint`, `CertificationRecord`) has no dependency on Spring, HTTP, JPA or a database product.

## 4. Explicit rejection decision

### Rejected: Observer / event-bus pattern for verification

**Why considered:** an event such as `ComplaintVerified` could later trigger notifications or integrations.

**Why rejected now:** the approved slice does not require SMS, Teams, offline synchronisation or another secondary effect. Introducing an event bus now would create extra event types, handlers, ordering questions and transaction-delivery failure modes. It would also make the dependencies of UC-04 less visible. This is accidental complexity for the current project.

**Reconsider only if:** a real Phase 2/3 requirement introduces a secondary action that must occur independently of the verification transaction. At that point an outbox/event adapter can be added behind a port without changing the domain transition API.

## 5. Pattern-to-code traceability

| Diagram/code participant | Decision represented |
|---|---|
| `VerifyMaintenanceReportUseCase` | Stable inbound interface for the core operation |
| `VerificationService` | Application Service / use-case coordinator |
| `ComplaintRepository` | Repository pattern and persistence boundary |
| `CertificationRepository` | Repository pattern and persistence boundary |
| `IdempotencyRepository` | Safe retry boundary from Lab 8 |
| `TransactionRunner` | Explicit Unit-of-Work boundary |
| `AccessControlPort` | Dependency inversion for authenticated role checks |
| `MaintenanceComplaint` | Domain lifecycle owner; remains framework-independent |

## 6. Exit record

**Greatest benefit:** the repository/transaction interface boundary. It lets the team test UC-04 against fake success, persistence failure and stale-state behaviour while keeping database details outside the core service.  
**Cost:** more interfaces, adapters and wiring than direct database access.  
**Test:** replace the repository/transaction ports with fakes; inject a failure after the complaint transition is attempted and confirm the externally visible result is a controlled persistence error and no committed partial state exists.
