package com.example.transportfirm.repository;

import com.example.transportfirm.entity.DriverInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<DriverInfo, Long> {
    Optional<DriverInfo> findById(Long id);

    Optional<DriverInfo> findByEmployeeId(Long employeeId);

}
