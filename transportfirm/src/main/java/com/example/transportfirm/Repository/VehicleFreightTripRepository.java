package com.example.transportfirm.repository;

import com.example.transportfirm.entity.VehicleFreightTrip;
import com.example.transportfirm.enums.TripStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VehicleFreightTripRepository extends JpaRepository<VehicleFreightTrip, UUID> {

    List<VehicleFreightTrip> findByVehicle_IdOrderByDepartureDateDesc(UUID vehicleId);

    @Query("SELECT t FROM VehicleFreightTrip t WHERE " +
           "(:search = '' OR " +
           "  LOWER(t.originCity) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(t.destinationCity) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(t.clientName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(t.vehicle.plateNumber) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:vehicleId IS NULL OR t.vehicle.id = :vehicleId)")
    Page<VehicleFreightTrip> search(
            @Param("search") String search,
            @Param("status") TripStatus status,
            @Param("vehicleId") UUID vehicleId,
            Pageable pageable);

    long countByStatus(TripStatus status);

    boolean existsByVehicle_IdAndStatus(UUID vehicleId, TripStatus status);

    boolean existsByVehicle_IdAndStatusAndIdNot(UUID vehicleId, TripStatus status, UUID excludeId);

    @Query("SELECT t FROM VehicleFreightTrip t JOIN FETCH t.vehicle WHERE t.departureDate BETWEEN :start AND :end ORDER BY t.departureDate DESC")
    List<VehicleFreightTrip> findByDepartureDateBetweenWithVehicle(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
