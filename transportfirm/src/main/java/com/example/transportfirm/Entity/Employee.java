package com.example.transportfirm.entity;

import com.example.transportfirm.Enum.ContractType;
import com.example.transportfirm.Enum.EmploymentStatus;
import com.example.transportfirm.Enum.JobTitle;
import com.example.transportfirm.Enum.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Employees_Table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Employee {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

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
    @Column(length = 100, nullable = false, unique = true)
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

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    // Универсален списък документи за ВСЕКИ служител
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("employee-docs")
    private List<EmployeeDocument> documents = new ArrayList<>();

    @OneToOne(mappedBy = "employee")
    @JsonManagedReference("employee-driver")
    private DriverInfo driverInfo;

    @OneToOne(mappedBy = "employee")
    @JsonManagedReference("employee-mechanic")
    private MechanicInfo mechanicInfo;

    @OneToOne(mappedBy = "employee")
    @JsonManagedReference("employee-dispatcher")
    private DispatcherInfo dispatcherInfo;

}
