package com.example.transportfirm.Controller;

import com.example.transportfirm.Entity.MechanicInfo;
import com.example.transportfirm.Service.MechanicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mechanics")
@RequiredArgsConstructor
public class MechanicController {

    private final MechanicService mechanicService;

    @PostMapping("/create/{employeeId}")
    public MechanicInfo create(@PathVariable Long employeeId) {
        return mechanicService.createMechanic(employeeId);
    }

    @GetMapping
    public List<MechanicInfo> getAll() {
        return mechanicService.getAll();
    }

    @GetMapping("/{id}")
    public MechanicInfo get(@PathVariable Long id) {
        return mechanicService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        mechanicService.deleteMechanic(id);
    }
}
