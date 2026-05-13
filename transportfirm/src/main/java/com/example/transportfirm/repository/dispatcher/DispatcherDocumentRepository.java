package com.example.transportfirm.repository.dispatcher;

import com.example.transportfirm.entity.DispatcherDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DispatcherDocumentRepository extends JpaRepository<DispatcherDocument, UUID> {
    List<DispatcherDocument> findByEmployee_Id(UUID employeeId);
}
