package com.example.transportfirm.entity;

import com.example.transportfirm.enums.InvoiceStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_invoice_number", columnList = "invoice_number", unique = true),
    @Index(name = "idx_invoice_trip",   columnList = "trip_id"),
    @Index(name = "idx_invoice_status", columnList = "status")
})
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Invoice {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 30)
    private String invoiceNumber;  // напр. INV-2026-0001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    @JsonIgnoreProperties({"vehicle", "hibernateLazyInitializer", "handler"})
    private VehicleFreightTrip trip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column
    private LocalDate dueDate;  // краен срок за плащане (по подразбиране 30 дни)

    // ── Клиент ──────────────────────────────────────────────────────────────
    @Column(length = 200)
    private String clientName;

    @Column(length = 500)
    private String clientAddress;

    @Column(name = "client_vat_number", length = 30)
    private String clientVatNumber;  // ДДС номер на клиента

    // ── Продавач ────────────────────────────────────────────────────────────
    @Column(name = "seller_name", length = 200)
    private String sellerName;

    @Column(name = "seller_vat_number", length = 30)
    private String sellerVatNumber;  // ДДС номер на фирмата

    @Column(name = "seller_address", length = 500)
    private String sellerAddress;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDate createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = LocalDate.now();
        if (issueDate == null) issueDate = LocalDate.now();
        if (dueDate == null && issueDate != null) dueDate = issueDate.plusDays(30);
    }
}
