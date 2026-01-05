package com.example.transportfirm.repository;

import com.example.transportfirm.entity.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {

    List<EmployeeDocument> findByEmployee_Id(Long employeeId);
}
