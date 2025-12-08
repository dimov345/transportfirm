package com.example.transportfirm.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicle_records")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VehicleRecord {

    @Id
    @NotBlank(message = "Plate number е задължителен")
    @Column(length = 20)
    private String plateNumber;

    @NotBlank(message = "Моделът е задължителен")
    @Column(length = 100)
    private String model;

    @NotBlank(message = "Номер на двигател е задължителен")
    @Column(length = 50)
    private String engineNumber;

    @NotBlank(message = "Шаси номер е задължителен")
    @Column(length = 50)
    private String chassisNumber;

    @NotNull(message = "Година на производство е задължителна")
    @Min(value = 1900, message = "Годината на производство не може да е преди 1900")
    @Max(value = 2100, message = "Годината на производство не може да е след 2100")
    private Integer yearOfManufacture;

    @NotBlank(message = "Собственикът е задължителен")
    @Column(length = 100)
    private String owner;

    @NotBlank(message = "Тип ППС е задължителен")
    @Column(length = 50)
    @Pattern(
            regexp = "Лек автомобил|Автобус|Лекотоварен автомобил \\(до 3\\.5 т\\)|Тежкотоварен камион|Автоплатформен и специализиран камион \\(бетоновози, самосвали и др\\.\\)|Линейка|Пожарна кола|Полицейски автомобил|Снегорини, почистваща техника и др\\.|Ремаркета и полуремаркета|Влекачи \\(тягови ППС за ремаркета\\)",
            message = "Невалиден тип ППС"
    )
    private String typePps;

    @NotBlank(message = "Техническо състояние е задължително")
    @Column(length = 50)
    @Pattern(
            regexp = "В движение|С временно отстранени неизправности|Спряно от движение|Повредено|Бракувано",
            message = "Невалидно техническо състояние"
    )
    private String technicalCondition;

    @NotBlank(message = "Стандарт на емисии е задължителен")
    @Column(length = 50)
    @Pattern(regexp = "Euro 1|Euro 2|Euro 3|Euro 4|Euro 5|Euro 6", message = "Невалиден стандарт на емисии")
    private String standardEmissions;

    private LocalDate kaskoOt;
    private LocalDate kaskoDo;
    private LocalDate grazhdanskaOtgovornostOt;
    private LocalDate grazhdanskaOtgovornostDo;
    private LocalDate gtpOt;
    private LocalDate gtpDo;
    private LocalDate vinetkaOt;
    private LocalDate vinetkaDo;

    // Група към която принадлежи камионът
    @ManyToOne
    @JoinColumn(name = "truck_group_id")
    private TruckGroup truckGroup;

    // Един шофьор кара един камион
    @OneToOne(mappedBy = "vehicle")
    private DriverInfo driver;

    // Документи на камиона
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
    private List<VehicleDocument> documents = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (kaskoOt != null && kaskoDo != null && kaskoDo.isBefore(kaskoOt)) {
            throw new IllegalArgumentException("Каско не може да изтече преди началната дата");
        }
        if (grazhdanskaOtgovornostOt != null && grazhdanskaOtgovornostDo != null &&
                grazhdanskaOtgovornostDo.isBefore(grazhdanskaOtgovornostOt)) {
            throw new IllegalArgumentException("Гражданската отговорност не може да изтече преди началната дата");
        }
        if (gtpOt != null && gtpDo != null && gtpDo.isBefore(gtpOt)) {
            throw new IllegalArgumentException("ГТП не може да изтече преди началната дата");
        }
        if (vinetkaOt != null && vinetkaDo != null && vinetkaDo.isBefore(vinetkaOt)) {
            throw new IllegalArgumentException("Винетката не може да изтече преди началната дата");
        }
    }
}
