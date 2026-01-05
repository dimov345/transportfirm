package com.example.transportfirm.repository;

import com.example.transportfirm.entity.VehicleDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, Long> {
    List<VehicleDocument> findByVehicle_PlateNumber(String plateNumber);
}