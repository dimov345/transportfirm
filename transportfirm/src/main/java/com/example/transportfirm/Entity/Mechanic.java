package com.example.transportfirm.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Entity
@Table(name = "mechanics")
@Data
@EqualsAndHashCode(callSuper = true)
public class Mechanic extends Employees {
    private String specialty;

    @ManyToMany
    @JoinTable(
            name = "mechanic_vehicles",
            joinColumns = @JoinColumn(name = "mechanic_id"),
            inverseJoinColumns = @JoinColumn(name = "vehicle_id")
    )
    private List<VehicleRecord> vehicles;
}
