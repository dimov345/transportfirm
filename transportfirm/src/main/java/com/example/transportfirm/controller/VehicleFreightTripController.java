package com.example.transportfirm.controller;

import com.example.transportfirm.entity.VehicleFreightTrip;
import com.example.transportfirm.service.VehicleFreightTripService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/freight-trips")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DISPATCHER')")
public class VehicleFreightTripController {

    private final VehicleFreightTripService tripService;

    public VehicleFreightTripController(VehicleFreightTripService tripService) {
        this.tripService = tripService;
    }

    // GET /freight-trips?search=&status=&vehicleId=&page=0&size=20
    @GetMapping
    public Page<VehicleFreightTrip> getPaged(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return tripService.getPaged(search, status, vehicleId, page, Math.min(size, 100));
    }

    // GET /freight-trips/all
    @GetMapping("/all")
    public List<VehicleFreightTrip> getAll() {
        return tripService.getAll();
    }

    // GET /freight-trips/{id}
    @GetMapping("/{id}")
    public VehicleFreightTrip getById(@PathVariable UUID id) {
        return tripService.getById(id);
    }

    // GET /freight-trips/vehicle/{vehicleId}
    @GetMapping("/vehicle/{vehicleId}")
    public List<VehicleFreightTrip> getByVehicle(@PathVariable UUID vehicleId) {
        return tripService.getByVehicleId(vehicleId);
    }

    // POST /freight-trips?vehicleId=<uuid>
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleFreightTrip create(
            @RequestParam UUID vehicleId,
            @RequestBody VehicleFreightTrip trip
    ) {
        return tripService.create(trip, vehicleId);
    }

    // PUT /freight-trips/{id}?vehicleId=<uuid>
    @PutMapping("/{id}")
    public VehicleFreightTrip update(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID vehicleId,
            @RequestBody VehicleFreightTrip trip
    ) {
        return tripService.update(id, trip, vehicleId);
    }

    // DELETE /freight-trips/{id} — ADMIN/MANAGER only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        tripService.delete(id);
    }
}
