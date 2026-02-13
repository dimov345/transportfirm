package com.example.transportfirm.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "drivers_info")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class DriverInfo {

    @Id
    @GeneratedValue
    @JsonIgnoreProperties({"driverInfo", "mechanicInfo", "documents", "hibernateLazyInitializer", "handler"})
    @Column(columnDefinition = "uuid")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnoreProperties({
            "driverInfo", "mechanicInfo", "dispatcherInfo", "documents", "user",
            "hibernateLazyInitializer", "handler"
    })
    private Employee employee;

    @OneToOne
    @JoinColumn(name = "vehicle_id", unique = true)
    @JsonIgnoreProperties({
            "driver", "documents", "truckGroup",
            "hibernateLazyInitializer", "handler"
    })
    private VehicleRecord vehicle;

    // Дати на документи
    private LocalDate driverLicenseIssuedOn;
    private LocalDate driverLicenseExpiresOn;

    private LocalDate qualificationCardIssuedOn;
    private LocalDate qualificationCardExpiresOn;

    private LocalDate psychologicalExamIssuedOn;
    private LocalDate psychologicalExamExpiresOn;

    private LocalDate digitalCardIssuedOn;
    private LocalDate digitalCardExpiresOn;


    @PrePersist
    @PreUpdate
    private void validateDates() {
        validate(driverLicenseIssuedOn, driverLicenseExpiresOn);
        validate(qualificationCardIssuedOn, qualificationCardExpiresOn);

        validate(psychologicalExamIssuedOn, psychologicalExamExpiresOn);
        validate(digitalCardIssuedOn, digitalCardExpiresOn);
    }

    private void validate(LocalDate issued, LocalDate expires) {
        if (issued != null && expires != null && expires.isBefore(issued)) {
            throw new IllegalArgumentException("Дата на изтичане не може да предхожда дата на издаване.");
        }
    }
}
