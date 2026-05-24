package com.example.transportfirm.service.employee;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.EmployeeDocument;
import com.example.transportfirm.enums.EmployeeDocumentType;
import com.example.transportfirm.repository.employee.EmployeeDocumentRepository;
import com.example.transportfirm.repository.employee.EmployeeRepository;
import com.example.transportfirm.util.FileValidationUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class EmployeeDocumentService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeDocumentRepository documentRepository;

    public EmployeeDocumentService(EmployeeRepository employeeRepository,
                                   EmployeeDocumentRepository documentRepository) {
        this.employeeRepository = employeeRepository;
        this.documentRepository = documentRepository;
    }

    public List<EmployeeDocument> getDocuments(UUID employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        return documentRepository.findByEmployee_Id(employeeId);
    }

    public EmployeeDocument uploadDocument(UUID employeeId,
                                           EmployeeDocumentType type,
                                           MultipartFile file) throws IOException {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        FileValidationUtil.validatePdf(file);

        EmployeeDocument doc = new EmployeeDocument();
        doc.setEmployee(employee);
        doc.setType(type);
        doc.setFileName(file.getOriginalFilename());
        doc.setFileData(file.getBytes());   // stored in DB — survives redeploys
        doc.setFilePath("DB");              // satisfies legacy NOT NULL constraint; fileData takes precedence on read

        return documentRepository.save(doc);
    }

    public EmployeeDocument getDocument(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee document not found"));
    }

    /** Returns raw PDF bytes — reads from DB (fileData) or falls back to legacy filePath. */
    public byte[] getDocumentBytes(UUID id) throws IOException {
        EmployeeDocument doc = getDocument(id);
        if (doc.getFileData() != null) return doc.getFileData();
        if (doc.getFilePath() != null) {
            Path path = Path.of(doc.getFilePath());
            if (Files.exists(path)) return Files.readAllBytes(path);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document file not found");
    }

    public void deleteDocument(UUID id) {
        EmployeeDocument doc = getDocument(id);
        // Legacy: clean up filesystem file if present
        if (doc.getFilePath() != null && !doc.getFilePath().equals("DB")) {
            try { Files.deleteIfExists(Path.of(doc.getFilePath())); } catch (IOException ignored) {}
        }
        documentRepository.delete(doc);
    }
}
