# UB-DormHub Traceability Matrix

## Requirement → Use Case → Analysis Element → Verification

| Requirement ID | Functional Requirement | Use Case | Analysis Element | Verification Evidence |
|---|---|---|---|---|
| FR-01 | The system shall allow a Resident Student to report a maintenance fault. | UC-01 Report Maintenance Fault | ResidentStudent, Room, MaintenanceComplaint | Normal test: a valid maintenance fault is submitted and recorded. |
| FR-02 | The system shall record the student associated with a maintenance complaint. | UC-02 Record Student Complaint | ResidentStudent, MaintenanceComplaint | Validation test: the complaint is associated with the correct student. |
| FR-03 | The system shall generate a complaint form for a maintenance complaint. | UC-03 Generate Complaint Form | ComplaintForm, MaintenanceComplaint | Normal test: a valid complaint generates the required complaint form. |
| FR-04 | The system shall allow an authorised Student Welfare Officer to verify a maintenance report. | UC-04 Verify Maintenance Report | StudentWelfareOfficer, MaintenanceComplaint, CertificationRecord | Authorisation and rule test: only an authorised officer can verify a valid complaint. |
| FR-05 | The system shall allow an authorised Student Welfare Officer to certify a valid complaint. | UC-05 Certify Complaint | StudentWelfareOfficer, CertificationRecord | Normal/rule test: a valid complaint receives a certification record. |
| FR-06 | The system shall allow an authorised Maintenance Manager to assign a verified complaint to Maintenance Staff. | UC-06 Assign Technician | MaintenanceManager, MaintenanceStaff, WorkOrder, MaintenanceComplaint | Rule test: only an eligible complaint can be assigned. |
| FR-07 | The system shall allow Maintenance Staff to view their assigned work orders. | UC-07 View Assigned Work Orders | MaintenanceStaff, WorkOrder | Normal test: staff can view their assigned work orders. |
| FR-08 | The system shall allow Maintenance Staff to record repair progress and mark completed work as resolved. | UC-08 Record Repair and Mark Resolved | MaintenanceStaff, WorkOrder, MaintenanceComplaint | Normal and rule test: authorised staff can update and resolve assigned work. |
| FR-09 | The system shall allow authorised users to track the current status of a maintenance complaint. | UC-09 Track Complaint Status | MaintenanceComplaint, WorkOrder | Normal test: the current complaint status is displayed correctly. |
| FR-10 | The system shall allow authorised users to monitor ongoing maintenance repairs. | UC-10 Monitor Repairs | MaintenanceManager, MaintenanceStaff, WorkOrder | Normal test: authorised users can view active repair work. |
| FR-11 | The system shall authenticate users before granting access to protected functions. | UC-11 Authenticate User | ResidentStudent, ResidentAssistant, StudentWelfareOfficer, MaintenanceStaff, MaintenanceManager, UniversityManagement | Security test: invalid credentials are rejected and protected functions remain inaccessible. |
| FR-12 | The system shall enforce role-based access according to the user's authorised role. | UC-12 Manage User Access | UniversityManagement, ResidentStudent, ResidentAssistant, StudentWelfareOfficer, MaintenanceStaff, MaintenanceManager | Authorisation test: users cannot access functions outside their role. |
| FR-13 | The system shall allow authorised management users to monitor system performance. | UC-13 Monitor System Performance | UniversityManagement, PerformanceReport | Normal test: an authorised management user can view system performance information. |

---

## Analysis Element Traceability

| Analysis Element | Responsibility / Meaning | Related Requirements |
|---|---|---|
| ResidentStudent | Represents the resident who reports and tracks maintenance complaints. | FR-01, FR-02, FR-09, FR-11, FR-12 |
| Room | Represents the student's assigned physical residence room. | FR-01, FR-02 |
| ResidenceBlock | Represents the residence block containing rooms. | FR-01, FR-02 |
| ResidentAssistant | Represents the residence student representative involved in the complaint process. | FR-11, FR-12 |
| MaintenanceComplaint | Represents a reported maintenance problem and stores its status and details. | FR-01, FR-02, FR-04, FR-05, FR-09 |
| ComplaintForm | Represents the complaint documentation generated for a maintenance complaint. | FR-03 |
| CertificationRecord | Records the verification/certification of a maintenance complaint by Student Welfare. | FR-04, FR-05 |
| WorkOrder | Represents the maintenance work generated and assigned for a complaint. | FR-06, FR-07, FR-08, FR-09, FR-10 |
| MaintenanceStaff | Represents technicians responsible for performing maintenance work. | FR-06, FR-07, FR-08, FR-12 |
| MaintenanceManager | Represents the authorised role responsible for assigning and monitoring maintenance work. | FR-06, FR-10, FR-12 |
| StudentWelfareOfficer | Represents the authorised role responsible for verifying and certifying complaints. | FR-04, FR-05, FR-12 |
| UniversityManagement | Represents authorised management users responsible for system oversight and access management. | FR-12, FR-13 |
| PerformanceReport | Represents information generated to monitor system performance. | FR-13 |

---

## Business Rule and State Traceability

`MaintenanceComplaint` is the main state-sensitive domain entity in the
maintenance workflow.

The core lifecycle is:

`Reported → Verified → Assigned → In Progress → Resolved → Closed`

A complaint may also be rejected following verification:

`Reported → Rejected`

The following rules must be enforced by the system.

| Rule | Related Requirement | Verification |
|---|---|---|
| A newly created maintenance complaint must begin in `Reported` status. | FR-01, FR-02 | State initialisation test. |
| Only an authorised Student Welfare Officer may verify or reject a complaint. | FR-04, FR-05, FR-12 | Role and authorisation test. |
| A complaint must be verified before it can be assigned. | FR-04, FR-06 | Invalid transition test: `Reported → Assigned` is rejected. |
| A work order must be assigned before maintenance work begins. | FR-06, FR-08 | Invalid transition test: unassigned work cannot become `In Progress`. |
| Only authorised Maintenance Staff may record repair progress or mark work as resolved. | FR-08, FR-12 | Authorisation test. |
| A maintenance task must be resolved before the complaint can be closed. | FR-08, FR-09 | Invalid transition test: `In Progress → Closed` is rejected. |
| A rejected complaint must not proceed to technician assignment. | FR-05, FR-06 | Rule test: `Rejected → Assigned` is rejected. |

---

## Use Case Coverage

| Use Case | Primary Actor | Main Domain Elements | Requirements |
|---|---|---|---|
| UC-01 Report Maintenance Fault | Resident Student | ResidentStudent, Room, MaintenanceComplaint | FR-01 |
| UC-02 Record Student Complaint | Resident Student | ResidentStudent, MaintenanceComplaint | FR-02 |
| UC-03 Generate Complaint Form | Resident Student / System | ComplaintForm, MaintenanceComplaint | FR-03 |
| UC-04 Verify Maintenance Report | Student Welfare Officer | StudentWelfareOfficer, MaintenanceComplaint, CertificationRecord | FR-04 |
| UC-05 Certify Complaint | Student Welfare Officer | StudentWelfareOfficer, CertificationRecord | FR-05 |
| UC-06 Assign Technician | Maintenance Manager | MaintenanceManager, WorkOrder, MaintenanceStaff | FR-06 |
| UC-07 View Assigned Work Orders | Maintenance Staff | MaintenanceStaff, WorkOrder | FR-07 |
| UC-08 Record Repair and Mark Resolved | Maintenance Staff | MaintenanceStaff, WorkOrder, MaintenanceComplaint | FR-08 |
| UC-09 Track Complaint Status | Resident Student / Authorised User | MaintenanceComplaint, WorkOrder | FR-09 |
| UC-10 Monitor Repairs | Maintenance Manager | MaintenanceManager, MaintenanceStaff, WorkOrder | FR-10 |
| UC-11 Authenticate User | All system users | User roles | FR-11 |
| UC-12 Manage User Access | University Management | UniversityManagement, user roles | FR-12 |
| UC-13 Monitor System Performance | University Management | UniversityManagement, PerformanceReport | FR-13 |

---

## Verification Coverage

The traceability matrix is verified through three categories of testing.

### 1. Normal Tests

These confirm that valid system behaviour works correctly.

Example:

`Report → Verify → Assign → In Progress → Resolved → Closed`

### 2. Rule / Validation Tests

These confirm that business rules and invalid actions are prevented.

Examples:

- An unverified complaint cannot be assigned.
- An unauthorised user cannot verify a complaint.
- A rejected complaint cannot be assigned.
- Unassigned work cannot be marked `In Progress`.
- An unresolved complaint cannot be closed.

### 3. Failure Tests

These confirm that the system handles important failures safely.

Example:

- If complaint creation fails, the system shall not display a false
  successful submission or create a partial/duplicate complaint.

---

