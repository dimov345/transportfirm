package com.example.transportfirm.controller;

import com.example.transportfirm.entity.MechanicInfo;
import com.example.transportfirm.service.MechanicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/mechanics")
@RequiredArgsConstructor
public class MechanicController {

    private final MechanicService mechanicService;

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

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        mechanicService.deleteMechanic(id);
    }
}
