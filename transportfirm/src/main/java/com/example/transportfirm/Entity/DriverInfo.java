package com.example.transportfirm.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "drivers_info")
@Data
public class DriverInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnoreProperties({"driverInfo", "mechanicInfo", "documents", "hibernateLazyInitializer", "handler"})
    private Long id;

    // Връзка към Employee (имейл взимаме от него)
    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Един шофьор кара един камион
    @OneToOne
    @JoinColumn(name = "vehicle_id")
    private VehicleRecord vehicle;

    // Дати на документи
    private LocalDate driverLicenseIssuedOn;
    private LocalDate driverLicenseExpiresOn;

    private LocalDate qualificationCardIssuedOn;
    private LocalDate qualificationCardExpiresOn;

    private LocalDate medicalExamIssuedOn;
    private LocalDate medicalExamExpiresOn;

    private LocalDate psychologicalExamIssuedOn;
    private LocalDate psychologicalExamExpiresOn;

    private LocalDate digitalCardIssuedOn;
    private LocalDate digitalCardExpiresOn;

    private LocalDate adrIssuedOn;
    private LocalDate adrExpiresOn;

    @PrePersist
    @PreUpdate
    private void validateDates() {
        validate(driverLicenseIssuedOn, driverLicenseExpiresOn);
        validate(qualificationCardIssuedOn, qualificationCardExpiresOn);
        validate(medicalExamIssuedOn, medicalExamExpiresOn);

        validate(psychologicalExamIssuedOn, psychologicalExamExpiresOn);
        validate(digitalCardIssuedOn, digitalCardExpiresOn);
        validate(adrIssuedOn, adrExpiresOn);
    }

    private void validate(LocalDate issued, LocalDate expires) {
        if (issued != null && expires != null && expires.isBefore(issued)) {
            throw new IllegalArgumentException("Дата на изтичане не може да предхожда дата на издаване.");
        }
    }
}
