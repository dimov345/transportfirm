package com.example.transportfirm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dispatchers_info")
@Data
public class DispatcherInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @OneToMany(mappedBy = "dispatcher", cascade = CascadeType.ALL)
    private List<TruckGroup> truckGroups = new ArrayList<>();
}
