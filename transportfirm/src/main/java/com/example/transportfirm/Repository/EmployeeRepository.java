package com.example.transportfirm.Repository;

import com.example.transportfirm.Entity.Employees;
import com.example.transportfirm.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employees, Long> {
    boolean existsByEgn(String egn);
    List<Employees> findByRole(Role role);
}
