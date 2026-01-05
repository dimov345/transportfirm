package com.example.transportfirm.controller;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.Enum.JobTitle;
import com.example.transportfirm.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.create(employee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(
            @PathVariable Long id,
            @RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.update(id, employee));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAll() {
        return ResponseEntity.ok(employeeService.getAll());
    }

    @GetMapping("/job/{jobTitle}")
    public ResponseEntity<List<Employee>> getByJobTitle(@PathVariable JobTitle jobTitle) {
        return ResponseEntity.ok(employeeService.getByJobTitle(jobTitle));
    }

    @GetMapping("/egn/{egn}")
    public ResponseEntity<Employee> getByEgn(@PathVariable String egn) {
        return ResponseEntity.ok(employeeService.getByEgn(egn));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
