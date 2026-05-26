package com.example.transportfirm.repository.mechanic;

import com.example.transportfirm.entity.MechanicInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MechanicRepository extends JpaRepository<MechanicInfo, UUID> {
    Optional<MechanicInfo> findByEmployee_Id(UUID employeeId);
}
