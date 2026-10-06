# Lab 7 evidence - Architecture alternatives and component structure

**Project:** UB-DormHub (Team 5) | **Branch:** `lab7` | **Lab date:** Friday 25 September 2026

## Files

| Required evidence | File |
|---|---|
| Architecture drivers and design-obligation table; quality-to-architecture traceability | `docs/quality-to-architecture.md` |
| Comparison of three feasible alternatives (same criteria) | `docs/architecture-options.md` |
| Editable component model + readable exports | `models/component-architecture.svg`, `.pdf` |
| ADR with context, alternatives, decision, consequences, risks, reconsideration triggers | `decisions/ADR-001-architecture.md` |


## Exit record

**Quality requirement that most influenced the architecture:** QS-04 Reliability (persistence failure must leave zero partial records and no false success). It led to one transaction per use case, which favoured one deployable application over microservices.

**Evidence that would make us revise the decision:**
- under 95% of complaint submissions confirmed within 2 s (QS-01) after one tuning pass;
- scope growing to several residences needing independent releases or ownership;
- more than two production components changing to add a notification adapter, or UI code reaching the repository;
- the chosen data store unable to give one atomic transaction across a use case's writes.

**Highest architectural risk:** a service writing outside the Unit of Work or setting `status` directly, leaving a `Verified` complaint with no CertificationRecord. Checked by a fault-injection test and a transition-table test.





