# UB-DormHub CRC( **Class, Responsibilities and Collaborators**) Cards

---

##  MaintenanceTicket

### Responsibilities

- Store the maintenance problem description.
- Maintain the ticket's current status.
- Associate the ticket with the reporting student.
- Associate the ticket with the affected room.
- Record assignment to maintenance staff.
- Support valid status transitions.
- Reject invalid status transitions.

### Collaborators

- Student
- Room
- StudentWelfareOfficer
- MaintenanceAdministrator
- MaintenanceStaff
- TicketStatus

---

## Student

### Responsibilities

- Identify the reporting resident.
- Report a maintenance problem for an associated room.
- View maintenance tickets that belong to the student's authorised scope.

### Collaborators

- Room
- MaintenanceTicket

---

##  Room

### Responsibilities

- Identify the residence room affected by a maintenance problem.
- Maintain its relationship to the residence.
- Identify the student assigned to the room.
- Provide the room context for maintenance tickets.

### Collaborators

- Student
- MaintenanceTicket

---

##  StudentWelfareOfficer

### Responsibilities

- Review reported maintenance tickets.
- Verify valid reports.
- Reject invalid reports.
- Ensure that only tickets in the `Reported` state are verified or rejected.

### Collaborators

- MaintenanceTicket
- Student
- Room

---

##  MaintenanceAdministrator

### Responsibilities

- Review verified maintenance tickets requiring assignment.
- Assign verified tickets to appropriate maintenance staff.
- Ensure that only verified tickets are assigned.

### Collaborators

- MaintenanceTicket
- MaintenanceStaff

---

##  MaintenanceStaff

### Responsibilities

- Receive assigned maintenance tickets.
- Start maintenance work.
- Update a ticket to `In Progress`.
- Mark completed maintenance work as `Resolved`.

### Collaborators

- MaintenanceTicket
- Room

---



## Responsibility Allocation Rationale

Responsibilities are allocated to the domain concept that owns or has the
information required to perform the responsibility.

For example, `MaintenanceTicket` is responsible for its status and valid
status transitions because it contains the ticket state. Student Welfare is
responsible for verification because verification is a role-specific domain
responsibility rather than a property of the Student.

This avoids placing all behaviour into one controller or system class and keeps
the analysis model focused on meaningful domain concepts.
