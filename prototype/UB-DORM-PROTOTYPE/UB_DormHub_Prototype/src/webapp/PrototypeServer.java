package ub.dormhub.webapp;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import ub.dormhub.adapters.InMemoryDemoStore;
import ub.dormhub.application.VerificationService;
import ub.dormhub.domain.CertificationRecord;
import ub.dormhub.domain.MaintenanceComplaint;
import ub.dormhub.ports.VerificationCommand;
import ub.dormhub.ports.VerificationResult;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JDK-only, locally runnable browser prototype for the existing UC-04 domain service.
 * DEMONSTRATION ONLY: test identities and data are in memory. This is not Spring Boot,
 * production authentication, or durable persistence.
 */
public final class PrototypeServer {
    private final Path webRoot;
    private volatile InMemoryDemoStore store;
    private volatile VerificationService verification;
    private final Map<Long, Details> details = new LinkedHashMap<>();
    private long nextId;

    private record Details(long id, long studentId, String studentName, String room, String category,
                           String title, String description, String reportedAt) { }
    private record Identity(long id, String role, String name) { }

    public PrototypeServer(Path webRoot) {
        this.webRoot = webRoot;
        resetData();
    }

    private synchronized void resetData() {
        store = new InMemoryDemoStore();
        verification = new VerificationService(store, store, store, store, store);
        details.clear();
        // The same example complaint identifiers and rooms appear in Lab 10 wireframes.
        details.put(1042L, new Details(1042, 1, "Neo M.", "Block 3 · Room 101", "Plumbing",
                "Leaking bathroom tap", "The bathroom tap has been leaking since Monday morning. Water is collecting under the sink.", "2026-10-20"));
        details.put(1043L, new Details(1043, 3, "Kabelo T.", "Block 1 · Room 214", "Electrical",
                "Broken electrical socket", "The socket on the study wall sparks when a plug is inserted.", "2026-10-21"));
        details.put(1044L, new Details(1044, 4, "Amogelang K.", "Block 3 · Room 118", "Furniture",
                "Damaged wardrobe hinge", "The wardrobe door hinge has broken and the door cannot close properly.", "2026-10-22"));
        details.put(1031L, new Details(1031, 1, "Neo M.", "Block 3 · Room 101", "Electrical",
                "Broken wall socket", "The electrical socket stopped working and has been replaced.", "2026-10-12"));
        nextId = 1045;
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("DORMHUB_PORT", "8765"));
        Path webRoot = Path.of(System.getProperty("dormhub.webRoot", "web")).toAbsolutePath().normalize();
        if (!Files.isRegularFile(webRoot.resolve("index.html"))) {
            System.err.println("Web files not found in " + webRoot + ". Run the script from the project root.");
            System.exit(2);
        }
        var app = new PrototypeServer(webRoot);
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", app::handle);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("UB-DormHub prototype running at http://localhost:" + port);
        System.out.println("Demo accounts: Student / Welfare Officer. Ctrl+C to stop.");
    }

    private void handle(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            String method = ex.getRequestMethod();
            if (method.equals("GET") && (path.equals("/") || path.equals("/app.js") || path.equals("/style.css"))) {
                serveStatic(ex, path);
                return;
            }
            if (method.equals("GET") && path.equals("/api/health")) {
                send(ex, 200, "{\"status\":\"ok\",\"prototype\":true}");
                return;
            }
            if (!path.startsWith("/api/")) { error(ex, 404, "NOT_FOUND", "Page not found."); return; }
            Identity user = identify(ex);
            if (user == null) { error(ex, 401, "UNAUTHENTICATED", "Please choose a demo account to continue."); return; }

            if (method.equals("GET") && path.equals("/api/v1/me")) {
                send(ex, 200, "{\"id\":" + user.id + ",\"name\":" + q(user.name) +
                        ",\"role\":" + q(user.role) + ",\"room\":\"Block 3 · Room 101\"}");
            } else if (method.equals("GET") && path.equals("/api/v1/complaints")) {
                send(ex, 200, listComplaints(user));
            } else if (method.equals("POST") && path.equals("/api/v1/complaints")) {
                if (!user.role.equals("STUDENT")) { error(ex, 403, "FORBIDDEN_ROLE", "Only residents can submit maintenance reports."); return; }
                createComplaint(ex, user);
            } else if (method.equals("POST") && path.equals("/api/demo/reset")) {
                if (!user.role.equals("OFFICER")) { error(ex, 403, "FORBIDDEN_ROLE", "Only the demo officer can reset the data."); return; }
                resetData();
                send(ex, 200, "{\"message\":\"Demo data reset successfully\"}");
            } else {
                Matcher matcher = Pattern.compile("^/api/v1/complaints/(\\d+)(/verification)?$").matcher(path);
                if (matcher.matches()) {
                    long id = Long.parseLong(matcher.group(1));
                    if (id <= 0) { error(ex, 400, "VALIDATION_FAILED", "Invalid complaint reference."); return; }
                    if (method.equals("GET") && matcher.group(2) == null) {
                        Details record = details.get(id);
                        if (record == null) { error(ex, 404, "COMPLAINT_NOT_FOUND", "Complaint not found."); return; }
                        if (user.role.equals("STUDENT") && record.studentId != user.id) {
                            error(ex, 403, "FORBIDDEN_ROLE", "You do not have access to this complaint."); return;
                        }
                        send(ex, 200, complaintJson(record));
                    } else if (method.equals("POST") && matcher.group(2) != null) {
                        if (!user.role.equals("OFFICER")) { error(ex, 403, "FORBIDDEN_ROLE", "Welfare Officer access required."); return; }
                        makeDecision(ex, user, id);
                    } else { error(ex, 405, "METHOD_NOT_ALLOWED", "This method is not supported."); }
                } else { error(ex, 404, "NOT_FOUND", "Endpoint not found."); }
            }
        } catch (IllegalArgumentException ex1) {
            String message = ex1.getMessage() == null ? "Invalid input." : ex1.getMessage();
            if (message.startsWith("COMPLAINT_NOT_FOUND")) error(ex, 404, "COMPLAINT_NOT_FOUND", "Complaint not found.");
            else error(ex, 400, "VALIDATION_FAILED", message.replace("VALIDATION_FAILED: ", ""));
        } catch (SecurityException se) {
            error(ex, 403, "FORBIDDEN_ROLE", "Welfare Officer access required.");
        } catch (IllegalStateException se) {
            String message = se.getMessage() == null ? "Operation failed." : se.getMessage();
            if (message.startsWith("INVALID_STATE")) error(ex, 409, "INVALID_STATE", "This complaint has already been reviewed. Refresh and try again.");
            else if (message.startsWith("IDEMPOTENCY_KEY_REUSED")) error(ex, 409, "IDEMPOTENCY_KEY_REUSED", "The request key was already used for a different decision.");
            else if (message.startsWith("PERSISTENCE_UNAVAILABLE")) error(ex, 503, "PERSISTENCE_UNAVAILABLE", "The decision was not saved. Please retry.");
            else { se.printStackTrace(); error(ex, 500, "INTERNAL_ERROR", "Something went wrong."); }
        } catch (Exception unexpected) {
            unexpected.printStackTrace();
            error(ex, 500, "INTERNAL_ERROR", "The action could not be completed.");
        } finally { ex.close(); }
    }

    private static Identity identify(HttpExchange ex) {
        String bearer = ex.getRequestHeaders().getFirst("Authorization");
        if ("Bearer demo-student".equals(bearer)) return new Identity(1, "STUDENT", "Neo M.");
        if ("Bearer demo-officer".equals(bearer)) return new Identity(2, "OFFICER", "Ms Kgosi");
        return null;
    }

    private void createComplaint(HttpExchange ex, Identity user) throws IOException {
        String body = readJson(ex);
        String cat = field(body, "category");
        String description = field(body, "description");
        if (cat == null || !List.of("Plumbing", "Electrical", "Furniture", "Other").contains(cat)) {
            error(ex, 400, "VALIDATION_FAILED", "Choose a fault category."); return;
        }
        if (description == null || description.strip().length() < 10 || description.strip().length() > 200) {
            error(ex, 400, "VALIDATION_FAILED", "Describe the fault in 10 to 200 characters."); return;
        }
        synchronized (this) {
            long id = nextId++;
            String title = description.strip().length() > 50 ? description.strip().substring(0, 47) + "…" : description.strip();
            Details record = new Details(id, user.id, user.name, "Block 3 · Room 101", cat,
                    title, description.strip(), LocalDate.now().toString());
            store.addReportedComplaint(id);
            details.put(id, record);
            send(ex, 201, complaintJson(record));
        }
    }

    private void makeDecision(HttpExchange ex, Identity user, long id) throws IOException {
        String body = readJson(ex);
        String decision = field(body, "decision");
        String reason = field(body, "rejectionReason");
        String key = ex.getRequestHeaders().getFirst("Idempotency-Key");
        if (key == null) key = "";
        if (decision == null || !List.of("VERIFY", "REJECT").contains(decision)) {
            error(ex, 400, "VALIDATION_FAILED", "Select Verify or Reject."); return;
        }
        VerificationCommand command = new VerificationCommand(id,
                VerificationCommand.Decision.valueOf(decision), reason, key, user.id);
        boolean replay = store.hasReplayKey(user.id, key);
        VerificationResult result = verification.execute(command);
        if (replay) ex.getResponseHeaders().set("Idempotent-Replayed", "true");
        String json = "{\"complaintId\":" + result.complaintId() + ",\"status\":" + q(result.status());
        if (result.status().equals("VERIFIED")) {
            CertificationRecord certification = store.certificationForComplaint(id)
                    .orElseThrow(() -> new IllegalStateException("Missing certification for verified complaint"));
            json += ",\"certification\":{\"certificationId\":" + certification.certificationId() +
                    ",\"certifiedBy\":" + certification.officerUserId() +
                    ",\"certifiedDate\":" + q(certification.certifiedDate().toString()) + "}";
        } else {
            json += ",\"rejectionReason\":" + q(store.findById(id).orElseThrow().rejectionReason());
        }
        json += "}";
        send(ex, result.status().equals("VERIFIED") ? 201 : 200, json);
    }

    private synchronized String listComplaints(Identity user) {
        List<Details> visible = new ArrayList<>(details.values());
        if (user.role.equals("STUDENT")) visible.removeIf(d -> d.studentId != user.id);
        visible.sort(Comparator.comparingLong(Details::id).reversed());
        return "[" + String.join(",", visible.stream().map(this::complaintJson).toList()) + "]";
    }

    private String complaintJson(Details d) {
        MaintenanceComplaint state = store.findById(d.id).orElseThrow();
        String reason = state.rejectionReason();
        return "{\"id\":" + d.id + ",\"reference\":" + q("UBD-" + d.id) +
                ",\"studentName\":" + q(d.studentName) + ",\"room\":" + q(d.room) +
                ",\"category\":" + q(d.category) + ",\"title\":" + q(d.title) +
                ",\"description\":" + q(d.description) + ",\"reportedAt\":" + q(d.reportedAt) +
                ",\"status\":" + q(state.status().name()) + ",\"rejectionReason\":" + q(reason) + "}";
    }

    private void serveStatic(HttpExchange ex, String path) throws IOException {
        String fileName = path.equals("/") ? "index.html" : path.substring(1);
        String mime = fileName.endsWith(".css") ? "text/css" : fileName.endsWith(".js") ? "text/javascript" : "text/html";
        byte[] bytes = Files.readAllBytes(webRoot.resolve(fileName));
        ex.getResponseHeaders().set("Content-Type", mime + "; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
    }
    private static String readJson(HttpExchange ex) throws IOException {
        String ct = ex.getRequestHeaders().getFirst("Content-Type");
        if (ct == null || !ct.toLowerCase(Locale.ROOT).startsWith("application/json"))
            throw new IllegalArgumentException("VALIDATION_FAILED: Expected application/json.");
        byte[] body = ex.getRequestBody().readNBytes(8193);
        if (body.length > 8192) throw new IllegalArgumentException("VALIDATION_FAILED: Request is too large.");
        String input = new String(body, StandardCharsets.UTF_8).trim();
        if (!input.startsWith("{") || !input.endsWith("}"))
            throw new IllegalArgumentException("VALIDATION_FAILED: Invalid JSON object.");
        return input;
    }
    /** Handles the string-valued fields used by this small JDK-only demonstration API. */
    private static String field(String json, String name) {
        Matcher m = Pattern.compile("\\\"" + Pattern.quote(name) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"").matcher(json);
        if (!m.find()) return null;
        String raw = m.group(1);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c != '\\') { out.append(c); continue; }
            if (++i >= raw.length()) throw new IllegalArgumentException("VALIDATION_FAILED: Malformed string escape.");
            char e = raw.charAt(i);
            switch (e) {
                case 'n' -> out.append('\n'); case 'r' -> out.append('\r'); case 't' -> out.append('\t');
                case '"' -> out.append('"'); case '\\' -> out.append('\\'); case '/' -> out.append('/');
                case 'u' -> {
                    if (i + 4 >= raw.length()) throw new IllegalArgumentException("VALIDATION_FAILED: Invalid Unicode escape.");
                    try { out.append((char) Integer.parseInt(raw.substring(i + 1, i + 5), 16)); }
                    catch (NumberFormatException ex) { throw new IllegalArgumentException("VALIDATION_FAILED: Invalid Unicode escape."); }
                    i += 4;
                }
                default -> throw new IllegalArgumentException("VALIDATION_FAILED: Invalid string escape.");
            }
        }
        return out.toString();
    }
    private static String q(String input) {
        if (input == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\""); case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n"); case '\r' -> sb.append("\\r"); case '\t' -> sb.append("\\t");
                default -> { if (c < 32) sb.append(String.format("\\u%04x", (int) c)); else sb.append(c); }
            }
        }
        return sb.append('"').toString();
    }
    private static void send(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
    }
    private static void error(HttpExchange ex, int status, String code, String message) throws IOException {
        send(ex, status, "{\"error\":{\"code\":" + q(code) + ",\"message\":" + q(message) + "}}");
    }
}
