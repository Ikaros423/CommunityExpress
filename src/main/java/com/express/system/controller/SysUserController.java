package com.express.system.controller;

import com.express.system.common.ApiResponse;
import com.express.system.common.exception.BusinessException;
import com.express.system.common.page.PageResponse;
import com.express.system.dto.query.UserPageQuery;
import com.express.system.entity.SysUser;
import com.express.system.entity.enums.SmsBizType;
import com.express.system.entity.enums.UserRole;
import com.express.system.security.CurrentUserProvider;
import com.express.system.security.JwtUser;
import com.express.system.security.JwtUtil;
import com.express.system.service.ISysUserService;
import com.express.system.service.PasswordResetService;
import com.express.system.service.SmsCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author ikaros
 * @since 2026-02-25
 */
@RestController
@RequestMapping("/system/users")
@Tag(name = "用户管理")
@Validated
public class SysUserController {

    private final ISysUserService sysUserService;
    private final JwtUtil jwtUtil;
    private final PasswordResetService passwordResetService;
    private final SmsCodeService smsCodeService;
    private final CurrentUserProvider currentUserProvider;

    public SysUserController(ISysUserService sysUserService,
                             JwtUtil jwtUtil,
                             PasswordResetService passwordResetService,
                             SmsCodeService smsCodeService,
                             CurrentUserProvider currentUserProvider) {
        this.sysUserService = sysUserService;
        this.jwtUtil = jwtUtil;
        this.passwordResetService = passwordResetService;
        this.smsCodeService = smsCodeService;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "用户列表查询")
    @GetMapping
    public ApiResponse<PageResponse<SysUser>> list(UserPageQuery query) {
        if (query == null) {
            query = new UserPageQuery();
        }
        return ApiResponse.success(sysUserService.pageByFilter(
                query.getUsername(), query.getRole(), query.getStatus(), query));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public ApiResponse<SysUser> detail(@Parameter(description = "用户ID") @PathVariable("id") Long id) {
        return ApiResponse.success(sysUserService.getDetail(id));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public ApiResponse<SysUser> create(@Valid @RequestBody SysUserCreateRequest request) {
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        return ApiResponse.success("创建成功", sysUserService.createUser(user));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public ApiResponse<SysUser> update(@Parameter(description = "用户ID") @PathVariable("id") Long id,
                                       @Valid @RequestBody SysUserUpdateRequest request) {
        JwtUser currentUser = currentUserProvider.getCurrentUserOrThrow();
        SysUser targetUser = sysUserService.getById(id);
        if (targetUser == null) {
            throw BusinessException.notFound("用户不存在");
        }
        if (currentUser != null
                && currentUser.getRole() == UserRole.ADMIN
                && targetUser.getRole() == UserRole.ADMIN
                && (currentUser.getUserId() == null || !currentUser.getUserId().equals(id))) {
            throw BusinessException.forbidden("管理员不能修改其他管理员账号");
        }
        if (currentUser != null
                && currentUser.getUserId() != null
                && currentUser.getUserId().equals(id)
                && currentUser.getRole() == UserRole.ADMIN
                && request.getRole() != null
                && request.getRole() != UserRole.ADMIN) {
            throw BusinessException.forbidden("管理员不能修改自己的角色");
        }
        SysUser user = new SysUser();
        user.setId(id);
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        return ApiResponse.success("更新成功", sysUserService.updateUser(user));
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@Parameter(description = "用户ID") @PathVariable("id") Long id) {
        SysUser targetUser = sysUserService.getById(id);
        if (targetUser == null) {
            throw BusinessException.notFound("用户不存在");
        }
        if (targetUser.getRole() == UserRole.ADMIN) {
            throw BusinessException.forbidden("管理员账号不允许删除");
        }
        return ApiResponse.success("删除成功", sysUserService.deleteUser(id));
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<SysUser> register(@Valid @RequestBody SysUserRegisterRequest request) {
        smsCodeService.verifyCode(request.getUsername(), SmsBizType.REGISTER, request.getCode());
        return ApiResponse.success("注册成功", sysUserService.registerUser(
                request.getUsername(), request.getPassword(), request.getNickname()));
    }

    @Operation(summary = "发送短信验证码(统一)")
    @PostMapping("/sms-code/request")
    public ApiResponse<Boolean> requestSmsCode(
            @Valid @RequestBody SmsCodeRequest request) {
        smsCodeService.requestCode(request.getPhone(), request.getBizType());
        return ApiResponse.success("验证码已发送", true);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<SysUserLoginResponse> login(@Valid @RequestBody SysUserLoginRequest request) {
        SysUser user = sysUserService.login(request.getAccount(), request.getPassword());
        String token = jwtUtil.generateToken(user);
        return ApiResponse.success("登录成功", new SysUserLoginResponse(token, user));
    }

    @Operation(summary = "刷新登录 token")
    @PostMapping("/refresh")
    public ApiResponse<SysUserTokenResponse> refresh() {
        JwtUser currentUser = currentUserProvider.getCurrentUserOrThrow();
        String token = jwtUtil.generateToken(currentUser);
        return ApiResponse.success("刷新成功", new SysUserTokenResponse(token));
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/password-reset/request")
    public ApiResponse<Boolean> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestCode(request.getPhone());
        return ApiResponse.success("验证码已发送", true);
    }

    @Operation(summary = "确认短信验证码并重置密码")
    @PostMapping("/password-reset/confirm")
    public ApiResponse<Boolean> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.confirmReset(request.getPhone(), request.getCode(), request.getNewPassword());
        return ApiResponse.success("密码重置成功", true);
    }

    @Schema(description = "创建用户请求")
    public static class SysUserCreateRequest {
        @Schema(description = "用户名(手机号)")
        @NotBlank(message = "用户名不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "用户名需为手机号")
        private String username;
        @Schema(description = "密码")
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度需在6-20之间")
        private String password;
        @Schema(description = "角色")
        @NotNull(message = "角色不能为空")
        private UserRole role;
        @Schema(description = "昵称")
        private String nickname;
        @Schema(description = "邮箱")
        private String email;
        @Schema(description = "状态")
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

    @Schema(description = "更新用户请求")
    public static class SysUserUpdateRequest {
        @Schema(description = "用户名(手机号)")
        @Pattern(regexp = "^1\\d{10}$", message = "用户名需为手机号")
        private String username;
        @Schema(description = "密码")
        private String password;
        @Schema(description = "昵称")
        private String nickname;
        @Schema(description = "邮箱")
        private String email;
        @Schema(description = "角色")
        private UserRole role;
        @Schema(description = "状态")
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

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
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

    @Schema(description = "用户注册请求")
    public static class SysUserRegisterRequest {
        @Schema(description = "用户名(手机号)")
        @NotBlank(message = "用户名不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "用户名需为手机号")
        private String username;
        @Schema(description = "密码")
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度需在6-64之间")
        private String password;
        @Schema(description = "短信验证码")
        @NotBlank(message = "验证码不能为空")
        private String code;
        @Schema(description = "昵称")
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

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    @Schema(description = "统一短信验证码请求")
    public static class SmsCodeRequest {
        @Schema(description = "手机号")
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
        private String phone;

        @Schema(description = "业务类型")
        @NotNull(message = "业务类型不能为空")
        private SmsBizType bizType;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public SmsBizType getBizType() {
            return bizType;
        }

        public void setBizType(SmsBizType bizType) {
            this.bizType = bizType;
        }
    }

    @Schema(description = "用户登录请求")
    public static class SysUserLoginRequest {
        @Schema(description = "账号(手机号)")
        @NotBlank(message = "账号不能为空")
        private String account;
        @Schema(description = "密码")
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

    @Schema(description = "登录响应")
    public static class SysUserLoginResponse {
        @Schema(description = "JWT Token")
        private String token;
        @Schema(description = "用户信息")
        private SysUser user;

        public SysUserLoginResponse(String token, SysUser user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public SysUser getUser() {
            return user;
        }

        public void setUser(SysUser user) {
            this.user = user;
        }
    }

    @Schema(description = "Token 刷新响应")
    public static class SysUserTokenResponse {
        @Schema(description = "JWT Token")
        private String token;

        public SysUserTokenResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    @Schema(description = "短信验证码请求")
    public static class PasswordResetRequest {
        @Schema(description = "手机号")
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
        private String phone;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }


    @Schema(description = "确认短信验证码重置密码请求")
    public static class PasswordResetConfirmRequest {
        @Schema(description = "手机号")
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
        private String phone;
        @Schema(description = "验证码")
        @NotBlank(message = "验证码不能为空")
        private String code;
        @Schema(description = "新密码")
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度需在6-20之间")
        private String newPassword;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

}
