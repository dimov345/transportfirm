package com.example.transportfirm.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dispatchers_info")
@Data
public class DispatcherInfo {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference("employee-dispatcher")
    private Employee employee;

    @OneToMany(mappedBy = "dispatcher", cascade = CascadeType.ALL)
    private List<TruckGroup> truckGroups = new ArrayList<>();
}
