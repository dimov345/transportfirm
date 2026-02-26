package com.example.transportfirm.service;

import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.entity.VehicleDocument;
import com.example.transportfirm.entity.VehicleMaintenanceRecord;
import com.example.transportfirm.enums.VehicleDocumentType;
import com.example.transportfirm.repository.DriverRepository;
import com.example.transportfirm.repository.VehicleMaintenanceRecordRepository;
import com.example.transportfirm.repository.VehicleRepository;
import com.example.transportfirm.repository.VehicleDocumentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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

    private final String uploadDir = "vehicle_documents";

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

    public List<VehicleRecord> getAll() {
        return vehicleRepository.findAll();
    }

    public List<VehicleRecord> getByGroupId(UUID groupId) {
        return vehicleRepository.findByMechanicGroup_IdOrDispatcherGroup_Id(groupId, groupId);
    }

    public VehicleRecord getByPlate(UUID id) {
        return vehicleRepository.findById(id)
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

        // изтриваме документите на камиона от файловата система
        List<VehicleDocument> docs = documentRepository.findByVehicle_Id(id);
        docs.forEach(doc -> {
            try {
                Files.deleteIfExists(Path.of(doc.getFilePath()));
            } catch (IOException ignored) {}
        });

        documentRepository.deleteAll(docs);
        vehicleRepository.delete(v);
    }


    // ===========================================================
    // DOCUMENTS LOGIC
    // ===========================================================

    public List<VehicleDocument> getDocumentsByVehicle(UUID id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found");
        }
        return documentRepository.findByVehicle_Id(id);
    }

    public VehicleDocument getDocumentById(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    public Path getDocumentPath(String filePath) {
        return Paths.get(filePath);
    }


    // ===========================================================
    // UPLOAD PDF DOCUMENT
    // ===========================================================

    public VehicleDocument addDocument(UUID id,
                                       VehicleDocumentType type,
                                       MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF is allowed");
        }

        VehicleRecord vehicle = vehicleRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));


        Path vehicleFolder = Paths.get(uploadDir, String.valueOf(id))
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(vehicleFolder);

        String uniqueName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path storedPath = vehicleFolder.resolve(uniqueName);

        Files.copy(file.getInputStream(), storedPath, StandardCopyOption.REPLACE_EXISTING);

        VehicleDocument doc = new VehicleDocument();
        doc.setVehicle(vehicle);
        doc.setType(type);
        doc.setFilePath(storedPath.toString());
        doc.setFileName(file.getOriginalFilename());

        return documentRepository.save(doc);
    }


    // ===========================================================
    // DELETE DOCUMENT
    // ===========================================================

    public void deleteDocument(UUID id) {
        VehicleDocument doc = documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        try {
            Files.deleteIfExists(Path.of(doc.getFilePath()));
        } catch (IOException ignored) {}

        documentRepository.delete(doc);
    }
}
