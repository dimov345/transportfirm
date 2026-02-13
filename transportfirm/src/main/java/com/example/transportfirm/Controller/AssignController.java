package com.example.transportfirm.controller;

import com.example.transportfirm.entity.DriverInfo;
import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.service.AssignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // =========================
    // COMMANDS (write)
    // =========================

    // PUT /api/assignments/drivers/{driverInfoId}/vehicle/{vehicleId}
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
}
