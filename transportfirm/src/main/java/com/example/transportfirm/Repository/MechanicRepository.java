package com.example.transportfirm.repository;

import com.example.transportfirm.entity.MechanicInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MechanicRepository extends JpaRepository<MechanicInfo, Long> {
}
