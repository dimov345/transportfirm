package com.example.transportfirm.service;

import com.example.transportfirm.entity.DriverInfo;
import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.repository.DriverRepository;
import com.example.transportfirm.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;


    // =========================
    // ASSIGN / UNASSIGN
    // =========================

    /**
     * Assign vehicle to driver.
     * Правила:
     * - vehicle не може да е зает от друг driver
     * - driver не може да има друга vehicle (строг вариант)
     */
    @Transactional
    public DriverInfo assignVehicleToDriver(UUID driverInfoId, UUID vehicleId) {
        DriverInfo driver = driverRepository.findById(driverInfoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DriverInfo not found"));

        VehicleRecord vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));

        DriverInfo otherDriver = driverRepository.findByVehicle_Id(vehicleId).orElse(null);
        if (otherDriver != null && !otherDriver.getId().equals(driverInfoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle is already assigned to another driver");
        }

        if (driver.getVehicle() != null && !driver.getVehicle().getId().equals(vehicleId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Driver already has a vehicle");
        }

        driver.setVehicle(vehicle);

        vehicle.setDriver(driver);

        return driverRepository.save(driver);
    }

    @Transactional
    public DriverInfo unassignVehicleFromDriver(UUID driverInfoId) {
        DriverInfo driver = driverRepository.findById(driverInfoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DriverInfo not found"));

        VehicleRecord vehicle = driver.getVehicle();
        if (vehicle == null) {
            return driver; // или хвърли 409/400, по избор
        }

        driver.setVehicle(null);
        vehicle.setDriver(null);

        return driverRepository.save(driver);
    }

    // =========================
    // READ (reverse lookups)
    // =========================

    public VehicleRecord getVehicleOfDriver(UUID driverInfoId) {
        DriverInfo driver = driverRepository.findById(driverInfoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DriverInfo not found"));
        return driver.getVehicle();
    }

    public DriverInfo getDriverOfVehicle(UUID vehicleId) {
        return driverRepository.findByVehicle_Id(vehicleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No driver assigned to this vehicle"));
    }

    public List<VehicleRecord> getAvailableVehicles() {
        return vehicleRepository.findByDriverIsNull();
    }
}
