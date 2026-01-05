package com.example.transportfirm.repository;

import com.example.transportfirm.entity.VehicleRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<VehicleRecord, String> {
}
