package com.example.transportfirm.Entity;

import com.example.transportfirm.Enum.ContractType;
import com.example.transportfirm.Enum.EmploymentStatus;
import com.example.transportfirm.Enum.JobTitle;
import com.example.transportfirm.Enum.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Employees_Table")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Лични данни
    @Column(length = 10, nullable = false, unique = true)
    @Pattern(regexp = "\\d{10}", message = "EGN трябва да е точно 10 цифри")
    private String egn;

    @NotBlank
    @Column(length = 200, nullable = false)
    private String name;

    @Past
    private LocalDate dateOfBirth;

    @NotBlank
    @Column(length = 20, nullable = false)
    private String phone;

    @Email
    @Column(length = 100, nullable = false)
    private String email;

    // Адреси
    private String addressPermanent;
    private String addressCurrent;

    // Фирмени данни
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobTitle jobTitle;

    @NotNull
    private LocalDate hiredDate;

    @Enumerated(EnumType.STRING)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    private LocalDate firedDate;

    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    private BigDecimal salary;

    private BigDecimal salaryNeto;

    private String salaryCurrency;

    private String workingHours;

    // Банкови данни
    private String bankName;

    @Column(length = 34)
    private String iban;

    // Достъп видими само за admin
    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Универсален списък документи за ВСЕКИ служител
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeDocument> documents = new ArrayList<>();


    @OneToOne(mappedBy = "employee")
    @JsonIgnore
    private DriverInfo driverInfo;

    @OneToOne(mappedBy = "employee")
    private MechanicInfo mechanicInfo;

}
