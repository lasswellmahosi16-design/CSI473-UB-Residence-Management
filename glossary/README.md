# Domain Glossary: UB-DormHub

* **UB-DormHub:** The proposed residence maintenance management system for reporting, verifying, assigning, tracking, resolving, and closing student residence maintenance faults.

* **Resident Student:** A registered UB student assigned to a residence room who reports maintenance faults and views the status of their maintenance tickets.

* **Resident Assistant (RA):** A student representative responsible for assisting residents with residence matters and, under the current complaint process, recording or facilitating the submission of room maintenance complaints.

* **Student Welfare:** The University administrative body responsible for reviewing and verifying maintenance requests before they proceed to Maintenance.

* **Student Welfare Officer:** An authorised representative of Student Welfare who reviews, verifies, or rejects reported maintenance tickets within UB-DormHub.

* **Maintenance Administrator:** An authorised user responsible for assigning verified maintenance tickets to appropriate maintenance staff.

* **Maintenance Staff:** Field technicians responsible for carrying out physical repairs, updating maintenance progress, and marking completed work as resolved.

* **Residence:** A University of Botswana student accommodation facility containing one or more rooms covered by the UB-DormHub system.

* **Room:** A specific student accommodation unit within a residence. Maintenance tickets are associated with the room where the fault occurs.

* **Fault Report:** A report submitted by a resident student describing a maintenance problem affecting their assigned room.

* **Maintenance Ticket:** A stateful system entity created from a valid fault report to manage the maintenance problem throughout its lifecycle. A ticket transitions through:  
  `Reported` → `Verified` → `Assigned` → `In Progress` → `Resolved` → `Closed`.

  A reported ticket may also transition to `Rejected` when the report is not accepted for maintenance action.

* **Room Condition Report:** A digital checklist completed during student move-in/check-in to record the condition of a room and identify pre-existing defects.

* **Ticket Status:** The current state of a maintenance ticket within its defined lifecycle.

* **Reported:** The initial status of a newly created maintenance ticket awaiting Student Welfare verification.

* **Verified:** The status of a maintenance ticket after an authorised Student Welfare Officer confirms that the reported fault is valid.

* **Rejected:** The status of a reported maintenance ticket that has been determined not to be valid or eligible for maintenance action.

* **Assigned:** The status of a verified maintenance ticket after it has been allocated to a maintenance staff member.

* **In Progress:** The status indicating that the assigned maintenance staff member has started working on the reported fault.

* **Resolved:** The status indicating that the reported maintenance problem has been repaired by Maintenance Staff.

* **Closed:** The final status of a maintenance ticket after the maintenance workflow has been completed.

* **Verification:** The process through which an authorised Student Welfare Officer reviews a reported fault and determines whether it should proceed to Maintenance.

* **Assignment:** The process of allocating a verified maintenance ticket to an appropriate Maintenance Staff member.

* **Resolution:** The process of completing the physical repair associated with a maintenance ticket and marking the fault as resolved.

* **Closure:** The final completion of a maintenance ticket after the reported fault has been resolved.
