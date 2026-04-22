package com.example.transportfirm.entity;

import com.example.transportfirm.enums.DriverDocumentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "driver_documents")
@Data
public class DriverDocument {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverDocumentType type;

    @Column(nullable = false)
    private String fileName;

    /** Legacy filesystem path — kept nullable for backward compat, new uploads use fileData. */
    @JsonIgnore
    @Column(nullable = true)
    private String filePath;

    /**
     * PDF bytes stored in DB — survives Railway redeploys.
     * NOTE: No @Lob on purpose — in Hibernate 6 @Lob on byte[] maps to PostgreSQL OID
     * (large object), which is incompatible with a BYTEA column. Plain byte[] with
     * columnDefinition = "bytea" maps cleanly to BYTEA.
     */
    @JsonIgnore
    @Column(name = "file_data", nullable = true, columnDefinition = "bytea")
    private byte[] fileData;

}
