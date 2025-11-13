package com.example.transportfirm.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "documents")
@Data
public class DriverDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_egn", referencedColumnName = "egn", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DriverInfo driver;

    private String fileName; // Оригинално име на файла
    private String filePath; // Пътя до файла на сървъра
}
