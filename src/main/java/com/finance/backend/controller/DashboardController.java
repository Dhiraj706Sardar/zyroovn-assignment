package com.finance.backend.controller;

import com.finance.backend.dto.response.DashboardSummaryResponse;
import com.finance.backend.dto.response.FinancialRecordResponse;
import com.finance.backend.security.UserPrincipal;
import com.finance.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard analytics endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(summary = "Get dashboard summary", description = "Get financial summary with totals and category breakdown. VIEWER sees only their own data, ANALYST and ADMIN see all data.")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(dashboardService.getSummary(startDate, endDate, 
            userPrincipal.getId(), userPrincipal.getRole().name()));
    }

    @GetMapping("/category-totals")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(summary = "Get category-wise totals", description = "Get total amounts grouped by category. VIEWER sees only their own data, ANALYST and ADMIN see all data.")
    public ResponseEntity<Map<String, BigDecimal>> getCategoryTotals(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(dashboardService.getCategoryWiseTotals(startDate, endDate,
            userPrincipal.getId(), userPrincipal.getRole().name()));
    }

    @GetMapping("/recent-activity")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(summary = "Get recent activity", description = "Get most recent financial transactions. VIEWER sees only their own data, ANALYST and ADMIN see all data.")
    public ResponseEntity<List<FinancialRecordResponse>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(dashboardService.getRecentActivity(limit,
            userPrincipal.getId(), userPrincipal.getRole().name()));
    }

    @GetMapping("/monthly-trends")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(summary = "Get monthly trends", description = "Get monthly aggregated financial data - insights feature (Analyst and Admin only). Shows all users' data for analysis.")
    public ResponseEntity<Map<String, Map<String, BigDecimal>>> getMonthlyTrends(
            @RequestParam(defaultValue = "6") int months,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(dashboardService.getMonthlyTrends(months,
            userPrincipal.getId(), userPrincipal.getRole().name()));
    }
}
