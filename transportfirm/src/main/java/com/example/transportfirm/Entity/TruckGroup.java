package com.example.transportfirm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "truck_groups")
@Data
public class TruckGroup {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String groupName;

    @ManyToOne
    @JoinColumn(name = "mechanic_id")
    private MechanicInfo mechanic;

    @ManyToOne
    @JoinColumn(name = "dispatcher_id")
    private DispatcherInfo dispatcher;

    @OneToMany(mappedBy = "truckGroup")
    private List<VehicleRecord> vehicles = new ArrayList<>();
}

