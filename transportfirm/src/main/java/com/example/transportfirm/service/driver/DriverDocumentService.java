package com.example.transportfirm.service.driver;

import com.example.transportfirm.entity.DriverDocument;
import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.enums.DriverDocumentType;
import com.example.transportfirm.repository.driver.DriverDocumentRepository;
import com.example.transportfirm.repository.employee.EmployeeRepository;
import com.example.transportfirm.util.FileValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverDocumentService {

    private final DriverDocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<DriverDocument> getByEmployee(UUID employeeId) {
        return documentRepository.findByEmployee_Id(employeeId);
    }

    @Transactional(readOnly = true)
    public DriverDocument getById(UUID docId) {
        return documentRepository.findById(docId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    @Transactional
    public DriverDocument add(UUID employeeId, DriverDocumentType type, MultipartFile file) throws IOException {
        FileValidationUtil.validatePdf(file);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        DriverDocument doc = new DriverDocument();
        doc.setEmployee(employee);
        doc.setType(type);
        doc.setFileName(file.getOriginalFilename());
        doc.setFileData(file.getBytes());
        doc.setFilePath("DB");

        return documentRepository.save(doc);
    }

    public byte[] getBytes(UUID docId) throws IOException {
        DriverDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        if (doc.getFileData() != null) return doc.getFileData();

        if (doc.getFilePath() != null) {
            Path path = Path.of(doc.getFilePath());
            if (Files.exists(path)) return Files.readAllBytes(path);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document file not found");
    }

    @Transactional
    public void delete(UUID docId) {
        DriverDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        if (doc.getFilePath() != null && !doc.getFilePath().equals("DB")) {
            try { Files.deleteIfExists(Path.of(doc.getFilePath())); } catch (IOException ignored) {}
        }

        documentRepository.delete(doc);
    }
}
