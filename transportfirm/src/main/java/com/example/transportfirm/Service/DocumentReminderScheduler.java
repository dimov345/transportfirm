package com.example.transportfirm.service;

import com.example.transportfirm.entity.DriverInfo;
import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.enums.EmploymentStatus;
import com.example.transportfirm.enums.Role;
import com.example.transportfirm.repository.DriverRepository;
import com.example.transportfirm.repository.EmployeeRepository;
import com.example.transportfirm.repository.VehicleRepository;
import com.example.transportfirm.service.auth.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Ежедневен scheduler за напомняния по имейл за изтекли/изтичащи документи.
 *
 * Логика:
 *  - Документи на шофьор  → имейл до самия шофьор
 *  - Документи на ППС     → имейл до спедитора на групата + всички ADMIN и MANAGER
 *  - ППС без спедиторска група → имейл само до ADMIN и MANAGER
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentReminderScheduler {

    private static final int WARNING_DAYS = 30;

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;

    /** Изпълнява се всеки ден в 08:00 сутринта. */
    @Scheduled(cron = "0 33 12 * * *")
    @Transactional(readOnly = true)
    public void sendDocumentReminders() {
        LocalDate today       = LocalDate.now();
        LocalDate warningDate = today.plusDays(WARNING_DAYS);

        log.info("Стартиране на проверка за документи за дата {}", today);

        List<String> notifyEmails = getNotifyEmails();
        log.info("Намерени {} имейла за нотификации (ADMIN/MANAGER)", notifyEmails.size());

        sendDriverReminders(today, warningDate);
        sendVehicleReminders(today, warningDate, notifyEmails);

        log.info("Проверката за документи приключи.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Driver document reminders
    // ─────────────────────────────────────────────────────────────────────────

    private void sendDriverReminders(LocalDate today, LocalDate warningDate) {
        List<DriverInfo> drivers = driverRepository.findAllWithEmployee();
        log.info("Намерени {} шофьора за проверка", drivers.size());

        for (DriverInfo driver : drivers) {
            Employee emp = driver.getEmployee();
            if (emp == null) continue;
            if (emp.getEmploymentStatus() != EmploymentStatus.ACTIVE) continue;

            String email = emp.getEmail();
            String name  = emp.getName();

            if (email == null || email.isBlank()) {
                log.debug("Шофьор '{}' няма имейл — пропускам", name);
                continue;
            }

            List<String[]> items = new ArrayList<>();
            checkDate(driver.getDriverLicenseExpiresOn(),     "Шофьорска книжка",       today, warningDate, items);
            checkDate(driver.getQualificationCardExpiresOn(), "Карта за квалификация",   today, warningDate, items);
            checkDate(driver.getPsychologicalExamExpiresOn(), "Психологически преглед",  today, warningDate, items);
            checkDate(driver.getDigitalCardExpiresOn(),       "Цифрова карта",           today, warningDate, items);

            if (items.isEmpty()) continue;

            try {
                emailService.sendDriverDocumentReminderEmail(email, name, items);
                log.info("Изпратен reminder до шофьор '{}' ({}): {} документа", name, email, items.size());
            } catch (Exception e) {
                log.error("Грешка при изпращане на reminder до шофьор '{}' ({}): {}", name, email, e.getMessage());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Vehicle document reminders
    // ─────────────────────────────────────────────────────────────────────────

    private void sendVehicleReminders(LocalDate today, LocalDate warningDate, List<String> notifyEmails) {
        // Uses JOIN FETCH query — no lazy loading required
        List<VehicleRecord> vehicles = vehicleRepository.findAllForReminders();
        log.info("Намерени {} ППС за проверка", vehicles.size());

        for (VehicleRecord vehicle : vehicles) {
            String plate = vehicle.getPlateNumber() != null ? vehicle.getPlateNumber() : "—";

            List<String[]> items = new ArrayList<>();
            checkDate(vehicle.getKaskoDo(),                  "Каско",                  today, warningDate, items);
            checkDate(vehicle.getGrazhdanskaOtgovornostDo(), "Гражданска отговорност", today, warningDate, items);
            checkDate(vehicle.getGtpDo(),                    "ГТП",                    today, warningDate, items);
            checkDate(vehicle.getVinetkaDo(),                "Винетка",                today, warningDate, items);

            if (items.isEmpty()) continue;

            // Send to the dispatcher of the group (if the vehicle has one)
            boolean sentToDispatcher = false;
            if (vehicle.getDispatcherGroup() != null
                    && vehicle.getDispatcherGroup().getDispatcher() != null
                    && vehicle.getDispatcherGroup().getDispatcher().getEmployee() != null) {

                String dispEmail = vehicle.getDispatcherGroup().getDispatcher().getEmployee().getEmail();
                if (dispEmail != null && !dispEmail.isBlank()) {
                    try {
                        emailService.sendVehicleDocumentReminderEmail(dispEmail, plate, items);
                        log.info("Изпратен reminder за ППС '{}' до спедитор ({})", plate, dispEmail);
                        sentToDispatcher = true;
                    } catch (Exception e) {
                        log.error("Грешка при изпращане до спедитор '{}' за ППС '{}': {}", dispEmail, plate, e.getMessage());
                    }
                }
            }

            if (!sentToDispatcher) {
                log.debug("ППС '{}' няма назначена спедиторска група — само ADMIN/MANAGER ще бъдат нотифицирани", plate);
            }

            // Always notify ADMIN and MANAGER
            for (String notifyEmail : notifyEmails) {
                try {
                    emailService.sendVehicleDocumentReminderEmail(notifyEmail, plate, items);
                    log.info("Изпратен reminder за ППС '{}' до {} ({})", plate,
                            sentToDispatcher ? "admin/manager" : "admin/manager (без спедитор)", notifyEmail);
                } catch (Exception e) {
                    log.error("Грешка при изпращане до '{}' за ППС '{}': {}", notifyEmail, plate, e.getMessage());
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns emails of all ADMIN and MANAGER employees who have an email set. */
    private List<String> getNotifyEmails() {
        return employeeRepository.findAll().stream()
                .filter(e -> (e.getRole() == Role.ADMIN || e.getRole() == Role.MANAGER)
                        && e.getEmail() != null && !e.getEmail().isBlank())
                .map(Employee::getEmail)
                .distinct()
                .toList();
    }

    /**
     * Adds an entry to items if the date is expired or expiring within WARNING_DAYS.
     * item format: {docType, status ("expired"|"expiring"), days, expiryDate}
     */
    private void checkDate(LocalDate expiryDate, String docType,
                           LocalDate today, LocalDate warningDate,
                           List<String[]> items) {
        if (expiryDate == null) return;
        if (expiryDate.isBefore(today)) {
            long days = ChronoUnit.DAYS.between(expiryDate, today);
            items.add(new String[]{docType, "expired", String.valueOf(days), expiryDate.toString()});
        } else if (!expiryDate.isAfter(warningDate)) {
            long days = ChronoUnit.DAYS.between(today, expiryDate);
            items.add(new String[]{docType, "expiring", String.valueOf(days), expiryDate.toString()});
        }
    }
}
