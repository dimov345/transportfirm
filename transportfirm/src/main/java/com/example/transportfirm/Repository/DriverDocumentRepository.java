package com.example.transportfirm.Repository;

import com.example.transportfirm.Entity.DriverDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface DriverDocumentRepository extends JpaRepository<DriverDocument, Long> {
    List<DriverDocument> findByEgn(String egn);
}
