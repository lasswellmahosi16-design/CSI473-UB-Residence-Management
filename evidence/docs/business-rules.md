# UB-DormHub Business Rules and Invariants

## Purpose

These rules define constraints that must remain true in the UB-DormHub domain.
They support the reporting, verification, assignment, maintenance and closure
workflow approved for the initial vertical slice.

## -01 — Initial Ticket State

Every newly created maintenance ticket shall have the status `Reported`.

A ticket must not be created directly in `Verified`, `Assigned`, `In Progress`,
`Resolved` or `Closed`.

## -02 — Verification Authority

Only an authorised Student Welfare Officer may verify or reject a maintenance
ticket.

## -03 — Verification State

Only a ticket in the `Reported` state may transition to `Verified` or
`Rejected`.

## -04 — Assignment Rule

Only a ticket in the `Verified` state may transition to `Assigned`.

## -05 — Maintenance Progress Rule

Only an assigned ticket may transition to `In Progress`.

## -06 — Resolution Rule

Only a ticket in the `In Progress` state may transition to `Resolved`.

## -07 — Closure Rule

Only a resolved ticket may transition to `Closed`.

## -08 — Invalid Transition Protection

The system shall reject any ticket-status transition that is not explicitly
permitted by the defined lifecycle.

Examples include:

- `Reported -> In Progress`
- `Reported -> Closed`
- `Verified -> Closed`
- `Assigned -> Closed`
- `Resolved -> Assigned`

## -09 — Student Room Association

A student may report a maintenance problem only for a room associated with
that student in the approved project scope.

## -10 — Ticket Ownership

Each maintenance ticket must identify the student who reported it and the
room affected by the reported maintenance problem.

## -11 — Assignment Requirement

A ticket cannot be moved to `In Progress` unless a maintenance staff member
has been assigned to it.

## -12 — Duplicate Active Reports

The system should prevent multiple active tickets representing the same
reported problem for the same room unless an authorised user permits a
separate report.

## State Invariants

At all times:

1. A `Reported` ticket has not yet been verified.
2. A `Verified` ticket has passed Student Welfare verification.
3. An `Assigned` ticket has an assigned maintenance staff member.
4. An `In Progress` ticket has an assigned maintenance staff member.
5. A `Resolved` ticket represents work that has been completed.
6. A `Closed` ticket has completed the required workflow.
7. A `Rejected` ticket cannot be assigned to Maintenance.

## Scope Constraint

The initial implementation is limited to one residence or a controlled set of
sample rooms.



