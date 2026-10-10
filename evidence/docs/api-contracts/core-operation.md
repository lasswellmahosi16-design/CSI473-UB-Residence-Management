# API contract - Verify Maintenance Report (UC-04) - Lab 8

**Why this operation:** it is the core Phase 1 interaction. It combines authorisation, input validation, lifecycle rules, transaction integrity and realistic retry/concurrency failures.

**Traceability:** UC-04; FR-04, FR-05, FR-11, FR-12; BR-02, BR-03, BR-04, BR-09, BR-13, BR-14; AC-03, AC-04, AC-05, AC-12; QS-03, QS-04.  
Machine-readable version: `core-operation.yaml` (OpenAPI 3.0.3).

## 1. Endpoint

```http
POST /api/v1/complaints/{complaintId}/verification
```

| Header | Required | Meaning |
|---|---|---|
| `Authorization: Bearer <token>` | yes | Authenticated officer identity and role come from the token, never the request body |
| `Idempotency-Key: <uuid>` | yes | Unique per user action; retries of the same action reuse the same key |
| `Content-Type: application/json` | yes | Request is JSON |

## 2. Request body

Verify:
```json
{ "decision": "VERIFY" }
```

Reject:
```json
{ "decision": "REJECT", "rejectionReason": "Fault is not in the reporting student's room." }
```

| Field | Type | Validation |
|---|---|---|
| `complaintId` (path) | integer | Required, positive |
| `decision` | string | Exactly `VERIFY` or `REJECT` |
| `rejectionReason` | string | Required for `REJECT`, trimmed length 10-500; must be absent for `VERIFY` |

## 3. Processing rules (stable order)

1. **Authenticate** token. Failure -> 401 `UNAUTHENTICATED`.
2. **Authorise** `WELFARE_OFFICER` (BR-02, BR-13). Failure -> 403 `FORBIDDEN_ROLE`. No state change.
3. **Validate** path/header/body. Failure -> 400 `VALIDATION_FAILED`.
4. **Begin one Unit of Work / transaction.**
5. **Idempotency check** for `(officer, key, operation)`. Same key + same request returns the stored original response. Same key + different complaint/body -> 409 `IDEMPOTENCY_KEY_REUSED`.
6. **Load** complaint. Missing -> 404 `COMPLAINT_NOT_FOUND`.
7. **Domain pre-check:** complaint must currently be `REPORTED` (BR-03, BR-09). Otherwise -> 409 `INVALID_STATE`.
8. **Conditional lifecycle write:** update only where `status='REPORTED'`. If zero rows are updated (concurrent/stale request), roll back -> 409 `INVALID_STATE`.
   - `VERIFY`: set status `VERIFIED`; create one `CertificationRecord` linked to complaint and officer (BR-04).
   - `REJECT`: set status `REJECTED` and persist the rejection reason; **do not create a CertificationRecord**, because the submitted Phase 1 glossary/BR-04 defines it as successful verification evidence.
9. Save the idempotency record with the exact response, still inside the transaction.
10. **Commit.** Any persistence/commit failure -> roll back -> 503 `PERSISTENCE_UNAVAILABLE`.

## 4. Success outcomes

### 4.1 VERIFY - `201 Created`

```json
{
  "complaintId": 1042,
  "status": "VERIFIED",
  "certification": {
    "certificationId": 311,
    "certifiedBy": 2,
    "certifiedDate": "2026-10-02T09:15:30Z"
  }
}
```

### 4.2 REJECT - `200 OK`

```json
{
  "complaintId": 1042,
  "status": "REJECTED",
  "rejectionReason": "Fault is not in the reporting student's room."
}
```

Neither response exposes password data, unrelated complaints or unnecessary student contact details.

## 5. Stable error meanings

All errors use:

```json
{ "error": { "code": "INVALID_STATE", "message": "...", "currentStatus": "VERIFIED" } }
```

`code` is stable; human-readable `message` may change.

| HTTP | Code | Meaning | Persistent state after response | Client action |
|---|---|---|---|---|
| 400 | `VALIDATION_FAILED` | Invalid/missing path, key or body | Unchanged | Correct input; use a new key for a new action |
| 401 | `UNAUTHENTICATED` | Missing/invalid/expired token | Unchanged | Re-authenticate |
| 403 | `FORBIDDEN_ROLE` | Authenticated user lacks Welfare Officer role | Unchanged | No retry unless permissions change |
| 404 | `COMPLAINT_NOT_FOUND` | Complaint id does not exist | Unchanged | Refresh queue |
| 409 | `INVALID_STATE` | Complaint is no longer `REPORTED` | Unchanged by this request | Refresh current status |
| 409 | `IDEMPOTENCY_KEY_REUSED` | Key was previously used for a different request | Unchanged | Use a new key |
| 503 | `PERSISTENCE_UNAVAILABLE` | Database/write/commit failure | **Rolled back to pre-request state** | Retry the same request with the **same** key |
| 500 | `INTERNAL_ERROR` | Unexpected application failure | Transaction rolled back where active | Report/support; avoid blind repeated new-key submissions |

## 6. Retry and concurrency behaviour

- **Same key, same request:** return the stored original HTTP status/body with `Idempotent-Replayed: true`; no duplicate decision.
- **Same key, different request:** 409 `IDEMPOTENCY_KEY_REUSED`.
- **New key after another user already decided the complaint:** 409 `INVALID_STATE`.
- **Two simultaneous new-key decisions:** only the conditional `status='REPORTED'` update that changes one row may continue. The other transaction returns 409 rather than producing duplicate or partial state.

## 7. Design rationale

**Selected:** a dedicated verification operation with a required idempotency key.  
**Alternative:** generic `PATCH /complaints/{id}` to set `status`.  
**Why not selected:** generic status patching would expose lifecycle state as arbitrary data and make it easier for controllers/clients to bypass the domain transition rules (ADR-001 R3).  
**Accepted consequence:** lifecycle actions use explicit endpoints and the prototype stores idempotency metadata.
