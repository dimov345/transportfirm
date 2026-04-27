package com.example.transportfirm.entity;

import com.example.transportfirm.enums.EmployeeDocumentType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "employee_documents")
@Data
public class EmployeeDocument {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference("employee-docs")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private EmployeeDocumentType type;

    private String fileName;

    /** Legacy filesystem path — kept for backward compat. New uploads use fileData. */
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
