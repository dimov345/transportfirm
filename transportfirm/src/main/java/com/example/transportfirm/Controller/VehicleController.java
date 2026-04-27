package com.example.transportfirm.controller;

import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.entity.VehicleDocument;
import com.example.transportfirm.enums.VehicleDocumentType;
import com.example.transportfirm.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vehicles")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DISPATCHER', 'MECHANIC')")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public ResponseEntity<List<VehicleRecord>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleRecord> getVehicle(@PathVariable UUID id) {
        return ResponseEntity.ok(vehicleService.getByPlate(id));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<VehicleRecord>> getVehiclesByGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(vehicleService.getByGroupId(groupId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public VehicleRecord createVehicle(@RequestBody VehicleRecord vehicle) {
        return vehicleService.save(vehicle);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<VehicleRecord> updateVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleRecord vehicle
    ) {
        return ResponseEntity.ok(vehicleService.update(id, vehicle));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void deleteVehicle(@PathVariable UUID id) {
        vehicleService.delete(id);
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<List<VehicleDocument>> getVehicleDocuments(@PathVariable UUID id) {
        return ResponseEntity.ok(vehicleService.getDocumentsByVehicle(id));
    }

    @PostMapping("/{id}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public VehicleDocument uploadDocument(
            @PathVariable UUID id,
            @RequestParam("type") VehicleDocumentType type,
            @RequestParam("file") MultipartFile file
    ) {
        return vehicleService.addDocument(id, type, file);
    }

    @GetMapping("/documents/{id}/download")
    public ResponseEntity<byte[]> downloadVehicleDocument(@PathVariable UUID id) {
        return vehicleService.downloadDocument(id);
    }

    @DeleteMapping("/documents/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void deleteDocument(@PathVariable UUID id) {
        vehicleService.deleteDocument(id);
    }
}
