package com.example.transportfirm.entity;

import com.example.transportfirm.enums.DriverDocumentType;
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
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverDocumentType type;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

}
