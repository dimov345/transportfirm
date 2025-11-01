package com.example.transportfirm.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Entity
@Table(name = "speditors")
@Data
@EqualsAndHashCode(callSuper = true)
public class Speditor extends Employees {
    @OneToMany(mappedBy = "speditor")
    private List<VehicleRecord> vehicles;
}
