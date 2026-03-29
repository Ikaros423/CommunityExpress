package com.express.system.controller;

import com.express.system.entity.SysUser;
import com.express.system.entity.enums.SmsBizType;
import com.express.system.entity.enums.UserRole;
import com.express.system.security.JwtUtil;
import com.express.system.service.ISysUserService;
import com.express.system.service.PasswordResetService;
import com.express.system.service.SmsCodeService;
import com.express.system.common.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.security.test.context.support.WithMockUser;
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

@WebMvcTest(controllers = SysUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = SysUserControllerTest.TestApplication.class)
@Import({GlobalExceptionHandler.class, ValidationAutoConfiguration.class})
class SysUserControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(excludeName = {
            "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
            "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
    })
    @Import(SysUserController.class)
    static class TestApplication {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ISysUserService sysUserService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private SmsCodeService smsCodeService;

    @Test
    void registerReturnsUser() throws Exception {
        // 注册接口：模拟 service 返回注册后的用户。
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("13900000003");
        when(sysUserService.registerUser(eq("13900000003"), eq("123456"), eq("普通用户"))).thenReturn(user);

        mockMvc.perform(post("/system/users/register")
                        .contentType("application/json")
                        .content("{\"username\":\"13900000003\",\"code\":\"123456\",\"password\":\"123456\",\"nickname\":\"普通用户\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("13900000003"));

        verify(smsCodeService).verifyCode(eq("13900000003"), eq(SmsBizType.REGISTER), eq("123456"));
    }

    @Test
    void requestSmsCodeReturnsSuccess() throws Exception {
        mockMvc.perform(post("/system/users/sms-code/request")
                        .contentType("application/json")
                        .content("{\"phone\":\"13900000003\",\"bizType\":\"REGISTER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(smsCodeService).requestCode(eq("13900000003"), eq(SmsBizType.REGISTER));
    }

    @Test
    void loginReturnsToken() throws Exception {
        // 登录接口：模拟 service 返回用户并生成 token。
        SysUser user = new SysUser();
        user.setId(2L);
        user.setUsername("13900000001");
        user.setRole(UserRole.ADMIN);
        when(sysUserService.login(eq("13900000001"), eq("123456"))).thenReturn(user);
        when(jwtUtil.generateToken(any(SysUser.class))).thenReturn("token-123");

        mockMvc.perform(post("/system/users/login")
                        .contentType("application/json")
                        .content("{\"account\":\"13900000001\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("token-123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listReturnsUsers() throws Exception {
        // 列表接口：ADMIN 可查询用户列表。
        SysUser user = new SysUser();
        user.setId(3L);
        user.setUsername("13900000002");
        when(sysUserService.listByFilter(eq(null), eq(null), eq(null))).thenReturn(List.of(user));

        mockMvc.perform(get("/system/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].username").value("13900000002"));
    }

    @Test
    void requestPasswordResetReturnsSuccess() throws Exception {
        // 请求短信验证码：手机号存在时返回成功。
        mockMvc.perform(post("/system/users/password-reset/request")
                        .contentType("application/json")
                        .content("{\"phone\":\"13900000003\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(passwordResetService).requestCode(eq("13900000003"));
    }

    @Test
    void confirmPasswordResetInvokesService() throws Exception {
        mockMvc.perform(post("/system/users/password-reset/confirm")
                        .contentType("application/json")
                        .content("{\"phone\":\"13900000003\",\"code\":\"123456\",\"newPassword\":\"654321\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(passwordResetService)
                .confirmReset(eq("13900000003"), eq("123456"), eq("654321"));
    }
}
