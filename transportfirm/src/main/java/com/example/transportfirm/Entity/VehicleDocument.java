package com.example.transportfirm.entity;

import com.example.transportfirm.Enum.VehicleDocumentType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "vehicle_documents")
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
    private String filePath;

}
