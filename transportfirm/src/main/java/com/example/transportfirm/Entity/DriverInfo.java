package com.example.transportfirm.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "Drivers_InfoTable")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DriverInfo {

    @Id
    @Column(length = 10, nullable = false)
    @Pattern(regexp = "\\d{10}", message = "EGN трябва да е точно 10 цифри")
    private String egn;

    @Column(length = 100, nullable = false)
    @NotBlank(message = "Името е задължително")
    private String name;

    @Column(length = 20, nullable = false)
    @NotBlank(message = "Телефонът е задължителен")
    private String phone;

    @Column(length = 100, nullable = false)
    @Email(message = "Невалиден имейл")
    private String email;

    @NotNull(message = "Дата на издаване на шофьорската книжка е задължителна")
    private LocalDate driverLicenseIssuedOn;

    @NotNull(message = "Дата на изтичане на шофьорската книжка е задължителна")
    private LocalDate driverLicenseExpiresOn;

    @NotNull(message = "Дата на издаване на квалификационната карта е задължителна")
    private LocalDate qualificationCardIssuedOn;

    @NotNull(message = "Дата на изтичане на квалификационната карта е задължителна")
    private LocalDate qualificationCardExpiresOn;

    private LocalDate psychologicalExamIssuedOn;
    private LocalDate psychologicalExamExpiresOn;
    private LocalDate digitalCardIssuedOn;
    private LocalDate digitalCardExpiresOn;

    // Предварителна проверка на дати
    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (driverLicenseExpiresOn.isBefore(driverLicenseIssuedOn)) {
            throw new IllegalArgumentException("Дата на изтичане на шофьорската книжка не може да е преди издаването");
        }
        if (qualificationCardExpiresOn.isBefore(qualificationCardIssuedOn)) {
            throw new IllegalArgumentException("Дата на изтичане на квалификационната карта не може да е преди издаването");
        }
        if (psychologicalExamIssuedOn != null && psychologicalExamExpiresOn != null &&
                psychologicalExamExpiresOn.isBefore(psychologicalExamIssuedOn)) {
            throw new IllegalArgumentException("Психологическият преглед не може да изтече преди издаването");
        }
        if (digitalCardIssuedOn != null && digitalCardExpiresOn != null &&
                digitalCardExpiresOn.isBefore(digitalCardIssuedOn)) {
            throw new IllegalArgumentException("Дигиталната карта не може да изтече преди издаването");
        }
    }
}
