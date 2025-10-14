package com.example.transportfirm.Repository;

import com.example.transportfirm.Entity.DriverInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<DriverInfo, String> {
}
