package com.example.transportfirm.Repository;

import com.example.transportfirm.Entity.Employee;
import com.example.transportfirm.Enum.JobTitle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEgn(String egn);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByUsername(String username);

    List<Employee> findByJobTitle(JobTitle jobTitle);
}
