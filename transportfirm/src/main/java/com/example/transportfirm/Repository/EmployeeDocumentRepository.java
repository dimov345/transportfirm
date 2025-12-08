package com.example.transportfirm.Repository;

import com.example.transportfirm.Entity.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {

    List<EmployeeDocument> findByEmployee_Id(Long employeeId);
}
