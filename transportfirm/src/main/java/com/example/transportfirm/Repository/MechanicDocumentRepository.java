package com.example.transportfirm.repository;

import com.example.transportfirm.entity.MechanicDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MechanicDocumentRepository extends JpaRepository<MechanicDocument, UUID> {
    List<MechanicDocument> findByEmployee_Id(UUID employeeId);
}
