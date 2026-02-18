package com.example.transportfirm.service;

import com.example.transportfirm.entity.VehicleMaintenanceDocument;
import com.example.transportfirm.entity.VehicleMaintenanceRecord;
import com.example.transportfirm.enums.VehicleMaintenanceDocumentType;
import com.example.transportfirm.repository.VehicleMaintenanceDocumentRepository;
import com.example.transportfirm.repository.VehicleMaintenanceRecordRepository;
import jakarta.transaction.Transactional;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class VehicleMaintenanceDocumentService {

    private static final String BASE_DIR = "maintenance_documents";

    private final VehicleMaintenanceDocumentRepository documentRepository;
    private final VehicleMaintenanceRecordRepository recordRepository;

    public VehicleMaintenanceDocumentService(
            VehicleMaintenanceDocumentRepository documentRepository,
            VehicleMaintenanceRecordRepository recordRepository
    ) {
        this.documentRepository = documentRepository;
        this.recordRepository = recordRepository;
    }

    public List<VehicleMaintenanceDocument> list(UUID recordId) {
        return documentRepository.findAllByMaintenanceRecord_Id(recordId);
    }

    @Transactional
    public VehicleMaintenanceDocument upload(UUID recordId, MultipartFile file, VehicleMaintenanceDocumentType docType) {

        VehicleMaintenanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Maintenance record not found"));

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        String safeName = UUID.randomUUID() + "_" + originalName.replaceAll("[\\\\/:*?\"<>|]", "_");

        Path dir = Paths.get(BASE_DIR, recordId.toString());
        Path target = dir.resolve(safeName).normalize();

        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("File save failed", e);
        }

        VehicleMaintenanceDocument doc = new VehicleMaintenanceDocument();
        doc.setMaintenanceRecord(record);
        doc.setFileName(originalName);
        doc.setFilePath(target.toString());
        doc.setDocType(docType);

        return documentRepository.save(doc);
    }

    public ResponseEntity<Resource> download(UUID documentId) {

        VehicleMaintenanceDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        Path path = Paths.get(doc.getFilePath()).normalize();

        if (!Files.exists(path)) {
            throw new RuntimeException("File not found on disk");
        }

        Resource resource = new FileSystemResource(path.toFile());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getFileName().replace("\"", "") + "\"")
                .body(resource);
    }

    @Transactional
    public void delete(UUID documentId) {

        VehicleMaintenanceDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        try {
            Files.deleteIfExists(Paths.get(doc.getFilePath()));
        } catch (IOException ignored) {}

        documentRepository.delete(doc);
    }
}
