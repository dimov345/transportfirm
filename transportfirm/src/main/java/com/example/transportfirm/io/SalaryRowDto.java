package com.example.transportfirm.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryRowDto {
    private UUID employeeId;
    private String name;
    private String jobTitle;
    private BigDecimal brutoEur;
    private BigDecimal netoEur;
    private BigDecimal taxesEur;
}
