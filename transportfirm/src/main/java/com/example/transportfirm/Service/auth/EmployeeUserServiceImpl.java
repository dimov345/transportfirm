package com.example.transportfirm.service.auth;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.User;
import com.example.transportfirm.io.CreateEmployeeRequest;
import com.example.transportfirm.repository.UserRepository;
import com.example.transportfirm.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class EmployeeUserServiceImpl implements EmployeeUserService {

    private final EmployeeService employeeService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional
    public void createEmployeeWithUser(CreateEmployeeRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        Employee employee = new Employee();
        employee.setEgn(request.getEgn());
        employee.setName(request.getName());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setAddressPermanent(request.getAddressPermanent());
        employee.setAddressCurrent(request.getAddressCurrent());
        employee.setContractType(request.getContractType());
        employee.setSalary(request.getSalary());
        employee.setSalaryNeto(request.getSalaryNeto());
        employee.setSalaryCurrency(request.getSalaryCurrency());
        employee.setWorkingHours(request.getWorkingHours());
        employee.setBankName(request.getBankName());
        employee.setIban(request.getIban());
        employee.setJobTitle(request.getJobTitle());
        employee.setHiredDate(request.getHiredDate());
        employee.setRole(request.getRole());

        employee = employeeService.createEmployee(employee);


        String tempPassword = generateTempPassword();

        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        long otpExpiry = System.currentTimeMillis() + (24 * 60 * 60 * 1000);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(employee.getEmail());
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setEmployee(employee);
        user.setIsAccountVerified(false);
        user.setVerifyOtp(otp);
        user.setVerifyOtpExpireAt(otpExpiry);

        userRepository.save(user);

        emailService.sendAdminCreatedUserEmail(
                user.getEmail(),
                user.getUsername(),
                tempPassword,
                otp
        );
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}