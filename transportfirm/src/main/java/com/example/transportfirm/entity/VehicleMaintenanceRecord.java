package com.example.transportfirm.entity;

import com.example.transportfirm.enums.MaintenanceStatus;
import com.example.transportfirm.enums.MaintenanceType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vehicle_maintenance_records", indexes = {
    @Index(name = "idx_maint_vehicle", columnList = "vehicle_id"),
    @Index(name = "idx_maint_status",  columnList = "status")
})
public class VehicleMaintenanceRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleRecord vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaintenanceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaintenanceStatus status = MaintenanceStatus.OPEN;

    private LocalDateTime openedAt = LocalDateTime.now();
    private LocalDateTime closedAt;

    private Long odometerKmAtService;

    @Column(length = 4000)
    private String description;

    // Данни за сервиз/доставчик (ако е външен)
    private String workshopName;
    private String invoiceNumber;
    private LocalDate invoiceDate;

    // ---- Суми (в една таблица) ----
    // Разделяме части и труд, за да е полезно на фирма (и отчетите)
    @Column(precision = 12, scale = 2)
    private BigDecimal partsNet = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal partsVat = BigDecimal.ZERO;

    // Ако има други такси (пътна помощ, диагностика, др.)
    @Column(precision = 12, scale = 2)
    private BigDecimal otherNet = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal otherVat = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalNet = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalVat = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalGross = BigDecimal.ZERO;

    @PrePersist
    @PreUpdate
    public void recalcTotals() {
        if (partsNet == null) partsNet = BigDecimal.ZERO;
        if (partsVat == null) partsVat = BigDecimal.ZERO;
        if (otherNet == null) otherNet = BigDecimal.ZERO;
        if (otherVat == null) otherVat = BigDecimal.ZERO;

        totalNet = partsNet.add(otherNet);
        totalVat = partsVat.add(otherVat);
        totalGross = totalNet.add(totalVat);
    }

    @JsonIgnore
    @OneToMany(mappedBy = "maintenanceRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<VehicleMaintenanceDocument> documents = new java.util.ArrayList<>();
}
