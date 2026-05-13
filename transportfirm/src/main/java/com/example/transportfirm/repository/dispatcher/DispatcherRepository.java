package com.example.transportfirm.repository.dispatcher;

import com.example.transportfirm.entity.DispatcherInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DispatcherRepository extends JpaRepository<DispatcherInfo, UUID> {
    Optional<DispatcherInfo> findByEmployee_Id(UUID employeeId);
}
