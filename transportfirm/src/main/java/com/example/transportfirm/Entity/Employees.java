package com.example.transportfirm.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public class Employees {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String egn;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String address;
    private LocalDate hireDate;
    private double salary;

    @Enumerated(EnumType.STRING)
    private Role role; // SPEDITOR, MECHANIC
    private boolean active = true;
}
