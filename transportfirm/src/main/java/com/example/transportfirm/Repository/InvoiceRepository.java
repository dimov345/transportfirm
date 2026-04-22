package com.example.transportfirm.repository;

import com.example.transportfirm.entity.Invoice;
import com.example.transportfirm.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    /** Plain lookup by trip id (no fetch – used only to check existence). */
    Optional<Invoice> findByTripId(UUID tripId);

    /** Fetches invoice by id with trip + vehicle in one query. */
    @Query("SELECT i FROM Invoice i JOIN FETCH i.trip t LEFT JOIN FETCH t.vehicle WHERE i.id = :id")
    Optional<Invoice> findByIdWithVehicle(@Param("id") UUID id);

    /** Fetches invoice with trip + vehicle in one query – use in toResponse() calls. */
    @Query("SELECT i FROM Invoice i JOIN FETCH i.trip t LEFT JOIN FETCH t.vehicle WHERE t.id = :tripId")
    Optional<Invoice> findByTripIdWithVehicle(@Param("tripId") UUID tripId);

    boolean existsByInvoiceNumber(String invoiceNumber);

    /** Counts invoices created in a given year (used for sequential numbering). */
    @Query("SELECT COUNT(i) FROM Invoice i WHERE YEAR(i.issueDate) = :year")
    long countByYear(@Param("year") int year);

    @Query(value = "SELECT i FROM Invoice i JOIN FETCH i.trip t LEFT JOIN FETCH t.vehicle ORDER BY i.issueDate DESC",
           countQuery = "SELECT COUNT(i) FROM Invoice i")
    Page<Invoice> findAllWithTripOrderByIssueDateDesc(Pageable pageable);

    @Query(value = "SELECT i FROM Invoice i JOIN FETCH i.trip t LEFT JOIN FETCH t.vehicle WHERE i.status = :status ORDER BY i.issueDate DESC",
           countQuery = "SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    Page<Invoice> findByStatusWithTripOrderByIssueDateDesc(@Param("status") InvoiceStatus status, Pageable pageable);

    // kept for backward compatibility
    Page<Invoice> findAllByOrderByIssueDateDesc(Pageable pageable);
    Page<Invoice> findByStatusOrderByIssueDateDesc(InvoiceStatus status, Pageable pageable);
}
