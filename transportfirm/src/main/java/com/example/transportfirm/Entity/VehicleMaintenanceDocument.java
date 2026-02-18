package com.example.transportfirm.entity;

import com.example.transportfirm.enums.VehicleMaintenanceDocumentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vehicle_maintenance_documents")
public class VehicleMaintenanceDocument {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maintenance_record_id", nullable = false)
    private VehicleMaintenanceRecord maintenanceRecord;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    private VehicleMaintenanceDocumentType docType;
}
