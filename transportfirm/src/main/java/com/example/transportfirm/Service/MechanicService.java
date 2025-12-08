package com.example.transportfirm.Service;

import com.example.transportfirm.Entity.Employee;
import com.example.transportfirm.Entity.MechanicInfo;
import com.example.transportfirm.Repository.EmployeeRepository;
import com.example.transportfirm.Repository.MechanicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MechanicService {

    private final MechanicRepository mechanicRepository;
    private final EmployeeRepository employeeRepository;

    public MechanicInfo createMechanic(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        MechanicInfo info = new MechanicInfo();
        info.setEmployee(employee);

        return mechanicRepository.save(info);
    }

    public MechanicInfo getById(Long id) {
        return mechanicRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mechanic not found"));
    }

    public List<MechanicInfo> getAll() {
        return mechanicRepository.findAll();
    }

    public void deleteMechanic(Long id) {
        mechanicRepository.deleteById(id);
    }
}
