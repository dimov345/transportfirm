package com.example.transportfirm.io.accounting;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class LeasingRowDto {
    private String vehicleId;
    private String vehiclePlate;
    private String leasingCompany;
    private String contractNumber;
    private String startDate;
    private String endDate;
    private BigDecimal monthlyPaymentEur;
    private BigDecimal totalForPeriodEur;
}
