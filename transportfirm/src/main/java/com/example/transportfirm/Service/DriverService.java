package com.example.transportfirm.Service;

import com.example.transportfirm.Entity.DriverDocument;
import com.example.transportfirm.Entity.DriverInfo;
import com.example.transportfirm.Repository.DriverDocumentRepository;
import com.example.transportfirm.Repository.DriverRepository;
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
public class DriverService {
    private final DriverRepository driverRepository;
    private final DriverDocumentRepository driverDocumentRepository;

    private final String uploadDir = "documents";

    public DriverService(DriverRepository driverRepository, DriverDocumentRepository driverDocumentRepository) {
        this.driverRepository = driverRepository;
        this.driverDocumentRepository = driverDocumentRepository;
    }

    // ==========================
    // DRIVER CRUD
    // ==========================

    public List<DriverInfo> getAll() {
        return driverRepository.findAll();
    }

    public DriverInfo getByEgn(String egn) {
        return driverRepository.findById(egn)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));
    }

    public DriverInfo save(DriverInfo driver) {
        return driverRepository.save(driver);
    }

    public DriverInfo update(String egn, DriverInfo driver) {
        if (!driverRepository.existsById(egn))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found");
        driver.setEgn(egn);
        return driverRepository.save(driver);
    }

    public void delete(String egn) {
        if (!driverRepository.existsById(egn))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found");
        driverRepository.deleteById(egn);
    }

    // ==========================
    // DRIVER DOCUMENTS CRUD
    // ==========================

    /** Връща всички документи за даден шофьор по ЕГН */
    public List<DriverDocument> getDocumentsByDriver(String egn) {
        if (!driverRepository.existsById(egn))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found");
        return driverDocumentRepository.findByDriverEgn(egn);
    }

    public Path getDocumentPath(String filePath) {
        return Paths.get(filePath);
    }

    /** Добавя нов документ към конкретен шофьор */
    public DriverDocument addDocument(String egn, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed");
        }

        DriverInfo driver = driverRepository.findById(egn)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));

        // Създаваме уникално име на файла
        String storedFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(targetLocation);
        Path filePath = targetLocation.resolve(storedFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        DriverDocument doc = new DriverDocument();
        doc.setDriver(driver);
        doc.setFileName(file.getOriginalFilename());
        doc.setFilePath(filePath.toString());

        return driverDocumentRepository.save(doc);
    }




    /** Изтрива документ по ID */
    public void deleteDocument(Long id) {
        if (!driverDocumentRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
        driverDocumentRepository.deleteById(id);
    }

    /** Връща документ по ID (например за download) */
    public DriverDocument getDocumentById(Long id) {
        return driverDocumentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }
}
