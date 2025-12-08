package com.example.transportfirm.Entity;

import com.example.transportfirm.Enum.DriverDocumentType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "driver_documents")
@Data
public class DriverDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
