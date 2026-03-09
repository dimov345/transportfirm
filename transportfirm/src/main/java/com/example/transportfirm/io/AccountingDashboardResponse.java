package com.example.transportfirm.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountingDashboardResponse {
    private String periodLabel;
    private AccountingSummaryDto summary;
    private List<SalaryRowDto> salaries;
    private List<TripRowDto> trips;
    private List<MaintenanceRowDto> maintenance;
    private List<LeasingRowDto> leasings;
}
