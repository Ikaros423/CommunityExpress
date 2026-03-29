package com.express.system.controller;

import com.express.system.common.GlobalExceptionHandler;
import com.express.system.common.exception.BusinessException;
import com.express.system.dto.DashboardRanksVO;
import com.express.system.dto.DashboardSummaryVO;
import com.express.system.dto.DashboardTrendPointVO;
import com.express.system.entity.enums.UserRole;
import com.express.system.security.CurrentUserProvider;
import com.express.system.security.JwtUser;
import com.express.system.service.IDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = DashboardControllerTest.TestApplication.class)
class DashboardControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(excludeName = {
            "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
            "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
    })
    @Import({DashboardController.class, ValidationAutoConfiguration.class, GlobalExceptionHandler.class})
    static class TestApplication {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IDashboardService dashboardService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @Test
    void staffCanGetSummary() throws Exception {
        JwtUser jwtUser = new JwtUser(2L, "13900000002", UserRole.STAFF);
        when(currentUserProvider.requireRole(UserRole.STAFF, UserRole.ADMIN)).thenReturn(jwtUser);

        DashboardSummaryVO summary = new DashboardSummaryVO();
        summary.setTotalExpress(10L);
        summary.setPendingPickup(4L);
        when(dashboardService.getSummary()).thenReturn(summary);

        mockMvc.perform(get("/system/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalExpress").value(10));
    }

    @Test
    void staffCanGetTrend() throws Exception {
        JwtUser jwtUser = new JwtUser(2L, "13900000002", UserRole.STAFF);
        when(currentUserProvider.requireRole(UserRole.STAFF, UserRole.ADMIN)).thenReturn(jwtUser);

        DashboardTrendPointVO point = new DashboardTrendPointVO();
        point.setDate("03-29");
        point.setCheckinCount(3L);
        point.setCheckoutCount(2L);
        when(dashboardService.getTrend(eq(7))).thenReturn(List.of(point));

        mockMvc.perform(get("/system/dashboard/trend").param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].date").value("03-29"));
    }

    @Test
    void userCannotGetRanks() throws Exception {
        JwtUser jwtUser = new JwtUser(3L, "13900000003", UserRole.USER);
        when(currentUserProvider.requireRole(UserRole.STAFF, UserRole.ADMIN))
                .thenThrow(BusinessException.forbidden("仅员工或管理员可查看数据看板"));
        mockMvc.perform(get("/system/dashboard/ranks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void adminCanGetRanks() throws Exception {
        JwtUser jwtUser = new JwtUser(1L, "13900000001", UserRole.ADMIN);
        when(currentUserProvider.requireRole(UserRole.STAFF, UserRole.ADMIN)).thenReturn(jwtUser);

        when(dashboardService.getRanks()).thenReturn(new DashboardRanksVO());

        mockMvc.perform(get("/system/dashboard/ranks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
