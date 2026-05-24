package com.example.transportfirm.repository.driver;

import com.example.transportfirm.entity.DriverDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DriverDocumentRepository extends JpaRepository<DriverDocument, UUID> {
    List<DriverDocument> findByEmployee_Id(UUID Id);
}
