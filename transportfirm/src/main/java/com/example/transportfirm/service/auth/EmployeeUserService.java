package com.example.transportfirm.service.auth;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.User;
import com.example.transportfirm.io.CreateEmployeeRequest;

public interface EmployeeUserService {

    void createEmployeeWithUser(CreateEmployeeRequest request);
}
