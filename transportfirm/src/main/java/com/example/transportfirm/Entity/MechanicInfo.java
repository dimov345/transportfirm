package com.example.transportfirm.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mechanics_info")
@Data
public class MechanicInfo {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference("employee-mechanic")
    private Employee employee;

    @OneToMany(mappedBy = "mechanic", cascade = CascadeType.ALL)
    private List<TruckGroup> truckGroups = new ArrayList<>();
}
