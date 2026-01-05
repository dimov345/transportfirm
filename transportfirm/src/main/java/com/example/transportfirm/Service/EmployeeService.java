package com.example.transportfirm.service;

import com.example.transportfirm.entity.DriverInfo;
import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.Enum.JobTitle;
import com.example.transportfirm.repository.DriverRepository;
import com.example.transportfirm.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DriverRepository driverInfoRepository;

    public Employee create(Employee employee) {

        Employee saved = employeeRepository.save(employee);

        if(saved.getJobTitle() == JobTitle.DRIVER){
            DriverInfo info = new DriverInfo();
            info.setEmployee(saved);
            driverInfoRepository.save(info);
        }

        return saved;
    }

    public Employee update(Long id, Employee updated) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Лични данни
        existing.setEgn(updated.getEgn());
        existing.setName(updated.getName());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());

        // Адреси
        existing.setAddressPermanent(updated.getAddressPermanent());
        existing.setAddressCurrent(updated.getAddressCurrent());

        // Фирмени данни
        existing.setJobTitle(updated.getJobTitle());
        existing.setHiredDate(updated.getHiredDate());
        existing.setEmploymentStatus(updated.getEmploymentStatus());
        existing.setFiredDate(updated.getFiredDate());
        existing.setContractType(updated.getContractType());
        existing.setSalary(updated.getSalary());
        existing.setWorkingHours(updated.getWorkingHours());

        // Банкови данни
        existing.setBankName(updated.getBankName());
        existing.setIban(updated.getIban());

        // Достъп
        existing.setUsername(updated.getUsername());
        existing.setPassword(updated.getPassword());
        existing.setRole(updated.getRole());

        return employeeRepository.save(existing);
    }


    public void delete(Long id) {
        employeeRepository.deleteById(id);
    }

    public Employee getById(Long id) {
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
