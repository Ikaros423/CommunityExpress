package com.express.system.controller;

import com.express.system.common.page.PageResponse;
import com.express.system.entity.ExpressInfo;
import com.express.system.entity.enums.UserRole;
import com.express.system.security.CurrentUserProvider;
import com.express.system.security.JwtUser;
import com.express.system.service.IExpressInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExpressInfoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = ExpressInfoControllerTest.TestApplication.class)
class ExpressInfoControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(excludeName = {
            "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
            "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
    })
    @Import(ExpressInfoController.class)
    static class TestApplication {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IExpressInfoService expressInfoService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @Test
    void listAsUserUsesOwnPhone() throws Exception {
        JwtUser jwtUser = new JwtUser(1L, "13900000003", UserRole.USER);
        when(currentUserProvider.getCurrentUserOrNull()).thenReturn(jwtUser);

        when(expressInfoService.pageForUser(eq(1L), eq("13900000003"),
                eq("SF100000001"), eq(null), eq(null), any()))
                .thenReturn(PageResponse.of(List.of(new ExpressInfo()), 1, 1, 15));

        mockMvc.perform(get("/system/expresses")
                        .param("trackingNumber", "SF100000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listAsStaffUsesProvidedFilters() throws Exception {
        JwtUser jwtUser = new JwtUser(2L, "13900000002", UserRole.STAFF);
        when(currentUserProvider.getCurrentUserOrNull()).thenReturn(jwtUser);

        when(expressInfoService.pageByFilter(eq("SF100000001"), eq("13900000001"),
                eq(1), eq(101), eq(1), eq(0), eq(null), any()))
                .thenReturn(PageResponse.of(List.of(new ExpressInfo()), 1, 1, 15));

        mockMvc.perform(get("/system/expresses")
                        .param("trackingNumber", "SF100000001")
                        .param("receiverPhone", "13900000001")
                        .param("status", "1")
                        .param("shelfCode", "101")
                        .param("shelfLayer", "1")
                        .param("sizeType", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void checkoutUsesOperatorUsername() throws Exception {
        JwtUser jwtUser = new JwtUser(3L, "13900000002", UserRole.STAFF);
        when(currentUserProvider.getCurrentUserOrThrow()).thenReturn(jwtUser);

        mockMvc.perform(post("/system/expresses/SF100000001/checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(expressInfoService).checkOut(eq("SF100000001"), eq("13900000002"), eq(UserRole.STAFF), eq(3L));
    }

    @Test
    void claimAsUserBindsExpress() throws Exception {
        JwtUser jwtUser = new JwtUser(1L, "13900000003", UserRole.USER);
        when(currentUserProvider.requireRole(UserRole.USER)).thenReturn(jwtUser);

        when(expressInfoService.claimForUser(eq(1L), eq("13900000003"), eq("SF100000001"), eq("13800000001")))
                .thenReturn(new ExpressInfo());

        mockMvc.perform(post("/system/expresses/claim")
                        .contentType("application/json")
                        .content("{\"trackingNumber\":\"SF100000001\",\"receiverPhone\":\"13800000001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
