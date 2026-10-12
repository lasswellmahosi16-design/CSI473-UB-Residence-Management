package ub.dormhub.ports;

public record VerificationCommand(
        long complaintId,
        Decision decision,
        String rejectionReason,
        String idempotencyKey,
        long currentUserId) {

    public enum Decision { VERIFY, REJECT }
}
