# Verify Maintenance Report

## Use Case Information

| Field | Description |
|---|---|
| Use Case Name | Verify Maintenance Report |
| Primary Actor | Student Welfare Officer |
| Supporting System | UB-DormHub |
| Goal | Verify that a reported maintenance problem is valid before it is assigned to Maintenance. |
| Priority | High |

---

## Preconditions

1. The Student Welfare Officer is authenticated.
2. The user has the Student Welfare role.
3. A maintenance ticket exists with status `Reported`.
4. The ticket contains the required report information.

---

## Trigger

A Student Welfare Officer selects a reported maintenance ticket for review.

---

## Main Success Scenario

1. The Student Welfare Officer opens the list of reported maintenance tickets.
2. The system displays available ticket details.
3. The officer selects a ticket.
4. The system displays the student's reported problem and room information.
5. The officer reviews the report.
6. The officer confirms that the report is valid.
7. The officer selects **Verify**.
8. The system checks that the officer is authorised.
9. The system changes the ticket status from `Reported` to `Verified`.
10. The system makes the verified ticket available for assignment to Maintenance.

---

## Alternative Flow A — Report Is Invalid

**A1.** The officer reviews the report and determines that it is invalid.

**A2.** The officer selects **Reject**.

**A3.** The system records the rejection.

**A4.** The system changes the ticket status from `Reported` to `Rejected`.

**A5.** The system prevents the rejected ticket from being assigned to Maintenance.

The use case ends.

---

## Alternative Flow B — Missing Information

**B1.** The officer identifies missing or insufficient information.

**B2.** The officer does not verify the ticket.

**B3.** The system keeps the ticket in the `Reported` state.

The ticket cannot proceed to assignment until the verification requirements
are satisfied.

---

## Exception Flow — Unauthorised User

**E1.** A user without the Student Welfare role attempts to verify the ticket.

**E2.** The system checks the user's role.

**E3.** The system rejects the operation.

**E4.** The ticket remains in its existing state.

---

## Postconditions — Success

1. The ticket status is `Verified`.
2. The ticket is available for assignment to Maintenance.
3. The ticket cannot be treated as unverified.

---

## Postconditions — Rejection

1. The ticket status is `Rejected`.
2. The ticket cannot be assigned to Maintenance.

---

## Business Rules

- Only authorised Student Welfare Officers may verify or reject reports.
- Only tickets in the `Reported` state may be verified or rejected.
- A verified ticket may proceed to assignment.
- A rejected ticket may not proceed to assignment.
