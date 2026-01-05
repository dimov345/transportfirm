package com.example.transportfirm.controller;

import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.entity.VehicleDocument;
import com.example.transportfirm.Enum.VehicleDocumentType;
import com.example.transportfirm.service.VehicleService;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "http://localhost:4200")
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

    @GetMapping("/{plateNumber}")
    public VehicleRecord getVehicle(@PathVariable String plateNumber) {
        return vehicleService.getByPlate(plateNumber);
    }

    @PostMapping
    public VehicleRecord createVehicle(@RequestBody VehicleRecord vehicle) {
        return vehicleService.save(vehicle);
    }

    @PutMapping("/{plateNumber}")
    public VehicleRecord updateVehicle(
            @PathVariable String plateNumber,
            @RequestBody VehicleRecord vehicle
    ) {
        return vehicleService.update(plateNumber, vehicle);
    }

    @DeleteMapping("/{plateNumber}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable String plateNumber) {
        vehicleService.delete(plateNumber);
        return ResponseEntity.noContent().build();
    }


    // ===========================================================
    // DOCUMENT LOGIC
    // ===========================================================

    @GetMapping("/{plateNumber}/documents")
    public List<VehicleDocument> getVehicleDocuments(@PathVariable String plateNumber) {
        return vehicleService.getDocumentsByVehicle(plateNumber);
    }


    @PostMapping("/{plateNumber}/documents")
    public VehicleDocument uploadDocument(
            @PathVariable String plateNumber,
            @RequestParam("type") VehicleDocumentType type,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return vehicleService.addDocument(plateNumber, type, file);
    }


    @GetMapping("/documents/{id}/download")
    public ResponseEntity<Resource> downloadVehicleDocument(@PathVariable Long id) {

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
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        vehicleService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
