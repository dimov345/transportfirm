package com.example.transportfirm.controller.accounting;

import com.example.transportfirm.io.accounting.InvoiceRequest;
import com.example.transportfirm.io.accounting.InvoiceResponse;
import com.example.transportfirm.service.accounting.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DISPATCHER', 'ACCOUNTANT')")
public class InvoiceController {

    private final InvoiceService invoiceService;

    /** Генерира или връща съществуваща фактура за курса (идемпотентна). */
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DISPATCHER')")
    @PostMapping("/trip/{tripId}")
    public ResponseEntity<InvoiceResponse> createOrGetForTrip(
            @PathVariable UUID tripId,
            @RequestBody(required = false) InvoiceRequest req) {
        return ResponseEntity.ok(invoiceService.createOrGetForTrip(tripId, req));
    }

    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String status) {
        return ResponseEntity.ok(invoiceService.getAll(page, Math.min(size, 100), status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<InvoiceResponse> getByTripId(@PathVariable UUID tripId) {
        Optional<InvoiceResponse> inv = invoiceService.getByTripId(tripId);
        return inv.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DISPATCHER')")
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> update(
            @PathVariable UUID id,
            @RequestBody InvoiceRequest req) {
        return ResponseEntity.ok(invoiceService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Връща HTML страница с фактурата — може да се отвори в нов таб и отпечата. */
    @GetMapping(value = "/{id}/html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getInvoiceHtml(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "html", java.nio.charset.StandardCharsets.UTF_8))
                .body(invoiceService.buildHtml(id));
    }
}
