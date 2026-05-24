package com.example.transportfirm.controller.accounting;

import com.example.transportfirm.enums.ReportPeriod;
import com.example.transportfirm.io.accounting.AccountingDashboardResponse;
import com.example.transportfirm.service.accounting.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
@RequiredArgsConstructor
public class ReportController {

    private static final MediaType XLSX_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReportService reportService;

    /**
     * GET /reports/download
     *   ?type=salary|trips|maintenance|combined
     *   &period=WEEKLY|MONTHLY|YEARLY
     *   &year=2026
     *   &month=3         (required for MONTHLY)
     *   &week=10         (required for WEEKLY)
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> download(
            @RequestParam String type,
            @RequestParam String period,
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer week
    ) {
        ReportPeriod reportPeriod;
        try {
            reportPeriod = ReportPeriod.valueOf(period.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Невалиден период: " + period);
        }

        byte[] data = switch (type.toLowerCase()) {
            case "salary"      -> reportService.generateSalaryReport(reportPeriod, year, month, week);
            case "trips"       -> reportService.generateTripsReport(reportPeriod, year, month, week);
            case "maintenance" -> reportService.generateMaintenanceReport(reportPeriod, year, month, week);
            case "combined"    -> reportService.generateCombinedReport(reportPeriod, year, month, week);
            default            -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Непознат тип отчет: " + type);
        };

        String filename = type + "_" + reportService.periodFileLabel(reportPeriod, year, month, week) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(XLSX_MEDIA_TYPE)
                .body(data);
    }

    /**
     * GET /reports/data
     *   &period=WEEKLY|MONTHLY|YEARLY
     *   &year=2026
     *   &month=3    (required for MONTHLY)
     *   &week=10    (required for WEEKLY)
     *
     * Returns JSON dashboard data for the accounting UI.
     */
    @GetMapping("/data")
    public ResponseEntity<AccountingDashboardResponse> getData(
            @RequestParam String period,
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer week
    ) {
        ReportPeriod reportPeriod;
        try {
            reportPeriod = ReportPeriod.valueOf(period.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Невалиден период: " + period);
        }
        return ResponseEntity.ok(reportService.getDashboardData(reportPeriod, year, month, week));
    }
}
