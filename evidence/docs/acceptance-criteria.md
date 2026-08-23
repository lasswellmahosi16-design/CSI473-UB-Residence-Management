01 — Successful Fault Report

Given a registered student is associated with a valid room
When the student submits a maintenance report containing all required information
Then the system shall create a unique ticket with status Reported and display the ticket reference to the student.

02 — Missing Information

Given a student is creating a maintenance report
When the student submits the report without a required fault description
Then the system shall reject the submission and identify the missing information without creating a ticket.

03 — Verification Rule

Given a maintenance ticket has status Reported
When an authorised Student Welfare user verifies the ticket
Then the system shall change the ticket status to Verified.

04 — Invalid Status Transition

Given a maintenance ticket has status Reported
When a maintenance user attempts to change the ticket directly to In Progress
Then the system shall reject the transition because the ticket has not yet been verified and assigned.

