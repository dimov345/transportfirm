package com.example.transportfirm.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripRowDto {
    private UUID tripId;
    private String departureDate;
    private String route;
    private String vehiclePlate;
    private UUID vehicleId;
    private String clientName;
    private String status;
    private BigDecimal revenueEur;
    private BigDecimal totalExpensesEur;
    private BigDecimal netProfitEur;
}
