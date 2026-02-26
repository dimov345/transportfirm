package com.example.transportfirm.repository;

import com.example.transportfirm.entity.VehicleRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<VehicleRecord, UUID> {
    List<VehicleRecord> findByDriverIsNull();
    List<VehicleRecord> findByMechanicGroup_IdOrDispatcherGroup_Id(UUID mechanicGroupId, UUID dispatcherGroupId);
}
