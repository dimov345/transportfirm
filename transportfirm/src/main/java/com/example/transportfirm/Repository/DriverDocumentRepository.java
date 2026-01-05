package com.example.transportfirm.repository;

import com.example.transportfirm.entity.DriverDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverDocumentRepository extends JpaRepository<DriverDocument, Long> {
    List<DriverDocument> findByEmployee_Id(Long Id);
}
