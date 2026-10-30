# UB-DormHub - User profile and task scenarios


## 1. Primary user profile - Resident Student

| Item | Description |
|---|---|
| Who | A UB student living in a residence room, often a first- or second-year student |
| Skills | Uses WhatsApp and a phone daily; may never have used a university web system |
| Device | Mostly a phone browser, sometimes a laptop. Mobile data can be slow or drop |
| Context | Reports a fault in a hurry (leaking tap, broken socket), often in the evening |
| Goal | Get the fault fixed and see who is dealing with it, without chasing paper forms |
| Pain points (from the proposal) | Paper forms get lost; repeated reporting because progress is invisible |
| Needs | Few steps, plain words, room already known, clear confirmation, status in one place |
| Accessibility | Some users have low vision or use keyboard/screen reader; screens must work at 200% zoom |

## 2. Secondary user profile - Student Welfare Officer (administrative task)

| Item | Description |
|---|---|
| Who | Student Welfare staff member who verifies complaints before Maintenance acts |
| Device | Office desktop or laptop, keyboard and mouse |
| Context | Works through a queue of Reported complaints during office hours |
| Goal | Decide quickly and correctly: verify or reject, with a recorded reason for rejections |
| Needs | Clear queue, all facts on one screen, hard-to-misclick decisions, no double-processing |

## 3. Task scenarios and success criteria

### Task T1 (primary) - Report a maintenance fault
**Scenario.** Neo is in Block 3, Room 101. The bathroom tap has been leaking since morning. He wants it on record so Maintenance can fix it.

| Success criterion | Measure |
|---|---|
| Neo submits a complete complaint for his own room | Complaint appears in "My complaints" with status Reported and a reference number |
| Done unaided and quickly | At least 4 of 5 first-time users finish in 3 minutes or less (QS-05); in the lab check, all 3 testers finish with at most 1 hint |
| Neo knows it worked | He can say "my complaint was recorded" and read out the reference number |
| Errors are recoverable | A missing field gives a message next to the field and nothing typed is lost |

**Shortest reasonable path** (after login): My complaints -> Report a fault -> choose category, write description -> Submit. Three screens, one choice (category), one typed field.

### Task T2 (primary) - Check complaint status
**Scenario.** Two days later Neo wants to know whether anyone has picked up his complaint.
**Success:** from the landing screen he finds the status and a plain-language line on what happens next, in one tap (FR-09, QS-02). **Shortest path:** My complaints -> open complaint.

### Task T3 (secondary / administrative) - Verify or reject a complaint
**Scenario.** Ms Kgosi has a queue of Reported complaints. She verifies a valid one and rejects one that was filed for the wrong room.

| Success criterion | Measure |
|---|---|
| Correct decision recorded | Status becomes Verified or Rejected; a CertificationRecord exists (AC-03, AC-04) |
| Rejection always has a reason | The form will not submit without a reason of 10 or more characters |
| No accidental decision | A wrong click on Reject can be cancelled before it is sent |
| Stale data handled | If someone else already decided, she sees why and the current status (409 INVALID_STATE) |

**Shortest reasonable path:** Verification queue -> open complaint -> Verify (or Reject + reason -> Confirm).

## 4. Design choices and consequences accepted

| Choice | Realistic alternative | Consequence accepted |
|---|---|---|
| Room is pre-filled from the student's record, shown as text | Student types the room number | Fewer errors and fewer fields, but a student with a wrong room record must ask Student Welfare to fix it (AC-02 blocks other rooms) |
| Status shown as words + icon + a one-line "what happens next" | Colour badge only | More text per row, but readable for colour-blind and screen-reader users (QS-06) |
| Verify and Reject are separate buttons; Reject opens a reason field and needs confirmation | One "Decision" dropdown | Rejection takes one extra step, which prevents accidental rejections |
| Mobile-first single column for the student; table layout for the officer | One layout for all roles | Two layouts to build and test |
