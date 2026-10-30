package ub.dormhub.ports;

import java.util.Optional;

public interface IdempotencyRepository {
    Optional<VerificationResult> findReplay(long userId, String key, long complaintId,
                                             VerificationCommand.Decision decision, String reason);
    void saveResult(long userId, String key, long complaintId,
                    VerificationCommand.Decision decision, String reason, VerificationResult result);
}
