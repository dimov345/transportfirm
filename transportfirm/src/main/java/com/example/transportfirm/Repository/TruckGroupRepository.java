package com.example.transportfirm.repository;

import com.example.transportfirm.enums.GroupType;
import com.example.transportfirm.entity.TruckGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TruckGroupRepository extends JpaRepository<TruckGroup, UUID> {
    List<TruckGroup> findAllByGroupType(GroupType groupType);

    List<TruckGroup> findAllByDispatcher_Id(UUID dispatcherId);

    List<TruckGroup> findAllByMechanic_Id(UUID mechanicId);

    List<TruckGroup> findByMechanic_Id(UUID mechanicId);

    List<TruckGroup> findByDispatcher_Id(UUID dispatcherId);
}

