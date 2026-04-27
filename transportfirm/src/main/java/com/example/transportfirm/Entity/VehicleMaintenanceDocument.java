package com.example.transportfirm.entity;

import com.example.transportfirm.enums.VehicleMaintenanceDocumentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private VehicleMaintenanceRecord maintenanceRecord;

    @Column(nullable = false)
    private String fileName;

    /** Legacy filesystem path — kept for backward compat. New uploads use fileData. */
    @JsonIgnore
    @Column(nullable = true)
    private String filePath;

    /**
     * File bytes stored in DB — survives Railway redeploys.
     * No @Lob — Hibernate 6 maps @Lob byte[] to OID, incompatible with BYTEA.
     */
    @JsonIgnore
    @Column(name = "file_data", nullable = true, columnDefinition = "bytea")
    private byte[] fileData;

    private VehicleMaintenanceDocumentType docType;
}
