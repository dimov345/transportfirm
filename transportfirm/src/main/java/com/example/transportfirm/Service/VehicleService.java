package com.example.transportfirm.Service;

import com.example.transportfirm.Entity.VehicleRecord;
import com.example.transportfirm.Entity.VehicleDocument;
import com.example.transportfirm.Repository.VehicleRepository;
import com.example.transportfirm.Repository.VehicleDocumentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository repository;
    private final VehicleDocumentRepository documentRepository;

    public VehicleService(VehicleRepository repository,
                          VehicleDocumentRepository documentRepository) {
        this.repository = repository;
        this.documentRepository = documentRepository;
    }

    // =============================
    // VEHICLE CRUD
    // =============================

    public List<VehicleRecord> getAll() {
        return repository.findAll();
    }

    public VehicleRecord getByPlate(String plateNumber) {
        return repository.findById(plateNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
    }

    public VehicleRecord save(VehicleRecord vehicle) {
        return repository.save(vehicle);
    }

    public VehicleRecord update(String plateNumber, VehicleRecord vehicle) {
        if (!repository.existsById(plateNumber)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found");
        }
        vehicle.setPlateNumber(plateNumber);
        return repository.save(vehicle);
    }

    public void delete(String plateNumber) {
        if (!repository.existsById(plateNumber)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found");
        }
        repository.deleteById(plateNumber);
    }

    // =============================
    // VEHICLE DOCUMENTS CRUD
    // =============================

    public List<VehicleDocument> getDocumentsByVehicle(String plateNumber) {
        if (!repository.existsById(plateNumber)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found");
        }
        return documentRepository.findByPlateNumber(plateNumber);
    }

    public VehicleDocument addDocument(String plateNumber, String fileName, byte[] pdfData) {
        VehicleRecord vehicle = repository.findById(plateNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));

        VehicleDocument doc = new VehicleDocument();
        doc.setPlateNumber(plateNumber);
        doc.setFileName(fileName);
        doc.setPdfData(pdfData);
        doc.setVehicle(vehicle);

        return documentRepository.save(doc);
    }

    public VehicleDocument getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
        }
        documentRepository.deleteById(id);
    }
}
