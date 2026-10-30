package ub.dormhub.ports;

public interface VerifyMaintenanceReportUseCase {
    VerificationResult execute(VerificationCommand command);
}
