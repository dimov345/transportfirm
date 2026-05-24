package com.example.transportfirm.service.mechanic;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.MechanicDocument;
import com.example.transportfirm.enums.MechanicDocumentType;
import com.example.transportfirm.repository.employee.EmployeeRepository;
import com.example.transportfirm.repository.mechanic.MechanicDocumentRepository;
import lombok.RequiredArgsConstructor;
import com.example.transportfirm.util.FileValidationUtil;
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
public class MechanicDocumentService {

    private final MechanicDocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<MechanicDocument> getDocuments(UUID employeeId) {
        if (!employeeRepository.existsById(employeeId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        return documentRepository.findByEmployee_Id(employeeId);
    }

    @Transactional(readOnly = true)
    public MechanicDocument getDocument(UUID docId) {
        return documentRepository.findById(docId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    @Transactional
    public MechanicDocument upload(UUID employeeId, MechanicDocumentType type, MultipartFile file) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        FileValidationUtil.validatePdf(file);

        try {
            MechanicDocument doc = new MechanicDocument();
            doc.setEmployee(employee);
            doc.setType(type);
            doc.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf");
            doc.setFileData(file.getBytes());
            doc.setFilePath("DB");
            return documentRepository.save(doc);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File read failed");
        }
    }

    public byte[] getDocumentBytes(UUID docId) throws IOException {
        MechanicDocument doc = getDocument(docId);
        if (doc.getFileData() != null) return doc.getFileData();
        if (doc.getFilePath() != null && !doc.getFilePath().equals("DB")) {
            Path path = Path.of(doc.getFilePath());
            if (Files.exists(path)) return Files.readAllBytes(path);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document file not found");
    }

    @Transactional
    public void delete(UUID docId) {
        MechanicDocument doc = getDocument(docId);
        if (doc.getFilePath() != null && !doc.getFilePath().equals("DB")) {
            try { Files.deleteIfExists(Path.of(doc.getFilePath())); } catch (IOException ignored) {}
        }
        documentRepository.delete(doc);
    }
}
