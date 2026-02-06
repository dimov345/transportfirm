package com.example.transportfirm.repository;

import com.example.transportfirm.entity.DispatcherInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DispatcherRepository extends JpaRepository<DispatcherInfo, UUID> {
}
