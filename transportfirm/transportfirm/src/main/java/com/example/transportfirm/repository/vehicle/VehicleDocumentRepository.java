package com.example.transportfirm.repository.vehicle;

import com.example.transportfirm.entity.VehicleDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, UUID> {
    List<VehicleDocument> findByVehicle_Id(UUID id);
}