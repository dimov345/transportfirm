package com.example.transportfirm.controller.auth;

import com.example.transportfirm.entity.TruckGroup;
import com.example.transportfirm.io.auth.CreateEmployeeRequest;
import com.example.transportfirm.repository.vehicle.TruckGroupRepository;
import com.example.transportfirm.service.vehicle.TruckGroupService;
import com.example.transportfirm.service.auth.EmployeeUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EmployeeUserService employeeUserService;

    private final TruckGroupService truckGroupService;

    private final TruckGroupRepository truckGroupRepository;

    @PostMapping("/create-employee")
    @ResponseStatus(HttpStatus.CREATED)
    public void createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        employeeUserService.createEmployeeWithUser(request);
    }

    // POST /admin/groups/dispatcher/{dispatcherId}?name=GroupSofia
    @PostMapping("/groups/dispatcher/{dispatcherId}")
    public TruckGroup createDispatcherGroup(
            @PathVariable UUID dispatcherId,
            @RequestParam("name") String groupName
    ) {
        return truckGroupService.createDispatcherGroup(groupName, dispatcherId);
    }

    // POST /admin/groups/mechanic/{mechanicId}?name=MechanicsA
    @PostMapping("/groups/mechanic/{mechanicId}")
    public TruckGroup createMechanicGroup(
            @PathVariable UUID mechanicId,
            @RequestParam("name") String groupName
    ) {
        return truckGroupService.createMechanicGroup(groupName, mechanicId);
    }

    @GetMapping("/mechanic/{mechanicId}")
    public ResponseEntity<List<TruckGroup>> getByMechanic(@PathVariable UUID mechanicId) {
        return ResponseEntity.ok(truckGroupRepository.findByMechanic_Id(mechanicId));
    }

    @GetMapping("/dispatcher/{dispatcherId}")
    public ResponseEntity<List<TruckGroup>> getByDispatcher(@PathVariable UUID dispatcherId) {
        return ResponseEntity.ok(truckGroupRepository.findByDispatcher_Id(dispatcherId));
    }
}
