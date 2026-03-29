package com.express.system.controller;

import com.express.system.common.ApiResponse;
import com.express.system.dto.DashboardRanksVO;
import com.express.system.dto.DashboardSummaryVO;
import com.express.system.dto.DashboardTrendPointVO;
import com.express.system.entity.enums.UserRole;
import com.express.system.security.CurrentUserProvider;
import com.express.system.service.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system/dashboard")
@Tag(name = "数据看板")
public class DashboardController {

    private final IDashboardService dashboardService;
    private final CurrentUserProvider currentUserProvider;

    public DashboardController(IDashboardService dashboardService,
                               CurrentUserProvider currentUserProvider) {
        this.dashboardService = dashboardService;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "看板核心指标")
    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryVO> summary() {
        ensureStaffOrAdmin();
        return ApiResponse.success(dashboardService.getSummary());
    }

    @Operation(summary = "看板趋势数据")
    @GetMapping("/trend")
    public ApiResponse<List<DashboardTrendPointVO>> trend(
            @Parameter(description = "统计天数（默认7）") @RequestParam(value = "days", required = false) Integer days) {
        ensureStaffOrAdmin();
        int safeDays = days == null ? 7 : days;
        return ApiResponse.success(dashboardService.getTrend(safeDays));
    }

    @Operation(summary = "看板榜单数据")
    @GetMapping("/ranks")
    public ApiResponse<DashboardRanksVO> ranks() {
        ensureStaffOrAdmin();
        return ApiResponse.success(dashboardService.getRanks());
    }

    private void ensureStaffOrAdmin() {
        currentUserProvider.requireRole(UserRole.STAFF, UserRole.ADMIN);
    }
}
