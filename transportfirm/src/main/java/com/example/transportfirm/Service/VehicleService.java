package com.example.transportfirm.Service;

import com.example.transportfirm.Entity.VehicleRecord;
import com.example.transportfirm.Repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {
    private final VehicleRepository repository;
    public VehicleService(VehicleRepository repository) { this.repository = repository; }

    public List<VehicleRecord> getAll() { return repository.findAll(); }

    public VehicleRecord getByPlate(String plateNumber) {
        return repository.findById(plateNumber).orElseThrow(() -> new RuntimeException("Vehicle not found"));
    }
    public VehicleRecord save(VehicleRecord vehicle) { return repository.save(vehicle); }

    public VehicleRecord update(String plateNumber, VehicleRecord vehicle) {
        if (!repository.existsById(plateNumber)) throw new RuntimeException("Vehicle not found");
        vehicle.setPlateNumber(plateNumber);
        return repository.save(vehicle);
    }
    public void delete(String plateNumber) { repository.deleteById(plateNumber); }
}

