package com.example.transportfirm.service;

import com.example.transportfirm.entity.Invoice;
import com.example.transportfirm.entity.VehicleFreightTrip;
import com.example.transportfirm.enums.InvoiceStatus;
import com.example.transportfirm.io.InvoiceRequest;
import com.example.transportfirm.io.InvoiceResponse;
import com.example.transportfirm.repository.InvoiceRepository;
import com.example.transportfirm.repository.VehicleFreightTripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final VehicleFreightTripRepository tripRepository;

    /**
     * Връща съществуваща фактура за курса, или създава нова ако няма.
     * Идемпотентна операция — безопасна за многократно извикване.
     */
    @Transactional
    public InvoiceResponse createOrGetForTrip(UUID tripId, InvoiceRequest req) {
        Optional<Invoice> existing = invoiceRepository.findByTripIdWithVehicle(tripId);
        if (existing.isPresent()) {
            log.debug("Намерена съществуваща фактура {} за курс {}", existing.get().getInvoiceNumber(), tripId);
            return toResponse(existing.get());
        }

        VehicleFreightTrip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NoSuchElementException("Курс не е намерен: " + tripId));

        Invoice invoice = new Invoice();
        invoice.setTrip(trip);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setIssueDate(req != null && req.getIssueDate() != null
                ? req.getIssueDate() : LocalDate.now());
        invoice.setDueDate(req != null && req.getDueDate() != null
                ? req.getDueDate() : invoice.getIssueDate().plusDays(30));
        // Прехвърли клиентски данни от курса ако не са зададени в заявката
        invoice.setClientName(req != null && req.getClientName() != null
                ? req.getClientName() : trip.getClientName());
        if (invoice.getClientAddress() == null) invoice.setClientAddress(trip.getClientAddress());
        if (invoice.getClientVatNumber() == null) invoice.setClientVatNumber(trip.getClientEik());

        // Прехвърли данни на продавача от курса
        if (invoice.getSellerName() == null) invoice.setSellerName(trip.getSellerName());
        if (invoice.getSellerVatNumber() == null) invoice.setSellerVatNumber(trip.getSellerEik());
        if (invoice.getSellerAddress() == null) invoice.setSellerAddress(trip.getSellerAddress());

        if (req != null) {
            // Override само ако изрично е зададено в заявката — не презаписвай с null
            if (req.getClientAddress()   != null) invoice.setClientAddress(req.getClientAddress());
            if (req.getClientVatNumber() != null) invoice.setClientVatNumber(req.getClientVatNumber());
            if (req.getSellerName()      != null) invoice.setSellerName(req.getSellerName());
            if (req.getSellerVatNumber() != null) invoice.setSellerVatNumber(req.getSellerVatNumber());
            if (req.getSellerAddress()   != null) invoice.setSellerAddress(req.getSellerAddress());
            if (req.getNotes()           != null) invoice.setNotes(req.getNotes());
            if (req.getStatus()          != null) invoice.setStatus(InvoiceStatus.valueOf(req.getStatus().toUpperCase()));
        }

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Създадена фактура {} за курс {}", saved.getInvoiceNumber(), tripId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getById(UUID id) {
        return toResponse(invoiceRepository.findByIdWithVehicle(id)
                .orElseThrow(() -> new NoSuchElementException("Фактура не е намерена: " + id)));
    }

    @Transactional(readOnly = true)
    public Optional<InvoiceResponse> getByTripId(UUID tripId) {
        return invoiceRepository.findByTripIdWithVehicle(tripId).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getAll(int page, int size, String status) {
        var pageable = PageRequest.of(page, size);
        if (status != null && !status.isBlank()) {
            InvoiceStatus s = InvoiceStatus.valueOf(status.toUpperCase());
            return invoiceRepository.findByStatusWithTripOrderByIssueDateDesc(s, pageable).map(this::toResponse);
        }
        return invoiceRepository.findAllWithTripOrderByIssueDateDesc(pageable).map(this::toResponse);
    }

    @Transactional
    public InvoiceResponse update(UUID id, InvoiceRequest req) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Фактура не е намерена: " + id));

        if (req.getIssueDate() != null)      invoice.setIssueDate(req.getIssueDate());
        if (req.getDueDate() != null)         invoice.setDueDate(req.getDueDate());
        if (req.getClientName() != null)      invoice.setClientName(req.getClientName());
        if (req.getClientAddress() != null)   invoice.setClientAddress(req.getClientAddress());
        if (req.getClientVatNumber() != null) invoice.setClientVatNumber(req.getClientVatNumber());
        if (req.getSellerName() != null)      invoice.setSellerName(req.getSellerName());
        if (req.getSellerVatNumber() != null) invoice.setSellerVatNumber(req.getSellerVatNumber());
        if (req.getSellerAddress() != null)   invoice.setSellerAddress(req.getSellerAddress());
        if (req.getNotes() != null)           invoice.setNotes(req.getNotes());
        if (req.getStatus() != null)          invoice.setStatus(InvoiceStatus.valueOf(req.getStatus().toUpperCase()));

        return toResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public void delete(UUID id) {
        if (!invoiceRepository.existsById(id)) {
            throw new NoSuchElementException("Фактура не е намерена: " + id);
        }
        invoiceRepository.deleteById(id);
        log.info("Изтрита фактура: {}", id);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String generateInvoiceNumber() {
        int year = LocalDate.now().getYear();
        long count = invoiceRepository.countByYear(year);
        String number;
        int attempt = 0;
        do {
            number = String.format("INV-%d-%04d", year, count + 1 + attempt);
            attempt++;
        } while (invoiceRepository.existsByInvoiceNumber(number) && attempt < 200);
        return number;
    }

    InvoiceResponse toResponse(Invoice inv) {
        InvoiceResponse r = new InvoiceResponse();
        r.setId(inv.getId());
        r.setInvoiceNumber(inv.getInvoiceNumber());
        r.setStatus(inv.getStatus().name());
        r.setIssueDate(inv.getIssueDate());
        r.setDueDate(inv.getDueDate());
        r.setClientName(inv.getClientName());
        r.setClientAddress(inv.getClientAddress());
        r.setClientVatNumber(inv.getClientVatNumber());
        r.setSellerName(inv.getSellerName());
        r.setSellerVatNumber(inv.getSellerVatNumber());
        r.setSellerAddress(inv.getSellerAddress());
        r.setNotes(inv.getNotes());
        r.setCreatedAt(inv.getCreatedAt());

        VehicleFreightTrip trip = inv.getTrip();
        if (trip != null) {
            r.setTripId(trip.getId());
            r.setTripRoute(trip.getOriginCity() + " → " + trip.getDestinationCity());
            r.setTripDepartureDate(trip.getDepartureDate());
            r.setTripArrivalDate(trip.getArrivalDate());
            r.setDistanceKm(trip.getDistanceKm());
            r.setRevenueEur(trip.getRevenueEur());
            r.setVatEur(trip.getVatEur());
            r.setTollFeesEur(trip.getTollFeesEur());
            r.setFuelCostEur(trip.getFuelCostEur());
            r.setBorderFeesEur(trip.getBorderFeesEur());
            r.setParkingAccommodationEur(trip.getParkingAccommodationEur());
            r.setOtherExpensesEur(trip.getOtherExpensesEur());
            r.setTotalExpensesEur(trip.getTotalExpensesEur());
            if (trip.getVehicle() != null) {
                r.setVehiclePlate(trip.getVehicle().getPlateNumber());
            }
            // Ако клиентското име не е зададено в самата фактура, вземи от курса
            if (r.getClientName() == null) {
                r.setClientName(trip.getClientName());
            }
        }
        return r;
    }
}
