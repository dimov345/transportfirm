package com.example.transportfirm.service;

import com.example.transportfirm.entity.DriverInfo;
import com.example.transportfirm.entity.VehicleFreightTrip;
import com.example.transportfirm.entity.VehicleMaintenanceRecord;
import com.example.transportfirm.entity.VehicleRecord;
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
import com.example.transportfirm.util.FinancialConstants;
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
        LocalDate monthEnd     = currentMonth.atEndOfMonth();

        // ── Counts via single targeted queries (no findAll) ───────────────────
        long activeEmployees = employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE);
        long activeVehicles  = vehicleRepository.countByVehicleStatusNot(VehicleStatus.IN_SERVICE);

        // ── Fetch 6 months of trips ONCE and derive current-month subset ──────
        LocalDate rangeStart = currentMonth.minusMonths(5).atDay(1);
        List<VehicleFreightTrip> allTrips =
                tripRepository.findByDepartureDateBetweenWithVehicle(rangeStart, monthEnd);

        List<VehicleFreightTrip> monthTrips = allTrips.stream()
                .filter(t -> t.getDepartureDate() != null &&
                             YearMonth.from(t.getDepartureDate()).equals(currentMonth))
                .toList();

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

        BigDecimal vatThisMonth = monthTrips.stream()
                .map(t -> orZero(t.getVatEur())).reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── Current month maintenance ─────────────────────────────────────────
        LocalDate monthStart = currentMonth.atDay(1);
        List<VehicleMaintenanceRecord> monthMaint = maintenanceRepository.findByPeriodWithVehicle(
                monthStart.atStartOfDay(), monthEnd.atTime(LocalTime.MAX));
        BigDecimal maintBgn = monthMaint.stream()
                .map(r -> orZero(r.getTotalGross())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal maintEur = maintBgn.multiply(FinancialConstants.BGN_TO_EUR).setScale(2, RoundingMode.HALF_UP);

        // ── Salary via aggregate queries (no findAll) ─────────────────────────
        BigDecimal salaryEur      = employeeRepository.sumSalaryByEmploymentStatus(EmploymentStatus.ACTIVE);
        BigDecimal salaryNetoEur  = employeeRepository.sumSalaryNetoByEmploymentStatus(EmploymentStatus.ACTIVE);
        BigDecimal salaryTaxesEur = salaryEur.subtract(salaryNetoEur);

        // ── Document expiry counts (lightweight — no notification objects) ─────
        long[] docCounts = countDocExpiry(today);

        // ── Monthly stats from pre-fetched trip data ──────────────────────────
        List<MonthlyStatDto> monthlyStats = buildMonthlyStats(currentMonth, allTrips);

        return new DashboardStatsDto(
                activeEmployees, activeVehicles, tripCount,
                revThisMonth, maintBgn,
                salaryEur, salaryNetoEur, salaryTaxesEur,
                tripExpThisMonth, vatThisMonth, maintEur,
                docCounts[0], docCounts[1],
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

    /**
     * Lightweight expiry counting used by getStats().
     * Avoids building full NotificationDto objects and avoids calling
     * buildNotifications() twice (once for counts, once for the notifications endpoint).
     * Vehicle date fields are primitive — no lazy-load risk. Driver query uses JOIN FETCH.
     */
    private long[] countDocExpiry(LocalDate today) {
        LocalDate warningDate = today.plusDays(30);
        long expired = 0, expiring = 0;

        for (VehicleRecord v : vehicleRepository.findAll()) {
            LocalDate[] dates = {
                v.getKaskoDo(), v.getGrazhdanskaOtgovornostDo(),
                v.getGtpDo(),   v.getVinetkaDo()
            };
            for (LocalDate d : dates) {
                if (d == null) continue;
                if (d.isBefore(today))           expired++;
                else if (!d.isAfter(warningDate)) expiring++;
            }
        }

        for (DriverInfo d : driverRepository.findAllWithEmployee()) {
            if (d.getEmployee() == null ||
                    d.getEmployee().getEmploymentStatus() != EmploymentStatus.ACTIVE) continue;
            LocalDate[] dates = {
                d.getDriverLicenseExpiresOn(),     d.getQualificationCardExpiresOn(),
                d.getPsychologicalExamExpiresOn(), d.getDigitalCardExpiresOn()
            };
            for (LocalDate dt : dates) {
                if (dt == null) continue;
                if (dt.isBefore(today))           expired++;
                else if (!dt.isAfter(warningDate)) expiring++;
            }
        }

        return new long[]{ expired, expiring };
    }

    /**
     * Builds full notification list for the /notifications endpoint.
     * Uses JOIN FETCH queries to avoid N+1 lazy-loading.
     */
    private List<NotificationDto> buildNotifications(LocalDate today) {
        LocalDate warningDate = today.plusDays(30);
        List<NotificationDto> result = new ArrayList<>();

        // Vehicle docs — JOIN FETCH dispatcher group + dispatcher + employee in one query
        vehicleRepository.findAllForReminders().forEach(v -> {
            String plate = v.getPlateNumber() != null ? v.getPlateNumber() : "—";
            String id    = v.getId() != null ? v.getId().toString() : "";
            String nav   = "/vehicles/" + id;
            addIfExpiry(result, v.getKaskoDo(),                  "Каско",                  plate, id, nav, "vehicle", today, warningDate);
            addIfExpiry(result, v.getGrazhdanskaOtgovornostDo(), "Гражданска отговорност",  plate, id, nav, "vehicle", today, warningDate);
            addIfExpiry(result, v.getGtpDo(),                    "ГТП",                    plate, id, nav, "vehicle", today, warningDate);
            addIfExpiry(result, v.getVinetkaDo(),                "Винетка",                plate, id, nav, "vehicle", today, warningDate);
        });

        // Driver docs — JOIN FETCH employee in one query
        driverRepository.findAllWithEmployee().forEach(d -> {
            if (d.getEmployee() == null ||
                    d.getEmployee().getEmploymentStatus() != EmploymentStatus.ACTIVE) return;

            String name  = d.getEmployee().getName() != null ? d.getEmployee().getName() : "—";
            String empId = d.getEmployee().getId() != null ? d.getEmployee().getId().toString() : "";
            String nav   = "/drivers/" + empId;

            addIfExpiry(result, d.getDriverLicenseExpiresOn(),     "Шофьорска книжка",      name, empId, nav, "driver", today, warningDate);
            addIfExpiry(result, d.getQualificationCardExpiresOn(), "Карта за квалификация", name, empId, nav, "driver", today, warningDate);
            addIfExpiry(result, d.getPsychologicalExamExpiresOn(), "Психологически преглед", name, empId, nav, "driver", today, warningDate);
            addIfExpiry(result, d.getDigitalCardExpiresOn(),       "Дигитална карта",        name, empId, nav, "driver", today, warningDate);
        });

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

    /**
     * Builds last-6-months stats from a pre-fetched trip list (no extra DB query).
     */
    private List<MonthlyStatDto> buildMonthlyStats(YearMonth currentMonth, List<VehicleFreightTrip> allTrips) {
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
