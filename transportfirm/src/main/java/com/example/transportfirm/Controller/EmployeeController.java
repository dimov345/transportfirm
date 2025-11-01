package com.example.transportfirm.Controller;

import com.example.transportfirm.Entity.*;
import com.example.transportfirm.Service.EmployeesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "http://localhost:4200")
public class EmployeeController {

    private final EmployeesService service;

    public EmployeeController(EmployeesService service) {
        this.service = service;
    }

    // =============================
    // Общ CRUD
    // =============================

    @GetMapping
    public List<Employees> getAll(@RequestParam(required = false) Role role) {
        if (role != null) return service.getByRole(role);
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Employees getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Employees create(@RequestBody Employees employee) {
        return service.create(employee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employees> update(@PathVariable Long id, @RequestBody Employees employee) {
        Employees updated = service.update(id, employee);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // =============================
    // Назначаване на групи
    // =============================

    @PostMapping("/{id}/assign-vehicles")
    public ResponseEntity<?> assignVehicles(@PathVariable Long id, @RequestBody List<String> plateNumbers,
                                            @RequestParam Role role) {
        return role == Role.SPEDITOR
                ? ResponseEntity.ok(service.assignVehiclesToSpeditor(id, plateNumbers))
                : ResponseEntity.ok(service.assignVehiclesToMechanic(id, plateNumbers));
    }
}
