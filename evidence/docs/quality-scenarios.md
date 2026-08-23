01 — Response Time

Quality attribute: Performance
Stimulus: A student submits a valid maintenance report.
Context: Normal system operation.
Expected response: The system validates the report and returns the ticket reference.
Measure: The response should be displayed within 2 seconds for at least 95% of valid submissions under the project's expected test load.

02 — Availability / Failure Recovery

Quality attribute: Reliability
Stimulus: Ticket creation fails because of a temporary system/service failure.
Context: Student is submitting a maintenance report.
Expected response: The system informs the student that the ticket was not created and allows the operation to be retried.
Measure: The system should provide the failure message within 5 seconds and must not create a duplicate ticket when the student retries.

03 — Authorization

Quality attribute: Security
Stimulus: A student attempts to access the verification function.
Context: The student is authenticated but does not have Student Welfare privileges.
Expected response: The system denies the operation.
Measure: 100% of unauthorized verification attempts in security testing must be rejected.

04 — Usability

Quality attribute: Usability
Stimulus: A student needs to report a room fault.
Context: The student uses the reporting interface on a standard desktop or mobile-sized interface.
Expected response: The student can identify the reporting function, enter the required information and submit the report without assistance.
Measure: At least 90% of test users should complete the report successfully without assistance.

05 — Status Accuracy

Quality attribute: Reliability/Consistency
Stimulus: An authorized user changes a ticket status.
Context: A valid state transition occurs.
Expected response: The new status is displayed consistently wherever the ticket is viewed.
Measure: 100% of tested valid status transitions must display the correct new status.



