package com.example.transportfirm.repository.vehicle;

import com.example.transportfirm.entity.VehicleMaintenanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface VehicleMaintenanceRecordRepository extends JpaRepository<VehicleMaintenanceRecord, UUID> {
    @Query(value = "SELECT m FROM VehicleMaintenanceRecord m JOIN FETCH m.vehicle WHERE m.vehicle.id = :vehicleId ORDER BY m.openedAt DESC",
           countQuery = "SELECT COUNT(m) FROM VehicleMaintenanceRecord m WHERE m.vehicle.id = :vehicleId")
    Page<VehicleMaintenanceRecord> findAllByVehicle_Id(@Param("vehicleId") UUID vehicleId, Pageable pageable);

    List<VehicleMaintenanceRecord> findAllByVehicle_Id(UUID vehicleId);

    @Query("SELECT m FROM VehicleMaintenanceRecord m JOIN FETCH m.vehicle WHERE m.openedAt BETWEEN :start AND :end ORDER BY m.openedAt DESC")
    List<VehicleMaintenanceRecord> findByOpenedAtBetweenWithVehicle(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Returns records where openedAt OR closedAt falls within the period.
     * Uses LEFT JOIN FETCH to include records even if vehicle is lazily-loaded.
     * Used by the accounting dashboard JSON view.
     */
    @Query("SELECT m FROM VehicleMaintenanceRecord m LEFT JOIN FETCH m.vehicle " +
           "WHERE (m.openedAt BETWEEN :start AND :end) " +
           "   OR (m.closedAt IS NOT NULL AND m.closedAt BETWEEN :start AND :end) " +
           "ORDER BY m.openedAt DESC")
    List<VehicleMaintenanceRecord> findByPeriodWithVehicle(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Used by DashboardService for monthly grouping over a wide date range.
     */
    @Query("SELECT m FROM VehicleMaintenanceRecord m LEFT JOIN FETCH m.vehicle " +
           "WHERE m.openedAt BETWEEN :start AND :end ORDER BY m.openedAt ASC")
    List<VehicleMaintenanceRecord> findByOpenedAtRangeWithVehicle(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
