package com.example.transportfirm.controller;

import com.example.transportfirm.entity.DriverDocument;
import com.example.transportfirm.entity.DriverInfo;
import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.enums.DriverDocumentType;
import com.example.transportfirm.enums.JobTitle;
import com.example.transportfirm.service.DriverService;
import com.example.transportfirm.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/drivers")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DISPATCHER')")
public class DriverController {

    private final DriverService driverService;
    private final EmployeeService employeeService;

    public DriverController(DriverService driverService, EmployeeService employeeService) {
        this.driverService = driverService;
        this.employeeService = employeeService;
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Employee> getEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(employeeService.getById(employeeId));
    }

    @GetMapping("/job/{jobTitle}")
    public ResponseEntity<List<Employee>> getAllDriversFromEmployee(@PathVariable JobTitle jobTitle) {
        return ResponseEntity.ok(employeeService.getByJobTitle(jobTitle));
    }

    @PutMapping(value = "/{driverInfoId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DriverInfo> updateDriver(
            @PathVariable UUID driverInfoId,
            @Valid @RequestBody DriverInfo driver
    ) {
        return ResponseEntity.ok(driverService.updateDriver(driverInfoId, driver));
    }

    @DeleteMapping("/{driverInfoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDriver(@PathVariable UUID driverInfoId) {
        driverService.deleteDriver(driverInfoId);
    }

    @GetMapping("/employee/{employeeId}/documents")
    public ResponseEntity<List<DriverDocument>> getDocuments(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(driverService.getDriverDocumentsByEmployee(employeeId));
    }

    @GetMapping("/documents/{docId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable UUID docId) throws IOException {
        DriverDocument doc = driverService.getDocument(docId);
        Path path = Paths.get(doc.getFilePath());
        byte[] fileBytes = Files.readAllBytes(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(fileBytes);
    }

    @PostMapping("/employee/{employeeId}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public DriverDocument upload(
            @PathVariable UUID employeeId,
            @RequestParam("type") DriverDocumentType type,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return driverService.addDocument(employeeId, type, file);
    }

    @DeleteMapping("/documents/{docId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDoc(@PathVariable UUID docId) {
        driverService.deleteDocument(docId);
    }
}
