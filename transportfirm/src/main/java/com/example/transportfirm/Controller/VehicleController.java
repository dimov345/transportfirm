package com.example.transportfirm.Controller;

import com.example.transportfirm.Entity.VehicleRecord;
import com.example.transportfirm.Entity.VehicleDocument;
import com.example.transportfirm.Service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
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
    public VehicleRecord getByPlate(@PathVariable String plateNumber) {
        return service.getByPlate(plateNumber);
    }

    @PostMapping
    public VehicleRecord create(@Valid @RequestBody VehicleRecord vehicle) {
        return service.save(vehicle);
    }

    @PutMapping("/{plateNumber}")
    public ResponseEntity<VehicleRecord> update(@PathVariable String plateNumber,
                                                @Valid @RequestBody VehicleRecord vehicle) {
        VehicleRecord updated = service.update(plateNumber, vehicle);
        return ResponseEntity.ok(updated);
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
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "File is empty");
        }

        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Only PDF files are allowed");
        }

        return service.addDocument(plateNumber, file.getOriginalFilename(), file.getBytes());
    }

    @GetMapping("/{plateNumber}/documents/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String plateNumber, @PathVariable Long id) {
        VehicleDocument document = service.getDocumentById(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getFileName() + "\"")
                .body(document.getPdfData());
    }

    @DeleteMapping("/{plateNumber}/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String plateNumber, @PathVariable Long id) {
        service.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
