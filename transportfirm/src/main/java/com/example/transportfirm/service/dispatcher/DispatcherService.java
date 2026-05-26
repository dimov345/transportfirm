package com.example.transportfirm.service.dispatcher;

import com.example.transportfirm.entity.DispatcherInfo;
import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.repository.dispatcher.DispatcherRepository;
import com.example.transportfirm.repository.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DispatcherService {

    private final DispatcherRepository dispatcherRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public DispatcherInfo getById(UUID id) {
        return dispatcherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dispatcher not found"));
    }

    @Transactional(readOnly = true)
    public DispatcherInfo getByEmployeeId(UUID employeeId) {
        return dispatcherRepository.findByEmployee_Id(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DispatcherInfo not found for employee"));
    }

    @Transactional(readOnly = true)
    public DispatcherInfo getByEmployeeEmail(String email) {
        Employee emp = employeeRepository.findByEmailWithInfos(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        if (emp.getDispatcherInfo() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This employee is not a dispatcher");
        }
        return emp.getDispatcherInfo();
    }

    @Transactional(readOnly = true)
    public List<DispatcherInfo> getAll() {
        return dispatcherRepository.findAll();
    }
}
