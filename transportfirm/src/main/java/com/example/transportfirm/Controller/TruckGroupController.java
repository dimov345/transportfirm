package com.example.transportfirm.controller;

import com.example.transportfirm.entity.TruckGroup;
import com.example.transportfirm.service.TruckGroupService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/truck-groups")
@RequiredArgsConstructor
public class TruckGroupController {

    private final TruckGroupService truckGroupService;

    @PostMapping("/create/{mechanicId}")
    public TruckGroup createGroup(@PathVariable UUID mechanicId, @RequestBody CreateGroupRequest req) {
        return truckGroupService.createGroup(mechanicId, req.getGroupName());
    }

    @GetMapping
    public List<TruckGroup> getAll() {
        return truckGroupService.getAllGroups();
    }

    @GetMapping("/{id}")
    public TruckGroup get(@PathVariable UUID id) {
        return truckGroupService.getGroup(id);
    }

    @PostMapping("/{groupId}/assign/{vehicleId}")
    public TruckGroup assignTruck(@PathVariable UUID groupId, @PathVariable UUID id) {
        return truckGroupService.assignTruck(groupId, id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        truckGroupService.deleteGroup(id);
    }

    @Data
    public static class CreateGroupRequest {
        private String groupName;
    }
}
