package com.example.transportfirm.controller;

import com.example.transportfirm.entity.EmployeeDocument;
import com.example.transportfirm.Enum.EmployeeDocumentType;
import com.example.transportfirm.service.EmployeeDocumentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/employee-documents")
public class EmployeeDocumentController {

    private final EmployeeDocumentService service;

    public EmployeeDocumentController(EmployeeDocumentService service) {
        this.service = service;
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<?> getDocuments(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(service.getDocuments(employeeId));
    }

    @PostMapping(value = "/upload/{employeeId}/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(
            @PathVariable UUID employeeId,
            @PathVariable EmployeeDocumentType type,
            @RequestPart("file") MultipartFile file) throws IOException {

        return ResponseEntity.ok(service.uploadDocument(employeeId, type, file));
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<byte[]> download(@PathVariable UUID documentId) throws IOException {
        EmployeeDocument doc = service.getDocument(documentId);

        byte[] fileBytes = Files.readAllBytes(Path.of(doc.getFilePath()));

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(fileBytes);
    }


    @DeleteMapping("/delete/{documentId}")
    public ResponseEntity<?> delete(@PathVariable UUID documentId) {
        service.deleteDocument(documentId);
        return ResponseEntity.ok("Document deleted");
    }
}
