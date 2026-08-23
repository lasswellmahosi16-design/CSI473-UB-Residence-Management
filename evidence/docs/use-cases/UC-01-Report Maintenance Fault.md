 Report Maintenance Fault

## Use Case Information

| Field | Description |
|---|---|
| Use Case Name | Report Maintenance Fault |
| Primary Actor | Student |
| Supporting System | UB-DormHub |
| Goal | Allow a student to report a maintenance problem affecting their assigned room. |
| Priority | High |

---

## Preconditions

1. The student has a valid UB-DormHub account.
2. The student is authenticated.
3. The student is associated with a valid residence room.
4. The UB-DormHub reporting service is available.

---

## Trigger

The student identifies a maintenance problem in their assigned room and
chooses to report the problem.

---

## Main Success Scenario

1. The student selects **Report Maintenance Fault**.
2. The system identifies the student's assigned room.
3. The student enters a description of the maintenance problem.
4. The student submits the maintenance report.
5. The system validates the submitted information.
6. The system checks for an existing active report for the same problem and room.
7. The system creates a unique maintenance ticket.
8. The system sets the ticket status to `Reported`.
9. The system records the ticket details.
10. The system makes the ticket available to an authorised Student Welfare Officer for verification.
11. The system displays the ticket reference and current status to the student.

---

## Alternative Flow A — Missing Required Information

**A1.** The student submits the report without required information.

**A2.** The system identifies the missing information.

**A3.** The system rejects the submission.

**A4.** The system displays a message explaining what information must be provided.

**A5.** The student corrects the information and resubmits the report.

The use case continues from Step 5 of the main success scenario.

---

## Alternative Flow B — Invalid Room

**B1.** The system cannot identify a valid room associated with the student.

**B2.** The system prevents the creation of the maintenance ticket.

**B3.** The system informs the student that their room information could not
be verified.

The use case ends without creating a ticket.

---

## Alternative Flow C — Duplicate Active Report

**C1.** The system detects an existing active maintenance ticket for the same
room and reported problem.

**C2.** The system prevents the creation of a duplicate active ticket.

**C3.** The system displays the existing ticket information to the student.

The use case ends.

---

## Exception Flow — Ticket Creation Failure

**E1.** The student submits a valid maintenance report.

**E2.** A system or database failure occurs while creating the ticket.

**E3.** The system does not report the ticket as successfully created.

**E4.** The system displays an error message to the student.

**E5.** The student can retry the operation when the service becomes available.

The system must not create duplicate tickets when the operation is retried.

---

## Postconditions — Success

1. A unique maintenance ticket exists.
2. The ticket is associated with the student's room.
3. The ticket has status `Reported`.
4. The ticket is available for Student Welfare verification.
5. The student can view the ticket reference.

---

## Postconditions — Failure

1. An invalid report does not create a maintenance ticket.
2. The student receives an appropriate error message.
3. The system does not create duplicate tickets because of a failed retry.

---

## Business Rules

- A valid room must be associated with the student.
- Required report information must be provided.
- Duplicate active reports should not be created.
- A newly created maintenance ticket must begin in the `Reported` state.
