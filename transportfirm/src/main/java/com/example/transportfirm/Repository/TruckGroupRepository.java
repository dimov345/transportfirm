package com.example.transportfirm.repository;

import com.example.transportfirm.entity.TruckGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TruckGroupRepository extends JpaRepository<TruckGroup, UUID> {
}
