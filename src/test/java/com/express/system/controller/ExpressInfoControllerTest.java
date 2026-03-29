package com.express.system.controller;

import com.express.system.entity.ExpressInfo;
import com.express.system.entity.enums.UserRole;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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

    @Test
    void listAsUserUsesOwnPhone() throws Exception {
        // 模拟 USER 登录，list 默认使用本人手机号过滤。
        JwtUser jwtUser = new JwtUser(1L, "13900000003", UserRole.USER);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                jwtUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 当传入期望参数时，模拟 service 返回一条记录。
        when(expressInfoService.listForUser(eq(1L), eq("13900000003"),
                eq("SF100000001"), eq(null), eq(false)))
                .thenReturn(List.of(new ExpressInfo()));

        try {
            // 断言接口返回成功。
            mockMvc.perform(get("/system/expresses")
                            .param("trackingNumber", "SF100000001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        } finally {
            // 清理上下文，避免污染其他测试。
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void listAsStaffUsesProvidedFilters() throws Exception {
        // STAFF 可以使用完整筛选条件，service 应原样接收。
        JwtUser jwtUser = new JwtUser(2L, "13900000002", UserRole.STAFF);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                jwtUser, null, List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(expressInfoService.listByFilter(eq("SF100000001"), eq("13900000001"),
                eq(1), eq(101), eq(1), eq(0), eq(null)))
                .thenReturn(List.of(new ExpressInfo()));

        try {
            // 断言返回成功。
            mockMvc.perform(get("/system/expresses")
                            .param("trackingNumber", "SF100000001")
                            .param("receiverPhone", "13900000001")
                            .param("status", "1")
                            .param("shelfCode", "101")
                            .param("shelfLayer", "1")
                            .param("sizeType", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void checkoutUsesOperatorUsername() throws Exception {
        // 出库应将操作者手机号写入 pickupPhone。
        JwtUser jwtUser = new JwtUser(3L, "13900000002", UserRole.STAFF);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                jwtUser, null, List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            // 仅传单号发起出库。
            mockMvc.perform(post("/system/expresses/SF100000001/checkout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 校验 service 收到操作者手机号。
            verify(expressInfoService).checkOut(eq("SF100000001"), eq("13900000002"), eq(UserRole.STAFF), eq(3L));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void claimAsUserBindsExpress() throws Exception {
        JwtUser jwtUser = new JwtUser(1L, "13900000003", UserRole.USER);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                jwtUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(expressInfoService.claimForUser(eq(1L), eq("13900000003"), eq("SF100000001"), eq("13800000001")))
                .thenReturn(new ExpressInfo());

        try {
            mockMvc.perform(post("/system/expresses/claim")
                            .contentType("application/json")
                            .content("{\"trackingNumber\":\"SF100000001\",\"receiverPhone\":\"13800000001\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
