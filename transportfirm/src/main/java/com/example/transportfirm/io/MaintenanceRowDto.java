package com.example.transportfirm.io;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaintenanceRowDto {
    private UUID recordId;
    private UUID vehicleId;
    private String vehiclePlate;
    private String maintenanceType;
    private String workshopName;
    private String openedAt;
    private BigDecimal totalGrossBgn;
    private BigDecimal totalGrossEur;
}
