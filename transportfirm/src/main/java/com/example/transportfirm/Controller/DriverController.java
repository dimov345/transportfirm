package com.example.transportfirm.Controller;

import com.example.transportfirm.Entity.DriverDocument;
import com.example.transportfirm.Entity.DriverInfo;
import com.example.transportfirm.Service.DriverService;
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
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "http://localhost:4200")
public class DriverController {

    private final DriverService service;

    public DriverController(DriverService service) {
        this.service = service;
    }

    // =============================
    // DRIVER CRUD
    // =============================

    @GetMapping
    public List<DriverInfo> getAll() {
        return service.getAll();
    }

    @GetMapping("/{egn}")
    public DriverInfo getByEgn(@PathVariable String egn) {
        return service.getByEgn(egn);
    }

    @PostMapping
    public DriverInfo create(@Valid @RequestBody DriverInfo driver) {
        return service.save(driver);
    }

    @PutMapping("/{egn}")
    public ResponseEntity<DriverInfo> update(@PathVariable String egn,
                                             @Valid @RequestBody DriverInfo driver) {
        DriverInfo updated = service.update(egn, driver);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{egn}")
    public void delete(@PathVariable String egn) {
        service.delete(egn);
    }

    // =============================
    // DRIVER DOCUMENTS CRUD
    // =============================


    @GetMapping("/{egn}/documents")
    public List<DriverDocument> getAllDocuments(@PathVariable String egn) {
        return service.getDocumentsByDriver(egn);
    }


    @PostMapping(value = "/{egn}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DriverDocument uploadDocument(
            @PathVariable String egn,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "File is empty");
        }

        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Only PDF files are allowed");
        }

        return service.addDocument(egn, file.getOriginalFilename(), file.getBytes());
    }


    @GetMapping("/{egn}/documents/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String egn, @PathVariable Long id) {
        DriverDocument document = service.getDocumentById(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getFileName() + "\"")
                .body(document.getPdfData());
    }


    @DeleteMapping("/{egn}/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String egn, @PathVariable Long id) {
        service.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
