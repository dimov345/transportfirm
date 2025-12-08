package com.example.transportfirm.Service;

import com.example.transportfirm.Entity.MechanicInfo;
import com.example.transportfirm.Entity.TruckGroup;
import com.example.transportfirm.Entity.VehicleRecord;
import com.example.transportfirm.Repository.MechanicRepository;
import com.example.transportfirm.Repository.TruckGroupRepository;
import com.example.transportfirm.Repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TruckGroupService {

    private final TruckGroupRepository truckGroupRepository;
    private final MechanicRepository mechanicRepository;
    private final VehicleRepository vehicleRepository;

    public TruckGroup createGroup(Long mechanicId, String groupName) {

        MechanicInfo mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic not found"));

        TruckGroup group = new TruckGroup();
        group.setGroupName(groupName);
        group.setMechanic(mechanic);

        return truckGroupRepository.save(group);
    }

    public List<TruckGroup> getAllGroups() {
        return truckGroupRepository.findAll();
    }

    public TruckGroup getGroup(Long id) {
        return truckGroupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
    }

    public TruckGroup assignTruck(Long groupId, String plateNumber) {
        TruckGroup group = getGroup(groupId);

        VehicleRecord truck = vehicleRepository.findById(plateNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Truck not found"));

        truck.setTruckGroup(group);
        vehicleRepository.save(truck);

        return group;
    }

    public void deleteGroup(Long id) {
        truckGroupRepository.deleteById(id);
    }
}
