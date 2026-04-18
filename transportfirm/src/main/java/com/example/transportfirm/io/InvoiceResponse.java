package com.example.transportfirm.io;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class InvoiceResponse {
    private UUID id;
    private String invoiceNumber;
    private String status;
    private LocalDate issueDate;
    private LocalDate dueDate;

    // Клиент
    private String clientName;
    private String clientAddress;
    private String clientVatNumber;

    // Продавач
    private String sellerName;
    private String sellerVatNumber;
    private String sellerAddress;

    // Курс (от свързания VehicleFreightTrip)
    private UUID tripId;
    private String tripRoute;           // originCity → destinationCity
    private String vehiclePlate;
    private LocalDate tripDepartureDate;
    private LocalDate tripArrivalDate;
    private Double distanceKm;

    // Финансови данни (от курса)
    private BigDecimal revenueEur;
    private BigDecimal vatEur;
    private BigDecimal tollFeesEur;
    private BigDecimal fuelCostEur;
    private BigDecimal borderFeesEur;
    private BigDecimal parkingAccommodationEur;
    private BigDecimal otherExpensesEur;
    private BigDecimal totalExpensesEur;

    private String notes;
    private LocalDate createdAt;
}
