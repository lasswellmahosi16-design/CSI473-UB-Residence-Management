# UB-DormHub — working browser prototype

**CSI473 Software Architecture and Design · Team 05 · University of Botswana**

This package contains **one working prototype** of the Residence & Maintenance Operations Hub. It provides a responsive interface for students and Student Welfare Officers, with a **real Java 21 HTTP backend** connected to the verification domain service originally designed in Lab 09/10. The six main screens from the Lab 10 wireframes are now usable rather than static drawings.

## Run it on Windows (simplest)

1. Install **JDK 21** (not just a Java Runtime). Check: open PowerShell and run `java -version` and `javac -version`. Both should show version 21.
2. Extract the ZIP. **Do not run the files from inside the ZIP.**
3. Double-click **`RUN_PROTOTYPE_WINDOWS.bat`**. A PowerShell window should open to compile and start the Java server. Leave this window open while testing.
4. Open **http://localhost:8765** in Chrome or Edge (the launcher also tries to open it automatically).
5. To stop, close the server's PowerShell window or press **Ctrl+C** in it.

**Alternative:** Run `powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1` in the extracted folder, then open http://localhost:8765.

## Run it on Linux / macOS

With JDK 21 installed, open a terminal in this folder and run:

```bash
bash scripts/run.sh
```

Then open **http://localhost:8765**.

The prototype uses only the JDK. You **do not need Maven, Spring Boot, a database server, Node.js or Python to run it**. Use `DORMHUB_PORT=8766 bash scripts/run.sh` to change the port on Linux/macOS. Close any other program using port 8765 before launching.

## Demonstrate the six screens

Start as the **Resident Student (Neo M.)**. Click **Report a fault**, choose a category, describe the fault (10–200 characters) and submit. The browser sends the report to the Java server, receives a complaint number (starting with **UBD-1045**) and opens **Complaint details**, where the student can see its progress. **My complaints** shows all the student's reports and current statuses.

Click **Switch demo account** at the bottom of the sidebar (or click the top-right profile) and choose **Student Welfare Officer (Ms Kgosi)**. The **Verification queue** shows the reported complaints. Open one to review its room, description and resident. Choose **Verify complaint** or **Reject complaint**. A rejection requires a 10–500 character reason, which is visible in that complaint's details. Switch back to the Student role and open the original complaint to see the updated status.

The project includes these connected views:

| View | What it does |
| --- | --- |
| F1 — Student overview / My complaints | View current reports, search, see status badges |
| F2 — Report a fault | Validate category and description, create complaint with reference |
| F3 — Complaint details | Show server-confirmed status, description and progress timeline |
| F4 — Officer overview / Verification queue | View reported complaints, oldest first, search and open them |
| F5 — Review complaint | Review resident/room/fault and verify through the original UC-04 service |
| F6 — Reject complaint | Validate and record rejection reason, keeping an audit-friendly result |

**Seeded examples:** UBD-1042 is a reported plumbing fault in Block 3 Room 101, UBD-1043 is a reported electrical fault in Block 1 Room 214, UBD-1044 is a reported furniture fault in Block 3 Room 118 and UBD-1031 is a previously closed student complaint.

## Important limitations

This is a **classroom demonstration, not a production deployment**. The project reports selected Spring Boot/Maven as the intended production framework; this prototype instead uses JDK 21's built-in `HttpServer` so that the complete workflow can be compiled, run and demonstrated offline without dependencies. The original Java domain/service/port code is reused for verification and rejection. A future Spring Boot adapter can replace the HTTP layer.

- **Demo identities only:** `Bearer demo-student` and `Bearer demo-officer` are fixed role tokens, not a real account login service. They must never be used for a real deployment.
- **Memory-only data:** requests and decisions persist while the server is running, but **reset on restart**. The officer also has a **Reset demo** button. There is no database yet.
- The complete maintenance lifecycle (technician assignment, repair and closure) is **illustrated in the status timeline**, not implemented as live technician workflows.
- Notification delivery, file uploads, production security and database transactions are not implemented. Verification is atomic within the **single-process in-memory demo store** only.
- This demonstrates the core use cases; it does not claim to be a finished Spring Boot application.

## Test the prototype

From the extracted folder:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test.ps1
```

or on Linux/macOS:

```bash
bash scripts/test.sh
```

The test runner executes **14 Java domain tests**. If Python 3 is installed, it additionally launches a temporary server on port 8766 and executes **23 HTTP integration checks** (authorisation, report validation, report creation, verification/rejection, replay keys, stale decisions, concurrency, student visibility and reset). Python is needed **only** for these optional HTTP smoke tests, not to run the application.

See [`evidence/test-results.txt`](evidence/test-results.txt) for the local execution log. Screenshots in `evidence/` show the interactive pages rendered against the Java backend. They are UI documentation, not proof of production-level security or a persistence database.

## Prototype folder layout

```
UB_DormHub_Prototype/
├── RUN_PROTOTYPE_WINDOWS.bat   One-click Windows launcher
├── README.md                   This guide
├── src/
│   ├── application/           Existing UC-04 VerificationService
│   ├── domain/                MaintenanceComplaint, CertificationRecord
│   ├── interfaces-or-ports/   Service contracts
│   ├── adapters/              Extended in-memory demo store
│   └── webapp/                Java HTTP server / API adapter
├── web/
│   ├── index.html             Accessible SVG icon symbols and app root
│   ├── style.css              Responsive blue/navy design
│   └── app.js                 Connected student/officer interface
├── scripts/                   Run and test on Windows/Linux/macOS
├── tests/                     Domain and real HTTP smoke tests
└── evidence/                  Local test log and preview screenshots
```

## Connected demonstration API

| Method | Route | Use |
| --- | --- | --- |
| `GET` | `/api/health` | Health check |
| `GET` | `/api/v1/me` | Current selected demo identity |
| `GET` | `/api/v1/complaints` | Role-filtered complaints list |
| `GET` | `/api/v1/complaints/{id}` | One complaint, with student ownership enforcement |
| `POST` | `/api/v1/complaints` | Student submits category + description |
| `POST` | `/api/v1/complaints/{id}/verification` | Officer verification/rejection using JSON, `Authorization` and UUID `Idempotency-Key` |
| `POST` | `/api/demo/reset` | Restore demo data (officer only) |

For the core verification route, the API honours the decision and idempotency rules in `evidence/docs/api-contracts/core-operation.md` from the earlier project work. This standalone prototype includes the implementation but not the full repository design documentation.

**GitHub note:** This ZIP contains the **prototype part only**, not the whole Team 05 repository. After reviewing it, copy this folder into your existing repository (for example `prototype/working-app/`) and link this README from the repository root.
