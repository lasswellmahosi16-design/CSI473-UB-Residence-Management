package ub.dormhub.ports;

import java.util.Optional;
import ub.dormhub.domain.MaintenanceComplaint;

public interface ComplaintRepository {
    Optional<MaintenanceComplaint> findById(long complaintId);
    void save(MaintenanceComplaint complaint);
}
