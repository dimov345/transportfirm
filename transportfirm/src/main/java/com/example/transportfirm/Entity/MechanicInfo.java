package com.example.transportfirm.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mechanics_info")
@Data
public class MechanicInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @OneToMany(mappedBy = "mechanic", cascade = CascadeType.ALL)
    private List<TruckGroup> truckGroups = new ArrayList<>();
}
