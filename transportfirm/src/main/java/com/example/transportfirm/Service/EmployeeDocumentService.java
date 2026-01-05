package com.example.transportfirm.service;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.EmployeeDocument;
import com.example.transportfirm.Enum.EmployeeDocumentType;
import com.example.transportfirm.repository.EmployeeDocumentRepository;
import com.example.transportfirm.repository.EmployeeRepository;
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
public class EmployeeDocumentService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeDocumentRepository documentRepository;

    private final String uploadDir = "employee_documents";

    public EmployeeDocumentService(EmployeeRepository employeeRepository,
                                   EmployeeDocumentRepository documentRepository) {
        this.employeeRepository = employeeRepository;
        this.documentRepository = documentRepository;
    }

    public List<EmployeeDocument> getDocuments(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        return documentRepository.findByEmployee_Id(employeeId);
    }

    public EmployeeDocument uploadDocument(Long employeeId,
                                           EmployeeDocumentType type,
                                           MultipartFile file) throws IOException {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed");
        }

        String storedFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path targetDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);

        Path filePath = targetDir.resolve(storedFileName);

        Files.copy(file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING);

        EmployeeDocument doc = new EmployeeDocument();
        doc.setEmployee(employee);
        doc.setType(type);
        doc.setFileName(file.getOriginalFilename());
        doc.setFilePath(filePath.toString());

        return documentRepository.save(doc);
    }

    public EmployeeDocument getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee document not found"));
    }

    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
        }
        documentRepository.deleteById(id);
    }
}
