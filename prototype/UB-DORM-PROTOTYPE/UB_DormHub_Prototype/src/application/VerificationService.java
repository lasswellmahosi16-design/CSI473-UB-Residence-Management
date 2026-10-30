package ub.dormhub.application;

import ub.dormhub.domain.CertificationRecord;
import ub.dormhub.domain.MaintenanceComplaint;
import ub.dormhub.ports.*;
import java.util.UUID;

/** Framework-independent UC-04 verification/rejection workflow. */
public final class VerificationService implements VerifyMaintenanceReportUseCase {
    private final AccessControlPort access;
    private final ComplaintRepository complaints;
    private final CertificationRepository certifications;
    private final IdempotencyRepository idempotency;
    private final TransactionRunner transactions;

    public VerificationService(AccessControlPort access, ComplaintRepository complaints,
                               CertificationRepository certifications, IdempotencyRepository idempotency,
                               TransactionRunner transactions) {
        this.access = access;
        this.complaints = complaints;
        this.certifications = certifications;
        this.idempotency = idempotency;
        this.transactions = transactions;
    }

    @Override
    public VerificationResult execute(VerificationCommand command) {
        if (command == null || command.currentUserId() <= 0) {
            throw new IllegalArgumentException("VALIDATION_FAILED: invalid officer identity");
        }
        if (!access.isWelfareOfficer(command.currentUserId())) {
            throw new SecurityException("FORBIDDEN_ROLE");
        }
        validate(command);
        // Replay check and decision share a single transaction to prevent concurrent double writes.
        return transactions.inTransaction(() -> idempotency.findReplay(
                        command.currentUserId(), command.idempotencyKey(), command.complaintId(),
                        command.decision(), command.rejectionReason())
                .orElseGet(() -> decide(command)));
    }

    private static void validate(VerificationCommand command) {
        if (command.complaintId() <= 0 || command.decision() == null) {
            throw new IllegalArgumentException("VALIDATION_FAILED: positive complaint ID and decision required");
        }
        try {
            UUID parsed = UUID.fromString(command.idempotencyKey());
            if (!parsed.toString().equalsIgnoreCase(command.idempotencyKey())) throw new Exception();
        } catch (Exception ex) {
            throw new IllegalArgumentException("VALIDATION_FAILED: Idempotency-Key must be a UUID");
        }
        if (command.decision() == VerificationCommand.Decision.VERIFY && command.rejectionReason() != null) {
            throw new IllegalArgumentException("VALIDATION_FAILED: verify request must not have a rejection reason");
        }
        if (command.decision() == VerificationCommand.Decision.REJECT &&
                (command.rejectionReason() == null || command.rejectionReason().trim().length() < 10 ||
                        command.rejectionReason().trim().length() > 500)) {
            throw new IllegalArgumentException("VALIDATION_FAILED: rejection reason must contain 10–500 characters");
        }
    }

    private VerificationResult decide(VerificationCommand command) {
        MaintenanceComplaint complaint = complaints.findById(command.complaintId())
                .orElseThrow(() -> new IllegalArgumentException("COMPLAINT_NOT_FOUND"));
        Long certificationId = null;
        if (command.decision() == VerificationCommand.Decision.VERIFY) {
            complaint.verify();
            CertificationRecord saved = certifications.save(
                    CertificationRecord.newRecord(complaint.complaintId(), command.currentUserId()));
            certificationId = saved.certificationId();
        } else {
            complaint.reject(command.rejectionReason());
        }
        complaints.save(complaint);
        VerificationResult result = new VerificationResult(
                complaint.complaintId(), complaint.status().name(), certificationId);
        idempotency.saveResult(command.currentUserId(), command.idempotencyKey(), complaint.complaintId(),
                command.decision(), command.rejectionReason(), result);
        return result;
    }
}
