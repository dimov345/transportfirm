package com.example.transportfirm.controller;

import com.example.transportfirm.enums.MaintenanceType;
import com.example.transportfirm.entity.VehicleMaintenanceDocument;
import com.example.transportfirm.entity.VehicleMaintenanceRecord;
import com.example.transportfirm.enums.VehicleMaintenanceDocumentType;
import com.example.transportfirm.service.VehicleMaintenanceDocumentService;
import com.example.transportfirm.service.VehicleMaintenanceService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/maintenance")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'MECHANIC')")
public class VehicleMaintenanceController {

    private final VehicleMaintenanceService maintenanceService;
    private final VehicleMaintenanceDocumentService documentService;

    public VehicleMaintenanceController(
            VehicleMaintenanceService maintenanceService,
            VehicleMaintenanceDocumentService documentService
    ) {
        this.maintenanceService = maintenanceService;
        this.documentService = documentService;
    }

    // --- RECORDS ---

    // GET /maintenance/vehicle/{vehicleId}?page=0&size=10
    @GetMapping("/vehicle/{vehicleId}")
    public Page<VehicleMaintenanceRecord> history(
            @PathVariable UUID vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return maintenanceService.getHistory(vehicleId, PageRequest.of(page, size));
    }

    // GET /maintenance/{recordId}
    @GetMapping("/{recordId}")
    public VehicleMaintenanceRecord getById(@PathVariable UUID recordId) {
        return maintenanceService.getById(recordId);
    }

    // POST /maintenance/create/{vehicleId}?type=REPAIR
    @PostMapping("/create/{vehicleId}")
    public VehicleMaintenanceRecord create(
            @PathVariable UUID vehicleId,
            @RequestParam("type") MaintenanceType type
    ) {
        return maintenanceService.create(vehicleId, type);
    }

    // PUT /maintenance/{recordId}
    // body: VehicleMaintenanceRecord (без DTO)
    @PutMapping("/{recordId}")
    public VehicleMaintenanceRecord update(
            @PathVariable UUID recordId,
            @RequestBody VehicleMaintenanceRecord updated
    ) {
        return maintenanceService.update(recordId, updated);
    }

    // PUT /maintenance/{recordId}/close
    @PutMapping("/{recordId}/close")
    public VehicleMaintenanceRecord close(@PathVariable UUID recordId) {
        return maintenanceService.close(recordId);
    }

    // --- DOCUMENTS ---

    // POST /maintenance/{recordId}/documents/upload?docType=INVOICE
    @PostMapping(value = "/{recordId}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VehicleMaintenanceDocument uploadDocument(
            @PathVariable UUID recordId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "docType", required = false) VehicleMaintenanceDocumentType docType
    ) {
        return documentService.upload(recordId, file, docType);
    }

    // GET /maintenance/{recordId}/documents
    @GetMapping("/{recordId}/documents")
    public List<VehicleMaintenanceDocument> listDocuments(@PathVariable UUID recordId) {
        return documentService.list(recordId);
    }

    // GET /maintenance/documents/{documentId}/download
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID documentId) {
        return documentService.download(documentId);
    }

    // DELETE /maintenance/documents/{documentId}
    @DeleteMapping("/documents/{documentId}")
    public void deleteDocument(@PathVariable UUID documentId) {
        documentService.delete(documentId);
    }


}

