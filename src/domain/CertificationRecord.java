package ub.dormhub.domain;

import java.time.Instant;

public record CertificationRecord(Long certificationId, long complaintId,
                                  long officerUserId, Instant certifiedDate) {
    public static CertificationRecord newRecord(long complaintId, long officerUserId) {
        return new CertificationRecord(null, complaintId, officerUserId, Instant.now());
    }
}
