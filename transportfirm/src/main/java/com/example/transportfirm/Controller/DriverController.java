package com.example.transportfirm.Controller;

import com.example.transportfirm.Entity.DriverDocument;
import com.example.transportfirm.Entity.DriverInfo;
import com.example.transportfirm.Service.DriverService;
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
    public DriverDocument uploadDocument(@PathVariable String egn,
                                         @RequestParam("file") MultipartFile file) throws IOException {
        return service.addDocument(egn, file);
    }

    @GetMapping("/{egn}/documents/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String egn, @PathVariable Long id) throws IOException {
        DriverDocument doc = service.getDocumentById(id);

        Path path = service.getDocumentPath(doc.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @DeleteMapping("/{egn}/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String egn, @PathVariable Long id) {
        service.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
