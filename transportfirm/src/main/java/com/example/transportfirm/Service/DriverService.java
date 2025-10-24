package com.example.transportfirm.Service;

import com.example.transportfirm.Entity.DriverDocument;
import com.example.transportfirm.Entity.DriverInfo;
import com.example.transportfirm.Repository.DriverDocumentRepository;
import com.example.transportfirm.Repository.DriverRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DriverService {
    private final DriverRepository driverRepository;
    private final DriverDocumentRepository driverDocumentRepository;

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
        return driverDocumentRepository.findByEgn(egn);
    }

    /** Добавя нов документ към конкретен шофьор */
    public DriverDocument addDocument(String egn, String fileName, byte[] pdfData) {
        DriverInfo driver = driverRepository.findById(egn)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));

        if (pdfData == null || pdfData.length == 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF data is empty");

        DriverDocument doc = new DriverDocument();
        doc.setDriver(driver);
        doc.setFileName(fileName);
        doc.setPdfData(pdfData);

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
