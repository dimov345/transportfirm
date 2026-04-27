package com.example.transportfirm.controller;

import com.example.transportfirm.entity.DispatcherDocument;
import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.TruckGroup;
import com.example.transportfirm.enums.DispatcherDocumentType;
import com.example.transportfirm.repository.EmployeeRepository;
import com.example.transportfirm.service.DispatcherDocumentService;
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
@RequestMapping("/dispatchers")
@RequiredArgsConstructor
public class DispatcherController {

    private final EmployeeRepository employeeRepository;
    private final TruckGroupService truckGroupService;
    private final DispatcherDocumentService documentService;

    // GET /dispatchers/me/groups — само за логнатия спедитор
    @PreAuthorize("hasRole('DISPATCHER')")
    @GetMapping("/me/groups")
    public List<TruckGroup> myGroups(Principal principal) {
        Employee emp = employeeRepository.findByEmailWithInfos(principal.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (emp.getDispatcherInfo() == null) {
            throw new RuntimeException("This employee is not a dispatcher");
        }

        UUID dispatcherId = emp.getDispatcherInfo().getId();
        return truckGroupService.getDispatcherGroups(dispatcherId);
    }

    // GET /dispatchers/{dispatcherId}/groups — за admin/manager преглед
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{dispatcherId}/groups")
    public List<TruckGroup> getGroupsByDispatcher(@PathVariable UUID dispatcherId) {
        return truckGroupService.getDispatcherGroups(dispatcherId);
    }

    // --- DOCUMENTS ---

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/employee/{employeeId}/documents")
    public List<DispatcherDocument> getDocuments(@PathVariable UUID employeeId) {
        return documentService.getDocuments(employeeId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/employee/{employeeId}/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public DispatcherDocument upload(
            @PathVariable UUID employeeId,
            @RequestParam("type") DispatcherDocumentType type,
            @RequestParam("file") MultipartFile file
    ) {
        return documentService.upload(employeeId, type, file);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/documents/{docId}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID docId) throws IOException {
        DispatcherDocument doc = documentService.getDocument(docId);
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
