package com.example.transportfirm.repository;

import com.example.transportfirm.entity.DriverInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<DriverInfo, UUID> {

    Optional<DriverInfo> findByEmployeeId(UUID employeeId);

    Optional<DriverInfo> findByVehicle_Id(UUID vehicleId);

}
