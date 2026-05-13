package com.example.transportfirm.service;

import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.entity.VehicleDocument;
import com.example.transportfirm.entity.VehicleMaintenanceRecord;
import com.example.transportfirm.enums.VehicleDocumentType;
import com.example.transportfirm.enums.VehicleStatus;
import com.example.transportfirm.repository.DriverRepository;
import com.example.transportfirm.repository.VehicleMaintenanceRecordRepository;
import com.example.transportfirm.repository.VehicleRepository;
import com.example.transportfirm.repository.VehicleDocumentRepository;
import com.example.transportfirm.util.FileValidationUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleDocumentRepository documentRepository;
    private final DriverRepository driverRepository;
    private final VehicleMaintenanceRecordRepository maintenanceRecordRepository;


    public VehicleService(VehicleRepository vehicleRepository,
                          VehicleDocumentRepository documentRepository,
                          DriverRepository driverRepository,
                          VehicleMaintenanceRecordRepository maintenanceRecordRepository) {
        this.vehicleRepository = vehicleRepository;
        this.documentRepository = documentRepository;
        this.driverRepository = driverRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
    }


    // ===========================================================
    // VEHICLE CRUD
    // ===========================================================

    @Transactional(readOnly = true)
    public List<VehicleRecord> getAll() {
        return vehicleRepository.findAllWithGroups();
    }

    @Transactional(readOnly = true)
    public List<VehicleRecord> getByGroupId(UUID groupId) {
        return vehicleRepository.findByGroupIdWithGroups(groupId);
    }

    @Transactional(readOnly = true)
    public VehicleRecord getByPlate(UUID id) {
        return vehicleRepository.findByIdWithGroups(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
    }

    public VehicleRecord save(VehicleRecord vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public VehicleRecord update(UUID id, VehicleRecord updated) {
        VehicleRecord existing = vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));

        updated.setPlateNumber(existing.getPlateNumber());
        return vehicleRepository.save(updated);
    }

    @Transactional
    public void delete(UUID id) {
        VehicleRecord v = vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));

        // откачаме шофьора от vehicle-то
        driverRepository.findByVehicle_Id(id).ifPresent(driverInfo -> {
            driverInfo.setVehicle(null);
            driverRepository.save(driverInfo);
        });

        // изтриваме maintenance записите (cascade ще изтрие maintenance документите)
        List<VehicleMaintenanceRecord> maintenanceRecords = maintenanceRecordRepository.findAllByVehicle_Id(id);
        maintenanceRecordRepository.deleteAll(maintenanceRecords);

        // изтриваме документите на камиона (legacy: и от файловата система)
        List<VehicleDocument> docs = documentRepository.findByVehicle_Id(id);
        docs.forEach(doc -> {
            if (doc.getFilePath() != null && !doc.getFilePath().equals("DB")) {
                try { Files.deleteIfExists(Path.of(doc.getFilePath())); } catch (IOException ignored) {}
            }
        });

        documentRepository.deleteAll(docs);
        vehicleRepository.delete(v);
    }


    // ===========================================================
    // DOCUMENTS LOGIC
    // ===========================================================

    @Transactional(readOnly = true)
    public List<VehicleDocument> getDocumentsByVehicle(UUID id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found");
        }
        return documentRepository.findByVehicle_Id(id);
    }

    @Transactional(readOnly = true)
    public VehicleDocument getDocumentById(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    // ===========================================================
    // UPLOAD PDF DOCUMENT
    // ===========================================================

    @Transactional
    public VehicleDocument addDocument(UUID id,
                                       VehicleDocumentType type,
                                       MultipartFile file) {

        VehicleRecord vehicle = vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));

        FileValidationUtil.validatePdf(file);

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf");

        try {
            VehicleDocument doc = new VehicleDocument();
            doc.setVehicle(vehicle);
            doc.setType(type);
            doc.setFileName(originalName);
            doc.setFileData(file.getBytes());  // stored in DB — survives redeploys
            doc.setFilePath("DB");             // satisfies legacy NOT NULL constraint; fileData takes precedence
            return documentRepository.save(doc);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File read failed");
        }
    }


    // ===========================================================
    // DOWNLOAD DOCUMENT
    // ===========================================================

    public ResponseEntity<byte[]> downloadDocument(UUID id) {
        VehicleDocument doc = documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

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

        String safeName = doc.getFileName() != null ? doc.getFileName().replace("\"", "") : "document.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }


    // ===========================================================
    // DELETE DOCUMENT
    // ===========================================================

    @Transactional
    public void deleteDocument(UUID id) {
        VehicleDocument doc = documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        // Legacy: clean up filesystem file if present
        if (doc.getFilePath() != null && !doc.getFilePath().equals("DB")) {
            try { Files.deleteIfExists(Path.of(doc.getFilePath())); } catch (IOException ignored) {}
        }

        documentRepository.delete(doc);
    }
}
