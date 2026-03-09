package com.example.transportfirm.service;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.VehicleFreightTrip;
import com.example.transportfirm.entity.VehicleMaintenanceRecord;
import com.example.transportfirm.enums.EmploymentStatus;
import com.example.transportfirm.enums.VehicleStatus;
import com.example.transportfirm.io.DashboardStatsDto;
import com.example.transportfirm.io.MonthlyStatDto;
import com.example.transportfirm.io.NotificationDto;
import com.example.transportfirm.repository.DriverRepository;
import com.example.transportfirm.repository.EmployeeRepository;
import com.example.transportfirm.repository.VehicleMaintenanceRecordRepository;
import com.example.transportfirm.repository.VehicleFreightTripRepository;
import com.example.transportfirm.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final BigDecimal BGN_TO_EUR = BigDecimal.ONE
            .divide(new BigDecimal("1.95583"), 6, RoundingMode.HALF_UP);

    private final EmployeeRepository employeeRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleFreightTripRepository tripRepository;
    private final VehicleMaintenanceRecordRepository maintenanceRepository;
    private final DriverRepository driverRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Stats
    // ─────────────────────────────────────────────────────────────────────────

    public DashboardStatsDto getStats() {
        LocalDate today        = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart   = currentMonth.atDay(1);
        LocalDate monthEnd     = currentMonth.atEndOfMonth();

        // ── Counts ────────────────────────────────────────────────────────────
        List<Employee> allEmployees = employeeRepository.findAll();
        long activeEmployees = allEmployees.stream()
                .filter(e -> e.getEmploymentStatus() == EmploymentStatus.ACTIVE).count();

        long activeVehicles = vehicleRepository.findAll().stream()
                .filter(v -> v.getVehicleStatus() != VehicleStatus.IN_SERVICE).count();

        // ── Current month trips ───────────────────────────────────────────────
        List<VehicleFreightTrip> monthTrips =
                tripRepository.findByDepartureDateBetweenWithVehicle(monthStart, monthEnd);
        int tripCount = monthTrips.size();

        BigDecimal revThisMonth = monthTrips.stream()
                .map(t -> orZero(t.getRevenueEur())).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tripExpThisMonth = monthTrips.stream()
                .map(t -> orZero(t.getVatEur())
                        .add(orZero(t.getFuelCostEur()))
                        .add(orZero(t.getTollFeesEur()))
                        .add(orZero(t.getBorderFeesEur()))
                        .add(orZero(t.getParkingAccommodationEur()))
                        .add(orZero(t.getOtherExpensesEur())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── Current month maintenance ─────────────────────────────────────────
        List<VehicleMaintenanceRecord> monthMaint = maintenanceRepository.findByPeriodWithVehicle(
                monthStart.atStartOfDay(), monthEnd.atTime(LocalTime.MAX));
        BigDecimal maintBgn = monthMaint.stream()
                .map(r -> orZero(r.getTotalGross())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal maintEur = maintBgn.multiply(BGN_TO_EUR).setScale(2, RoundingMode.HALF_UP);

        // ── Current month VAT from trips ──────────────────────────────────────
        BigDecimal vatThisMonth = monthTrips.stream()
                .map(t -> orZero(t.getVatEur())).reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── Current month salary (active employees) ───────────────────────────
        List<Employee> activeEmpList = allEmployees.stream()
                .filter(e -> e.getEmploymentStatus() == EmploymentStatus.ACTIVE).toList();
        BigDecimal salaryBgn = activeEmpList.stream()
                .map(e -> orZero(e.getSalary()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal salaryEur = salaryBgn.multiply(BGN_TO_EUR).setScale(2, RoundingMode.HALF_UP);
        BigDecimal salaryNetoBgn = activeEmpList.stream()
                .map(e -> orZero(e.getSalaryNeto()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal salaryNetoEur  = salaryNetoBgn.multiply(BGN_TO_EUR).setScale(2, RoundingMode.HALF_UP);
        BigDecimal salaryTaxesEur = salaryEur.subtract(salaryNetoEur);

        // ── Document expiry counts ────────────────────────────────────────────
        List<NotificationDto> notifications = buildNotifications(today);
        long expiredCount  = notifications.stream().filter(n -> "expired".equals(n.getStatus())).count();
        long expiringCount = notifications.stream().filter(n -> "expiring".equals(n.getStatus())).count();

        // ── Last 6 months stats ───────────────────────────────────────────────
        List<MonthlyStatDto> monthlyStats = buildMonthlyStats(currentMonth);

        return new DashboardStatsDto(
                activeEmployees, activeVehicles, tripCount,
                revThisMonth, maintBgn,
                salaryEur, salaryNetoEur, salaryTaxesEur,
                tripExpThisMonth, vatThisMonth, maintEur,
                expiredCount, expiringCount,
                monthlyStats);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Notifications
    // ─────────────────────────────────────────────────────────────────────────

    public List<NotificationDto> getNotifications() {
        return buildNotifications(LocalDate.now());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private List<NotificationDto> buildNotifications(LocalDate today) {
        LocalDate warningDate = today.plusDays(30);
        List<NotificationDto> result = new ArrayList<>();

        // ── Vehicle documents ─────────────────────────────────────────────────
        vehicleRepository.findAll().forEach(v -> {
            String plate = v.getPlateNumber() != null ? v.getPlateNumber() : "—";
            String id    = v.getId() != null ? v.getId().toString() : "";
            String nav   = "/vehicles/" + id;
            addIfExpiry(result, v.getKaskoDo(),                  "Каско",                   plate, id, nav, "vehicle", today, warningDate);
            addIfExpiry(result, v.getGrazhdanskaOtgovornostDo(), "Гражданска отговорност",  plate, id, nav, "vehicle", today, warningDate);
            addIfExpiry(result, v.getGtpDo(),                    "ГТП",                     plate, id, nav, "vehicle", today, warningDate);
            addIfExpiry(result, v.getVinetkaDo(),                "Винетка",                 plate, id, nav, "vehicle", today, warningDate);
        });

        // ── Driver document dates ─────────────────────────────────────────────
        driverRepository.findAll().forEach(d -> {
            if (d.getEmployee() == null
                    || d.getEmployee().getEmploymentStatus() != EmploymentStatus.ACTIVE) return;

            String name  = d.getEmployee().getName() != null ? d.getEmployee().getName() : "—";
            String empId = d.getEmployee().getId() != null ? d.getEmployee().getId().toString() : "";
            String nav   = "/drivers/" + empId;

            addIfExpiry(result, d.getDriverLicenseExpiresOn(),     "Шофьорска книжка",      name, empId, nav, "driver", today, warningDate);
            addIfExpiry(result, d.getQualificationCardExpiresOn(), "Карта за квалификация", name, empId, nav, "driver", today, warningDate);
            addIfExpiry(result, d.getPsychologicalExamExpiresOn(), "Психологически преглед",name, empId, nav, "driver", today, warningDate);
            addIfExpiry(result, d.getDigitalCardExpiresOn(),       "Дигитална карта",         name, empId, nav, "driver", today, warningDate);
        });

        // Sort: most expired (most negative days) first, then expiring soonest
        result.sort(Comparator.comparingLong(NotificationDto::getDaysUntilExpiry));
        return result;
    }

    private void addIfExpiry(List<NotificationDto> list, LocalDate expiryDate,
                             String docType, String entityName, String entityId,
                             String navTo, String entityType,
                             LocalDate today, LocalDate warningDate) {
        if (expiryDate == null) return;
        long days = ChronoUnit.DAYS.between(today, expiryDate);
        if (expiryDate.isBefore(today)) {
            list.add(new NotificationDto(entityType, docType, entityName, entityId,
                    expiryDate, "expired", days, navTo));
        } else if (!expiryDate.isAfter(warningDate)) {
            list.add(new NotificationDto(entityType, docType, entityName, entityId,
                    expiryDate, "expiring", days, navTo));
        }
    }

    private List<MonthlyStatDto> buildMonthlyStats(YearMonth currentMonth) {
        LocalDate rangeStart = currentMonth.minusMonths(5).atDay(1);
        LocalDate rangeEnd   = currentMonth.atEndOfMonth();

        List<VehicleFreightTrip> allTrips =
                tripRepository.findByDepartureDateBetweenWithVehicle(rangeStart, rangeEnd);

        Map<YearMonth, List<VehicleFreightTrip>> byMonth = allTrips.stream()
                .filter(t -> t.getDepartureDate() != null)
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.getDepartureDate())));

        List<MonthlyStatDto> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym    = currentMonth.minusMonths(i);
            List<VehicleFreightTrip> trips = byMonth.getOrDefault(ym, Collections.emptyList());

            double rev = trips.stream()
                    .mapToDouble(t -> orZero(t.getRevenueEur()).doubleValue()).sum();
            double exp = trips.stream()
                    .mapToDouble(t -> orZero(t.getVatEur())
                            .add(orZero(t.getFuelCostEur()))
                            .add(orZero(t.getTollFeesEur()))
                            .add(orZero(t.getBorderFeesEur()))
                            .add(orZero(t.getParkingAccommodationEur()))
                            .add(orZero(t.getOtherExpensesEur())).doubleValue()).sum();

            String label = ym.getMonth()
                    .getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("bg"))
                    + " '" + String.valueOf(ym.getYear()).substring(2);

            result.add(new MonthlyStatDto(label, rev, exp, rev - exp, trips.size()));
        }
        return result;
    }

    private BigDecimal orZero(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
