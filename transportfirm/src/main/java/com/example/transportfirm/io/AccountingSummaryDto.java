package com.example.transportfirm.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountingSummaryDto {
    private BigDecimal tripRevenueEur;
    private BigDecimal totalSalaryBrutoBgn;
    private BigDecimal totalSalaryBrutoEur;
    private BigDecimal tripExpensesEur;
    private BigDecimal maintenanceCostsBgn;
    private BigDecimal maintenanceCostsEur;
    private BigDecimal netBalanceEur;
    private int employeeCount;
    private int tripCount;
    private int maintenanceCount;
    private BigDecimal leasingTotalEur;
    private int leasedVehicleCount;
    private BigDecimal vatTotalEur;
    private BigDecimal salaryTaxesEur;
}
