package com.example.transportfirm.Repository;

import com.example.transportfirm.Entity.MechanicInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MechanicRepository extends JpaRepository<MechanicInfo, Long> {
}
