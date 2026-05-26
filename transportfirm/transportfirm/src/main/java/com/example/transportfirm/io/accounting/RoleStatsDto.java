package com.example.transportfirm.io.accounting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleStatsDto {
    // DISPATCHER
    private int  dispatcherGroupsCount;
    private int  dispatcherVehiclesCount;
    private long plannedTripsCount;
    private long inTransitTripsCount;
    private long completedTripsThisMonthCount;

    // MECHANIC
    private int  mechanicGroupsCount;
    private int  mechanicVehiclesCount;
    private long openMaintenanceCount;
    private long closedMaintenanceThisMonthCount;

    // DRIVER
    private long   expiredDocsCount;
    private long   expiringDocsCount;
    private String assignedVehiclePlate;
    private Long   daysUntilNextExpiry;
}
