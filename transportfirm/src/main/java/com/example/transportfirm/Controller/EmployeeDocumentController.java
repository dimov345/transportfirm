package com.example.transportfirm.controller;

import com.example.transportfirm.entity.EmployeeDocument;
import com.example.transportfirm.Enum.EmployeeDocumentType;
import com.example.transportfirm.service.EmployeeDocumentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/employee-documents")
public class EmployeeDocumentController {

    private final EmployeeDocumentService service;

    public EmployeeDocumentController(EmployeeDocumentService service) {
        this.service = service;
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<List<EmployeeDocument>> getDocuments(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(service.getDocuments(employeeId));
    }

    @PostMapping(value = "/upload/{employeeId}/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeDocument uploadDocument(
            @PathVariable UUID employeeId,
            @PathVariable EmployeeDocumentType type,
            @RequestPart("file") MultipartFile file) throws IOException {
        return service.uploadDocument(employeeId, type, file);
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<byte[]> download(@PathVariable UUID documentId) throws IOException {
        EmployeeDocument doc = service.getDocument(documentId);
        byte[] fileBytes = Files.readAllBytes(Path.of(doc.getFilePath()));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(fileBytes);
    }

    @DeleteMapping("/delete/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID documentId) {
        service.deleteDocument(documentId);
    }
}
