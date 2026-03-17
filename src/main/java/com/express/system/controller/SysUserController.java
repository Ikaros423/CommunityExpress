package com.express.system.controller;

import com.express.system.common.ApiResponse;
import com.express.system.entity.SysUser;
import com.express.system.entity.enums.UserRole;
import com.express.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
@RestController
@RequestMapping("/system/sysUser")
@Validated
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    @GetMapping("/list")
    public ApiResponse<List<SysUser>> list(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "role", required = false) UserRole role,
            @RequestParam(value = "status", required = false) Integer status) {
        return ApiResponse.success(sysUserService.listByFilter(username, phone, role, status));
    }

    @GetMapping("/detail")
    public ApiResponse<SysUser> detail(@RequestParam("id") Long id) {
        return ApiResponse.success(sysUserService.getDetail(id));
    }

    @PostMapping("/create")
    public ApiResponse<SysUser> create(@Valid @RequestBody SysUserCreateRequest request) {
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        return ApiResponse.success("创建成功", sysUserService.createUser(user));
    }

    @PostMapping("/update")
    public ApiResponse<SysUser> update(@Valid @RequestBody SysUserUpdateRequest request) {
        SysUser user = new SysUser();
        user.setId(request.getId());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        return ApiResponse.success("更新成功", sysUserService.updateUser(user));
    }

    @PostMapping("/delete")
    public ApiResponse<Boolean> delete(@RequestParam("id") Long id) {
        return ApiResponse.success("删除成功", sysUserService.deleteUser(id));
    }

    @PostMapping("/register")
    public ApiResponse<SysUser> register(@Valid @RequestBody SysUserRegisterRequest request) {
        return ApiResponse.success("注册成功", sysUserService.registerUser(
                request.getUsername(), request.getPassword(), request.getPhone(), request.getNickname()));
    }

    @PostMapping("/login")
    public ApiResponse<SysUser> login(@Valid @RequestBody SysUserLoginRequest request) {
        return ApiResponse.success("登录成功", sysUserService.login(request.getAccount(), request.getPassword()));
    }

    public static class SysUserCreateRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 2, max = 15, message = "用户名长度需在2-15之间")
        private String username;
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度需在6-20之间")
        private String password;
        @NotNull(message = "角色不能为空")
        private UserRole role;
        private String nickname;
        private String phone;
        private String email;
        private Byte status;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public UserRole getRole() {
            return role;
        }

        public void setRole(UserRole role) {
            this.role = role;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Byte getStatus() {
            return status;
        }

        public void setStatus(Byte status) {
            this.status = status;
        }
    }

    public static class SysUserUpdateRequest {
        @NotNull(message = "用户ID不能为空")
        private Long id;
        private String username;
        private String password;
        private String nickname;
        private String phone;
        private String email;
        private UserRole role;
        private Byte status;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public UserRole getRole() {
            return role;
        }

        public void setRole(UserRole role) {
            this.role = role;
        }

        public Byte getStatus() {
            return status;
        }

        public void setStatus(Byte status) {
            this.status = status;
        }
    }

    public static class SysUserRegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 2, max = 32, message = "用户名长度需在2-32之间")
        private String username;
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度需在6-64之间")
        private String password;
        private String phone;
        private String nickname;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }

    public static class SysUserLoginRequest {
        @NotBlank(message = "账号不能为空")
        private String account;
        @NotBlank(message = "密码不能为空")
        private String password;

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
