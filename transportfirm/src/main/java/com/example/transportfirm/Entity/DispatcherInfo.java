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
@Table(name = "dispatchers_info")
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
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

    @JsonIgnore
    @OneToMany(mappedBy = "dispatcher")
    private List<TruckGroup> dispatcherGroups = new ArrayList<>();
}
