package com.example.transportfirm.entity;

import com.example.transportfirm.enums.VehicleDocumentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "vehicle_documents", indexes = {
    @Index(name = "idx_veh_doc_vehicle", columnList = "vehicle_id")
})
@Data
public class VehicleDocument {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private VehicleRecord vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleDocumentType type;

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

}
