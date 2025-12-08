package com.example.transportfirm.Controller;

import com.example.transportfirm.Entity.EmployeeDocument;
import com.example.transportfirm.Enum.EmployeeDocumentType;
import com.example.transportfirm.Service.EmployeeDocumentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/employee-documents")
public class EmployeeDocumentController {

    private final EmployeeDocumentService service;

    public EmployeeDocumentController(EmployeeDocumentService service) {
        this.service = service;
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<?> getDocuments(@PathVariable Long employeeId) {
        return ResponseEntity.ok(service.getDocuments(employeeId));
    }

    @PostMapping(value = "/upload/{employeeId}/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long employeeId,
            @PathVariable EmployeeDocumentType type,
            @RequestPart("file") MultipartFile file) throws IOException {

        return ResponseEntity.ok(service.uploadDocument(employeeId, type, file));
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<?> download(@PathVariable Long documentId) throws IOException {
        EmployeeDocument doc = service.getDocument(documentId);

        byte[] fileBytes = Files.readAllBytes(Path.of(doc.getFilePath()));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(fileBytes);
    }

    @DeleteMapping("/delete/{documentId}")
    public ResponseEntity<?> delete(@PathVariable Long documentId) {
        service.deleteDocument(documentId);
        return ResponseEntity.ok("Document deleted");
    }
}
