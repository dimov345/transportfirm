package com.example.transportfirm.Service;

import com.example.transportfirm.Entity.VehicleRecord;
import com.example.transportfirm.Entity.VehicleDocument;
import com.example.transportfirm.Repository.VehicleRepository;
import com.example.transportfirm.Repository.VehicleDocumentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleDocumentRepository documentRepository;

    private final String uploadDir = "vehicle_documents";

    public VehicleService(VehicleRepository vehicleRepository,
                          VehicleDocumentRepository documentRepository) {
        this.vehicleRepository = vehicleRepository;
        this.documentRepository = documentRepository;
    }

    // =============================
    // VEHICLE CRUD
    // =============================

    public List<VehicleRecord> getAll() {
        return vehicleRepository.findAll();
    }

    public VehicleRecord getByPlate(String plateNumber) {
        return vehicleRepository.findById(plateNumber)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
    }

    public VehicleRecord save(VehicleRecord vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public VehicleRecord update(String plateNumber, VehicleRecord vehicle) {
        if (!vehicleRepository.existsById(plateNumber)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found");
        }
        vehicle.setPlateNumber(plateNumber);
        return vehicleRepository.save(vehicle);
    }

    public void delete(String plateNumber) {
        if (!vehicleRepository.existsById(plateNumber)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found");
        }
        vehicleRepository.deleteById(plateNumber);
    }

    // =============================
    // VEHICLE DOCUMENTS CRUD
    // =============================

    public List<VehicleDocument> getDocumentsByVehicle(String plateNumber) {
        if (!vehicleRepository.existsById(plateNumber)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found");
        }
        return documentRepository.findByVehicle_PlateNumber(plateNumber);
    }

    public Path getDocumentPath(String filePath) {
        return Paths.get(filePath);
    }

    /** Абсолютно същата логика като при DriverService */
    public VehicleDocument addDocument(String plateNumber, MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed");
        }

        VehicleRecord vehicle = vehicleRepository.findById(plateNumber)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));

        // Генерираме уникално име
        String storedFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path targetLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(targetLocation);

        Path filePath = targetLocation.resolve(storedFileName);

        // Качване
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Създаваме entity
        VehicleDocument doc = new VehicleDocument();
        doc.setVehicle(vehicle);
        doc.setFileName(file.getOriginalFilename());
        doc.setFilepath(filePath.toString());

        return documentRepository.save(doc);
    }

    public VehicleDocument getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle document not found"));
    }

    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
        }
        documentRepository.deleteById(id);
    }
}
