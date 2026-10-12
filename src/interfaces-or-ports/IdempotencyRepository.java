package ub.dormhub.ports;

import java.util.Optional;

public interface IdempotencyRepository {
    Optional<VerificationResult> findReplay(long userId, String key, long complaintId,
                                             VerificationCommand.Decision decision);
    void saveResult(long userId, String key, long complaintId,
                    VerificationCommand.Decision decision, VerificationResult result);
}
