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
        if (reason == null || reason.trim().length() < 10) {
            throw new IllegalArgumentException("A meaningful rejection reason is required");
        }
        status = Status.REJECTED;
        rejectionReason = reason.trim();
    }

    private void requireReported() {
        if (status != Status.REPORTED) {
            throw new IllegalStateException("Complaint must be REPORTED before verification decision");
        }
    }
}
