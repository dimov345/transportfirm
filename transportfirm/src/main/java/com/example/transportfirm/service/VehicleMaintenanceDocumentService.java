package com.example.transportfirm.service;

import com.example.transportfirm.entity.VehicleMaintenanceDocument;
import com.example.transportfirm.entity.VehicleMaintenanceRecord;
import com.example.transportfirm.enums.VehicleMaintenanceDocumentType;
import com.example.transportfirm.repository.VehicleMaintenanceDocumentRepository;
import com.example.transportfirm.repository.VehicleMaintenanceRecordRepository;
import org.springframework.http.HttpHeaders;
import com.example.transportfirm.util.FileValidationUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class VehicleMaintenanceDocumentService {

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
    public VehicleMaintenanceDocument upload(UUID recordId,
                                             MultipartFile file,
                                             VehicleMaintenanceDocumentType docType) {

        VehicleMaintenanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance record not found"));

        FileValidationUtil.validatePdf(file);

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf");

        try {
            VehicleMaintenanceDocument doc = new VehicleMaintenanceDocument();
            doc.setMaintenanceRecord(record);
            doc.setFileName(originalName);
            doc.setFileData(file.getBytes());  // stored in DB — survives redeploys
            doc.setFilePath("DB");             // satisfies legacy NOT NULL constraint; fileData takes precedence
            doc.setDocType(docType);
            return documentRepository.save(doc);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File read failed");
        }
    }

    public ResponseEntity<byte[]> download(UUID documentId) {

        VehicleMaintenanceDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        byte[] bytes = null;

        // Prefer DB-stored bytes
        if (doc.getFileData() != null) {
            bytes = doc.getFileData();
        } else if (doc.getFilePath() != null && !doc.getFilePath().equals("DB")) {
            // Legacy fallback: file stored on disk before migration
            try {
                Path path = Path.of(doc.getFilePath());
                if (Files.exists(path)) bytes = Files.readAllBytes(path);
            } catch (IOException ignored) {}
        }

        if (bytes == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document file not found");

        String safeName = doc.getFileName().replace("\"", "");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @Transactional
    public void delete(UUID documentId) {
        VehicleMaintenanceDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        // Legacy: clean up filesystem file if present
        if (doc.getFilePath() != null && !doc.getFilePath().equals("DB")) {
            try { Files.deleteIfExists(Path.of(doc.getFilePath())); } catch (IOException ignored) {}
        }

        documentRepository.delete(doc);
    }
}
