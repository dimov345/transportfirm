package com.example.transportfirm.entity;

import com.example.transportfirm.Enum.VehicleDocumentType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "vehicle_documents")
@Data
public class VehicleDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", referencedColumnName = "plateNumber", nullable = false)
    private VehicleRecord vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleDocumentType type;

    private String fileName;
    private String filePath;

}
