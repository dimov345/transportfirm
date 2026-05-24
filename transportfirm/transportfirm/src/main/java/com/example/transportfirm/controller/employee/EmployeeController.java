package com.example.transportfirm.controller.employee;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.enums.JobTitle;
import com.example.transportfirm.repository.employee.EmployeeRepository;
import com.example.transportfirm.service.employee.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    /** GET /employees/me — returns the current user's own Employee record (all authenticated roles). */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Employee> getMe(Principal principal) {
        Employee emp = employeeRepository.findByEmailWithInfos(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        return ResponseEntity.ok(emp);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Employee> update(
            @PathVariable UUID id,
            @RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.update(id, employee));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable UUID id) {
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

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        employeeService.delete(id);
    }
}
