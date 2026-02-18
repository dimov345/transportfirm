package com.example.transportfirm.repository;

import com.example.transportfirm.entity.VehicleMaintenanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VehicleMaintenanceRecordRepository extends JpaRepository<VehicleMaintenanceRecord, UUID> {
    Page<VehicleMaintenanceRecord> findAllByVehicle_Id(UUID vehicleId, Pageable pageable);
}
