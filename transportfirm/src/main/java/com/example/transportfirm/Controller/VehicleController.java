package com.example.transportfirm.Controller;

import com.example.transportfirm.Entity.VehicleRecord;
import com.example.transportfirm.Entity.VehicleDocument;
import com.example.transportfirm.Service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "http://localhost:4200")
public class VehicleController {

    private final VehicleService service;

    public VehicleController(VehicleService service) {
        this.service = service;
    }

    // =============================
    // VEHICLE CRUD
    // =============================

    @GetMapping
    public List<VehicleRecord> getAll() {
        return service.getAll();
    }

    @GetMapping("/{plateNumber}")
    public VehicleRecord getByPlateNumber(@PathVariable String plateNumber) {
        return service.getByPlate(plateNumber);
    }

    @PostMapping
    public VehicleRecord create(@Valid @RequestBody VehicleRecord vehicle) {
        return service.save(vehicle);
    }

    @PutMapping("/{plateNumber}")
    public ResponseEntity<VehicleRecord> update(
            @PathVariable String plateNumber,
            @Valid @RequestBody VehicleRecord vehicle) {

        VehicleRecord updated = service.update(plateNumber, vehicle);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{plateNumber}")
    public void delete(@PathVariable String plateNumber) {
        service.delete(plateNumber);
    }

    // =============================
    // VEHICLE DOCUMENTS CRUD
    // =============================

    @GetMapping("/{plateNumber}/documents")
    public List<VehicleDocument> getAllDocuments(@PathVariable String plateNumber) {
        return service.getDocumentsByVehicle(plateNumber);
    }

    @PostMapping(value = "/{plateNumber}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VehicleDocument uploadDocument(
            @PathVariable String plateNumber,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        return service.addDocument(plateNumber, file);
    }

    @GetMapping("/{plateNumber}/documents/{id}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable String plateNumber,
            @PathVariable Long id) throws IOException {

        VehicleDocument doc = service.getDocumentById(id);

        Path path = service.getDocumentPath(doc.getFilepath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @DeleteMapping("/{plateNumber}/documents/{id}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable String plateNumber,
            @PathVariable Long id) {

        service.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
