package com.example.transportfirm.controller;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.MechanicDocument;
import com.example.transportfirm.entity.MechanicInfo;
import com.example.transportfirm.entity.TruckGroup;
import com.example.transportfirm.enums.MechanicDocumentType;
import com.example.transportfirm.repository.EmployeeRepository;
import com.example.transportfirm.service.MechanicDocumentService;
import com.example.transportfirm.service.MechanicService;
import com.example.transportfirm.service.TruckGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/mechanics")
@RequiredArgsConstructor
public class MechanicController {

    private final MechanicService mechanicService;
    private final MechanicDocumentService documentService;
    private final TruckGroupService truckGroupService;
    private final EmployeeRepository employeeRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/create/{employeeId}")
    public MechanicInfo create(@PathVariable UUID employeeId) {
        return mechanicService.createMechanic(employeeId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public List<MechanicInfo> getAll() {
        return mechanicService.getAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public MechanicInfo get(@PathVariable UUID id) {
        return mechanicService.getById(id);
    }

    // GET /mechanics/me/groups — само за логнатия механик
    @PreAuthorize("hasRole('MECHANIC')")
    @GetMapping("/me/groups")
    public List<TruckGroup> myGroups(Principal principal) {
        Employee emp = employeeRepository.findByEmailWithInfos(principal.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (emp.getMechanicInfo() == null) {
            throw new RuntimeException("This employee is not a mechanic");
        }

        UUID mechanicId = emp.getMechanicInfo().getId();
        return truckGroupService.getMechanicGroups(mechanicId);
    }

    // GET /mechanics/{mechanicId}/groups — за admin/manager преглед
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{mechanicId}/groups")
    public List<TruckGroup> getGroupsByMechanic(@PathVariable UUID mechanicId) {
        return truckGroupService.getMechanicGroups(mechanicId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        mechanicService.deleteMechanic(id);
    }

    // --- DOCUMENTS ---

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/employee/{employeeId}/documents")
    public List<MechanicDocument> getDocuments(@PathVariable UUID employeeId) {
        return documentService.getDocuments(employeeId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/employee/{employeeId}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public MechanicDocument upload(
            @PathVariable UUID employeeId,
            @RequestParam("type") MechanicDocumentType type,
            @RequestParam("file") MultipartFile file
    ) {
        return documentService.upload(employeeId, type, file);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/documents/{docId}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID docId) throws IOException {
        MechanicDocument doc = documentService.getDocument(docId);
        byte[] bytes = documentService.getDocumentBytes(docId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/documents/{docId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable UUID docId) {
        documentService.delete(docId);
    }
}
