package com.example.transportfirm.controller.accounting;

import com.example.transportfirm.io.accounting.DashboardStatsDto;
import com.example.transportfirm.io.accounting.NotificationDto;
import com.example.transportfirm.io.accounting.RoleStatsDto;
import com.example.transportfirm.service.accounting.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        return ResponseEntity.ok(dashboardService.getNotifications());
    }

    @GetMapping("/role-stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoleStatsDto> getRoleStats(Principal principal) {
        return ResponseEntity.ok(dashboardService.getRoleStats(principal.getName()));
    }
}
