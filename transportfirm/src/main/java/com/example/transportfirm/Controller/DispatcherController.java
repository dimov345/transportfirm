package com.example.transportfirm.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.TruckGroup;
import com.example.transportfirm.repository.EmployeeRepository;
import com.example.transportfirm.service.TruckGroupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dispatchers")
public class DispatcherController {

    private final EmployeeRepository employeeRepository;
    private final TruckGroupService truckGroupService;

    public DispatcherController(EmployeeRepository employeeRepository, TruckGroupService truckGroupService) {
        this.employeeRepository = employeeRepository;
        this.truckGroupService = truckGroupService;
    }

    // GET /dispatchers/me/groups — само за логнатия спедитор
    @PreAuthorize("hasRole('DISPATCHER')")
    @GetMapping("/me/groups")
    public List<TruckGroup> myGroups(Principal principal) {
        Employee emp = employeeRepository.findByEmailWithInfos(principal.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (emp.getDispatcherInfo() == null) {
            throw new RuntimeException("This employee is not a dispatcher");
        }

        UUID dispatcherId = emp.getDispatcherInfo().getId();
        return truckGroupService.getDispatcherGroups(dispatcherId);
    }

    // GET /dispatchers/{dispatcherId}/groups — за admin/manager преглед
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{dispatcherId}/groups")
    public List<TruckGroup> getGroupsByDispatcher(@PathVariable UUID dispatcherId) {
        return truckGroupService.getDispatcherGroups(dispatcherId);
    }
}
