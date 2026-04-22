package com.example.transportfirm.service;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.MechanicInfo;
import com.example.transportfirm.repository.EmployeeRepository;
import com.example.transportfirm.repository.MechanicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MechanicService {

    private final MechanicRepository mechanicRepository;
    private final EmployeeRepository employeeRepository;

    public MechanicInfo createMechanic(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        MechanicInfo info = new MechanicInfo();
        info.setEmployee(employee);

        return mechanicRepository.save(info);
    }

    @Transactional(readOnly = true)
    public MechanicInfo getById(UUID id) {
        return mechanicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic not found"));
    }

    @Transactional(readOnly = true)
    public List<MechanicInfo> getAll() {
        return mechanicRepository.findAll();
    }

    public void deleteMechanic(UUID id) {
        mechanicRepository.deleteById(id);
    }
}
