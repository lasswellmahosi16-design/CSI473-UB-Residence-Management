# UB-DormHub - Framework and dependency-management decision 

## Decision

Use **Java 21 with Spring Boot** for the Phase 2 implementation of the selected layered modular architecture.

Framework use is limited to the outer layers:

- **Spring Web / MVC** for HTTP controllers and JSON request/response mapping;
- **Bean Validation** for structural input validation;
- **Spring Security** for authentication and role enforcement at the application boundary;
- **Spring Data JPA** (or a small JPA adapter) for persistence adapters;
- **Spring transaction management** behind the `TransactionRunner` boundary;
- **constructor injection** for dependency management.

Core application interfaces and domain classes stay plain Java and must not require Spring annotations to express business rules.

## Why this fits UB-DormHub

| Need | Framework support | Project consequence |
|---|---|---|
| Explicit REST operation from Lab 8 | Spring Web controllers | Maps `POST /api/v1/complaints/{id}/verification` cleanly without putting workflow rules in the controller. |
| Role protection (QS-03) | Spring Security | Authentication/roles are checked server-side; the service still receives a stable access abstraction. |
| Atomic verification transaction (QS-04) | Spring transaction management | Complaint state and CertificationRecord can commit or roll back as one use case. |
| Persistence isolation | JPA adapters behind repository ports | The application service depends on project interfaces, not on concrete JPA repositories. |
| Testability | Constructor injection and test support | Fakes/mocks can replace repositories and transaction/security ports. |
| Semester delivery | Convention and integrated tooling | Reduces infrastructure code while keeping one deployable application. |

## Dependency-management approach

Use **Maven** with one application module initially. Dependencies are declared centrally in `pom.xml`; Spring Boot dependency management provides compatible versions. Avoid adding libraries that do not solve a current requirement.

Recommended dependency groups for the prototype:

- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-security`
- `spring-boot-starter-data-jpa`
- database driver selected for the implementation environment
- `spring-boot-starter-test` for tests

The project should not introduce a separate DI container, event bus, mapping framework or distributed-service library unless evidence shows a need.

## Alternatives considered

### Plain Java/Jakarta Servlet stack
Feasible and gives maximum control, but the team would write more authentication, request mapping, validation and transaction plumbing. That work does not add value to the residence-maintenance workflow.

### Microservice framework / multiple services
Rejected for the same reason recorded in ADR-001: no requirement needs independent deployment or scaling, while distributed failure handling would make QS-04 harder to guarantee.

## Framework boundary rule

The following are allowed to know Spring: controllers, configuration, security adapters, transaction adapter and persistence adapters.

The following should remain framework-independent: use-case interfaces, `VerificationService` business coordination logic where practical, domain classes and repository/port interfaces.

This rule gives the team the productivity of Spring Boot without allowing the framework to become the design.
