package com.example.transportfirm.entity;

import com.example.transportfirm.Enum.EmployeeDocumentType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "employee_documents")
@Data
public class EmployeeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private EmployeeDocumentType type;

    private String fileName;
    private String filePath;
}
