package com.example.transportfirm.repository;

import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.enums.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<VehicleRecord, UUID> {

    List<VehicleRecord> findByDriverIsNull();
    List<VehicleRecord> findByMechanicGroup_IdOrDispatcherGroup_Id(UUID mechanicGroupId, UUID dispatcherGroupId);

    /** Fetches all vehicles with their dispatcher group + dispatcher + employee in one query (no lazy loading). */
    @Query("SELECT DISTINCT v FROM VehicleRecord v " +
           "LEFT JOIN FETCH v.dispatcherGroup dg " +
           "LEFT JOIN FETCH dg.dispatcher di " +
           "LEFT JOIN FETCH di.employee")
    List<VehicleRecord> findAllForReminders();

    // Paginated search + filter for vehicle list
    @Query("SELECT v FROM VehicleRecord v WHERE " +
           "(:search = '' OR LOWER(v.plateNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(v.owner) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:technicalCondition = '' OR v.technicalCondition = :technicalCondition) " +
           "AND (:typePps = '' OR v.typePps = :typePps) " +
           "AND (:vehicleStatus IS NULL OR v.vehicleStatus = :vehicleStatus)")
    Page<VehicleRecord> search(
            @Param("search") String search,
            @Param("technicalCondition") String technicalCondition,
            @Param("typePps") String typePps,
            @Param("vehicleStatus") VehicleStatus vehicleStatus,
            Pageable pageable);
}
