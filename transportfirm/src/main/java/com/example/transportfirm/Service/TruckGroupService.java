package com.example.transportfirm.service;

import com.example.transportfirm.enums.GroupType;
import com.example.transportfirm.entity.DispatcherInfo;
import com.example.transportfirm.entity.MechanicInfo;
import com.example.transportfirm.entity.TruckGroup;
import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.repository.DispatcherRepository;
import com.example.transportfirm.repository.MechanicRepository;
import com.example.transportfirm.repository.TruckGroupRepository;
import com.example.transportfirm.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TruckGroupService {

    private final TruckGroupRepository truckGroupRepository;
    private final VehicleRepository vehicleRecordRepository;
    private final DispatcherRepository dispatcherRepository;
    private final MechanicRepository mechanicRepository;

    public TruckGroupService(
            TruckGroupRepository truckGroupRepository,
            VehicleRepository vehicleRecordRepository,
            DispatcherRepository dispatcherRepository,
            MechanicRepository mechanicRepository
    ) {
        this.truckGroupRepository = truckGroupRepository;
        this.vehicleRecordRepository = vehicleRecordRepository;
        this.dispatcherRepository = dispatcherRepository;
        this.mechanicRepository = mechanicRepository;
    }

    public TruckGroup getById(UUID groupId) {
        return truckGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("TruckGroup not found"));
    }

    public List<TruckGroup> getAll() {
        return truckGroupRepository.findAll();
    }

    public List<TruckGroup> getAllByType(GroupType groupType) {
        return truckGroupRepository.findAllByGroupType(groupType);
    }

    @Transactional
    public TruckGroup createDispatcherGroup(String groupName, UUID dispatcherId) {
        DispatcherInfo dispatcher = dispatcherRepository.findById(dispatcherId)
                .orElseThrow(() -> new RuntimeException("Dispatcher not found"));

        TruckGroup group = new TruckGroup();
        group.setGroupName(groupName);
        group.setGroupType(GroupType.DISPATCHER);
        group.setDispatcher(dispatcher);
        group.setMechanic(null);

        return truckGroupRepository.save(group);
    }

    @Transactional
    public TruckGroup createMechanicGroup(String groupName, UUID mechanicId) {
        MechanicInfo mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new RuntimeException("Mechanic not found"));

        TruckGroup group = new TruckGroup();
        group.setGroupName(groupName);
        group.setGroupType(GroupType.MECHANIC);
        group.setMechanic(mechanic);
        group.setDispatcher(null);

        return truckGroupRepository.save(group);
    }

    @Transactional
    public TruckGroup setDispatcherOwner(UUID groupId, UUID dispatcherId) {
        TruckGroup group = getById(groupId);
        if (group.getGroupType() != GroupType.DISPATCHER) {
            throw new RuntimeException("Group type must be DISPATCHER");
        }

        DispatcherInfo dispatcher = dispatcherRepository.findById(dispatcherId)
                .orElseThrow(() -> new RuntimeException("Dispatcher not found"));

        group.setDispatcher(dispatcher);
        group.setMechanic(null);

        return truckGroupRepository.save(group);
    }

    @Transactional
    public TruckGroup setMechanicOwner(UUID groupId, UUID mechanicId) {
        TruckGroup group = getById(groupId);
        if (group.getGroupType() != GroupType.MECHANIC) {
            throw new RuntimeException("Group type must be MECHANIC");
        }

        MechanicInfo mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new RuntimeException("Mechanic not found"));

        group.setMechanic(mechanic);
        group.setDispatcher(null);

        return truckGroupRepository.save(group);
    }

    @Transactional
    public void assignVehicleToDispatcherGroup(UUID groupId, UUID vehicleId) {
        TruckGroup group = getById(groupId);
        if (group.getGroupType() != GroupType.DISPATCHER) {
            throw new RuntimeException("You can assign to dispatcherGroup only if groupType is DISPATCHER");
        }
        if (group.getDispatcher() == null) {
            throw new RuntimeException("Dispatcher group must have dispatcher owner");
        }

        VehicleRecord vehicle = vehicleRecordRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        // правило: 1 vehicle -> 1 dispatcher group (просто overwrite)
        vehicle.setDispatcherGroup(group);
        vehicleRecordRepository.save(vehicle);
    }

    @Transactional
    public void assignVehicleToMechanicGroup(UUID groupId, UUID vehicleId) {
        TruckGroup group = getById(groupId);
        if (group.getGroupType() != GroupType.MECHANIC) {
            throw new RuntimeException("You can assign to mechanicGroup only if groupType is MECHANIC");
        }
        if (group.getMechanic() == null) {
            throw new RuntimeException("Mechanic group must have mechanic owner");
        }

        VehicleRecord vehicle = vehicleRecordRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        // правило: 1 vehicle -> 1 mechanic group (просто overwrite)
        vehicle.setMechanicGroup(group);
        vehicleRecordRepository.save(vehicle);
    }

    @Transactional
    public void removeVehicleFromDispatcherGroup(UUID groupId, UUID vehicleId) {
        TruckGroup group = getById(groupId);
        if (group.getGroupType() != GroupType.DISPATCHER) {
            throw new RuntimeException("Group type must be DISPATCHER");
        }

        VehicleRecord vehicle = vehicleRecordRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (vehicle.getDispatcherGroup() != null && groupId.equals(vehicle.getDispatcherGroup().getId())) {
            vehicle.setDispatcherGroup(null);
            vehicleRecordRepository.save(vehicle);
        }
    }

    @Transactional
    public void removeVehicleFromMechanicGroup(UUID groupId, UUID vehicleId) {
        TruckGroup group = getById(groupId);
        if (group.getGroupType() != GroupType.MECHANIC) {
            throw new RuntimeException("Group type must be MECHANIC");
        }

        VehicleRecord vehicle = vehicleRecordRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (vehicle.getMechanicGroup() != null && groupId.equals(vehicle.getMechanicGroup().getId())) {
            vehicle.setMechanicGroup(null);
            vehicleRecordRepository.save(vehicle);
        }
    }

    public List<TruckGroup> getDispatcherGroups(UUID dispatcherId) {
        return truckGroupRepository.findAllByDispatcher_Id(dispatcherId);
    }

    public List<TruckGroup> getMechanicGroups(UUID mechanicId) {
        return truckGroupRepository.findAllByMechanic_Id(mechanicId);
    }

}
