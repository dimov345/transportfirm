package com.example.transportfirm.repository.driver;

import com.example.transportfirm.entity.DriverInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<DriverInfo, UUID> {

    Optional<DriverInfo> findByEmployeeId(UUID employeeId);

    Optional<DriverInfo> findByVehicle_Id(UUID vehicleId);

    /** Fetches all drivers with their employee in one query (no lazy-load risk). */
    @Query("SELECT d FROM DriverInfo d JOIN FETCH d.employee")
    List<DriverInfo> findAllWithEmployee();
}
