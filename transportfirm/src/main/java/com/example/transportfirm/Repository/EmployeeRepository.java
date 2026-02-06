package com.example.transportfirm.repository;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.Enum.JobTitle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEgn(String egn);

    List<Employee> findByJobTitle(JobTitle jobTitle);
}
