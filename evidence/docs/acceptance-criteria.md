# UB-DormHub Acceptance Criteria

## AC-01 - Valid maintenance fault submission
**Given** an authenticated Resident Student with an assigned sample room,  
**When** the student submits complete fault information for that room,  
**Then** the system shall create a MaintenanceComplaint linked to the student and room with status `Reported` and generate a ComplaintForm.

Traceability: FR-01, FR-02, FR-03; BR-01, BR-10, BR-11.

## AC-02 - Invalid room association
**Given** an authenticated Resident Student,  
**When** the student attempts to report a fault for a room not associated with them,  
**Then** the system shall reject the submission and shall not create a MaintenanceComplaint.

Traceability: FR-01; BR-10.

## AC-03 - Successful verification
**Given** a MaintenanceComplaint in `Reported` and an authorised StudentWelfareOfficer,  
**When** the officer verifies the complaint,  
**Then** the system shall create a CertificationRecord identifying the officer and change the complaint to `Verified`.

Traceability: FR-04, FR-05, FR-12; BR-02, BR-03, BR-04.

## AC-04 - Rejected complaint
**Given** a MaintenanceComplaint in `Reported` and an authorised StudentWelfareOfficer,  
**When** the officer determines that the complaint is invalid or ineligible and rejects it,  
**Then** the system shall change the complaint to `Rejected` and it shall not become available for assignment.

Traceability: FR-04, FR-12; BR-02, BR-03, BR-14.

## AC-05 - Invalid verification state
**Given** a MaintenanceComplaint that is not `Reported`,  
**When** a StudentWelfareOfficer attempts to verify it again,  
**Then** the system shall reject the action and keep the current status unchanged.

Traceability: FR-04; BR-03, BR-09.

## AC-06 - Valid technician assignment
**Given** a complaint in `Verified` and an authorised MaintenanceManager,  
**When** the manager assigns an available MaintenanceStaff member,  
**Then** the system shall create a WorkOrder and change the complaint to `Assigned`.

Traceability: FR-06; BR-05, BR-06.

## AC-07 - Unverified complaint cannot be assigned
**Given** a complaint in `Reported`,  
**When** a MaintenanceManager attempts to assign it,  
**Then** the system shall reject the assignment and retain `Reported`.

Traceability: FR-06; BR-05, BR-09.

## AC-08 - Repair progress and resolution
**Given** an assigned WorkOrder for an authorised MaintenanceStaff member,  
**When** the staff member starts work and later records completion,  
**Then** the complaint shall progress `Assigned -> In Progress -> Resolved` and the WorkOrder shall record the repair progress.

Traceability: FR-07, FR-08; BR-06, BR-07.

## AC-09 - Complaint status tracking
**Given** an authenticated authorised user,  
**When** the user opens an accessible complaint,  
**Then** the system shall display the complaint's current persisted status accurately.

Traceability: FR-09, FR-11, FR-12.

## AC-10 - Closure rule
**Given** a complaint in `Resolved` and an authorised MaintenanceManager,  
**When** the manager closes the complaint,  
**Then** the status shall change to `Closed`.

Traceability: FR-14; BR-08.

## AC-11 - Unresolved complaint cannot close
**Given** a complaint in `In Progress`,  
**When** a user attempts to close it,  
**Then** the system shall reject the action and retain `In Progress`.

Traceability: FR-14; BR-08, BR-09.

## AC-12 - Protected function authorisation
**Given** an authenticated user without the required role,  
**When** the user attempts a protected verification, assignment or repair-update operation,  
**Then** the system shall deny the operation and shall not change complaint or work-order state.

Traceability: FR-11, FR-12; BR-13.

