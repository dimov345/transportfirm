package com.example.transportfirm.controller;

import com.example.transportfirm.entity.TruckGroup;
import com.example.transportfirm.service.TruckGroupService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/truck-groups")
@RequiredArgsConstructor
public class TruckGroupController {

    private final TruckGroupService truckGroupService;

    @PostMapping("/create/{mechanicId}")
    public TruckGroup createGroup(@PathVariable Long mechanicId, @RequestBody CreateGroupRequest req) {
        return truckGroupService.createGroup(mechanicId, req.getGroupName());
    }

    @GetMapping
    public List<TruckGroup> getAll() {
        return truckGroupService.getAllGroups();
    }

    @GetMapping("/{id}")
    public TruckGroup get(@PathVariable Long id) {
        return truckGroupService.getGroup(id);
    }

    @PostMapping("/{groupId}/assign/{plateNumber}")
    public TruckGroup assignTruck(@PathVariable Long groupId, @PathVariable String plateNumber) {
        return truckGroupService.assignTruck(groupId, plateNumber);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        truckGroupService.deleteGroup(id);
    }

    @Data
    public static class CreateGroupRequest {
        private String groupName;
    }
}
