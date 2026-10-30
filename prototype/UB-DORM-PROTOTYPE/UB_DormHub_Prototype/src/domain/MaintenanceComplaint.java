package ub.dormhub.domain;

public final class MaintenanceComplaint {
    public enum Status { REPORTED, VERIFIED, REJECTED, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED }
    private final long complaintId;
    private Status status;
    private String rejectionReason;

    public MaintenanceComplaint(long complaintId, Status status) {
        this.complaintId = complaintId;
        this.status = status;
    }
    public long complaintId() { return complaintId; }
    public Status status() { return status; }
    public String rejectionReason() { return rejectionReason; }

    public void verify() {
        requireReported();
        status = Status.VERIFIED;
        rejectionReason = null;
    }
    public void reject(String reason) {
        requireReported();
        if (reason == null || reason.trim().length() < 10 || reason.trim().length() > 500) {
            throw new IllegalArgumentException("VALIDATION_FAILED: rejection reason must contain 10–500 characters");
        }
        status = Status.REJECTED;
        rejectionReason = reason.trim();
    }
    public MaintenanceComplaint copy() {
        MaintenanceComplaint copied = new MaintenanceComplaint(complaintId, status);
        copied.rejectionReason = rejectionReason;
        return copied;
    }
    private void requireReported() {
        if (status != Status.REPORTED) {
            throw new IllegalStateException("INVALID_STATE: only REPORTED complaints can be decided");
        }
    }
}
