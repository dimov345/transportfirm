package com.example.transportfirm.service;

import com.example.transportfirm.entity.DriverDocument;
import com.example.transportfirm.entity.DriverInfo;
import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.enums.DriverDocumentType;
import com.example.transportfirm.repository.DriverDocumentRepository;
import com.example.transportfirm.repository.DriverRepository;
import com.example.transportfirm.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverDocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;

    private final String uploadDir = "driver_documents";


    // =========================
    // DRIVER CRUD
    // =========================


    public DriverInfo updateDriver(UUID driverInfoId, DriverInfo updated) {
        DriverInfo existing = driverRepository.findById(driverInfoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DriverInfo not found"));

        existing.setDriverLicenseIssuedOn(updated.getDriverLicenseIssuedOn());
        existing.setDriverLicenseExpiresOn(updated.getDriverLicenseExpiresOn());
        existing.setQualificationCardIssuedOn(updated.getQualificationCardIssuedOn());
        existing.setQualificationCardExpiresOn(updated.getQualificationCardExpiresOn());
        existing.setPsychologicalExamIssuedOn(updated.getPsychologicalExamIssuedOn());
        existing.setPsychologicalExamExpiresOn(updated.getPsychologicalExamExpiresOn());
        existing.setDigitalCardIssuedOn(updated.getDigitalCardIssuedOn());
        existing.setDigitalCardExpiresOn(updated.getDigitalCardExpiresOn());

        // по желание: ако искаш да позволиш assignment на vehicle
        // existing.setVehicle(updated.getVehicle());

        return driverRepository.save(existing);
    }


    public void deleteDriver(UUID id) {
        driverRepository.deleteById(id);
    }


    // =========================
    // DOCUMENTS
    // =========================
    public List<DriverDocument> getDriverDocumentsByEmployee(UUID employeeId) {
        return documentRepository.findByEmployee_Id(employeeId);
    }

    public DriverDocument getDocument(UUID docId) {
        return documentRepository.findById(docId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    public DriverDocument addDocument(UUID employeeId,
                                      DriverDocumentType type,
                                      MultipartFile file) throws IOException {

        if (file.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");

        if (!"application/pdf".equalsIgnoreCase(file.getContentType()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF allowed");

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        Files.createDirectories(Paths.get(uploadDir));

        String storedName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir).resolve(storedName);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        DriverDocument doc = new DriverDocument();
        doc.setEmployee(employee);
        doc.setType(type);
        doc.setFileName(file.getOriginalFilename());
        doc.setFilePath(path.toString());

        return documentRepository.save(doc);
    }

    public void deleteDocument(UUID id) {
        DriverDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        try {
            Files.deleteIfExists(Path.of(doc.getFilePath()));
        } catch (IOException ignored) {}

        documentRepository.delete(doc);
    }
}
