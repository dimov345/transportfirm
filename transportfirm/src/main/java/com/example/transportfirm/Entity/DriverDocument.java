package com.example.transportfirm.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Documents")
@Data
public class DriverDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, nullable = false)
    private String egn;

    private String fileName;

    @Lob
    @Column(columnDefinition = "bytea")
    private byte[] pdfData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "egn", referencedColumnName = "egn", insertable = false, updatable = false)
    private DriverInfo driver;
}
