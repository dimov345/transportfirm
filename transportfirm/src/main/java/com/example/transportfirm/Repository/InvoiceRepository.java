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

    Optional<Invoice> findByTripId(UUID tripId);

    boolean existsByInvoiceNumber(String invoiceNumber);

    /** Counts invoices created in a given year (used for sequential numbering). */
    @Query("SELECT COUNT(i) FROM Invoice i WHERE YEAR(i.issueDate) = :year")
    long countByYear(@Param("year") int year);

    Page<Invoice> findAllByOrderByIssueDateDesc(Pageable pageable);

    Page<Invoice> findByStatusOrderByIssueDateDesc(InvoiceStatus status, Pageable pageable);
}
