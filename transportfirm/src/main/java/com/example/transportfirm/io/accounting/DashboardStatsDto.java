package com.example.transportfirm.io.accounting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDto {
    // ── Quick counts ──────────────────────────────────────────────
    private long activeEmployeesCount;
    private long activeVehiclesCount;
    private int tripsThisMonthCount;

    // ── Current month financials ──────────────────────────────────
    private BigDecimal revenueThisMonthEur;
    private BigDecimal maintenanceCostsThisMonthBgn;

    // ── Current month expense breakdown (for donut chart) ─────────
    private BigDecimal salaryBrutoEur;     // current month total salary → EUR
    private BigDecimal salaryNetoEur;      // current month net salary → EUR
    private BigDecimal salaryTaxesEur;     // current month salary taxes → EUR
    private BigDecimal tripExpensesEur;    // current month trip costs → EUR
    private BigDecimal vatThisMonthEur;    // current month VAT from trips
    private BigDecimal maintenanceEur;     // current month maintenance → EUR

    // ── Document expiry counters ──────────────────────────────────
    private long expiredDocsCount;
    private long expiringDocsCount;

    // ── Last 6 months data (for bar / line charts) ────────────────
    private List<MonthlyStatDto> monthlyStats;
}
