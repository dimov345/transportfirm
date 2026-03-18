package com.example.transportfirm.entity;

import com.example.transportfirm.enums.TripStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vehicle_freight_trips", indexes = {
    @Index(name = "idx_freight_trip_vehicle",  columnList = "vehicle_id"),
    @Index(name = "idx_freight_trip_status",   columnList = "status"),
    @Index(name = "idx_freight_trip_dep_date", columnList = "departure_date")
})
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VehicleFreightTrip {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnoreProperties({"driver", "documents", "mechanicGroup",
                           "hibernateLazyInitializer", "handler"})
    private VehicleRecord vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status = TripStatus.PLANNED;

    @NotNull(message = "Датата на тръгване е задължителна")
    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "arrival_date")
    private LocalDate arrivalDate;

    @NotBlank(message = "Начална точка е задължителна")
    @Column(length = 200, nullable = false)
    private String originCity;

    @NotBlank(message = "Крайна точка е задължителна")
    @Column(length = 200, nullable = false)
    private String destinationCity;

    private Double distanceKm;

    @Column(length = 200)
    private String clientName;

    // Financials — all in EUR
    @Column(precision = 10, scale = 2)
    private BigDecimal revenueEur;

    @Column(precision = 10, scale = 2)
    private BigDecimal vatEur;

    @Column(precision = 10, scale = 2)
    private BigDecimal tollFeesEur;

    private Double fuelLiters;

    @Column(precision = 10, scale = 2)
    private BigDecimal fuelCostEur;

    @Column(precision = 10, scale = 2)
    private BigDecimal borderFeesEur;

    @Column(precision = 10, scale = 2)
    private BigDecimal parkingAccommodationEur;

    @Column(precision = 10, scale = 2)
    private BigDecimal otherExpensesEur;

    // Cargo
    @Column(length = 500)
    private String cargoDescription;

    private Double cargoWeightTons;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDate createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
    }

    /**
     * Computes total trip expenses in EUR (sum of all cost components).
     * Not persisted — derived from existing columns.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public java.math.BigDecimal getTotalExpensesEur() {
        java.math.BigDecimal zero = java.math.BigDecimal.ZERO;
        return nvl(vatEur)
                .add(nvl(fuelCostEur))
                .add(nvl(tollFeesEur))
                .add(nvl(borderFeesEur))
                .add(nvl(parkingAccommodationEur))
                .add(nvl(otherExpensesEur));
    }

    private static java.math.BigDecimal nvl(java.math.BigDecimal v) {
        return v != null ? v : java.math.BigDecimal.ZERO;
    }
}
