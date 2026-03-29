package com.express.system.controller;

import com.express.system.entity.SendOrder;
import com.express.system.entity.enums.UserRole;
import com.express.system.security.JwtUser;
import com.express.system.service.ISendOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SendOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = SendOrderControllerTest.TestApplication.class)
class SendOrderControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(excludeName = {
            "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
            "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
    })
    @Import({SendOrderController.class, ValidationAutoConfiguration.class})
    static class TestApplication {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ISendOrderService sendOrderService;

    @Test
    void userCanCreateSendOrder() throws Exception {
        JwtUser jwtUser = new JwtUser(3L, "13900000003", UserRole.USER);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                jwtUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        SendOrder order = new SendOrder();
        order.setId(10L);
        when(sendOrderService.createForUser(
                eq(3L),
                eq("13900000003"),
                eq("A小区1栋"),
                eq("张三"),
                eq("13800000001"),
                eq("B小区2栋"),
                eq((byte) 0),
                eq("测试")
        )).thenReturn(order);

        try {
            mockMvc.perform(post("/system/send-orders")
                            .contentType("application/json")
                            .content("{\"senderPhone\":\"13999999999\",\"senderAddress\":\"A小区1栋\",\"receiverName\":\"张三\",\"receiverPhone\":\"13800000001\",\"receiverAddress\":\"B小区2栋\",\"packageType\":0,\"remark\":\"测试\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(sendOrderService).createForUser(
                    eq(3L),
                    eq("13900000003"),
                    eq("A小区1栋"),
                    eq("张三"),
                    eq("13800000001"),
                    eq("B小区2栋"),
                    eq((byte) 0),
                    eq("测试")
            );
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void userListOnlyCallsUserQuery() throws Exception {
        JwtUser jwtUser = new JwtUser(3L, "13900000003", UserRole.USER);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                jwtUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(sendOrderService.listByUser(eq(3L), eq((byte) 0))).thenReturn(List.of(new SendOrder()));

        try {
            mockMvc.perform(get("/system/send-orders").param("status", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void staffCanUpdateStatus() throws Exception {
        JwtUser jwtUser = new JwtUser(2L, "13900000002", UserRole.STAFF);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                jwtUser, null, List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(sendOrderService.updateStatus(eq(10L), eq((byte) 1))).thenReturn(new SendOrder());

        try {
            mockMvc.perform(put("/system/send-orders/10/status")
                            .contentType("application/json")
                            .content("{\"status\":1}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(sendOrderService).updateStatus(eq(10L), eq((byte) 1));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
