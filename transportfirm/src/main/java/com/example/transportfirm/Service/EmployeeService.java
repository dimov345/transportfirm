package com.example.transportfirm.service;

import com.example.transportfirm.entity.DispatcherInfo;
import com.example.transportfirm.entity.DriverInfo;
import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.Enum.JobTitle;
import com.example.transportfirm.entity.MechanicInfo;
import com.example.transportfirm.repository.DispatcherRepository;
import com.example.transportfirm.repository.DriverRepository;
import com.example.transportfirm.repository.EmployeeRepository;
import com.example.transportfirm.repository.MechanicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DriverRepository driverInfoRepository;
    private final MechanicRepository  mechanicRepository;
    private final DispatcherRepository dispatcherRepository;

    public Employee createEmployee(Employee employee) {
        Employee saved = employeeRepository.save(employee);

        switch (saved.getJobTitle()) {
            case DRIVER -> {
                DriverInfo info = new DriverInfo();
                info.setEmployee(saved);
                driverInfoRepository.save(info);
            }
            case MECHANIC -> {
                MechanicInfo info = new MechanicInfo();
                info.setEmployee(saved);
                mechanicRepository.save(info);
            }
            case DISPATCHER -> {
                DispatcherInfo info = new DispatcherInfo();
                info.setEmployee(saved);
                dispatcherRepository.save(info);
            }
        }

        return saved;
    }

    public Employee update(UUID id, Employee updated) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Забрана за промяна на jobTitle
        if (updated.getJobTitle() != null && existing.getJobTitle() != updated.getJobTitle()) {
            throw new RuntimeException("Job title cannot be changed after creation");
        }

        // Лични данни
        existing.setEgn(updated.getEgn());
        existing.setName(updated.getName());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());

        // Адреси
        existing.setAddressPermanent(updated.getAddressPermanent());
        existing.setAddressCurrent(updated.getAddressCurrent());

        // Фирмени данни (БЕЗ jobTitle)
        existing.setHiredDate(updated.getHiredDate());
        existing.setEmploymentStatus(updated.getEmploymentStatus());
        existing.setFiredDate(updated.getFiredDate());
        existing.setContractType(updated.getContractType());
        existing.setSalary(updated.getSalary());
        existing.setSalaryNeto(updated.getSalaryNeto());        // каза, че си го оправил, но го оставям тук
        existing.setSalaryCurrency(updated.getSalaryCurrency()); // и това
        existing.setWorkingHours(updated.getWorkingHours());

        // Банкови данни
        existing.setBankName(updated.getBankName());
        existing.setIban(updated.getIban());

        // Достъп
        existing.setRole(updated.getRole());

        return employeeRepository.save(existing);
    }



    public void delete(UUID id) {
        employeeRepository.deleteById(id);
    }

    public Employee getById(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    public List<Employee> getByJobTitle(JobTitle jobTitle) {
        return employeeRepository.findByJobTitle(jobTitle);
    }

    public Employee getByEgn(String egn) {
        return employeeRepository.findByEgn(egn)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }
}
