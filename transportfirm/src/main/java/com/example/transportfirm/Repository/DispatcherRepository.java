package com.example.transportfirm.repository;

import com.example.transportfirm.entity.DispatcherInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispatcherRepository extends JpaRepository<DispatcherInfo, Long> {
}
