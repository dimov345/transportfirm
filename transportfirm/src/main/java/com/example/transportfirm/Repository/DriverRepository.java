package com.example.transportfirm.Repository;

import com.example.transportfirm.Entity.DriverInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<DriverInfo, Long> {
    Optional<DriverInfo> findById(Long id);

    Optional<DriverInfo> findByEmployeeId(Long employeeId);

}
