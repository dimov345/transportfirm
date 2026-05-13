package com.example.transportfirm.entity;

import com.example.transportfirm.enums.DispatcherDocumentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "dispatcher_documents", indexes = {
    @Index(name = "idx_dis_doc_employee", columnList = "employee_id")
})
@Data
public class DispatcherDocument {

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
    private DispatcherDocumentType type;

    @Column(nullable = false)
    private String fileName;

    /** Legacy filesystem path — kept for backward compat. New uploads use fileData. */
    @JsonIgnore
    @Column(nullable = true)
    private String filePath;

    /**
     * PDF bytes stored in DB — survives Railway redeploys.
     * No @Lob — Hibernate 6 maps @Lob byte[] to OID, incompatible with BYTEA.
     */
    @JsonIgnore
    @Column(name = "file_data", nullable = true, columnDefinition = "bytea")
    private byte[] fileData;
}
