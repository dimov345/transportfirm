package com.example.transportfirm.service.scheduler;

import com.example.transportfirm.entity.DriverInfo;
import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.enums.EmploymentStatus;
import com.example.transportfirm.enums.Role;
import com.example.transportfirm.repository.driver.DriverRepository;
import com.example.transportfirm.repository.employee.EmployeeRepository;
import com.example.transportfirm.repository.vehicle.VehicleRepository;
import com.example.transportfirm.service.auth.EmailService;
import com.example.transportfirm.service.auth.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentReminderScheduler {

    private static final int WARNING_DAYS = 30;

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final EmployeeRepository employeeRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    /** Изпълнява се всеки ден в 08:00 сутринта. */
    @Scheduled(cron = "0 0 8 * * *", zone = "Europe/Sofia")
    @Transactional
    public void sendDocumentReminders() {
        LocalDate today       = LocalDate.now();
        LocalDate warningDate = today.plusDays(WARNING_DAYS);

        log.info("Стартиране на проверка за документи за дата {}", today);

        List<String> notifyEmails = getNotifyEmails();
        List<String> notifyPhones = getNotifyPhones();
        log.info("Намерени {} имейла и {} телефона за нотификации (ADMIN/MANAGER)",
                notifyEmails.size(), notifyPhones.size());
        if (notifyEmails.isEmpty()) {
            log.warn("Няма намерени имейли за ADMIN/MANAGER — нотификациите за ППС може да не достигнат!");
        }

        sendDriverReminders(today, warningDate);
        sendVehicleReminders(today, warningDate, notifyEmails, notifyPhones);

        log.info("Проверката за документи приключи.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Driver document reminders
    // ─────────────────────────────────────────────────────────────────────────

    private void sendDriverReminders(LocalDate today, LocalDate warningDate) {
        List<DriverInfo> drivers;
        try {
            drivers = driverRepository.findAllWithEmployee();
        } catch (Exception e) {
            log.error("Грешка при зареждане на шофьори от базата данни: {}", e.getMessage(), e);
            return;
        }

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
            checkDate(driver.getQualificationCardExpiresOn(), "Квалификационна карта",   today, warningDate, items);
            checkDate(driver.getPsychologicalExamExpiresOn(), "Психологически преглед",  today, warningDate, items);
            checkDate(driver.getDigitalCardExpiresOn(),       "Дигитална карта",           today, warningDate, items);

            if (items.isEmpty()) continue;

            try {
                emailService.sendDriverDocumentReminderEmail(email, name, items);
                log.info("Изпратен email reminder до шофьор '{}' ({}): {} документа", name, email, items.size());
            } catch (Exception e) {
                log.error("Грешка при изпращане на email reminder до шофьор '{}' ({}): {}", name, email, e.getMessage(), e);
            }

            String phone = emp.getPhone();
            if (phone != null && !phone.isBlank()) {
                String smsText = buildDriverSmsSummary(name, items);
                smsService.sendSms(phone, smsText);
                log.info("Изпратен SMS reminder до шофьор '{}' ({}): {} документа", name, phone, items.size());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Vehicle document reminders
    // ─────────────────────────────────────────────────────────────────────────

    private void sendVehicleReminders(LocalDate today, LocalDate warningDate,
                                       List<String> notifyEmails, List<String> notifyPhones) {
        List<VehicleRecord> vehicles;
        try {
            vehicles = vehicleRepository.findAllForReminders();
        } catch (Exception e) {
            log.error("Грешка при зареждане на ППС от базата данни: {}", e.getMessage(), e);
            return;
        }

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
            try {
                if (vehicle.getDispatcherGroup() != null
                        && vehicle.getDispatcherGroup().getDispatcher() != null
                        && vehicle.getDispatcherGroup().getDispatcher().getEmployee() != null) {

                    Employee dispEmp = vehicle.getDispatcherGroup().getDispatcher().getEmployee();
                    String dispEmail = dispEmp.getEmail();
                    if (dispEmail != null && !dispEmail.isBlank()) {
                        emailService.sendVehicleDocumentReminderEmail(dispEmail, plate, items);
                        log.info("Изпратен email reminder за ППС '{}' до спедитор ({})", plate, dispEmail);
                        sentToDispatcher = true;
                    }
                    String dispPhone = dispEmp.getPhone();
                    if (dispPhone != null && !dispPhone.isBlank()) {
                        smsService.sendSms(dispPhone, buildVehicleSmsSummary(plate, items));
                        log.info("Изпратен SMS reminder за ППС '{}' до спедитор ({})", plate, dispPhone);
                    }
                }
            } catch (Exception e) {
                log.error("Грешка при изпращане до спедитор за ППС '{}': {}", plate, e.getMessage(), e);
            }

            if (!sentToDispatcher) {
                log.debug("ППС '{}' няма назначена спедиторска група — само ADMIN/MANAGER ще бъдат нотифицирани", plate);
            }

            // Always notify ADMIN and MANAGER via email
            for (String notifyEmail : notifyEmails) {
                try {
                    emailService.sendVehicleDocumentReminderEmail(notifyEmail, plate, items);
                    log.info("Изпратен email reminder за ППС '{}' до {} (ADMIN/MANAGER)", plate, notifyEmail);
                } catch (Exception e) {
                    log.error("Грешка при изпращане до '{}' за ППС '{}': {}", notifyEmail, plate, e.getMessage(), e);
                }
            }

            // Always notify ADMIN and MANAGER via SMS
            String vehicleSmsText = buildVehicleSmsSummary(plate, items);
            for (String notifyPhone : notifyPhones) {
                smsService.sendSms(notifyPhone, vehicleSmsText);
                log.info("Изпратен SMS reminder за ППС '{}' до {} (ADMIN/MANAGER)", plate, notifyPhone);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns distinct emails of all ADMIN and MANAGER employees.
     * Uses a direct JPQL projection to avoid loading unnecessary entity data.
     */
    private List<String> getNotifyEmails() {
        try {
            List<String> emails = employeeRepository.findEmailsByRoles(List.of(Role.ADMIN, Role.MANAGER));
            log.debug("Заредени {} имейла за ADMIN/MANAGER", emails.size());
            return emails;
        } catch (Exception e) {
            log.error("Грешка при зареждане на ADMIN/MANAGER имейли: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /** Returns distinct phone numbers of all active ADMIN and MANAGER employees. */
    private List<String> getNotifyPhones() {
        try {
            List<String> phones = employeeRepository.findPhonesByRoles(List.of(Role.ADMIN, Role.MANAGER));
            log.debug("Заредени {} телефона за ADMIN/MANAGER", phones.size());
            return phones;
        } catch (Exception e) {
            log.error("Грешка при зареждане на ADMIN/MANAGER телефони: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /** Builds a formatted SMS message for driver document reminders. */
    private String buildDriverSmsSummary(String driverName, List<String[]> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("[TransTrack] Документи\n");
        sb.append("Шофьор: ").append(driverName).append("\n");
        for (String[] item : items) {
            sb.append("• ").append(item[0]).append(": ");
            if ("expired".equals(item[1])) {
                sb.append("ИЗТЕКЪЛ (").append(formatSmsDate(item[3])).append(")");
            } else if ("0".equals(item[2])) {
                sb.append("изтича ДНЕС (").append(formatSmsDate(item[3])).append(")");
            } else {
                sb.append("след ").append(item[2]).append(" дни (").append(formatSmsDate(item[3])).append(")");
            }
            sb.append("\n");
        }
        sb.append("Моля подновете навреме!");
        return sb.toString();
    }

    /** Builds a formatted SMS message for vehicle document reminders. */
    private String buildVehicleSmsSummary(String plate, List<String[]> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("[TransTrack] Документи ППС\n");
        sb.append("ППС: ").append(plate).append("\n");
        for (String[] item : items) {
            sb.append("• ").append(item[0]).append(": ");
            if ("expired".equals(item[1])) {
                sb.append("ИЗТЕКЪЛ (").append(formatSmsDate(item[3])).append(")");
            } else if ("0".equals(item[2])) {
                sb.append("изтича ДНЕС (").append(formatSmsDate(item[3])).append(")");
            } else {
                sb.append("след ").append(item[2]).append(" дни (").append(formatSmsDate(item[3])).append(")");
            }
            sb.append("\n");
        }
        sb.append("Необходимо подновяване!");
        return sb.toString();
    }

    /** Formats ISO date (2026-04-25) → short BG format (25.04.26). */
    private String formatSmsDate(String isoDate) {
        if (isoDate == null || isoDate.length() < 10) return isoDate;
        // isoDate = "2026-04-25"
        String[] p = isoDate.split("-");
        if (p.length < 3) return isoDate;
        return p[2] + "." + p[1] + "." + p[0].substring(2); // 25.04.26
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
