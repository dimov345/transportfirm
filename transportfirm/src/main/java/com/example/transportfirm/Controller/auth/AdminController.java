package com.example.transportfirm.controller.auth;

import com.example.transportfirm.io.CreateEmployeeRequest;
import com.example.transportfirm.service.auth.EmployeeUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EmployeeUserService employeeUserService;

    @PostMapping("/create-employee")
    @ResponseStatus(HttpStatus.CREATED)
    public void createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        employeeUserService.createEmployeeWithUser(request);
    }
}