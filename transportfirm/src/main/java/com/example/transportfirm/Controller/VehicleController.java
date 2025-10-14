package com.example.transportfirm.Controller;

import com.example.transportfirm.Entity.VehicleRecord;
import com.example.transportfirm.Service.VehicleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "http://localhost:4200")
public class VehicleController {
    private final VehicleService service;
    public VehicleController(VehicleService service) { this.service = service; }

    @GetMapping public List<VehicleRecord> getAll() { return service.getAll(); }

    @GetMapping("/{plateNumber}") public VehicleRecord getByPlate(@PathVariable String plateNumber) { return service.getByPlate(plateNumber); }

    @PostMapping public VehicleRecord create(@RequestBody VehicleRecord vehicle) { return service.save(vehicle); }

    @PutMapping("/{plateNumber}") public VehicleRecord update(@PathVariable String plateNumber, @RequestBody VehicleRecord vehicle) { return service.update(plateNumber, vehicle); }

    @DeleteMapping("/{plateNumber}") public void delete(@PathVariable String plateNumber) { service.delete(plateNumber); }
}
