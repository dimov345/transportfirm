package com.example.transportfirm.Repository;

import com.example.transportfirm.Entity.VehicleRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<VehicleRecord, String> {
}
