package com.example.transportfirm.controller.auth;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.User;
import com.example.transportfirm.enums.ContractType;
import com.example.transportfirm.enums.EmploymentStatus;
import com.example.transportfirm.enums.JobTitle;
import com.example.transportfirm.enums.Role;
import com.example.transportfirm.repository.EmployeeRepository;
import com.example.transportfirm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One-time bootstrap endpoint — creates the first ADMIN if none exists.
 * Self-disables after the first successful call (returns 410 Gone thereafter).
 * Accessible without authentication only when no ADMIN exists in the database.
 */
@RestController
@RequestMapping("/auth/bootstrap")
@RequiredArgsConstructor
public class BootstrapController {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    @Transactional
    public ResponseEntity<String> bootstrap(@RequestBody BootstrapRequest req) {
        // Single efficient existence check — no full table scan
        if (employeeRepository.existsByRole(Role.ADMIN)) {
            // 410 Gone: endpoint is permanently disabled after first use
            return ResponseEntity.status(410).body("Системата вече е инициализирана. Този endpoint е деактивиран.");
        }

        Employee employee = new Employee();
        employee.setEgn(req.egn() != null ? req.egn() : "0000000000");
        employee.setName(req.name());
        employee.setEmail(req.email());
        employee.setPhone(req.phone() != null ? req.phone() : "0000000000");
        employee.setJobTitle(JobTitle.ADMINISTRATOR);
        employee.setRole(Role.ADMIN);
        employee.setHiredDate(LocalDate.now());
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setContractType(ContractType.FULL_TIME);
        employee.setSalary(BigDecimal.ZERO);
        employee.setSalaryNeto(BigDecimal.ZERO);
        employee.setSalaryCurrency("EUR");
        employee.setWorkingHours("8");
        employee.setBankName("-");
        employee.setIban("-");

        employee = employeeRepository.save(employee);

        User user = new User();
        user.setUsername(req.username() != null ? req.username() : req.email());
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setEmployee(employee);
        user.setIsAccountVerified(true);
        user.setFirstLogin(false);

        userRepository.save(user);

        return ResponseEntity.ok("Admin създаден успешно. Endpoint-ът е самодеактивиран — следващи заявки ще получат 410.");
    }

    record BootstrapRequest(String name, String email, String password,
                            String username, String egn, String phone) {}
}
