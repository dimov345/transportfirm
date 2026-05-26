package com.example.transportfirm.repository.vehicle;

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

    @Query("SELECT t FROM VehicleFreightTrip t JOIN FETCH t.vehicle WHERE t.vehicle.id = :vehicleId ORDER BY t.departureDate DESC")
    List<VehicleFreightTrip> findByVehicle_IdOrderByDepartureDateDesc(@Param("vehicleId") UUID vehicleId);

    /** All trips ordered by date with vehicle pre-fetched — used by getAll(). */
    @Query("SELECT t FROM VehicleFreightTrip t JOIN FETCH t.vehicle ORDER BY t.departureDate DESC")
    List<VehicleFreightTrip> findAllWithVehicle();

    @Query(value =
           "SELECT t FROM VehicleFreightTrip t JOIN FETCH t.vehicle v WHERE " +
           "(:search = '' OR " +
           "  LOWER(t.originCity) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(t.destinationCity) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(t.clientName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(v.plateNumber) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:vehicleId IS NULL OR v.id = :vehicleId)",
           countQuery =
           "SELECT COUNT(t) FROM VehicleFreightTrip t JOIN t.vehicle v WHERE " +
           "(:search = '' OR " +
           "  LOWER(t.originCity) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(t.destinationCity) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(t.clientName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(v.plateNumber) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:vehicleId IS NULL OR v.id = :vehicleId)")
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

    @Query("SELECT COUNT(t) FROM VehicleFreightTrip t WHERE t.status = :status AND t.vehicle.id IN :vehicleIds")
    long countByStatusAndVehicleIdIn(@Param("status") TripStatus status, @Param("vehicleIds") List<UUID> vehicleIds);

    @Query("SELECT COUNT(t) FROM VehicleFreightTrip t WHERE t.status = :status AND t.vehicle.id IN :vehicleIds AND t.departureDate BETWEEN :start AND :end")
    long countCompletedInMonth(@Param("status") TripStatus status, @Param("vehicleIds") List<UUID> vehicleIds, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
