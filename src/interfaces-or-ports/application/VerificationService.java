package ub.dormhub.application;

import ub.dormhub.domain.CertificationRecord;
import ub.dormhub.domain.MaintenanceComplaint;
import ub.dormhub.ports.*;

public final class VerificationService implements VerifyMaintenanceReportUseCase {
    private final AccessControlPort access;
    private final ComplaintRepository complaints;
    private final CertificationRepository certifications;
    private final IdempotencyRepository idempotency;
    private final TransactionRunner transactions;

    public VerificationService(AccessControlPort access,
                               ComplaintRepository complaints,
                               CertificationRepository certifications,
                               IdempotencyRepository idempotency,
                               TransactionRunner transactions) {
        this.access = access;
        this.complaints = complaints;
        this.certifications = certifications;
        this.idempotency = idempotency;
        this.transactions = transactions;
    }

    @Override
    public VerificationResult execute(VerificationCommand command) {
        if (!access.isWelfareOfficer(command.currentUserId())) {
            throw new SecurityException("FORBIDDEN_ROLE");
        }

        return idempotency.findReplay(command.currentUserId(), command.idempotencyKey(),
                        command.complaintId(), command.decision())
                .orElseGet(() -> transactions.inTransaction(() -> decide(command)));
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

        idempotency.saveResult(command.currentUserId(), command.idempotencyKey(),
                command.complaintId(), command.decision(), result);
        return result;
    }
}
