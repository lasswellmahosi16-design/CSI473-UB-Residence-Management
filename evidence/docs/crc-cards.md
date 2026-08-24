# UB-DormHub CRC( **Class, Responsibilities and Collaborators**) Cards

---

# CRC Cards: UB-DormHub

## CRC-01 — MaintenanceComplaint

### Responsibilities

- Represent a reported maintenance problem.
- Store the details of the reported fault.
- Identify the student who reported the complaint.
- Identify the affected room.
- Maintain the current complaint status.
- Support valid complaint-status transitions.
- Prevent invalid status transitions.
- Provide the complaint information required for verification, assignment,
  repair tracking, and closure.

### Collaborators

- ResidentStudent
- Room
- ComplaintForm
- CertificationRecord
- WorkOrder
- StudentWelfareOfficer
- MaintenanceManager

---

## CRC-02 — WorkOrder

### Responsibilities

- Represent maintenance work created for an eligible complaint.
- Identify the Maintenance Staff member assigned to the work.
- Record maintenance work progress.
- Support the transition of maintenance work to resolved.
- Provide work information for repair monitoring and complaint-status
  tracking.

### Collaborators

- MaintenanceComplaint
- MaintenanceStaff
- MaintenanceManager
- Room

---

## CRC-03 — ResidentStudent

### Responsibilities

- Report maintenance faults affecting an assigned room.
- Provide the information required to create a maintenance complaint.
- View the status of their maintenance complaint.
- Receive information about the progress of their reported fault.

### Collaborators

- Room
- MaintenanceComplaint
- ComplaintForm

---

## CRC-04 — StudentWelfareOfficer

### Responsibilities

- Review reported maintenance complaints.
- Verify whether a reported complaint is valid.
- Reject complaints that do not satisfy the verification requirements.
- Certify valid maintenance complaints.
- Ensure that only eligible complaints proceed to Maintenance.

### Collaborators

- MaintenanceComplaint
- CertificationRecord
- ResidentStudent
- Room

---

## CRC-05 — MaintenanceManager

### Responsibilities

- Review verified maintenance complaints requiring action.
- Assign eligible complaints to appropriate Maintenance Staff.
- Monitor ongoing maintenance repairs.
- View the progress of assigned work.

### Collaborators

- MaintenanceComplaint
- WorkOrder
- MaintenanceStaff

---

## CRC-06 — MaintenanceStaff

### Responsibilities

- View work orders assigned to them.
- Perform the physical maintenance work.
- Record repair progress.
- Mark completed maintenance work as resolved.

### Collaborators

- WorkOrder
- MaintenanceComplaint
- Room

---

## CRC-07 — Room

### Responsibilities

- Identify the physical room affected by a maintenance complaint.
- Identify the student associated with the room.
- Provide the location information required for maintenance work.

### Collaborators

- ResidentStudent
- MaintenanceComplaint
- WorkOrder
- ResidenceBlock

---

## CRC-08 — ComplaintForm

### Responsibilities

- Represent the complaint documentation generated from a maintenance
  complaint.
- Contain the information required to identify and process the complaint.
- Provide complaint information to authorised users during the maintenance
  workflow.

### Collaborators

- MaintenanceComplaint
- ResidentStudent
- StudentWelfareOfficer

---

## CRC-09 — CertificationRecord

### Responsibilities

- Record the verification/certification of a maintenance complaint.
- Identify the Student Welfare Officer who performed the certification.
- Associate the certification with the relevant maintenance complaint.
- Provide evidence that the complaint passed the verification stage.

### Collaborators

- MaintenanceComplaint
- StudentWelfareOfficer

---

## Responsibility Allocation

Responsibilities are allocated to the concept that owns the information or
has the authority required to perform the responsibility.

For example, `MaintenanceComplaint` is responsible for maintaining its
complaint status because the complaint owns its lifecycle.

`StudentWelfareOfficer` is responsible for verification and certification
because these activities require the Student Welfare role.

`MaintenanceManager` is responsible for technician assignment, while
`MaintenanceStaff` is responsible for carrying out and recording repair work.

This allocation keeps responsibilities close to the domain concepts and
reduces unnecessary coupling between unrelated concepts.

For example, `MaintenanceTicket` is responsible for its status and valid
status transitions because it contains the ticket state. Student Welfare is
responsible for verification because verification is a role-specific domain
responsibility rather than a property of the Student.

This avoids placing all behaviour into one controller or system class and keeps
the analysis model focused on meaningful domain concepts.
