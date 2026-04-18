package com.example.transportfirm.io;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InvoiceRequest {
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String clientName;
    private String clientAddress;
    private String clientVatNumber;
    private String sellerName;
    private String sellerVatNumber;
    private String sellerAddress;
    private String notes;
    /** Optional status override: DRAFT | ISSUED | PAID | CANCELLED */
    private String status;
}
