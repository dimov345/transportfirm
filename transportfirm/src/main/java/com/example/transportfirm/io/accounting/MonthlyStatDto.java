package com.example.transportfirm.io.accounting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyStatDto {
    private String monthLabel;
    private double revenueEur;
    private double tripExpensesEur;
    private double netProfitEur;
    private int tripCount;
}
