package com.example.transportfirm.io.auth;

import com.example.transportfirm.enums.ContractType;
import com.example.transportfirm.enums.JobTitle;
import com.example.transportfirm.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateEmployeeRequest {

    // лични
    @NotBlank
    private String egn;

    @NotBlank
    private String name;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    private String phone;

    @Email
    @NotBlank
    private String email;

    // адреси
    private String addressPermanent;
    private String addressCurrent;

    // фирмени
    @NotNull
    private JobTitle jobTitle;

    @NotNull
    private Role role;

    @NotNull
    private LocalDate hiredDate;

    @NotNull
    private ContractType contractType;

    @NotNull
    private BigDecimal salary;

    private BigDecimal salaryNeto;

    @NotBlank
    private String salaryCurrency;

    @NotBlank
    private String workingHours;

    // банкови
    @NotBlank
    private String bankName;

    @NotBlank
    private String iban;

    // auth
    @NotBlank
    private String username;
}
