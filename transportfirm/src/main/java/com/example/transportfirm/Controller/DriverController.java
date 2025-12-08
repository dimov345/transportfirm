package com.example.transportfirm.Controller;

import com.example.transportfirm.Entity.DriverDocument;
import com.example.transportfirm.Entity.DriverInfo;
import com.example.transportfirm.Entity.Employee;
import com.example.transportfirm.Enum.DriverDocumentType;
import com.example.transportfirm.Enum.JobTitle;
import com.example.transportfirm.Service.DriverService;
import com.example.transportfirm.Service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    private final EmployeeService employeeService;

    public DriverController(DriverService driverService, EmployeeService employeeService) {
        this.driverService = driverService;
        this.employeeService = employeeService;

    }

    // ---------------- DRIVER CRUD ----------------

    @GetMapping
    public List<DriverInfo> getAllDrivers() {
        return driverService.getAllDrivers();
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<?> getDriver(@PathVariable Long employeeId) {
        DriverInfo info = driverService.getDriver(employeeId);

        if (info == null) {
            return ResponseEntity.ok("NO_INFO");
        }

        return ResponseEntity.ok(info);
    }



    @GetMapping("/job/{jobTitle}")
    public ResponseEntity<List<Employee>> getAllDriversFromEmployee(@PathVariable JobTitle jobTitle) {
        return ResponseEntity.ok(employeeService.getByJobTitle(jobTitle));
    }


    @PostMapping
    public DriverInfo createDriver(@RequestBody DriverInfo driver) {
        return driverService.saveDriver(driver);
    }

    @PutMapping("/{id}")
    public DriverInfo updateDriver(@PathVariable Long id, @RequestBody DriverInfo driver) {
        return driverService.updateDriver(id, driver);
    }

    @DeleteMapping("/{id}")
    public void deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id);
    }

    // ---------------- DOCUMENTS ----------------

    @GetMapping("/{id}/documents")
    public List<DriverDocument> getDocuments(@PathVariable Long id) {
        return driverService.getDriverDocuments(id);
    }

    @PostMapping("/{id}/documents")
    public DriverDocument upload(
            @PathVariable Long id,
            @RequestParam("type") DriverDocumentType type,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return driverService.addDocument(id, type, file);
    }

    @GetMapping("/documents/{docId}")
    public DriverDocument getDoc(@PathVariable Long docId) {
        return driverService.getDocument(docId);
    }

    @GetMapping("/documents/{docId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long docId) throws IOException {

        DriverDocument doc = driverService.getDocument(docId);

        java.nio.file.Path path = java.nio.file.Paths.get(doc.getFilePath());
        byte[] fileBytes = java.nio.file.Files.readAllBytes(path);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + doc.getFileName() + "\"")
                .header("Content-Type", "application/pdf")
                .body(fileBytes);
    }


    @DeleteMapping("/documents/{docId}")
    public void deleteDoc(@PathVariable Long docId) {
        driverService.deleteDocument(docId);
    }
}
