package com.example.transportfirm.entity;

import com.example.transportfirm.enums.GroupType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "dispatcher", "mechanic"})
@Table(name = "truck_groups")
public class TruckGroup {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String groupName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupType groupType;

    // Owner за DISPATCHER група (при MECHANIC ще е null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatcher_id")
    private DispatcherInfo dispatcher;

    // Owner за MECHANIC група (при DISPATCHER ще е null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mechanic_id")
    private MechanicInfo mechanic;

    // Списък камиони за DISPATCHER група
    @JsonIgnore
    @OneToMany(mappedBy = "dispatcherGroup")
    private List<VehicleRecord> dispatcherVehicles = new ArrayList<>();

    // Списък камиони за MECHANIC група
    @JsonIgnore
    @OneToMany(mappedBy = "mechanicGroup")
    private List<VehicleRecord> mechanicVehicles = new ArrayList<>();
}
