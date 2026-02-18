package com.example.transportfirm.controller;

import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.entity.VehicleDocument;
import com.example.transportfirm.enums.VehicleDocumentType;
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

    @GetMapping
    public ResponseEntity<List<VehicleRecord>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleRecord> getVehicle(@PathVariable UUID id) {
        return ResponseEntity.ok(vehicleService.getByPlate(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleRecord createVehicle(@RequestBody VehicleRecord vehicle) {
        return vehicleService.save(vehicle);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VehicleRecord> updateVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody VehicleRecord vehicle
    ) {
        return ResponseEntity.ok(vehicleService.update(id, vehicle));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable UUID id) {
        vehicleService.delete(id);
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<List<VehicleDocument>> getVehicleDocuments(@PathVariable UUID id) {
        return ResponseEntity.ok(vehicleService.getDocumentsByVehicle(id));
    }

    @PostMapping("/{id}/documents")
    @ResponseStatus(HttpStatus.CREATED)
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable UUID id) {
        vehicleService.deleteDocument(id);
    }
}
