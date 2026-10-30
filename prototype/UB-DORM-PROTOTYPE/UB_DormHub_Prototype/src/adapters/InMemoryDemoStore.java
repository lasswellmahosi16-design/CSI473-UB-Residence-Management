package ub.dormhub.adapters;

import java.util.*;
import java.util.function.Supplier;
import ub.dormhub.domain.*;
import ub.dormhub.ports.*;

/** In-memory, process-local demonstrator adapter; NOT a database or production security system. */
public final class InMemoryDemoStore implements AccessControlPort, ComplaintRepository,
        CertificationRepository, IdempotencyRepository, TransactionRunner {
    private final Map<Long, MaintenanceComplaint> complaints = new LinkedHashMap<>();
    private final Map<Long, CertificationRecord> certifications = new LinkedHashMap<>();
    private final Map<String, Replay> replays = new HashMap<>();
    private long nextCertification = 300;
    private boolean failNextComplaintSave;

    private record Replay(long complaintId, VerificationCommand.Decision decision,
                          String normalizedReason, VerificationResult result) { }

    public InMemoryDemoStore() {
        seed(1042, MaintenanceComplaint.Status.REPORTED);
        seed(1043, MaintenanceComplaint.Status.REPORTED);
        seed(1044, MaintenanceComplaint.Status.REPORTED);
        seed(1031, MaintenanceComplaint.Status.CLOSED);
    }

    private void seed(long id, MaintenanceComplaint.Status status) {
        complaints.put(id, new MaintenanceComplaint(id, status));
    }

    /** Adds a newly reported complaint to the demo repository. */
    public synchronized void addReportedComplaint(long complaintId) {
        if (complaints.containsKey(complaintId)) throw new IllegalArgumentException("Duplicate complaint id");
        complaints.put(complaintId, new MaintenanceComplaint(complaintId, MaintenanceComplaint.Status.REPORTED));
    }
    public synchronized Optional<CertificationRecord> certificationForComplaint(long complaintId) {
        return certifications.values().stream().filter(c -> c.complaintId() == complaintId).findFirst();
    }
    public synchronized boolean hasReplayKey(long officerId, String key) {
        return replays.containsKey(identity(officerId, key));
    }
    @Override public boolean isWelfareOfficer(long userId) { return userId == 2; }
    @Override public Optional<MaintenanceComplaint> findById(long id) {
        return Optional.ofNullable(complaints.get(id)).map(MaintenanceComplaint::copy);
    }
    @Override public void save(MaintenanceComplaint complaint) {
        if (failNextComplaintSave) {
            failNextComplaintSave = false;
            throw new IllegalStateException("PERSISTENCE_UNAVAILABLE: simulated complaint write failure");
        }
        complaints.put(complaint.complaintId(), complaint.copy());
    }
    @Override public CertificationRecord save(CertificationRecord record) {
        CertificationRecord saved = new CertificationRecord(++nextCertification, record.complaintId(),
                record.officerUserId(), record.certifiedDate());
        certifications.put(saved.certificationId(), saved);
        return saved;
    }
    private static String identity(long userId, String key) { return userId + "::" + key; }
    private static String normalized(String value) { return value == null ? null : value.trim(); }

    @Override public Optional<VerificationResult> findReplay(long userId, String key, long complaintId,
            VerificationCommand.Decision decision, String reason) {
        Replay replay = replays.get(identity(userId, key));
        if (replay == null) return Optional.empty();
        if (replay.complaintId() != complaintId || replay.decision() != decision ||
                !Objects.equals(replay.normalizedReason(), normalized(reason))) {
            throw new IllegalStateException("IDEMPOTENCY_KEY_REUSED: key belongs to another request");
        }
        return Optional.of(replay.result());
    }
    @Override public void saveResult(long userId, String key, long complaintId,
            VerificationCommand.Decision decision, String reason, VerificationResult result) {
        replays.put(identity(userId, key), new Replay(complaintId, decision, normalized(reason), result));
    }
    /** Atomic for THIS IN-MEMORY DEMO ONLY; a persistent adapter needs a real database transaction. */
    @Override public synchronized <T> T inTransaction(Supplier<T> work) {
        Map<Long, MaintenanceComplaint> beforeComplaints = new LinkedHashMap<>();
        complaints.forEach((id, item) -> beforeComplaints.put(id, item.copy()));
        Map<Long, CertificationRecord> beforeCertifications = new LinkedHashMap<>(certifications);
        Map<String, Replay> beforeReplays = new HashMap<>(replays);
        long previousNextId = nextCertification;
        try {
            return work.get();
        } catch (RuntimeException ex) {
            complaints.clear(); complaints.putAll(beforeComplaints);
            certifications.clear(); certifications.putAll(beforeCertifications);
            replays.clear(); replays.putAll(beforeReplays);
            nextCertification = previousNextId;
            throw ex;
        }
    }
    public synchronized void simulateComplaintWriteFailureOnce() { failNextComplaintSave = true; }
    public synchronized List<MaintenanceComplaint> listComplaints() {
        return complaints.values().stream().map(MaintenanceComplaint::copy).toList();
    }
    public synchronized int certificationCount() { return certifications.size(); }
    public synchronized int replayCount() { return replays.size(); }
}
