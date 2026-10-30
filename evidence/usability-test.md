# UB-DormHub - Usability test record

## 1. Test set-up (10 minutes of preparation)

| Item | Plan |
|---|---|
| Participants | 3 people from outside your team. At least 2 should look like the primary user (a student who has not seen the design). Use 1 for the Welfare Officer task (a classmate playing the role is acceptable; say so in the record) |
| Material | Open `wireframes.html` on one phone (F1 to F3) and one laptop (F4 to F6). Print the task cards below |
| Method | Think-aloud. The tester reads a task card and tries it by pointing or tapping at the frames. One team member plays the "computer" and says what happens. One team member only takes notes. Nobody gives hints unless the tester is stuck for 30 seconds |
| Not tested | Visual style (grey wireframes), speed of a real system, screen readers |
| Consent | Tell testers you test the design, not them. Do not record names; use P1, P2, P3 |

**Task cards**

| Card | Read to the tester |
|---|---|
| T1 | "You live in Block 3, Room 101. The bathroom tap is leaking. Use this system to report it." |
| T2 | "You reported it two days ago. Find out whether anyone has started dealing with it." |
| T3 | "You work for Student Welfare. Check complaint UBD-1043 and reject it because it was reported for the wrong room." |

**Measures per task:** completed? (yes / yes with hint / no), time in seconds, number of wrong taps, hints given, quotes.

## 2. Pre-test design review (hypotheses, NOT observations)

| # | Risk expected | Where | Why we worry | Candidate change if observed |
|---|---|---|---|---|
| H1 | Tester does not understand status words such as "Verified" | F1, F3 | "Verified" is internal wording | Replace with plain wording, e.g. "Checked by Student Welfare", keep the one-line next step |
| H2 | Tester misses the category list or picks a wrong one | F2 | A dropdown hides the choices | Show 5 to 6 common categories as visible choices |
| H3 | Officer clicks Reject by mistake or cannot find how to give a reason | F5, F6 | Two decision buttons on one screen | Already separated and "Reject..." opens a reason step; check it is understood |
| H4 | Tester thinks the room is wrong or cannot change it | F2 | Room is shown but not editable | Add text "Not your room? Contact Student Welfare" beside the room |
| H5 | Tester is unsure the report was sent | F3 | Confirmation might be read as just another page | Make the "Done" message and reference number larger, add "Take a note of this number" |

## 3. Session record (FILL IN)

### 3a. Participants

| ID | Role played | Description (e.g. year of study, phone user) | Date |
|---|---|---|---|
| P1 | Student | FILL IN | |
| P2 | Student | FILL IN | |
| P3 | Welfare Officer (or student) | FILL IN | |

### 3b. Task results

| Task | Tester | Completed (yes / hint / no) | Time (s) | Wrong taps | Observation (what happened, what they said) | Difficulty level (low / medium / high) |
|---|---|---|---|---|---|---|
| T1 | P1 | FILL IN | | | FILL IN | |
| T1 | P2 | FILL IN | | | FILL IN | |
| T2 | P1 | FILL IN | | | FILL IN | |
| T2 | P2 | FILL IN | | | FILL IN | |
| T3 | P3 | FILL IN | | | FILL IN | |

**Result against the criteria in `user-profile-and-tasks.md`:** FILL IN (for example "T1: 2 of 2 completed within 3 minutes, 1 hint").

## 4. Design changes made because of what was observed (FILL IN)

| # | Problem observed (which tester, which task) | Cause | Change made to the design | File and commit | How it will be verified after implementation |
|---|---|---|---|---|---|
| 1 | FILL IN | | | | |
| 2 | | | | | |

Edit `prototype/wireframes.html`, re-export the PDF, and commit again with a message such as "Lab 10: revise F2 after usability test (P1 missed category)". Both commits together are your "visible revision after critique".

## 5. Exit record (FILL IN after the session)

| | |
|---|---|
| **Most important usability problem observed** | FILL IN |
| **Interface change made** | FILL IN |
| **How it will be verified after implementation** | FILL IN. Example form: "Repeat T1 with 5 new first-time users on the working prototype; at least 4 finish unaided in 3 minutes or less (QS-05)." |

## 6. Limits of this test
Three people on a paper-like prototype can reveal obvious problems but cannot prove the 4-of-5 target. That target is checked again on the working system.
