package com.example.transportfirm.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "VehicleDocuments")
@Data
public class VehicleDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plateNumber;
    private String fileName;

    @Lob
    @Column(columnDefinition = "bytea")
    private byte[] pdfData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plateNumber", referencedColumnName = "plateNumber", insertable = false, updatable = false)
    private VehicleRecord vehicle;
}
