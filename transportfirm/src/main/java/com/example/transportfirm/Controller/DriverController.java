package com.example.transportfirm.controller;

import com.example.transportfirm.entity.DriverDocument;
import com.example.transportfirm.entity.DriverInfo;
import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.Enum.DriverDocumentType;
import com.example.transportfirm.Enum.JobTitle;
import com.example.transportfirm.service.DriverService;
import com.example.transportfirm.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/drivers")
public class DriverController {

    private final DriverService driverService;
    private final EmployeeService employeeService;

    public DriverController(DriverService driverService, EmployeeService employeeService) {
        this.driverService = driverService;
        this.employeeService = employeeService;
    }

    // 🔹 ВРЪЩА EMPLOYEE С ВЪТРЕШЕН driverInfo / mechanicInfo / dispatcherInfo
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Employee> getEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(employeeService.getById(employeeId));
    }

    // 🔹 ВСИЧКИ СЛУЖИТЕЛИ С jobTitle=DRIVER
    @GetMapping("/job/{jobTitle}")
    public ResponseEntity<List<Employee>> getAllDriversFromEmployee(@PathVariable JobTitle jobTitle) {
        return ResponseEntity.ok(employeeService.getByJobTitle(jobTitle));
    }

    // 🔹 UPDATE DriverInfo
    @PutMapping(value = "/{driverInfoId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DriverInfo updateDriver(
            @PathVariable UUID driverInfoId,
            @Valid @RequestBody DriverInfo driver
    ) {
        return driverService.updateDriver(driverInfoId, driver);
    }

    // 🔹 DELETE DriverInfo
    @DeleteMapping("/{driverInfoId}")
    public void deleteDriver(@PathVariable UUID driverInfoId) {
        driverService.deleteDriver(driverInfoId);
    }

    // ---------------- DOCUMENTS ----------------

    // ✅ employeeId вместо driverInfoId
    @GetMapping("/employee/{employeeId}/documents")
    public List<DriverDocument> getDocuments(@PathVariable UUID employeeId) {
        return driverService.getDriverDocumentsByEmployee(employeeId);
    }

    @GetMapping("/documents/{docId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable UUID docId) throws IOException {
        DriverDocument doc = driverService.getDocument(docId);

        java.nio.file.Path path = java.nio.file.Paths.get(doc.getFilePath());
        byte[] fileBytes = java.nio.file.Files.readAllBytes(path);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + doc.getFileName() + "\"")
                .header("Content-Type", "application/pdf")
                .body(fileBytes);
    }

    // ✅ employeeId вместо driverInfoId
    @PostMapping("/employee/{employeeId}/documents")
    public DriverDocument upload(
            @PathVariable UUID employeeId,
            @RequestParam("type") DriverDocumentType type,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return driverService.addDocument(employeeId, type, file);
    }

    @DeleteMapping("/documents/{docId}")
    public void deleteDoc(@PathVariable UUID docId) {
        driverService.deleteDocument(docId);
    }

}

