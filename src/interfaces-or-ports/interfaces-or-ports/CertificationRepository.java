package ub.dormhub.ports;

import ub.dormhub.domain.CertificationRecord;

public interface CertificationRepository {
    CertificationRecord save(CertificationRecord record);
}
