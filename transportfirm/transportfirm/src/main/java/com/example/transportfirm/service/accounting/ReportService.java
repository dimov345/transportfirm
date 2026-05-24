package com.example.transportfirm.service.accounting;

import com.example.transportfirm.entity.Employee;
import com.example.transportfirm.entity.VehicleFreightTrip;
import com.example.transportfirm.entity.VehicleMaintenanceRecord;
import com.example.transportfirm.enums.EmploymentStatus;
import com.example.transportfirm.enums.JobTitle;
import com.example.transportfirm.enums.MaintenanceType;
import com.example.transportfirm.enums.ReportPeriod;
import com.example.transportfirm.io.accounting.*;
import com.example.transportfirm.entity.VehicleRecord;
import com.example.transportfirm.repository.employee.EmployeeRepository;
import com.example.transportfirm.util.FinancialConstants;
import com.example.transportfirm.repository.vehicle.VehicleFreightTripRepository;
import com.example.transportfirm.repository.vehicle.VehicleMaintenanceRecordRepository;
import com.example.transportfirm.repository.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.DayOfWeek;
import java.time.temporal.WeekFields;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final EmployeeRepository employeeRepository;
    private final VehicleFreightTripRepository tripRepository;
    private final VehicleMaintenanceRecordRepository maintenanceRepository;
    private final VehicleRepository vehicleRepository;

    // =================================================================
    // PUBLIC API
    // =================================================================

    @Transactional(readOnly = true)
    public byte[] generateSalaryReport(ReportPeriod period, int year, Integer month, Integer week) {
        List<Employee> employees = employeeRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE);
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            buildSalarySheet(wb, wb.createSheet("Заплати"), employees, period, year, month, week);
            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException("Грешка при генериране на отчета", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateTripsReport(ReportPeriod period, int year, Integer month, Integer week) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            LocalDate[] range = getDateRange(period, year, month, week);
            List<VehicleFreightTrip> trips = tripRepository.findByDepartureDateBetweenWithVehicle(range[0], range[1]);
            buildTripsSheet(wb, wb.createSheet("Курсове"), trips);
            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException("Грешка при генериране на отчета", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateMaintenanceReport(ReportPeriod period, int year, Integer month, Integer week) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            LocalDate[] range = getDateRange(period, year, month, week);
            List<VehicleMaintenanceRecord> records = maintenanceRepository.findByOpenedAtBetweenWithVehicle(
                    range[0].atStartOfDay(), range[1].atTime(LocalTime.MAX));
            buildMaintenanceSheet(wb, wb.createSheet("Поддръжка"), records);
            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException("Грешка при генериране на отчета", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateCombinedReport(ReportPeriod period, int year, Integer month, Integer week) {
        List<Employee> employees = employeeRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE);
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            LocalDate[] range = getDateRange(period, year, month, week);
            List<VehicleFreightTrip> trips = tripRepository.findByDepartureDateBetweenWithVehicle(range[0], range[1]);
            List<VehicleMaintenanceRecord> records = maintenanceRepository.findByOpenedAtBetweenWithVehicle(
                    range[0].atStartOfDay(), range[1].atTime(LocalTime.MAX));

            buildSalarySheet(wb, wb.createSheet("Заплати"), employees, period, year, month, week);
            buildTripsSheet(wb, wb.createSheet("Курсове"), trips);
            buildMaintenanceSheet(wb, wb.createSheet("Поддръжка"), records);
            buildSummarySheet(wb, wb.createSheet("Обобщение"), employees, trips, records, period, year, month, week);

            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException("Грешка при генериране на отчета", e);
        }
    }

    // =================================================================
    // DASHBOARD DATA (JSON — for accounting UI)
    // =================================================================

    @Transactional(readOnly = true)
    public AccountingDashboardResponse getDashboardData(ReportPeriod period, int year, Integer month, Integer week) {
        LocalDate[] range = getDateRange(period, year, month, week);
        String label = periodLabel(period, year, month, week);

        // ── Salaries ─────────────────────────────────────────────────
        List<Employee> employees = employeeRepository.findByEmploymentStatus(EmploymentStatus.ACTIVE);
        BigDecimal factor = salaryFactor(period);

        List<SalaryRowDto> salaries = employees.stream().map(emp -> {
            BigDecimal bruto = orZero(emp.getSalary()).multiply(factor).setScale(2, RoundingMode.HALF_UP);
            BigDecimal neto  = orZero(emp.getSalaryNeto()).multiply(factor).setScale(2, RoundingMode.HALF_UP);
            BigDecimal taxes = bruto.subtract(neto);
            return new SalaryRowDto(emp.getId(), emp.getName(), jobTitleLabel(emp.getJobTitle()),
                    bruto, neto, taxes);
        }).toList();

        // ── Trips ─────────────────────────────────────────────────────
        List<VehicleFreightTrip> trips = tripRepository.findByDepartureDateBetweenWithVehicle(range[0], range[1]);

        List<TripRowDto> tripRows = trips.stream().map(t -> {
            BigDecimal rev      = orZero(t.getRevenueEur());
            BigDecimal expenses = orZero(t.getVatEur())
                    .add(orZero(t.getFuelCostEur()))
                    .add(orZero(t.getTollFeesEur()))
                    .add(orZero(t.getBorderFeesEur()))
                    .add(orZero(t.getParkingAccommodationEur()))
                    .add(orZero(t.getOtherExpensesEur()));
            BigDecimal profit = rev.subtract(expenses);
            String route = (t.getOriginCity() != null ? t.getOriginCity() : "") + " → "
                         + (t.getDestinationCity() != null ? t.getDestinationCity() : "");
            return new TripRowDto(
                    t.getId(),
                    t.getDepartureDate() != null ? t.getDepartureDate().toString() : null,
                    route,
                    t.getVehicle() != null ? t.getVehicle().getPlateNumber() : null,
                    t.getVehicle() != null ? t.getVehicle().getId() : null,
                    t.getClientName(),
                    t.getStatus() != null ? t.getStatus().name() : null,
                    rev, expenses, profit);
        }).toList();

        // ── Maintenance ───────────────────────────────────────────────
        List<VehicleMaintenanceRecord> mainRecords = maintenanceRepository.findByPeriodWithVehicle(
                range[0].atStartOfDay(), range[1].atTime(LocalTime.MAX));

        List<MaintenanceRowDto> mainRows = mainRecords.stream().map(r -> {
            BigDecimal grossEur = orZero(r.getTotalGross()).setScale(2, RoundingMode.HALF_UP);
            return new MaintenanceRowDto(
                    r.getId(),
                    r.getVehicle() != null ? r.getVehicle().getId() : null,
                    r.getVehicle() != null ? r.getVehicle().getPlateNumber() : null,
                    maintenanceTypeLabel(r.getType()),
                    r.getWorkshopName(),
                    r.getOpenedAt() != null ? r.getOpenedAt().toLocalDate().toString() : null,
                    grossEur);
        }).toList();

        // ── Leasing ───────────────────────────────────────────────────
        List<VehicleRecord> leasedVehicles = vehicleRepository.findAllLeasedInPeriod(range[0], range[1]);

        BigDecimal periodMonths = switch (period) {
            case WEEKLY  -> BigDecimal.ONE.divide(FinancialConstants.WEEKS_PER_MONTH, 6, RoundingMode.HALF_UP);
            case MONTHLY -> BigDecimal.ONE;
            case YEARLY  -> new BigDecimal("12");
        };

        List<LeasingRowDto> leasingRows = leasedVehicles.stream().map(v -> {
            BigDecimal monthlyPayment = orZero(v.getLeasingMonthlyPaymentEur());
            BigDecimal totalForPeriod = monthlyPayment.multiply(periodMonths).setScale(2, RoundingMode.HALF_UP);
            return new LeasingRowDto(
                    v.getId() != null ? v.getId().toString() : null,
                    v.getPlateNumber(),
                    v.getLeasingCompany(),
                    v.getLeasingContractNumber(),
                    v.getLeasingStartDate() != null ? v.getLeasingStartDate().toString() : null,
                    v.getLeasingEndDate() != null ? v.getLeasingEndDate().toString() : null,
                    monthlyPayment,
                    totalForPeriod);
        }).toList();

        BigDecimal leasingTotal = leasingRows.stream().map(LeasingRowDto::getTotalForPeriodEur)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── Summary ───────────────────────────────────────────────────
        BigDecimal totalBrutoEur = salaries.stream().map(SalaryRowDto::getBrutoEur)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalNetoEur  = salaries.stream().map(SalaryRowDto::getNetoEur)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal salaryTaxes   = totalBrutoEur.subtract(totalNetoEur);
        BigDecimal tripRevenue   = tripRows.stream().map(TripRowDto::getRevenueEur)
                                           .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tripExpenses  = tripRows.stream().map(TripRowDto::getTotalExpensesEur)
                                           .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal vatTotal      = trips.stream().map(t -> orZero(t.getVatEur()))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal maintEur      = mainRows.stream().map(MaintenanceRowDto::getTotalGrossEur)
                                           .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netBalance    = tripRevenue.subtract(tripExpenses)
                                              .subtract(totalBrutoEur)
                                              .subtract(maintEur)
                                              .subtract(leasingTotal);

        AccountingSummaryDto summary = new AccountingSummaryDto(
                tripRevenue, totalBrutoEur,
                tripExpenses, maintEur, netBalance,
                employees.size(), trips.size(), mainRecords.size(),
                leasingTotal, leasedVehicles.size(), vatTotal, salaryTaxes,
                totalNetoEur);

        return new AccountingDashboardResponse(label, summary, salaries, tripRows, mainRows, leasingRows);
    }

    // =================================================================
    // SHEET BUILDERS
    // =================================================================

    private void buildSalarySheet(XSSFWorkbook wb, Sheet sheet,
                                   List<Employee> employees,
                                   ReportPeriod period, int year, Integer month, Integer week) {
        CellStyle hStyle  = makeHeaderStyle(wb);
        CellStyle money   = makeMoneyStyle(wb);
        CellStyle footer  = makeFooterMoneyStyle(wb);
        CellStyle fText   = makeFooterTextStyle(wb);

        String[] headers = {
            "Служител", "Длъжност", "Вид договор",
            "Бруто (€)", "Нето (€)", "Данъци (€)", "Период"
        };
        writeHeader(sheet, hStyle, headers);

        BigDecimal factor = salaryFactor(period);
        String label      = periodLabel(period, year, month, week);
        BigDecimal totalB = BigDecimal.ZERO;
        BigDecimal totalN = BigDecimal.ZERO;

        int rowNum = 1;
        for (Employee emp : employees) {
            BigDecimal bruto = orZero(emp.getSalary()).multiply(factor).setScale(2, RoundingMode.HALF_UP);
            BigDecimal neto  = orZero(emp.getSalaryNeto()).multiply(factor).setScale(2, RoundingMode.HALF_UP);
            BigDecimal taxes = bruto.subtract(neto);

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(emp.getName());
            row.createCell(1).setCellValue(jobTitleLabel(emp.getJobTitle()));
            row.createCell(2).setCellValue(emp.getContractType() != null ? emp.getContractType().name() : "—");
            setMoney(row, 3, bruto, money);
            setMoney(row, 4, neto,  money);
            setMoney(row, 5, taxes, money);
            row.createCell(6).setCellValue(label);

            totalB = totalB.add(bruto);
            totalN = totalN.add(neto);
        }

        Row footerRow = sheet.createRow(rowNum);
        Cell ftLabel = footerRow.createCell(0);
        ftLabel.setCellValue("ОБЩО (" + employees.size() + " служители):");
        ftLabel.setCellStyle(fText);
        setMoney(footerRow, 3, totalB,                     footer);
        setMoney(footerRow, 4, totalN,                     footer);
        setMoney(footerRow, 5, totalB.subtract(totalN),    footer);

        autoSize(sheet, headers.length);
    }

    private void buildTripsSheet(XSSFWorkbook wb, Sheet sheet, List<VehicleFreightTrip> trips) {
        CellStyle hStyle  = makeHeaderStyle(wb);
        CellStyle money   = makeMoneyStyle(wb);
        CellStyle footer  = makeFooterMoneyStyle(wb);
        CellStyle fText   = makeFooterTextStyle(wb);

        String[] headers = {
            "Дата тръгване", "Маршрут", "Камион", "Клиент",
            "Приходи (€)", "ДДС (€)", "Гориво (€)", "Пътни такси (€)",
            "Такси граница (€)", "Паркинг (€)", "Др. разходи (€)", "Нетна печалба (€)"
        };
        writeHeader(sheet, hStyle, headers);

        // column totals for indices 4..11
        BigDecimal[] totals = new BigDecimal[12];
        for (int i = 4; i < 12; i++) totals[i] = BigDecimal.ZERO;

        int rowNum = 1;
        for (VehicleFreightTrip t : trips) {
            BigDecimal rev    = orZero(t.getRevenueEur());
            BigDecimal vat    = orZero(t.getVatEur());
            BigDecimal fuel   = orZero(t.getFuelCostEur());
            BigDecimal tolls  = orZero(t.getTollFeesEur());
            BigDecimal border = orZero(t.getBorderFeesEur());
            BigDecimal park   = orZero(t.getParkingAccommodationEur());
            BigDecimal other  = orZero(t.getOtherExpensesEur());
            BigDecimal profit = rev.subtract(vat).subtract(fuel).subtract(tolls)
                                   .subtract(border).subtract(park).subtract(other);

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(t.getDepartureDate() != null ? t.getDepartureDate().toString() : "");
            row.createCell(1).setCellValue(t.getOriginCity() + " → " + t.getDestinationCity());
            row.createCell(2).setCellValue(t.getVehicle() != null ? t.getVehicle().getPlateNumber() : "");
            row.createCell(3).setCellValue(t.getClientName() != null ? t.getClientName() : "");
            setMoney(row,  4, rev,    money);
            setMoney(row,  5, vat,    money);
            setMoney(row,  6, fuel,   money);
            setMoney(row,  7, tolls,  money);
            setMoney(row,  8, border, money);
            setMoney(row,  9, park,   money);
            setMoney(row, 10, other,  money);
            setMoney(row, 11, profit, money);

            BigDecimal[] vals = {rev, vat, fuel, tolls, border, park, other, profit};
            for (int i = 0; i < vals.length; i++) totals[4 + i] = totals[4 + i].add(vals[i]);
        }

        Row footerRow = sheet.createRow(rowNum);
        Cell ftLabel = footerRow.createCell(0);
        ftLabel.setCellValue("ОБЩО (" + trips.size() + " курса):");
        ftLabel.setCellStyle(fText);
        for (int i = 4; i < 12; i++) setMoney(footerRow, i, totals[i], footer);

        autoSize(sheet, headers.length);
    }

    private void buildMaintenanceSheet(XSSFWorkbook wb, Sheet sheet, List<VehicleMaintenanceRecord> records) {
        CellStyle hStyle = makeHeaderStyle(wb);
        CellStyle money  = makeMoneyStyle(wb);
        CellStyle footer = makeFooterMoneyStyle(wb);
        CellStyle fText  = makeFooterTextStyle(wb);

        String[] headers = {
            "Дата (отворен)", "Камион", "Тип", "Сервиз",
            "Части нето (€)", "Части ДДС (€)",
            "Др. нето (€)", "Др. ДДС (€)",
            "Общо бруто (€)"
        };
        writeHeader(sheet, hStyle, headers);

        BigDecimal totalEur = BigDecimal.ZERO;

        int rowNum = 1;
        for (VehicleMaintenanceRecord r : records) {
            BigDecimal grossEur = orZero(r.getTotalGross()).setScale(2, RoundingMode.HALF_UP);

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(r.getOpenedAt() != null ? r.getOpenedAt().toLocalDate().toString() : "");
            row.createCell(1).setCellValue(r.getVehicle() != null ? r.getVehicle().getPlateNumber() : "");
            row.createCell(2).setCellValue(maintenanceTypeLabel(r.getType()));
            row.createCell(3).setCellValue(r.getWorkshopName() != null ? r.getWorkshopName() : "");
            setMoney(row, 4, orZero(r.getPartsNet()).setScale(2, RoundingMode.HALF_UP),  money);
            setMoney(row, 5, orZero(r.getPartsVat()).setScale(2, RoundingMode.HALF_UP),  money);
            setMoney(row, 6, orZero(r.getOtherNet()).setScale(2, RoundingMode.HALF_UP),  money);
            setMoney(row, 7, orZero(r.getOtherVat()).setScale(2, RoundingMode.HALF_UP),  money);
            setMoney(row, 8, grossEur,                                                    money);

            totalEur = totalEur.add(grossEur);
        }

        Row footerRow = sheet.createRow(rowNum);
        Cell ftLabel = footerRow.createCell(0);
        ftLabel.setCellValue("ОБЩО (" + records.size() + " записа):");
        ftLabel.setCellStyle(fText);
        setMoney(footerRow, 8, totalEur, footer);

        autoSize(sheet, headers.length);
    }

    private void buildSummarySheet(XSSFWorkbook wb, Sheet sheet,
                                    List<Employee> employees,
                                    List<VehicleFreightTrip> trips,
                                    List<VehicleMaintenanceRecord> records,
                                    ReportPeriod period, int year, Integer month, Integer week) {
        CellStyle labelStyle = makeLabelStyle(wb);
        CellStyle sectionStyle = makeSectionStyle(wb);
        CellStyle money = makeMoneyStyle(wb);
        CellStyle normal = wb.createCellStyle();

        BigDecimal factor    = salaryFactor(period);
        BigDecimal totalB    = employees.stream().map(e -> orZero(e.getSalary()).multiply(factor))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalN    = employees.stream().map(e -> orZero(e.getSalaryNeto()).multiply(factor))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tripRev   = trips.stream().map(t -> orZero(t.getRevenueEur())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tripVat   = trips.stream().map(t -> orZero(t.getVatEur())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tripFuel  = trips.stream().map(t -> orZero(t.getFuelCostEur())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tripTolls = trips.stream().map(t -> orZero(t.getTollFeesEur())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tripBord  = trips.stream().map(t -> orZero(t.getBorderFeesEur())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tripPark  = trips.stream().map(t -> orZero(t.getParkingAccommodationEur())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tripOther = trips.stream().map(t -> orZero(t.getOtherExpensesEur())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tripProfit = tripRev.subtract(tripVat).subtract(tripFuel)
                                        .subtract(tripTolls).subtract(tripBord)
                                        .subtract(tripPark).subtract(tripOther);
        BigDecimal maintEur = records.stream().map(r -> orZero(r.getTotalGross())).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

        String label = periodLabel(period, year, month, week);

        Object[][] rows = {
            // {label, value, isSection}
            {"ОБОБЩЕН ФИНАНСОВ ОТЧЕТ",    null, true},
            {"Период",                     label, false},
            {null, null, false},
            {"— ЗАПЛАТИ —",               null, true},
            {"Активни служители",          employees.size(), false},
            {"Обща бруто заплата (€)",    totalB, false},
            {"Общо нето заплати (€)",     totalN, false},
            {"Общо данъци и осигуровки (€)", totalB.subtract(totalN), false},
            {null, null, false},
            {"— КУРСОВЕ —",               null, true},
            {"Брой курсове",              trips.size(), false},
            {"Общи приходи (€)",          tripRev, false},
            {"ДДС от курсове (€)",        tripVat, false},
            {"Нетна печалба курсове (€)", tripProfit, false},
            {null, null, false},
            {"— ПОДДРЪЖКА —",             null, true},
            {"Брой записа поддръжка",     records.size(), false},
            {"Общо разходи (€)",          maintEur, false},
        };

        for (int i = 0; i < rows.length; i++) {
            if (rows[i][0] == null) continue;
            Row row = sheet.createRow(i);
            Cell labelCell = row.createCell(0);
            labelCell.setCellValue(rows[i][0].toString());
            boolean isSection = (boolean) rows[i][2];
            labelCell.setCellStyle(isSection ? sectionStyle : labelStyle);

            if (rows[i][1] != null) {
                Cell valCell = row.createCell(1);
                if (rows[i][1] instanceof BigDecimal bd) {
                    valCell.setCellValue(bd.doubleValue());
                    valCell.setCellStyle(money);
                } else if (rows[i][1] instanceof Integer n) {
                    valCell.setCellValue(n);
                    valCell.setCellStyle(normal);
                } else {
                    valCell.setCellValue(rows[i][1].toString());
                    valCell.setCellStyle(normal);
                }
            }
        }

        sheet.setColumnWidth(0, 42 * 256);
        sheet.setColumnWidth(1, 22 * 256);
    }

    // =================================================================
    // HELPERS — period calculation
    // =================================================================

    private LocalDate[] getDateRange(ReportPeriod period, int year, Integer month, Integer week) {
        return switch (period) {
            case WEEKLY -> {
                LocalDate monday = LocalDate.now()
                        .with(WeekFields.ISO.weekBasedYear(), year)
                        .with(WeekFields.ISO.weekOfWeekBasedYear(), week != null ? week : 1)
                        .with(DayOfWeek.MONDAY);
                yield new LocalDate[]{monday, monday.plusDays(6)};
            }
            case MONTHLY -> {
                YearMonth ym = YearMonth.of(year, month != null ? month : 1);
                yield new LocalDate[]{ym.atDay(1), ym.atEndOfMonth()};
            }
            case YEARLY -> new LocalDate[]{LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)};
        };
    }

    private BigDecimal salaryFactor(ReportPeriod period) {
        return switch (period) {
            case WEEKLY  -> BigDecimal.ONE.divide(FinancialConstants.WEEKS_PER_MONTH, 6, RoundingMode.HALF_UP);
            case MONTHLY -> BigDecimal.ONE;
            case YEARLY  -> new BigDecimal("12");
        };
    }

    public String periodLabel(ReportPeriod period, int year, Integer month, Integer week) {
        return switch (period) {
            case WEEKLY  -> "Седмица " + week + " / " + year;
            case MONTHLY -> {
                String[] bg = {"", "Януари", "Февруари", "Март", "Април", "Май", "Юни",
                               "Юли", "Август", "Септември", "Октомври", "Ноември", "Декември"};
                int m = (month != null && month >= 1 && month <= 12) ? month : 1;
                yield bg[m] + " " + year;
            }
            case YEARLY  -> String.valueOf(year);
        };
    }

    public String periodFileLabel(ReportPeriod period, int year, Integer month, Integer week) {
        return switch (period) {
            case WEEKLY  -> "sedmitsa_" + week + "_" + year;
            case MONTHLY -> month + "_" + year;
            case YEARLY  -> String.valueOf(year);
        };
    }

    // =================================================================
    // HELPERS — POI styles
    // =================================================================

    private void writeHeader(Sheet sheet, CellStyle style, String[] headers) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(style);
        }
    }

    private CellStyle makeHeaderStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); s.setFont(f);
        s.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private CellStyle makeSectionStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setFontHeightInPoints((short) 12); s.setFont(f);
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private CellStyle makeLabelStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); s.setFont(f);
        return s;
    }

    private CellStyle makeMoneyStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        DataFormat df = wb.createDataFormat();
        s.setDataFormat(df.getFormat("#,##0.00"));
        return s;
    }

    private CellStyle makeFooterMoneyStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); s.setFont(f);
        s.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        DataFormat df = wb.createDataFormat();
        s.setDataFormat(df.getFormat("#,##0.00"));
        return s;
    }

    private CellStyle makeFooterTextStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); s.setFont(f);
        s.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private void setMoney(Row row, int col, BigDecimal val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val != null ? val.doubleValue() : 0.0);
        cell.setCellStyle(style);
    }

    private void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) sheet.autoSizeColumn(i);
    }

    private BigDecimal orZero(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    // =================================================================
    // HELPERS — label mapping
    // =================================================================

    private String jobTitleLabel(JobTitle jt) {
        if (jt == null) return "—";
        return switch (jt) {
            case DRIVER        -> "Шофьор";
            case MECHANIC      -> "Механик";
            case DISPATCHER    -> "Спедитор";
            case ACCOUNTANT    -> "Счетоводител";
            case MANAGER       -> "Мениджър";
            case HR            -> "ЧР";
            case ADMINISTRATOR -> "Администратор";
        };
    }

    private String maintenanceTypeLabel(MaintenanceType t) {
        if (t == null) return "—";
        return switch (t) {
            case REPAIR      -> "Ремонт";
            case SERVICE     -> "Сервиз";
            case INSPECTION  -> "Технически преглед";
            case TIRE_CHANGE -> "Смяна на гуми";
            case BREAKDOWN   -> "Авария";
            case ACCIDENT    -> "Катастрофа";
            case OTHER       -> "Друго";
        };
    }

    private byte[] toBytes(XSSFWorkbook wb) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);
        return bos.toByteArray();
    }
}
