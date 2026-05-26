package com.example.transportfirm.repository.vehicle;

import com.example.transportfirm.entity.VehicleMaintenanceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VehicleMaintenanceDocumentRepository
        extends JpaRepository<VehicleMaintenanceDocument, UUID> {

    List<VehicleMaintenanceDocument> findAllByMaintenanceRecord_Id(UUID recordId);
}
