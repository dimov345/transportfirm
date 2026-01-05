package com.example.transportfirm.service;

import com.example.transportfirm.entity.DriverDocument;
import com.example.transportfirm.entity.DriverInfo;
import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.Enum.DriverDocumentType;
import com.example.transportfirm.repository.DriverDocumentRepository;
import com.example.transportfirm.repository.DriverRepository;
import com.example.transportfirm.repository.EmployeeRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverDocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;

    private final String uploadDir = "driver_documents";

    public DriverService(DriverRepository driverRepository,
                         DriverDocumentRepository documentRepository, EmployeeRepository employeeRepository) {
        this.driverRepository = driverRepository;
        this.documentRepository = documentRepository;
        this.employeeRepository = employeeRepository;
    }

    // =========================
    // DRIVER CRUD
    // =========================
    public List<DriverInfo> getAllDrivers() {
        return driverRepository.findAll();
    }

    public DriverInfo getDriver(Long employeeId) {
        return driverRepository.findByEmployeeId(employeeId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "No driverInfo for this employee"));
    }

    public DriverInfo saveDriver(DriverInfo driver) {
        return driverRepository.save(driver);
    }

    public DriverInfo updateDriver(Long employeeId, DriverInfo updated) {
        DriverInfo existing = driverRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DriverInfo not found"));

        // ❗ Променяй директно съществуващия обект
        existing.setDriverLicenseIssuedOn(updated.getDriverLicenseIssuedOn());
        existing.setDriverLicenseExpiresOn(updated.getDriverLicenseExpiresOn());
        existing.setQualificationCardIssuedOn(updated.getQualificationCardIssuedOn());
        existing.setQualificationCardExpiresOn(updated.getQualificationCardExpiresOn());
        existing.setPsychologicalExamIssuedOn(updated.getPsychologicalExamIssuedOn());
        existing.setPsychologicalExamExpiresOn(updated.getPsychologicalExamExpiresOn());
        existing.setDigitalCardIssuedOn(updated.getDigitalCardIssuedOn());
        existing.setDigitalCardExpiresOn(updated.getDigitalCardExpiresOn());

        // ❗ НЕ пипай ID, НЕ замествай employee, НЕ прави save(updated)
        return driverRepository.save(existing);
    }

    public void deleteDriver(Long id) {
        driverRepository.deleteById(id);
    }


    // =========================
    // DOCUMENTS
    // =========================
    public List<DriverDocument> getDriverDocuments(Long Id) {
        return documentRepository.findByEmployee_Id(Id);
    }

    public DriverDocument addDocument(Long employeeId,
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


    public DriverDocument getDocument(Long id) {
        return documentRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
}
