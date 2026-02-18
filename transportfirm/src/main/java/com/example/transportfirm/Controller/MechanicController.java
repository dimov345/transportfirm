package com.example.transportfirm.controller;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.MechanicInfo;
import com.example.transportfirm.entity.TruckGroup;
import com.example.transportfirm.repository.EmployeeRepository;
import com.example.transportfirm.service.MechanicService;
import com.example.transportfirm.service.TruckGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/mechanics")
@RequiredArgsConstructor
public class MechanicController {

    private final MechanicService mechanicService;
    private final TruckGroupService truckGroupService;
    private final EmployeeRepository employeeRepository;

    @PostMapping("/create/{employeeId}")
    public MechanicInfo create(@PathVariable UUID employeeId) {
        return mechanicService.createMechanic(employeeId);
    }

    @GetMapping
    public List<MechanicInfo> getAll() {
        return mechanicService.getAll();
    }

    @GetMapping("/{id}")
    public MechanicInfo get(@PathVariable UUID id) {
        return mechanicService.getById(id);
    }

    // GET /mechanics/me/groups
    @GetMapping("/me/groups")
    public List<TruckGroup> myGroups(Principal principal) {
        Employee emp = employeeRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (emp.getMechanicInfo() == null) {
            throw new RuntimeException("This employee is not a mechanic");
        }

        UUID mechanicId = emp.getMechanicInfo().getId();
        return truckGroupService.getMechanicGroups(mechanicId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        mechanicService.deleteMechanic(id);
    }
}
