package com.example.transportfirm.controller;

import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.entity.VehicleDocument;
import com.example.transportfirm.Enum.VehicleDocumentType;
import com.example.transportfirm.service.VehicleService;

import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }


    // ===========================================================
    // VEHICLE CRUD
    // ===========================================================

    @GetMapping
    public List<VehicleRecord> getAllVehicles() {
        return vehicleService.getAll();
    }

    @GetMapping("/{id}")
    public VehicleRecord getVehicle(@PathVariable UUID id) {
        return vehicleService.getByPlate(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public VehicleRecord createVehicle(@RequestBody VehicleRecord vehicle) {
        return vehicleService.save(vehicle);
    }


    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public VehicleRecord updateVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleRecord vehicle
    ) {
        return vehicleService.update(id, vehicle);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable UUID id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }


    // ===========================================================
    // DOCUMENT LOGIC
    // ===========================================================

    @GetMapping("/{id}/documents")
    public List<VehicleDocument> getVehicleDocuments(@PathVariable UUID id) {
        return vehicleService.getDocumentsByVehicle(id);
    }


    @PostMapping("/{id}/documents")
    public VehicleDocument uploadDocument(
            @PathVariable UUID id,
            @RequestParam("type") VehicleDocumentType type,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return vehicleService.addDocument(id, type, file);
    }


    @GetMapping("/documents/{id}/download")
    public ResponseEntity<Resource> downloadVehicleDocument(@PathVariable UUID id) {

        VehicleDocument doc = vehicleService.getDocumentById(id);
        Path filePath = vehicleService.getDocumentPath(doc.getFilePath());

        File file = filePath.toFile();
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        FileSystemResource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }


    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID id) {
        vehicleService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
