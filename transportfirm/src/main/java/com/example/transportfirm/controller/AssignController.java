package com.example.transportfirm.controller;

import com.example.transportfirm.entity.DriverInfo;
import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.service.AssignService;
import com.example.transportfirm.service.TruckGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
public class AssignController {

    private final AssignService assignService;

    private final TruckGroupService truckGroupService;

    // =========================
    // COMMANDS (write)
    // =========================

    // PUT /api/assignments/drivers/{driverInfoId}/vehicle/{vehicleId}
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/drivers/{driverInfoId}/vehicle/{vehicleId}")
    public DriverInfo assignVehicleToDriver(@PathVariable UUID driverInfoId,
                                            @PathVariable UUID vehicleId) {
        try {
            return assignService.assignVehicleToDriver(driverInfoId, vehicleId);
        } catch (NoSuchElementException ex) {
            // driver/vehicle не съществува
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            // напр. vehicle е вече заето
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            // лоши данни
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // DELETE /api/assignments/drivers/{driverInfoId}/vehicle
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/drivers/{driverInfoId}/vehicle")
    public DriverInfo unassignVehicleFromDriver(@PathVariable UUID driverInfoId) {
        try {
            return assignService.unassignVehicleFromDriver(driverInfoId);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            // напр. няма какво да се откачи
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
    }

    // =========================
    // QUERIES (read)
    // =========================

    // GET /api/assignments/drivers/{driverInfoId}/vehicle
    @GetMapping("/drivers/{driverInfoId}/vehicle")
    public ResponseEntity<VehicleRecord> getVehicleOfDriver(@PathVariable UUID driverInfoId) {
        try {
            VehicleRecord vehicle = assignService.getVehicleOfDriver(driverInfoId);

            if (vehicle == null) return ResponseEntity.noContent().build();

            return ResponseEntity.ok(vehicle);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    // GET /api/assignments/vehicles/{vehicleId}/driver
    @GetMapping("/vehicles/{vehicleId}/driver")
    public ResponseEntity<DriverInfo> getDriverOfVehicle(@PathVariable UUID vehicleId) {
        try {
            DriverInfo driver = assignService.getDriverOfVehicle(vehicleId);

            if (driver == null) return ResponseEntity.noContent().build();

            return ResponseEntity.ok(driver);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    // GET /api/assignments/vehicles/available
    @GetMapping("/vehicles/available")
    public List<VehicleRecord> getAvailableVehicles() {
        return assignService.getAvailableVehicles();
    }


    // PUT /assign/dispatcher-group/{groupId}/vehicle/{vehicleId}
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/dispatcher-group/{groupId}/vehicle/{vehicleId}")
    public void assignVehicleToDispatcherGroup(@PathVariable UUID groupId, @PathVariable UUID vehicleId) {
        truckGroupService.assignVehicleToDispatcherGroup(groupId, vehicleId);
    }

    // DELETE /assign/dispatcher-group/{groupId}/vehicle/{vehicleId}
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/dispatcher-group/{groupId}/vehicle/{vehicleId}")
    public void removeVehicleFromDispatcherGroup(@PathVariable UUID groupId, @PathVariable UUID vehicleId) {
        truckGroupService.removeVehicleFromDispatcherGroup(groupId, vehicleId);
    }

    // PUT /assign/mechanic-group/{groupId}/vehicle/{vehicleId}
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/mechanic-group/{groupId}/vehicle/{vehicleId}")
    public void assignVehicleToMechanicGroup(@PathVariable UUID groupId, @PathVariable UUID vehicleId) {
        truckGroupService.assignVehicleToMechanicGroup(groupId, vehicleId);
    }

    // DELETE /assign/mechanic-group/{groupId}/vehicle/{vehicleId}
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/mechanic-group/{groupId}/vehicle/{vehicleId}")
    public void removeVehicleFromMechanicGroup(@PathVariable UUID groupId, @PathVariable UUID vehicleId) {
        truckGroupService.removeVehicleFromMechanicGroup(groupId, vehicleId);
    }
}
