package com.example.transportfirm.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mechanics_info", indexes = {
    @Index(name = "idx_mechanic_emp_id", columnList = "employee_id")
})
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class MechanicInfo {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonBackReference("employee-mechanic")
    private Employee employee;

    @JsonIgnore
    @OneToMany(mappedBy = "mechanic")
    private List<TruckGroup> mechanicGroups = new ArrayList<>();
}
